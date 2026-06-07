# TODO — Logistic Docking Connector: Pair Registry e Vehicle Inventory Exposure

## Stato di partenza

Questo documento definisce il TODO tecnico per sostituire la logica temporanea del `LogisticDockingConnector` con una base architetturale più vicina a Simulated.

Fonti usate per l'allineamento:

- Repository `Tvrsier/Create-Logistic`, branch `logistic_inventory`, per lo stato attuale delle classi principali.
- Handoff package `create_logistic_handoff_package.zip`, solo per reference Simulated / Sable / Create e note progetto.
- Lo zip Java interno al package non deve essere considerato fonte primaria se diverge dal repository.

## Obiettivo funzionale

Il `LogisticDockingConnector` deve funzionare come un Docking Connector Simulated, ma con semantica logistic-specific:

- il connector fisico gestisce estensione, pairing, dock/undock e capability exposure;
- l'inventario esposto non deve essere uno storage locale primario del connector;
- quando il connector è collegato a un vehicle, deve esporre una vista dell'inventario aggregato del `LogisticVehicleContext`;
- l'esposizione dell'inventario deve passare da un wrapper stabile, invalidabile e pair-aware, non dal `CombinedInvWrapper` grezzo restituito direttamente dal blocco.

## 1. Cosa fa il codice originale Simulated

### 1.1 DockingConnectorBlockEntity

Il `DockingConnectorBlockEntity` originale:

- estende `SmartBlockEntity`;
- mantiene due animazioni separate:
  - `extension`, legata allo stato powered/extended;
  - `feet`, legata allo stato paired/locked;
- mantiene uno stato di docking con valori equivalenti a:
  - `UNPOWERED`;
  - `EXTENDED`;
  - `LOCKING`;
  - `LOCKED`;
- salva e ripristina:
  - powered state;
  - valori animazione extension/feet;
  - posizione dell'altro connector;
  - sublevel id dell'altro connector;
  - inventory/tank/wired element;
- espone metodi lifecycle chiave:
  - `pairTo(other)`;
  - `setDock(other, isLocked, targetOrientation, relativePos, relativeOrientation)`;
  - `unDock()`;
  - `getOtherConnector()`;
  - `hasOtherConnector()`;
- non delega tutto alla BE locale: usa un controller centrale `MagnetMap` e un oggetto pair dedicato `DockingConnectorPair`.

### 1.2 DockingConnectorPair

`DockingConnectorPair` è l'oggetto responsabile della relazione tra due connector.

Responsabilità principali:

- validare che entrambi i connector siano estesi;
- calcolare distanza e orientamento relativo tra le punte dei connector;
- evitare pair fighting scegliendo il pair più vicino;
- decidere quando passare a stato locking/locked;
- chiamare `setDock(...)` su entrambi i connector;
- chiamare `unDock()` su entrambi i connector;
- gestire aspetti fisici Sable/Simulated come constraint, relative pose e smoothing.

### 1.3 DockingConnectorBlock.afterMove

Il blocco originale implementa un hook di movimento/sublevel:

- se un connector viene mosso da Sable;
- se aveva un altro connector valido;
- se il connector connesso puntava ancora alla vecchia posizione;
- allora scollega lo stato vecchio e ricrea il pairing sulla nuova BE.

Questo serve a evitare riferimenti stale dopo movimenti o riassemblaggi.

### 1.4 Inventory originale

Simulated usa:

- `DockingConnectorSoloInventory`;
- `DockingConnectorDuoInventory`.

Il duo inventory non è un semplice merge generico: inserimento ed estrazione hanno una semantica direzionale:

- inserire dal nostro lato manda item all'inventario dell'altro connector;
- estrarre dal nostro lato estrae dall'inventario locale;
- la vista duo cambia in base al fatto che il connector sia docked.

Per Create Logistic questa parte non va copiata 1:1, perché il dominio non è più “trasferisci da slot singolo a slot singolo”, ma “esponi inventory aggregato del vehicle context”.

## 2. Stato attuale Create Logistic rilevante

### 2.1 LogisticDockingConnectorBlockEntity

Sul branch `logistic_inventory`, la BE è già migrata a `SmartBlockEntity` e contiene:

- `ItemStackHandler localInventory = new ItemStackHandler(9)`;
- `LogisticDockingState dockingState`;
- `LogisticVehicleContext vehicleContext`;
- `LerpedFloat extension`;
- `LerpedFloat feet`;
- `BlockPos otherConnectorPos`;
- `pairTo(...)` locale;
- `unDock()` locale;
- `tryPairSameGrid()` temporaneo;
- `getEffectiveItemHandler()` che ritorna `vehicleContext.inventoryState().combinedHandler()` se disponibile, altrimenti `localInventory`.

Questa è una buona base visuale, ma non è ancora una base finale per il pairing.

### 2.2 LogisticVehicleInventoryState

Attualmente conserva una mappa:

```java
Map<BlockPos, LogisticInventoryRef> inventories
```

Ogni `LogisticInventoryRef` contiene:

```java
BlockPos localPos
BlockState state
IItemHandler handler
```

Il metodo `combinedHandler()` crea ogni volta un nuovo `CombinedInvWrapper`, filtrando solo handler che implementano `IItemHandlerModifiable`.

### 2.3 LogisticVehicleRegistry

Attualmente registra `LogisticVehicleContext` tramite:

- `vehicleId`;
- `ServerSubLevel`.

Questo è importante perché il connector non dovrebbe cercare inventari in modo locale/ad hoc: deve risalire al `LogisticVehicleContext` tramite sublevel/vehicle registry.

## 3. Problema architetturale da risolvere

La logica attuale è troppo locale alla BE:

```text
BE A trova BE B
BE A setta otherConnectorPos
BE B setta otherConnectorPos
feet usa hasOtherConnector()
getEffectiveItemHandler() ritorna combinedHandler()
```

Questa logica basta per test visuali, ma rischia bug quando entrano in gioco:

- movimento Sable/sublevel;
- connector rimossi o chunk unloaded;
- capability cacheate da altre mod;
- più connector vicini;
- più connector sullo stesso vehicle;
- pairing tra world/static dock e vehicle dock;
- invalidazione del `VehicleContext`;
- ricostruzione del combined inventory.

## 4. Decisione proposta

Implementare una variante logistic-specific, ma fedele nelle responsabilità, della struttura Simulated:

```text
content/logistic/docking_connector/
├── LogisticDockingPairRegistry
├── LogisticDockingPair
├── LogisticDockingEndpoint
├── LogisticDockingExposedItemHandler
└── LogisticDockingInventoryExposureMode
```

Il blocco resta in:

```text
content/block/logistic_docking_connector/
├── LogisticDockingConnectorBlock
├── LogisticDockingConnectorBlockEntity
└── PairedLogisticDockingConnectorBlock
```

Renderer e partial models restano client-only.

## 5. TODO implementativo

### Fase 1 — Pulizia stato BE e responsabilità

- [ ] Lasciare nella BE solo lo stato locale necessario:
  - animazioni `extension` / `feet`;
  - `dockingState`;
  - `otherConnectorPos` temporaneamente persistito;
  - eventuale `otherConnectorSubLevelId` futuro;
  - reference/runtime cache al pair, se utile ma non persistente.
- [ ] Marcare `tryPairSameGrid()` come temporaneo oppure rimuoverlo quando entra il registry.
- [ ] Spostare la decisione di pairing fuori dalla BE.
- [ ] Mantenere `pairTo(...)` e `unDock()` come API pubbliche della BE, ma farle delegare o notificare il registry/pair.
- [ ] Conservare nomi e responsabilità simili a Simulated dove sensato.

### Fase 2 — LogisticDockingPairRegistry

Creare:

```java
public final class LogisticDockingPairRegistry {
    public static void tick(ServerLevel level);
    public static Optional<LogisticDockingPair> getPair(ServerLevel level, BlockPos first, BlockPos second);
    public static LogisticDockingPair getOrCreatePair(ServerLevel level, LogisticDockingConnectorBlockEntity first, LogisticDockingConnectorBlockEntity second);
    public static void removePair(ServerLevel level, BlockPos first, BlockPos second);
    public static void removePairsFor(ServerLevel level, BlockPos connectorPos);
}
```

Responsabilità:

- mantenere pair centralizzati per level;
- normalizzare la chiave del pair, evitando duplicati A-B/B-A;
- impedire pair fighting;
- rimuovere pair invalidi;
- offrire entry point per tick server o lazy tick;
- in futuro integrare ricerca per sublevel/movement context se necessario.

Da decidere:

- registry statico globale con mappa per `ServerLevel`;
- registry agganciato a saved data / level capability;
- registry volatile runtime-only ricostruibile dai connector.

Per ora consigliato: runtime-only per evitare persistenza prematura, ma con cleanup robusto.

### Fase 3 — LogisticDockingPair

Creare:

```java
public final class LogisticDockingPair {
    private final ServerLevel level;
    private final BlockPos firstPos;
    private final BlockPos secondPos;

    public void tick();
    public boolean isValid();
    public boolean canDock();
    public void dock(boolean force);
    public void unDock();
    public Optional<LogisticDockingConnectorBlockEntity> first();
    public Optional<LogisticDockingConnectorBlockEntity> second();
}
```

Responsabilità iniziali:

- validare che entrambe le BE esistano;
- validare che entrambe siano estese;
- validare facing opposto;
- validare distanza logica iniziale;
- chiamare `setDock(...)` o equivalente su entrambe;
- chiamare `unDock()` su entrambe;
- aggiornare `dockingState` su entrambe;
- aggiornare `otherConnectorPos` su entrambe;
- gestire la transizione `POWERED/EXTENDED -> LOCKING -> LOCKED`.

Responsabilità da rinviare:

- constraint fisici Sable;
- smoothing orientamento;
- tolleranze configurabili;
- nearest-pair search completa stile MagnetMap.

### Fase 4 — State API nella BE

Aggiungere o stabilizzare:

```java
public void setDock(LogisticDockingConnectorBlockEntity other, boolean locked)
public void clearDock(LogisticDockingConnectorBlockEntity expectedOther)
public boolean canAcceptPairWith(LogisticDockingConnectorBlockEntity other)
public Optional<BlockPos> getOtherConnectorPos()
public Optional<LogisticVehicleContext> getVehicleContext()
```

Nota: `setDock` non deve cercare pair; deve solo applicare lo stato deciso dal pair.

### Fase 5 — Inventory exposure wrapper

Creare:

```java
public final class LogisticDockingExposedItemHandler implements IItemHandler {
    private final Supplier<Optional<LogisticDockingPair>> pairSupplier;
    private final LogisticDockingConnectorBlockEntity owner;
    private final LogisticDockingInventoryExposureMode mode;
}
```

Responsabilità:

- non contenere item propri;
- delegare al `LogisticVehicleContext.inventoryState()` quando valido;
- rifiutare operazioni se il connector non è paired/locked, se questa regola viene scelta;
- rifiutare operazioni se il `VehicleContext` non è valido;
- evitare di esporre handler stale;
- permettere in futuro strategie diverse.

Modalità iniziali possibili:

```java
public enum LogisticDockingInventoryExposureMode {
    DIRECT_COMBINED,
    ROUND_ROBIN_READ,
    FILTERED,
    DISABLED_WHEN_UNLOCKED
}
```

Per la prima implementazione usare:

```text
DIRECT_COMBINED + validation wrapper
```

Non usare ancora round robin come feature effettiva, ma progettare la classe in modo da poterlo aggiungere.

### Fase 6 — Sostituire getEffectiveItemHandler

Da:

```java
public IItemHandler getEffectiveItemHandler() {
    if (vehicleContext != null && vehicleContext.inventoryState() != null) {
        IItemHandler combinedHandler = vehicleContext.inventoryState().combinedHandler();
        if (combinedHandler.getSlots() > 0) {
            return combinedHandler;
        }
    }
    return localInventory;
}
```

A una forma più stabile:

```java
public IItemHandler getEffectiveItemHandler() {
    return exposedItemHandler;
}
```

Dove `exposedItemHandler` è creato una volta nella BE e delega dinamicamente al context/pair valido.

Domanda architetturale da decidere prima del codice:

- Il fallback `localInventory` deve restare?

Opzioni:

1. Rimuoverlo come storage funzionale e tenerlo solo per debug/migrazione.
2. Tenerlo quando il connector non è associato a nessun vehicle.
3. Rimuoverlo del tutto quando l'inventory exposure è stabile.

Consiglio: tenerlo temporaneamente, ma non usarlo come endpoint finale se il connector è pensato come porta al vehicle inventory.

### Fase 7 — Capability registration

Verificare dove viene esposta la capability item del blocco.

- [ ] Se già registrata: sostituire il provider per usare `getEffectiveItemHandler()` wrapper.
- [ ] Se non ancora registrata: aggiungere provider NeoForge corretto per `Capabilities.ItemHandler.BLOCK`.
- [ ] Verificare side-aware behavior:
  - connector può esporre solo dal lato frontale?
  - connector può esporre da tutti i lati?
  - il paired placeholder deve esporre la capability o delegare alla BE principale?

Scelta consigliata iniziale:

- BE principale espone handler;
- paired placeholder non contiene stato proprio;
- se serve capability sul placeholder, deve risalire alla BE principale tramite facing/opposite.

### Fase 8 — VehicleContext binding

Stabilire come il connector ottiene il `LogisticVehicleContext`.

Possibili fonti:

1. `LogisticControllerBlockEntity` lo assegna ai connector durante scan/eventi.
2. Il connector risale al sublevel con `Sable.HELPER.getContaining(...)` e chiama `LogisticVehicleRegistry.getBySublevel(...)`.
3. Il pair decide quale lato è vehicle e quale lato è static dock.

Consiglio:

- non hardcodare nella BE una scansione inventari;
- usare `LogisticVehicleRegistry` come sorgente principale;
- se entrambi i connector hanno vehicle context, definire regole esplicite:
  - vehicle-to-world;
  - vehicle-to-vehicle;
  - world-to-vehicle.

### Fase 9 — afterMove / Sable hooks

Implementare solo dopo Fasi 2-4.

TODO:

- [ ] Far implementare a `LogisticDockingConnectorBlock` l'equivalente di `BlockSubLevelAssemblyListener`, se disponibile nel progetto.
- [ ] In `afterMove`, leggere il vecchio connector e il connector connesso.
- [ ] Chiamare `unDock()` sul vecchio stato.
- [ ] Chiamare `pairTo(...)` o registry `getOrCreatePair(...).dock(true)` sulla nuova BE.
- [ ] Aggiornare client con block event/sendData dove necessario.

Non implementare `afterMove` prima del pair registry: dipende dal lifecycle corretto di pair/unpair.

### Fase 10 — Test manuali

#### Test pairing base

- [ ] Due connector frontali, entrambi powered.
- [ ] Si estendono entrambi.
- [ ] Si pairano.
- [ ] Feet/side pistons si alzano solo quando paired.
- [ ] Spegnendo uno dei due, entrambi fanno undock.
- [ ] Il paired placeholder viene rimosso correttamente.

#### Test no pair fighting

- [ ] Tre connector vicini.
- [ ] Solo la coppia più corretta deve pairarsi.
- [ ] Nessun connector deve rimanere con `otherConnectorPos` stale.

#### Test inventory exposure

- [ ] Vehicle con più inventory registrati.
- [ ] Connector paired espone slots aggregati.
- [ ] Inserimento da mod esterna raggiunge il vehicle inventory.
- [ ] Estrazione da mod esterna legge il vehicle inventory.
- [ ] Spegnendo/undockando, la capability non deve continuare a esporre uno stale handler.

#### Test chunk/move/remove

- [ ] Rimozione di un connector paired.
- [ ] Rimozione del paired placeholder.
- [ ] Unload/reload chunk.
- [ ] Movimento/assemblaggio Sable, quando `afterMove` sarà implementato.

## 6. Opzioni architetturali

### Opzione A — Implementazione fedele ora

Implementare subito:

- `LogisticDockingPairRegistry`;
- `LogisticDockingPair`;
- wrapper inventory exposure;
- `afterMove` solo dopo pair lifecycle stabile.

Pro:

- base stabile;
- meno rischio di stale state;
- crescita futura più semplice;
- più vicina a Simulated.

Contro:

- più codice iniziale;
- richiede test manuali più lunghi;
- alcuni dettagli Sable resteranno comunque da integrare dopo.

### Opzione B — Workaround temporaneo controllato

Mantenere `tryPairSameGrid()` e `otherConnectorPos`, ma introdurre solo `LogisticDockingExposedItemHandler`.

Pro:

- permette di testare subito inventory exposure;
- meno refactor immediato.

Contro:

- pairing resta fragile;
- `afterMove` non va implementato sopra questa base;
- più rischio di dover riscrivere dopo.

### Opzione C — Rinviare inventory exposure

Prima chiudere solo pairing registry e pair lifecycle, poi inventario.

Pro:

- evita di sovrapporre due problemi;
- pairing diventa affidabile prima delle capability.

Contro:

- non testa subito il valore principale del blocco;
- richiede una seconda fase per capire i problemi con mod esterne.

## 7. Scelta consigliata

Procedere con Opzione A, ma per step:

1. `LogisticDockingPairRegistry` + `LogisticDockingPair` minimale.
2. BE delegante, con `pairTo/unDock/setDock` puliti.
3. `LogisticDockingExposedItemHandler` che delega al `VehicleContext` valido.
4. Capability provider.
5. Test base.
6. Solo dopo: `afterMove` Sable e strategie avanzate di estrazione.

## 8. Nota sul problema Drawer / Round Robin

Esporre direttamente il `CombinedInvWrapper` evita lo storage locale del connector, ma non garantisce da solo round robin corretto.

Se una mod esterna interroga slot in ordine e prende il primo item disponibile, anche un combined inventory può favorire sempre gli slot iniziali.

Per questo il TODO prevede un wrapper dedicato:

```text
LogisticDockingExposedItemHandler
```

Questo wrapper inizialmente può delegare in ordine diretto, ma in futuro può implementare:

- round robin extraction cursor;
- priority per inventory;
- filtri per item;
- side-aware insertion/extraction;
- modalità configurabile da GUI.

## 9. Regola di progetto

Non trattare il `LogisticDockingConnector` come un semplice blocco animato.

È un endpoint logistico ispirato a Simulated, quindi prima di aggiungere workaround locali bisogna sempre verificare se manca uno di questi sistemi:

- pair registry / manager;
- pair object;
- lifecycle dock/undock;
- Sable movement hook;
- capability wrapper;
- vehicle context binding;
- inventory exposure strategy.
