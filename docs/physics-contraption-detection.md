# Rilevamento delle "Physics Contraptions" — Analisi del codice di `simulated`

Questo documento raccoglie l'analisi del codice della mod `simulated` (pacchetto `simulated`) relativa a come identificare — dalla tua mod — quali contraptions sono state trasformate / gestite come "physics contraptions" (quelle soggette alla simulazione fisica fornita da Sable e controllate dal `PhysicsAssembler`).

Checklist (cosa contiene questo file)
- [x] Marker e file rilevanti (file / classi / registrazioni) con spiegazioni
- [x] Estratti chiave dal codice (breve) e significato
- [x] 3 strategie pratiche per rilevare una physics contraption (pro/contro)
- [x] Snippet di esempio adattabili (senza dipendenze forti e con Sable)
- [x] API / classi importanti da consultare

Nota: il contenuto si basa sui sorgenti trovati in `simulated/common/src/...` nel workspace.

---

## 1) Marker e file rilevanti (sintesi)

- `PhysicsAssemblerBlockEntity` — simulated/content/blocks/physics_assembler/PhysicsAssemblerBlockEntity.java
  - È il BlockEntity che gestisce assemble/disassemble fisico. Contiene il flag `primaryAssembler` (legato all'NBT `IsPrimary`) e il metodo `assembleOrDisassemble()` che chiama `SimAssemblyHelper.assembleFromSingleBlock(...)` quando assembla.
  - Marker utilizzabili: il `BlockEntity` stesso (istanza della classe) o il suo `BlockEntityType` di registry (registrato come `simulated:physics_assembler`).

- `PhysicsAssemblerBlock` — simulated/content/blocks/physics_assembler/PhysicsAssemblerBlock.java
  - Implementa `IBE<PhysicsAssemblerBlockEntity>` e restituisce `SimBlockEntityTypes.PHYSICS_ASSEMBLER.get()` come `BlockEntityType`.
  - Ha metodo `afterMove(...)` che chiama `pabe.setParent(serverLevel)` per mantenere relazione dopo spostamenti.

- `SimBlockEntityTypes` — simulated/index/SimBlockEntityTypes.java
  - Registra `PHYSICS_ASSEMBLER` con registrate key `physics_assembler` (namespace `simulated`).

- `SimAssemblyHelper` — simulated/util/SimAssemblyHelper.java
  - Contiene `assembleFromSingleBlock(...)`, `disassembleSubLevel(...)`, e `disassembleAndAddCreateContraptions(...)`.
  - Interagisce con `ControlledContraptionEntity` di Create, disassembla contraptions Create e li include nell'assembly Sable.

- `SimAssemblyContraption` — simulated/util/assembly/SimAssemblyContraption.java
  - Costruisce la lista dei blocchi della contraption, controlla superGlue/honeyGlue, ecc. Usata per searchMovedStructure(...) prima dell'assembly.

- `PrimaryAssemblerExtension` (mixin interface) e `ServerSubLevelMixin`
  - `PrimaryAssemblerExtension` aggiunge i metodi `simulated$getPrimaryAssembler()` e `simulated$setPrimaryAssembler(...)` al `ServerSubLevel` (mixin implementato in `ServerSubLevelMixin`).
  - Significato: quando un SubLevel viene creato/controllato dal `PhysicsAssembler`, la mod può salvare la posizione (`BlockPos`) del `PhysicsAssembler` primario nel `ServerSubLevel`.

- `DisassemblyPrevention` — simulated/content/blocks/physics_assembler/assembly_preventer/DisassemblyPrevention.java
  - Usa `PrimaryAssemblerExtension` per verificare il primary assembler prima di permettere la disassembly.

---

## 2) Estratti chiave (concisi) e cosa significano

- NBT flag "IsPrimary"
  - In `PhysicsAssemblerBlockEntity.write(...)` viene scritto:
    - `compound.putBoolean("IsPrimary", this.primaryAssembler);`
  - In `read(...)` viene letto `tag.getBoolean("IsPrimary")`.
  - Significato: ogni `PhysicsAssemblerBlockEntity` persiste il flag `primaryAssembler` nel proprio NBT; puoi leggere questo BE per sapere se è attualmente flagged come primary.

- Registrazione BlockEntity
  - `SimBlockEntityTypes.PHYSICS_ASSEMBLER` è quella entry; il registry key è `simulated:physics_assembler`.
  - Significato: puoi riconoscere il BE confrontando il suo BlockEntityType registry key.

- Relazione SubLevel <-> Primary Assembler
  - `PrimaryAssemblerExtension` aggiunge un campo `simulated$primaryAssembler` al `ServerSubLevel` tramite mixin. `PhysicsAssemblerBlockEntity#setParent(Level)` imposta questo campo quando `primaryAssembler` è true.
  - Significato: il ServerSubLevel porta un marker `primaryAssembler` (BlockPos) che identifica il BE primario per quel sublevel.

- Interazione con Create
  - `SimAssemblyHelper.disassembleAndAddCreateContraptions(...)` cerca entità `ControlledContraptionEntity` nel bounding box e, se il loro `controllerPos` è incluso, disassembla il contraption Create e include i suoi blocchi.
  - Significato: la mod integra contraptions Create nel processo di assembly/disassembly e usa il controllerPos dei ControlledContraptionEntity per collegare contraptions Create al nuovo SubLevel.

---

## 3) Strategie pratiche per rilevare una physics contraption

Di seguito tre strategie utili, ordinate per semplicità e robustezza.

Strategia A — Controllo tramite BlockEntity (registry-name) [raccomandata se non vuoi dipendere da simulated]
- Concetto: dato un ControlledContraptionEntity o l'area che lo contiene, cerca blocchi che abbiano un `BlockEntity` con registry key `simulated:physics_assembler` (o istanza di `PhysicsAssemblerBlockEntity` se compili contro `simulated`).
- Implementazione:
  - Se hai il `controllerPos` (Create) usalo direttamente: leggi `level.getBlockEntity(controllerPos)`.
  - Altrimenti usa il bounding box/transform del contraption ed esegui una scansione rapida per BlockEntities all'interno dell'area (ottimizza iterando solo pos con BE).
  - Confronto senza dipendenza forte: ottieni ResourceLocation tramite `Registry.BLOCK_ENTITY_TYPE.getKey(be.getType())` e confrontalo con `new ResourceLocation("simulated","physics_assembler")`.
- Pro:
  - Non richiede dipendenza di compile-time su `simulated`.
  - Semplice ed efficace quando il controller è nelle vicinanze.
- Contro:
  - Scansione di grandi bbox può essere costosa.
  - Alcune contraptions potrebbero non avere il controller all'interno del bbox usato.

Strategia B — Usare Sable/SubLevel + PrimaryAssemblerExtension (più precisa) [se hai Sable]
- Concetto: ottenere il `SubLevel` tramite Sable (Sable.HELPER.getContaining(...)) e verificare il campo `simulated$primaryAssembler` (tramite l'interfaccia `PrimaryAssemblerExtension` o reflection).
- Implementazione:
  - SubLevel sub = Sable.HELPER.getContaining(level, pos);
  - Se (sub instanceof ServerSubLevel) cast a `PrimaryAssemblerExtension` e chiama `simulated$getPrimaryAssembler()`; se non null => physics contraption.
- Pro:
  - Molto affidabile; legge il marker interno che `simulated` usa.
  - Non richiede scansione del mondo.
- Contro:
  - Richiede Sable API e/o l'interfaccia `PrimaryAssemblerExtension` accessibile; senza queste devi usare reflection (più fragile).

Strategia C — Verificare presenza nella pipeline fisica (RigidBody / PhysicsPipeline) [per integrazioni fisiche avanzate]
- Concetto: controllare se per il SubLevel esiste un RigidBody nel `PhysicsPipeline` (Sable) o se `RigidBodyHandle.of(subLevel)` è valido.
- Implementazione:
  - Ottieni container: `ServerSubLevelContainer container = SubLevelContainer.getContainer(serverLevel);`
  - Usa `RigidBodyHandle.of(subLevel)` o controlla pipeline per vederne presenza/stato.
- Pro:
  - Verifica tecnica che la contraption sia effettivamente simulata.
- Contro:
  - Richiede conoscenza dell'API di Sable; più complesso e fragile a cambi di API.

---

## 4) Snippet di esempio

A) Esempio senza dipendenza forte (controllo tramite registry key)

```java
// Pseudo-codice da adattare
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

ResourceLocation target = new ResourceLocation("simulated", "physics_assembler");

boolean hasPhysicsAssemblerNearby(Level level, AABB bbox) {
    // itera sui chunk/pos contenuti nel bbox (ottimizza come necessario)
    for (BlockPos pos : iteratePositionsWithin(bbox)) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) continue;
        ResourceLocation key = Registry.BLOCK_ENTITY_TYPE.getKey(be.getType());
        if (target.equals(key)) return true;
    }
    return false;
}
```

B) Esempio con Sable + PrimaryAssemblerExtension (se hai Sable & l'interfaccia disponibile)

```java
SubLevel sub = Sable.HELPER.getContaining(level, someBlockPosOrEntity);
if (sub instanceof ServerSubLevel serverSub) {
    PrimaryAssemblerExtension ext = (PrimaryAssemblerExtension) serverSub; // interfaccia dal package simulated
    BlockPos primary = ext.simulated$getPrimaryAssembler();
    if (primary != null) {
        // physics contraption
    }
}
```

C) Controllo RigidBody (concettuale)

```java
ServerSubLevelContainer container = SubLevelContainer.getContainer(serverLevel);
PhysicsPipeline pipeline = container.physicsSystem().getPipeline();
RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
if (handle != null && handle.isValid()) {
    // contraption è attivo nella pipeline fisica
}
```

---

## 5) API e classi utili da consultare
- Create: `ControlledContraptionEntity`, `Contraption` (per bounding box, controllerPos)
- Simulated: `PhysicsAssemblerBlockEntity`, `PhysicsAssemblerBlock`, `SimBlockEntityTypes`, `SimAssemblyHelper`, `SimAssemblyContraption`
- Sable: `Sable`, `SubLevel`, `ServerSubLevel`, `SubLevelContainer`, `PhysicsPipeline`, `RigidBodyHandle`
- Minecraft: `BlockEntity`, `BlockEntityType`, `Registry` (per leggere registry keys), `BlockPos`, `AABB`/`BoundingBox`

---

## 6) Raccomandazione pratica breve
- Se la tua mod non compila contro `simulated` e vuoi una soluzione robusta e semplice: implementa la Strategia A (confronto registry key `simulated:physics_assembler`) cercando il BE nel bounding box o al `controllerPos` del `ControlledContraptionEntity`.
- Se già usi Sable o vuoi integrazione profonda: usa Strategia B (Sable.HELPER + `PrimaryAssemblerExtension`) perché è il marker interno più affidabile.

---

Se vuoi, posso:
- generare direttamente un helper Java nella tua mod che implementa la Strategia A (senza dipendenze su `simulated`) pronto da incollare, o
- generare la versione che usa Sable e `PrimaryAssemblerExtension` (se mi confermi che la tua mod ha Sable sul classpath).

Dimmi quale preferisci e te la preparo (oppure preparo entrambe).
