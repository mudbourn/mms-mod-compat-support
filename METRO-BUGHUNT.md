# ModMetro Bug Hunt — Implementation Sheet

Working doc for parallel Claude threads. **Read the Shared Context section, then
only your own task card.** Do not read the other task cards — that is the point
of this sheet.

Repo: `~/Documents/GitHub/mms-mod-compat-support`

---

## Shared Context (everyone reads this)

### What we're patching

ModMetro is a **closed-source MIT mod**. We never edit it. Every fix is a mixin
in `src/main/java/info/mudbourn/mmscompat/mixin/metrofix/`, registered in
`src/main/resources/mms_compat.metrofix.mixins.json`, gated by `MetroFixGate`
(fires whenever `modmetro` is loaded).

### Getting the decompiled source

You need to read ModMetro's bytecode to target it. Do this once:

```bash
mkdir -p /tmp/mm && cd /tmp/mm && unzip -oq ~/Documents/GitHub/mms-mod-compat-support/libs/modmetro-v1.jar -d jar && vineflower -dgs=1 jar/com/example/modmetro out
```

Output lands in `/tmp/mm/out/`. The two files that matter:

- `MetroCartEntity.java` (649 lines) — all cart logic. Key methods:
  `tick`, `tickLeadCart`, `tickFollowerCart`, `applyProximityBraking`,
  `findFrontCart`, `findNearestRail`, `isConnectedByRail`, `isOnRail`,
  `manageChunkLoading`
- `MetroSpawnerItem.java` (57 lines) — train spawning

Decompiled names are **intermediary** (`class_1937`, `method_5773`). In mixin
code you write **Mojang/yarn-mapped** names (`Level`, `tick`) — the existing
mixins in `metrofix/` are the reference for how each maps.

### Hard rules (these have bitten before)

1. **A mixin package owns every class in it.** No helper classes inside
   `mixin/metrofix/` — they crash at runtime. Helpers go in
   `info.mudbourn.mmscompat.metro.*`. There is a build check enforcing this.
2. **`@At("TAIL")` ≠ `@At("RETURN")`.** TAIL injects at the *last* return only.
   If the method has early returns and you need all of them, use RETURN.
   This caused the stuck-slow-train bug (fixed in 0.6.7).
3. **Bump `mod_version` in `gradle.properties` before building**, or
   `mms-deploy` ships nothing.
4. **`defaultRequire: 1`** — a mixin that fails to apply crashes startup loudly.
   That is intentional. Don't lower it.
5. **Do not commit, push, or release.** Leave the working tree ready and report
   what you changed. The user handles all git operations.
6. Build with `./gradlew build`. A clean build is necessary but **not**
   sufficient — mixins apply at class load, not compile.

### Existing helpers you should use, not reimplement

- `metro/MetroRailPath.java` — BFS along the actual rail spine.
  `MetroRailPath.spineBehind(world, fromRail, frontRail, maxSteps, tolerance)`
  returns the ordered rail blocks walking back from a cart; `.distance(...)`
  gives rail-following distance. **Use this instead of straight-line geometry** —
  straight-line measurement is the root of the U-turn, reverse, and detector
  bugs, and of half the follower bugs below.
- `metro/MetroTuning.java` — JSON config at `config/mms_compat_metro.json`.
  Add new knobs here (field + `Data` field + load + save), never as literals.

### The one number everyone needs

**A minecart riding a flat rail at block Y `R` sits at `y = R + 0.0625`.**

ModMetro (and one of our own mixins) snaps carts to `R + 0.5` in five places.
That 0.4375-block lift is the floating-cart bug. If your task touches a
`teleportTo`, it uses `R + 0.0625`.

### Reporting back

When done, report: files changed, what the mixin targets, whether it built, and
anything you could not verify. Say plainly if something is untested — none of
this can be runtime-tested from here.

---

## Task A — Vertical alignment (floating carts)

**Symptom:** follower carts float ~0.44 blocks above the rail. The lead cart is
always correct. Carts that look *correct* are the ones that never re-snapped;
the floating ones are the ones where realignment ran.

**Root cause:** every rail snap uses `railY + 0.5` instead of `railY + 0.0625`.

Five call sites:

| Where | Detail |
|---|---|
| `MetroCartEntity.tickFollowerCart` | off-rail recovery branch, `teleportTo(x+0.5, railY+0.5, z+0.5)` |
| `MetroCartEntity.tickFollowerCart` | catch-up snap, `bestRail.getY() + 0.5` |
| `MetroCartEntity.tickFollowerCart` | no-rail fallback, snaps to front cart's Y |
| `MetroSpawnerItem` | `pos.getY() + 0.5` (**Task F owns this one — skip it**) |
| `mixin/metrofix/MetroFollowerSeparationMixin.java` | two `teleportTo(rail.getX()+0.5, rail.getY()+0.5, …)` calls — ours, inherited the bug |

**Do:**

1. Fix our own mixin's two teleports directly (plain edit, no mixin needed).
2. New mixin `MetroRailSnapMixin` correcting the ModMetro sites. Prefer
   `@Redirect` on the `teleportTo` calls inside `tickFollowerCart` over
   `@ModifyConstant` on `0.5` — that constant appears many times in the method
   for X/Z centering and you must not touch those.
3. Harden `isOnRail`: it currently returns true for a cart hovering at `+0.5`,
   so recovery never re-fires and the cart floats forever. Add a vertical-error
   check — if the cart is more than ~0.2 above the rail it rides, treat it as
   off-rail so recovery runs.
4. `findNearestRail` may return `center.above()`, adding a further full block of
   lift. Consider preferring at-or-below candidates.

**Don't:** touch X/Z centering, or the spawner.

---

## Task B — Orphan carts and the terminal choke

**Symptom:** 2-ish carts detach and strand at a terminal station. Arriving
trains then brake to a permanent halt — an indefinite choke. Consistently the
same terminal.

**Root cause — two defects that compound:**

1. **`applyProximityBraking` mis-identifies own-consist carts.** It skips a cart
   only when `myTrainId.equals(other.getLeadCartUuid())`, where `myTrainId` is
   *this cart's own UUID*. That works only for the lead cart of an intact train.
   Once a cart is orphaned or renumbered by a reversal, it stops matching and
   **becomes a foreign obstacle to its own train**. It is stationary, so
   `brakeFactor` drives arrivals to zero and holds them there.
2. **`tickFollowerCart` has no orphan recovery.** When `findFrontCart` returns
   null it does `leadSearchRetries++; return;` — and **`leadSearchRetries` is
   never read anywhere in the class**. One missed tick at a terminal orphans a
   cart permanently. Terminals are exactly where reversals renumber consists
   (see `MetroReverseConsistMixin`), so this fires there.

**Do:**

1. Fix consist identity in braking: compare `leadCartUuid` to `leadCartUuid`
   (falling back to own UUID when this cart *is* the lead), so a cart never
   brakes for its own train.
2. Additionally ignore stationary orphans as brake targets — a cart with no
   resolvable lead should not gate the line.
3. Consume `leadSearchRetries`: after N failed lookups, escalate —
   (a) re-link by `leadCartUuid` against all loaded carts, else
   (b) re-index into the nearest consist on the same line, else
   (c) despawn. `metro/MetroTrainDespawn.java` already exists; check whether it
   covers case (c) before writing new code.
4. Put N and the despawn toggle in `MetroTuning`.

**Coordinate:** Task C depends on this landing first. Read
`mixin/metrofix/MetroReverseConsistMixin.java` before starting — it already
renumbers consists and re-points `leadCartUuid`, and your fix must agree with it.

---

## Task C — Departure never reaches top speed

**Blocked on Task B.** Do not start until B has landed.

**Symptom:** after a station dwell, a train sometimes cruises fast but clearly
below top speed, indefinitely.

**Root cause:** `tickLeadCart` departs at `lastDirection × 0.4` and ramps
`× acceleration_factor` (our `MetroAccelerationMixin`, default 1.1) per tick
while under `MetroConfig.speed`. But `applyProximityBraking` runs *every* tick
and scales velocity by `brakeFactor` whenever anything is within
`brake_distance` on connected rail. Ramp-up and brake-decay reach a **stable
equilibrium below top speed**. Same root cause as the Task B choke, milder
symptom — which is why B lands first: verify the symptom still exists after B.

**Do:**

1. Re-test after B. If it's gone, report that and stop — do not patch.
2. If it persists: separate ramp from braking. Preferred approach is a
   post-dwell grace window (suppress proximity braking for N ticks after
   departure, or floor `brakeFactor` while accelerating). N in `MetroTuning`.
3. Do not raise `acceleration_factor` as the fix — that masks it and makes
   arrivals harsher.

**Interacts with:** `MetroCruiseZoneMixin` (level-triggered clamp, only ever
lowers speed) and `MetroSlowZoneMixin` (edge-triggered staged ramp). Read both
headers before touching velocity — they are well-documented and the reasoning
matters.

---

## Task D — HUD shows no line and no next stop

**Symptom:** only a handful of carts show the current line; none show the next
stop.

**Root cause:** `lineName` is a **lead-cart-only field**. Followers copy
`CURRENT_STATION` / `NEXT_STATION` from the lead only inside the
`dist <= spacing + 4` branch of `tickFollowerCart`, and **never copy `lineName`
at all**. `metro/MetroLineSyncServer.java:47` reads the line off whichever cart
the player is sitting in — empty for every follower. "A handful show the line" =
the players who happened to board cart 0.

**Do:**

1. In `MetroLineSyncServer`, resolve the rider's **consist lead** (via the
   cart's `leadCartUuid`) and read line + next-stop from that, not from the
   rider's own cart. This alone fixes the reported symptom for riders.
2. Also propagate `lineName` to followers unconditionally in `tickFollowerCart`
   (outside the spacing branch), so any other consumer of the field is correct
   too. `MetroCartLineAccessor` already exposes the field.
3. Sanity-check the client side: `mixin/metrofix/client/MetroHudTextMixin.java`
   already suppresses the stock "Prox:" line and draws a persistent one from the
   synced value — confirm it renders once the server actually sends a non-empty
   next stop.

**Fully independent** of A/B/C/F. Small and self-contained.

---

## Task E — Off-screen realignment (INVESTIGATION ONLY — no patch)

**Unverified user theory:** carts may fail to keep formation when realignment
has to happen outside any player's view, possibly related to the render distance
the carts force-load.

**Do not write a fix.** Add instrumentation only:

1. `MetroCartEntity.manageChunkLoading` force-loads a
   `MetroConfig.chunk_load_radius` square every 10 ticks via chunk tickets.
   Note that radius and whether it is smaller than the follower search range.
2. Add a debug counter (gated behind a new `MetroTuning` boolean, default off)
   logging each realignment/snap event with: cart index, distance to the nearest
   player, and whether the front cart was resolvable.
3. Report what the instrumentation would show. Do not enable it by default.

Deliverable is a short findings note plus the gated logging, nothing more.

---

## Task F — Spawn validation

**Symptom / requirement (from the user, verbatim intent):** carts should always
spawn on rails, never off-rail. If the full consist cannot be placed, either
warn and spawn nothing, or offset the spawn until every cart lands on a rail
directly connected to the next (orthogonal adjacency only — **no diagonals**).
Repro: `wagons = 6`, spawn at the far end of an 8-block track.

**Root cause:** `MetroSpawnerItem.useOn` checks for a rail under the clicked
block **only**, then places carts `1..n` along the raw player look vector at
`spacing` intervals with **zero** rail validation and no failure path. Off-rail,
diagonal, into walls, past the end of track — all silently allowed. It also uses
the `pos.getY() + 0.5` lift (see Shared Context; Task A owns the other four
sites, you own this one).

**Do:** rewrite the placement in a mixin on `MetroSpawnerItem`.

1. Walk the rail spine backwards from the clicked block using
   `MetroRailPath.spineBehind(...)` — orthogonal adjacency, no diagonals.
2. If the spine supports all `MetroConfig.wagons` carts at `MetroConfig.spacing`,
   place them on it at `railY + 0.0625`.
3. If it doesn't, try walking **forward** first (the offset case) before giving
   up.
4. If neither direction fits, **spawn nothing** and send the player a failure
   message naming how many carts fit vs. how many are needed. Do not consume the
   item. Route the message through `MetroText` — the existing localization layer,
   see `MetroText.java` and the `mms_compat` lang files.

**Fully independent** of A/B/C/D.

---

## Suggested parallelization

- **A**, **D**, **F** can run concurrently — no shared files.
- **B** runs concurrently with those, but **C waits on B**.
- **E** is read-only investigation, safe any time.
- All threads touch `mms_compat.metrofix.mixins.json` and `gradle.properties`.
  Expect conflicts there; keep those edits minimal and mention them in your
  report so they can be merged by hand.
