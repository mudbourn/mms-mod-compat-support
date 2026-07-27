# ModMetro Bug Hunt — Round 2

Working doc for parallel Claude threads. **Read Shared Context, then only your
own task card.** Do not read the other cards, and do not read
`METRO-BUGHUNT.md` (round 1, already implemented) — everything you need is here.

Repo: `~/Documents/GitHub/mms-mod-compat-support`, currently at
`mod_version=0.9.10`, builds clean.

---

## Shared Context (everyone reads this)

### What we're patching

ModMetro is a **closed-source MIT mod**. We never edit it. Every fix is a mixin
in `src/main/java/info/mudbourn/mmscompat/mixin/metrofix/`, registered in
`src/main/resources/mms_compat.metrofix.mixins.json`, gated by `MetroFixGate`
(applies whenever `modmetro` is loaded).

### Getting the decompiled source

```bash
mkdir -p /tmp/mm && cd /tmp/mm && unzip -oq ~/Documents/GitHub/mms-mod-compat-support/libs/modmetro-v1.jar -d jar && vineflower -dgs=1 jar/com/example/modmetro out
```

Reference files: `/tmp/mm/out/MetroCartEntity.java` (649 lines, all cart logic)
and `/tmp/mm/out/MetroSpawnerItem.java` (57 lines).

Decompiled names are **intermediary** (`class_1937`, `method_5773`). This repo
uses **Mojang mappings** (`Level`, `tick`, `getDeltaMovement`, `blockPosition`).
Existing mixins in `metrofix/` are the reference for how each maps. Two you will
likely need, because they are easy to get wrong:

| Intermediary | Mojang |
|---|---|
| `method_5651(class_11368)` | `load(ValueInput)` |
| `method_5647(class_11372)` | `saveWithoutId(ValueOutput)` |

Note ModMetro overrides `load` / `saveWithoutId` — **not**
`readAdditionalSaveData` / `addAdditionalSaveData`. Target what it actually
overrides.

### Hard rules (these have bitten before)

1. **A mixin package owns every class in it.** No helper classes inside
   `mixin/metrofix/` — they crash at runtime. Helpers go in
   `info.mudbourn.mmscompat.metro.*`. A build check enforces this.
2. **`@At("TAIL")` ≠ `@At("RETURN")`.** TAIL injects at the *last* return only.
   If the method has early returns and you need all of them, use RETURN. This
   caused the stuck-slow-train bug (fixed in 0.6.7).
3. **Bump `mod_version` in `gradle.properties` before building**, or
   `mms-deploy` ships nothing.
4. **`defaultRequire: 1`** — a mixin that fails to apply crashes startup loudly.
   Intentional. Don't lower it.
5. **Do not commit, push, or release.** Leave the working tree ready and report.
6. `./gradlew build`. A clean build is necessary but **not** sufficient — mixins
   apply at class load, not compile. Nothing here can be runtime-tested.

### Helpers that already exist — use, don't reimplement

- `metro/MetroRailPath.java` — BFS along the real rail spine.
  `spineBehind(world, fromRail, frontRail, maxSteps, tolerance)` and
  `distance(...)`. **Use this instead of straight-line geometry** — straight-line
  measurement is the root of the U-turn, reverse, and detector bugs.
- `metro/MetroTuning.java` — JSON config at `config/mms_compat_metro.json`. New
  knobs go here (field + `Data` field + load + save), never as literals.
- `mixin/metrofix/MetroCartStateAccessor.java` — `@Accessor` interface reaching
  private fields on carts **other than `this`** (`lastDirection`,
  `lastStationPos`, `lineName`, `cachedFrontCart`, `cachedLeadCart`,
  `isWaiting`, `stationWaitTimer`, `leadSearchRetries`). `@Shadow` only reaches
  your own instance. Add accessors here if you need more.
- `metro/MetroTrainDespawn.java` — existing despawn plumbing.

### Facts already established — do not re-derive

- A minecart on a flat rail at block Y `R` sits at **`y = R + 0.0625`**. Round 1
  fixed the `R + 0.5` snaps.
- `lastDirection` **is already persisted** by `MetroDirectionPersistMixin`.
  Nothing to do there.
- `TicketType.FORCED` registers with flags `15` =
  `PERSIST | LOADING | SIMULATION | KEEP_DIMENSION_ACTIVE`, no timeout, no
  expire-if-unloaded. ModMetro's force-load tickets **survive server restart and
  never expire**, released only in `remove()`. Chunks are not the husk-train
  cause.
- Round 1 landed: `MetroRailSnapMixin`, `MetroOrphanRecoveryMixin`,
  `MetroFollowerLineSyncMixin`, plus `MetroTuning` keys
  `rail_vertical_tolerance`, `orphan_recovery_ticks`, `orphan_despawn_enabled`.

### Reporting back

Report: files changed, what each mixin targets, whether it built, and anything
you could not verify. Say plainly what is untested.

---

## Task G+H — Husk trains on reboot *(one card; these must land together)*

**Priority: critical. Nothing else ships before this.**

**Symptom:** after every server restart — crash, update, or clean reboot — trains
are dead on the line. They must be manually removed and the lines re-circulated
by hand.

### Root cause (G)

`saveWithoutId` writes `TrainIndex` to NBT and `load` restores it — **into the
private field only**. It never re-populates the `TRAIN_INDEX` synched-data
entry. Synched data is not serialized by vanilla; each entity re-populates its
own, and this one doesn't. `TRAIN_INDEX` is written only by the public
`setTrainData(UUID, int)`, called from the spawner and from
`MetroReverseConsistMixin` — never on the load path.

Meanwhile `getTrainIndex()` reads the **synched** value. So after every restart
every cart reports index `0` while its field holds the true number. Three
failures at once:

1. `findFrontCart` computes `targetIdx = trainIndex - 1` from the *field* but
   matches candidates on `getTrainIndex()`. Followers at index ≥2 search for a
   value nothing reports → null → **orphaned**. The index-1 follower searches
   for 0, which *everything* now reports — including itself, so it can select
   itself as its own front cart (distance 0, spacing error `-spacing`).
2. `isNextStationOccupied` filters on `other.getTrainIndex() == 0`, so every
   cart in the world counts as a lead. The next station always reads occupied,
   the 20-tick re-check loop never clears, and **trains never depart**.
3. Anything else keying off consist position inherits the same lie.

### Root cause (H) — regression risk from round 1

`MetroOrphanRecoveryMixin` escalates to despawn after `orphan_recovery_ticks`
(100), and `orphan_despawn_enabled` defaults to **`true`**. Given (G), every
follower from index 2 up is orphaned within ~5 seconds of world load — so on the
next reboot the recovery mixin **deletes most of the fleet**. This interlock is
not optional and must not be deferred to a later version than G.

### Do

1. **G:** mixin at `@At("TAIL")` of `load(ValueInput)` on `MetroCartEntity`,
   re-populating the synched index from the restored field. `setTrainData` is
   public and writes both, and `getLeadCartUuid()` is public — so this can be a
   single call using the already-restored `leadCartUuid` and `trainIndex`.
   Confirm at TAIL that both fields are populated (ModMetro's `load` restores
   them before returning); if ordering is wrong, use RETURN instead and guard.
2. **H:** an interlock in `MetroOrphanRecoveryMixin`:
   - a **post-startup grace window** (default ~600 ticks, in `MetroTuning`)
     during which orphan escalation never despawns;
   - and a precondition that the cart's synched index is **consistent with its
     field** before it is judged orphaned at all — the G bug must not be able to
     manufacture orphans again if a future ModMetro update reintroduces it.
3. Do **not** simply flip `orphan_despawn_enabled` to false and call it done.
   Despawn is the correct terminal escalation for a genuinely stranded cart;
   what's wrong is despawning carts that were never orphaned.

### Verify before reporting

Re-read `MetroReverseConsistMixin` — it renumbers consists and re-points
`leadCartUuid` via `setTrainData`. Your load-path fix must not fight it, and a
train saved mid-reversal must come back consistent.

---

## Task F — Spawn validation *(carried from round 1, unchanged)*

**Requirement:** carts must always spawn on rails, never off-rail. If the full
consist cannot be placed, either warn and spawn nothing, or offset the spawn
until every cart lands on a rail directly connected to the next — **orthogonal
adjacency only, no diagonals**. Repro: `wagons = 6`, spawn at the far end of an
8-block track.

**Root cause:** `MetroSpawnerItem.useOn` checks for a rail under the clicked
block **only**, then places carts `1..n` along the raw player look vector at
`spacing` intervals with **zero** rail validation and no failure path.
Off-rail, diagonal, into walls, past the end of track — all silently allowed. It
also uses a `pos.getY() + 0.5` lift; the correct on-rail height is
`railY + 0.0625` (round 1 fixed the other four sites, this one is yours).

**Do:** rewrite placement in a mixin on `MetroSpawnerItem`.

1. Walk the rail spine backwards from the clicked block with
   `MetroRailPath.spineBehind(...)` — orthogonal adjacency, no diagonals.
2. If the spine supports all `MetroConfig.wagons` carts at `MetroConfig.spacing`,
   place them on it at `railY + 0.0625`.
3. If not, try walking **forward** first (the offset case) before giving up.
4. If neither direction fits, **spawn nothing**, don't consume the item, and
   send the player a message naming how many carts fit vs. how many are needed.
   Route it through `MetroText` (see `MetroText.java` and the `mms_compat` lang
   files) — the localization layer already exists.

**Independent** of every other card.

---

## Task C — Departure never reaches top speed *(re-test first)*

**Symptom:** after a station dwell a train sometimes cruises fast but clearly
below top speed, indefinitely.

**Suspected cause:** `tickLeadCart` departs at `lastDirection × 0.4` and ramps
`× acceleration_factor` (`MetroAccelerationMixin`, default 1.1) per tick while
under `MetroConfig.speed`. But `applyProximityBraking` runs *every* tick and
scales velocity by `brakeFactor` whenever anything sits within `brake_distance`
on connected rail. Ramp-up and brake-decay can settle at a **stable equilibrium
below top speed**.

Round 1's Task B fixed the consist-identity bug in `applyProximityBraking` (a
cart used to brake for its own train) and added orphan recovery. Both were
plausible sources of the phantom obstacle.

**Do:**

1. **Re-assess against the current code first.** If the mechanism no longer
   exists, report that and **stop — do not patch**.
2. If it persists: separate ramp from braking. Preferred is a post-dwell grace
   window (suppress proximity braking for N ticks after departure, or floor
   `brakeFactor` while accelerating). N in `MetroTuning`.
3. Do **not** raise `acceleration_factor` as the fix — it masks the problem and
   makes arrivals harsher.

**Read before touching velocity:** `MetroCruiseZoneMixin` (level-triggered
clamp, only ever lowers speed) and `MetroSlowZoneMixin` (edge-triggered staged
ramp). Both have long explanatory headers and the reasoning matters.

---

## Task E — Off-screen realignment *(INVESTIGATION ONLY — no fix)*

**Unverified user theory:** carts may fail to hold formation when realignment
happens outside any player's view, possibly bounded by the render distance the
carts force-load.

**Do not write a fix.** Instrumentation only:

1. Note `MetroConfig.chunk_load_radius` and whether it is smaller than the
   follower search range in `findFrontCart` / `applyProximityBraking`.
2. Add a debug counter, gated behind a new `MetroTuning` boolean **defaulting to
   off**, logging each realignment/snap with: cart index, distance to nearest
   player, and whether the front cart was resolvable.
3. Report what it would show.

Deliverable: a short findings note plus gated logging. Nothing else.

---

## Task I — Force-load ticket accounting *(INVESTIGATION ONLY — no fix)*

`TicketType.FORCED` persists across restart and never expires; ModMetro releases
tickets only in `remove()`. Every husk cart therefore holds a
`chunk_load_radius` square loaded **permanently, across reboots**. The server has
been OOM-killed before (exit 137) with a 10G heap.

**Do:** quantify only — carts × radius → chunk count → rough memory, and whether
tickets accumulate across reboots or are re-registered idempotently. Check
whether `MetroTrainDespawn` releases tickets on its despawn path. **No patch**;
report numbers and a recommendation.

---

## Parallelization

- **G+H is a hard gate.** It ships first and alone. Do not run other cards'
  changes into the same build until it is verified.
- After G+H: **F**, **C**, **E**, **I** are mutually independent and can run
  concurrently. C, E, and I may well end with no code at all — that is a valid
  outcome, and preferable to a speculative patch.
- All threads touch `gradle.properties`, and F touches
  `mms_compat.metrofix.mixins.json`. Keep those edits minimal and flag them in
  your report for hand-merging.
