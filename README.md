# MMS Mod Compat Support

Server-wide mod compatibility patches for MMSLive01, on Fabric 1.21.11.

Every patch is gated: each mixin config declares an `IMixinConfigPlugin` that checks
`FabricLoader.isModLoaded(...)` for the mods it stands between, so removing a mod
from the pack disables its patches instead of crashing the game.

## Building

```bash
./gradlew build
```

The jar lands in `build/libs/`.

### `libs/` must be populated first

This repository does **not** track the mods it compiles against. They are other
projects' redistributables under their own licenses, and shipping them inside an
MIT-licensed repo would misrepresent those licenses. `libs/` is gitignored, and a
fresh clone will fail to compile until you fill it in.

Every jar below comes from the pack itself — copy them out of the Prism client's
`mods/` folder (or the prod/testing server's), matching versions exactly. Version
skew here shows up as confusing mixin failures at runtime rather than build errors:

```
ACE_mc1.21.11-5.0.0.jar
MutantMonsters-v21.11.2-mc1.21.11-Fabric.jar
Origins-Legacy-1.12.16+1.21.11.jar
PuzzlesLib-v21.11.13-mc1.21.11-Fabric.jar
absolutrevive-v1.2.1b.jar
aerialhell-0.7.7.7_fabric1.21.11.jar
arc-19.1.1-fabric.jar
bettercombat-fabric-3.1.0+1.21.11.jar
cardinal-components-base-7.3.1.jar
emf_compat_core_1.21.11_1.0.0.jar
entity_model_features-3.2.4-1.21.11-fabric.jar
entity_texture_features_1.21.11-fabric-7.1.jar
expanded_weaponry-0.8.jar
frostiful-2.4.0+1.21.11.jar
furnish-31.jar
jobsplus-19.1.0-fabric.jar
kielsons-api-0.8.1.jar
mapsyncer-1.0.3-fabric-1.21.11-bugfix.jar
modmetro-v1.jar
rubies-1.0.0+1.21.11.jar
sounds-2.4.23+edge+1.21.11-fabric.jar
xaerominimap-fabric-1.21.11-26.4.2.jar
```

Two of these are not obvious from the source imports alone:

- **Kielson's API** is a nested jar inside Expanded Weaponry. Nested jars are not
  placed on the compile classpath, so it has to be extracted and added separately.
- **Entity Texture Features** is needed because EMF's
  `EMFEntityRenderState#emfEntity()` returns an ETF type; reading a UUID off it
  will not compile without ETF present.

## Conventions

**Bump `mod_version` in `gradle.properties` before every build you intend to
deploy.** `mms-deploy` matches on filename, so rebuilding at an unchanged version
silently ships nothing.

**A mixin config owns every class under its `package`.** Mixin refuses to load a
class in that package as an ordinary class, so a plain helper placed there dies
with `IllegalClassLoadError` the first time it is touched — including a constant
read by a sibling mixin. Helpers belong in a normal package; see
`client/HeldPoseSource.java`. `./gradlew build` runs `checkMixinPackages`, which
fails the build on any class in a mixin package that its config does not declare.

**Commit modified files, not just new ones.** Release `v0.9.47` added 31 new files
and omitted every edit to an already-tracked file — `fabric.mod.json`,
`build.gradle`, `gradle.properties`, and a visibility change. The result compiled
nowhere and left five mixin configs undeclared, so `heldpose` and `cemrelay`
shipped inside the jar without ever loading. Check `git status` for ` M` lines
before tagging.
