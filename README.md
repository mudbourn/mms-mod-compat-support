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

The required jars are the `files("libs/...")` lines in `build.gradle`, which
is the source of truth for names and versions. Copies live in the private
`mms-libs` repo under `mms-mod-compat-support/`, and CI copies that folder into
`libs/`. They must match the versions the pack ships. Version skew here shows up
as confusing mixin failures at runtime rather than build errors.

One of these is not obvious from the source imports alone:

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
read by a sibling mixin. Helpers belong in a normal package. `./gradlew build`
runs `checkMixinPackages`, which fails the build on any class in a mixin package
that its config does not declare.

**Commit modified files, not just new ones.** Release `v0.9.47` added 31 new files
and omitted every edit to an already-tracked file — `fabric.mod.json`,
`build.gradle`, `gradle.properties`, and a visibility change. The result compiled
nowhere and left five mixin configs undeclared, so they shipped inside the jar without ever
loading. Check `git status` for ` M` lines before tagging.
