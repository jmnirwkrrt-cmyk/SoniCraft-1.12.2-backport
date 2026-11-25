# SoniCraft Legacy — Forge 1.12.2, P48

An unofficial, modified backport of **SoniCraft 1.10.1 by EnderPreston**.
See LICENSE.MD for CC BY-NC 3.0 licensing and upstream attribution.
Russian documentation: README_RU.md.

## Install

Use Java 8, Minecraft 1.12.2, Forge 14.23.5.x and GeckoLib Forge
1.12.2 3.0.31. Replace the previous SoniCraft JAR; do not install both.
Advanced Rocketry is optional. Back up saves before updating.
P47 mechanics, including companions, structures and Tornado, are retained.

## P48 changes

- Ordinary Egg Pawns join the supplemental biome spawner in nine urban/industrial
  zones, plus Green Hill, Emerald Hill and Seaside Hill at a lower weight.
  Bunny Pawns inhabit Night Carnival, Casino Night and Studiopolis.
- Added Eggrobo, Flapper and Battle Flapper registrations, spawn eggs, original
  textures/models and adapted combat AI. Both natural spawn tables and the
  supplemental spawner use the new roster. Structure entity import accepts
  these IDs. This is an adapted port, not a claim of complete original AI parity.
- Fixed supplemental cap accounting: unsuccessful attempts do not consume the
  remaining global budget. Existing cap/interval settings remain effective.
- Fourteen additional independent building routes: Casino Night (2), Night
  Carnival (1), Star Light (2), Chemical Plant (3), Press Garden (3), Scrap Brain (3).
  Existing source templates now have independent placement opportunities and
  entrance-relative heights, without requiring the main city/boss root first.
- Added the original Green Hill/Emerald Hill loops and Angel Island boulder
  features, with source placement offsets and ground restrictions. Remaining
  unported-biome features are listed explicitly in asset_report.json.
- Excluded Angel Island's source biome palette from normal Overworld generation
  and the custom SoniCraft Overworld palette. Registered IDs and existing chunks
  remain intact; Mobius's own palette is unchanged. Existing Overworld biome
  lists cannot re-enable the excluded island zones through this registration.
- Overworld weights are multiplied by `overworld.frequencyMultiplierP48`
  (default 1/3, rounded, minimum 1 for enabled weights; 0 disables insertion).
  This is relative climate weight, not an exact percentage of world area.
- Fresh configurations always use Mobius ID 1123, even without AR installed.
  Existing configured IDs are preserved. AR treats IDs >=10000 as star IDs;
  old 11230 saves need migration, described below.

Generation changes affect new chunks/placements only. Existing arenas and
buildings are not deleted. P47's maximum of one arena per connected biome
region remains in effect; generation is not guaranteed.

## Existing Mobius 11230 saves and Advanced Rocketry

Do not only change the config: that would point to a different dimension folder.
The included Python 3.10+ tool creates a separate migrated copy. Close Minecraft
and the server first; verify 1123 is unused in other mods and AR planet settings.

```sh
python migrate_mobius.py "/path/to/world" "/path/to/config/sonicraft.cfg"
python migrate_mobius.py "/path/to/world" "/path/to/config/sonicraft.cfg" --copy-to "/path/to/world-P48"
```

First command: read-only validation/report. Second: copy the full save, rename
DIM11230 to DIM1123 inside the copy, update recognized dimension references in
player/world NBT and region chunks, and produce `sonicraft.cfg.P48-migrated`
inside the new save. The original world and config are never edited. Install
the generated config as config/sonicraft.cfg, then load the migrated copy.
Use `--new ID` for a different verified free ID between 2 and 9999.

The tool validates NBT/region data before copying and refuses existing output
folders/target dimensions. It preserves unrelated numeric values. It handles
standard named dimension fields, Forge player respawn entries and dimension
data keys; arbitrary custom formats/cached coordinates in other mods are not
universally migratable. It does not edit AR galaxy configuration or existing
rocket/station routes. Check those after loading the copy. Your original
save and original config provide the rollback path. No real user save was
available for an end-to-end migration test.

## Source package and build

This is an incremental-source distribution, not a falsely flattened Gradle
project. `p48/` contains the current Java sources, resource changes, ASM patches,
verifiers and migration tool. `historical/` contains the available P27 base
sources and subsequent local patch sources. `embedded-source/` retains source
records embedded in P47. Historical sources are reference material and may
precede later bytecode patches; do not compile them as a single current tree.
`baseline/P47.jar` is the exact reproducible input for the P48 patch build.
`baseline/SHA256.txt` pins it. The release JAR is separate.

Use JDK 8, Python 3.10+, a Forge 1.12.2 MCP-mapped development JAR,
GeckoLib 3.0.31, MCP-to-SRG TSRG mappings and a dependency classpath containing
ASM (including asm-tree), Gson, Guava, Netty, LWJGL, Vecmath, Log4j,
Commons IO/Lang and Fastutil. These dependencies are not vendored.

```sh
bash p48/BUILD.sh baseline/P47.jar /path/forge-mcp.jar /path/geckolib.jar /path/output.tsrg '/path/deps/*' output/P48.jar
```

The build verifies the baseline hash, compiles Java 8 helpers, remaps to SRG,
applies bytecode changes, checks method/field links, verifies the archive and
resources, exercises the migration on synthetic saves, and loads all geometry
and animations through the real GeckoLib loader. Runtime resource overlays are
already supplied; asset/model generator scripts document their provenance and
require the original SoniCraft JAR/source tree if rerun.

Minecraft gameplay, multiplayer, live spawn density, rendered models, and the
user's full AR modpack still need in-game testing.
