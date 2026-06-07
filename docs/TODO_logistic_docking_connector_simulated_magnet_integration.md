# TODO - Logistic Docking Connector: integrazione MagnetMap Simulated

## Decisione architetturale

Useremo **Opzione A**: riutilizzo diretto delle classi generiche di Simulated/Aeronautics per il sistema magnetico/pairing spaziale.

Create Logistic sarà quindi compatibile con una specifica versione di Aeronautics/Simulated. Se in futuro Simulated cambia firme o package, verrà aggiornata la versione compatibile di Create Logistic invece di duplicare codice interno.

## Classi Simulated da riutilizzare

Package previsto:

```java
dev.simulated_team.simulated.content.blocks.redstone_magnet
```

Classi da importare direttamente:

```java
MagnetMap
MagnetBehaviour
MagnetConsumer
MagnetPair
MagnetPairIdentifier
SimMagnet
```

Classi da NON riutilizzare direttamente per ora:

```java
RedstoneMagnetBlock
RedstoneMagnetBlockEntity
```

Motivo: sono implementazioni concrete del blocco magnete Simulated, mentre a noi serve solo l'infrastruttura generica.

---

## Stato attuale del nostro BE

`LogisticDockingConnectorBlockEntity` contiene ancora troppe responsabilità:

- stato docking;
- animazioni `extension` e `feet`;
- inventario locale fallback;
- riferimento diretto a `LogisticVehicleContext`;
- `otherConnectorPos`;
- `pairTo`;
- `unDock`;
- `tryPairSameGrid`;
- spawn/remove del paired front block;
- capability/inventory exposure tramite `getEffectiveItemHandler()`.

L'obiettivo è mantenerlo come `SmartBlockEntity`, ma trasformarlo in endpoint leggero che delega pairing e inventario.

---

## Nuove responsabilità target

### `LogisticDockingConnectorBlockEntity`

Package:

```text
eu.tvrsier.create_logistic.content.block.logistic_docking_connector
```

Responsabilità residue:

- estendere `SmartBlockEntity`;
- implementare `SimMagnet`;
- gestire animazioni client/server (`extension`, `feet`);
- mantenere stato blocco (`POWERED`, `EXTENDED`);
- esporre dati minimi persistenti;
- aggiungere `MagnetBehaviour` in `addBehaviours`;
- delegare pairing a classi logistic;
- delegare inventory exposure al pair/context.

Da rimuovere/spostare:

- `tryPairSameGrid()`;
- `pairTo()`;
- `unDock()`;
- gestione diretta completa di `otherConnectorPos`;
- logica futura di inventory bridge.

---

### `LogisticDockingConnectorPair`

Package:

```text
eu.tvrsier.create_logistic.content.logistic.docking_connector
```

Estende:

```java
MagnetPair<LogisticDockingConnectorBlockEntity>
```

Responsabilità:

- rappresentare il pair attivo tra due logistic docking connector;
- validare che i due connector siano ancora presenti e compatibili;
- aggiornare lo stato dei due BE quando il pair nasce, resta vivo o viene invalidato;
- chiamare lifecycle tipo `pairTo` / `unDock`, ma fuori dal BE;
- in futuro esporre il bridge inventory tra dock statico e vehicle context.

Nota: `MagnetPair.tick()` nell'originale mette `alive = false`. Il pair resta vivo solo se qualcuno lo riattiva tramite `MagnetMap.tryAddPair(...)`, che in caso di pair già esistente imposta `currentPair.alive = true`. La nostra logica deve rispettare questo pattern.

---

### `LogisticDockingConnectorPairingService`

Package:

```text
eu.tvrsier.create_logistic.content.logistic.docking_connector
```

Responsabilità:

- contenere la `MagnetMap<LogisticDockingConnectorBlockEntity>`;
- cercare connector vicini usando `MAGNET_CONTROLLER.findNearby(...)`;
- applicare regole logistic per decidere se due connector possono pairare;
- creare/riattivare pair usando `MAGNET_CONTROLLER.tryAddPair(...)`;
- chiamare `MAGNET_CONTROLLER.tick(level)` da un punto server-side stabile;
- chiamare eventualmente `MAGNET_CONTROLLER.physicsTick(...)` se necessario per forze/sublevel.

Possibile struttura:

```java
public final class LogisticDockingConnectorPairingService {
    public static final MagnetMap<LogisticDockingConnectorBlockEntity> MAGNET_CONTROLLER = new MagnetMap<>();

    public static void tryPair(LogisticDockingConnectorBlockEntity connector) { ... }

    public static void tick(Level level) { ... }

    private static boolean canPair(LogisticDockingConnectorBlockEntity a, LogisticDockingConnectorBlockEntity b) { ... }
}
```

---

### `LogisticDockingExposedInventory`

Package:

```text
eu.tvrsier.create_logistic.content.logistic.docking_connector
```

Responsabilità futura:

- esporre l'inventario del `VehicleContext` tramite il docking pair;
- evitare inventario locale come sorgente primaria;
- invalidarsi se pair/context/connector non sono più validi;
- proteggere da capability cacheate da mod esterne;
- eventualmente introdurre strategie future: direct slot order, round robin, priority, side-aware.

Per ora può essere rinviato dopo il pairing, ma la separazione deve già preparargli lo spazio.

---

## Modifiche puntuali al BE

### 1. Implementare `SimMagnet`

Aggiungere:

```java
implements SimMagnet
```

Metodi richiesti:

```java
Quaternionfc getOrientation();
SubLevel getLatestSubLevel();
Vec3 getMagnetPosition();
Vector3d setMagneticMoment(Vector3d v);
boolean magnetActive();
```

Implementazione iniziale consigliata:

- `magnetActive()` ritorna `isExtended()` / powered + extended;
- `getMagnetPosition()` usa posizione centrale/frontale coerente con il connector;
- `getLatestSubLevel()` usa Sable/SubLevel helper come nel resto del progetto;
- `getOrientation()` ricava l'orientamento dal sublevel se presente, altrimenti identity;
- `setMagneticMoment(...)` usa la facing del blocco e deve essere coerente con il verso del docking.

Da verificare contro `DockingConnectorBlockEntity` originale prima di scrivere la versione finale.

### 2. Aggiungere `MagnetBehaviour`

In `addBehaviours`:

```java
this.magnetBehaviour = new MagnetBehaviour(this, LogisticDockingConnectorPairingService.MAGNET_CONTROLLER);
behaviours.add(this.magnetBehaviour);
```

### 3. Spostare pairing fuori dal BE

Sostituire nel tick server:

```java
tryPairSameGrid();
```

con:

```java
LogisticDockingConnectorPairingService.tryPair(this);
```

### 4. Rendere lifecycle package-private/internal

Il BE può mantenere metodi minimi tipo:

```java
void onPairCreated(LogisticDockingConnectorBlockEntity other);
void onPairRemoved(LogisticDockingConnectorBlockEntity other);
```

Ma non dovrebbe più decidere autonomamente chi pairare.

---

## Regole di pairing logistic

Condizioni minime:

- entrambi i BE esistono;
- entrambi sono `magnetActive()`;
- entrambi sono extended;
- facing opposte;
- posizioni coerenti con il front block/paired block;
- non sono lo stesso connector;
- non hanno pair incompatibile già attivo;
- stesso `Level`;
- compatibilità con sublevel/Sable da confermare.

---

## Inventario: decisione confermata

Il connector non deve possedere lo storage primario.

Target:

```text
LogisticDockingConnectorBlockEntity
  -> LogisticDockingConnectorPair
    -> LogisticVehicleContext
      -> LogisticVehicleInventoryState
        -> combined handler / exposed handler
```

`localInventory` può rimanere solo come fallback temporaneo o essere rimosso quando la capability bridge sarà pronta.

---

## Rischi accettati

- Forte dipendenza da Simulated/Aeronautics internals.
- Aggiornamenti futuri di Aeronautics possono richiedere una versione nuova di Create Logistic.
- `MagnetPair` porta con sé logica fisica Sable: va verificato se vogliamo usarla interamente o solo come base lifecycle.
- `MagnetMap.tick(level)` deve essere chiamato da un punto stabile; se non viene chiamato, i pair non vengono puliti correttamente.
- Se `tryAddPair` non viene richiamato periodicamente, i pair muoiono perché `MagnetPair.tick()` imposta `alive = false`.

---

## Ordine implementativo consigliato

1. Importare classi Simulated redstone magnet nel codice Create Logistic.
2. Creare `LogisticDockingConnectorPairingService` con `MAGNET_CONTROLLER`.
3. Far implementare `SimMagnet` al `LogisticDockingConnectorBlockEntity`.
4. Aggiungere `MagnetBehaviour` al BE.
5. Estrarre `tryPairSameGrid` nel pairing service.
6. Creare `LogisticDockingConnectorPair extends MagnetPair<LogisticDockingConnectorBlockEntity>`.
7. Sostituire `pairTo/unDock` diretti con lifecycle methods controllati dal pair.
8. Aggiungere tick server globale per `MAGNET_CONTROLLER.tick(level)`.
9. Solo dopo: implementare `LogisticDockingExposedInventory`.
10. Solo dopo: rimuovere o declassare `localInventory`.

---

## Primo obiettivo di compilazione

Obiettivo minimo della prossima patch:

- il BE compila implementando `SimMagnet`;
- `MagnetBehaviour` registra il connector nella `MagnetMap`;
- il vecchio pairing locale viene sostituito da `LogisticDockingConnectorPairingService.tryPair(this)`;
- il comportamento in-game resta equivalente a prima per il pairing semplice;
- nessun inventario logistic nuovo viene ancora esposto.

