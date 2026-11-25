## Контроль версии 0.1.26 (только исходники)

- 1314/1314 уникальных блоков; 361 исходный NBT без отсутствующих ID SoniCraft, без изменения исходного NBT.
- Все 25 новых блоков: оригинальные blockstate JSON, 106 моделей, 81 текстура и инвентарные модели проверены.
- 98 Java файлов проходят разбор синтаксиса. Проверки не выполняют компиляцию типов Forge.
- `./gradlew help` снова остановлен на DNS `services.gradle.org` до JavaCompile.
- Прямой альтернативный запрос дистрибутива Gradle также не сработал.
- Не проверены игра, перезагрузка мира с TileEntity, GUI, оригинальные механизмы боссов.

**0.1.21:** Python `tools/verify_021.py` и `tools/verify_complexes_021.py` прошли; Java AST: 90 файлов. Проба сборки не перешла к компиляции: `UnknownHostException: services.gradle.org`; подробнее в `SOURCE_COMPLEXES_021_UPDATE_RU.md` и `build-attempt-0.1.21.log`. Нет игрового `.jar`, Forge/клиент не тестированы.

## Дополнение 0.1.18
- `tools/verify_018.py`: 1100 зарегистрированных блоков, 167 новых, 361 оригинальный NBT, 309 с полным покрытием SoniCraft ID; рекурсивные ссылки на модели/текстуры.
- `tools/check_java_syntax.java`: 84 Java-файла, синтаксическая проверка без зависимостей Forge.
- Реальная компиляция не состоялась из-за `UnknownHostException: services.gradle.org`; подробности в `build-attempt-0.1.18.log`.

# Проверка 0.1.10

Сборка Java 8 / Forge 1.12.2 успешна; все 18 проверок пройдены. Результаты — `verification-0.1.10.txt`.

Новый `columnContentCheck` проверяет шесть состояний, соединение/разрыв по X/Y/Z, поворот и метаданные, установку/удаление опоры сломанной верхушки, несовместимые оси, выпадение/Creative pick/Silk Touch, сопротивление взрыву и составную коллизию/выбор лучом. `environmentCheck` проверяет точные координаты всех 12 шаблонов и отказ при препятствиях. Повторно прошли проверки персонажей, анимаций, бадников, флоры, портала, изумрудов, блоков, плит/стен, рельефа и группировки биомов.

Проверены ресурсы 426 базовых блоков и 1241 ссылка на модели, включая шаблоны Minecraft 1.12. Графический клиент и сетевой сеанс не запускались; визуальные результаты в игре пока не подтверждены. Новые структуры появляются только при генерации новых чанков.

# Проверка 0.1.9

Сборка успешна; все 17 проверок пройдены. Подробности — `verification-0.1.9.txt`. Проверены 108 Geo-анимаций, 775 кубов 16 Geo-моделей, 1200 JSON-моделей и 86 рецептов. Общий звук Metal Sonic/бадников регистрируется однократно. Семь текстур сущностей и две Geo-модели совпадают с оригиналом побайтово.

`badnikContentCheck`: исходные приоритеты целей, настоящая melee-задача с проверкой команды навигации/урона, иммунитеты, мирная сложность, NBT и категория для учёта лимита спавна. 60 вызовов смерти проверяют оба освобождаемых вида, диапазон металлолома и отсутствие повторной выдачи. Проверяются `doMobLoot`, поведение животных, исходные цели персонажей и движение/сброс крыльев Flicky.

`animationPlaybackCheck` расширен двумя новыми Geo-моделями. `renderRegressionCheck` прогоняет их кубы через настоящий renderCube GeckoLib без OpenGL-отрисовки. Java-модели перенесены по исходной иерархии, UV и позам: Flicky 7 кубов, Pocky 9, Rhinobot 11. Все размеры кубов в этих моделях целочисленные, инфляция сохранена.

Физический путь движения использует штатный навигатор 1.12; в тесте melee подменяется только выдача пути. Графический клиент, сетевой спавн, светящиеся слои и поведение популяции в длительном игровом сеансе не проверялись.

# Проверка 0.1.8

Сборка успешна, все 16 проверок пройдены. Результаты: `verification-0.1.8.txt`. На контрольном участке 8192 × 8192 блоков число границ сократилось с 885658 до 96751 (данные четвертной сетки генерации); 61% новых границ соединяют биомы одной группы. Все 16/20 биомов найдены на расширенной выборке. Проверка присутствия всех биомов расширена с 8192 до 32768 блоков: после увеличения размеров небольшой участок закономерно не обязан содержать всю палитру.

Новые проверки: `shapeContentCheck` — плиты, объединение, расход, коллизия, выпадение, метаданные; стены и соединения. `landmarkCheck` — 403 исходные клетки, отказ при чужом биоме, незагруженном чанке, отсутствии опоры и препятствии. `regionalBiomesCheck` — размеры и соседство, сохранение палитр 16/20, отрицательные координаты, стабильность разбиения на чанки, кеш биомов и поиск зон.

Родители моделей и ссылки из blockstates разрешаются по настоящим ресурсам Minecraft 1.12.2 через `tools/validate_model_links.py`. Это дополнение к разбору JSON штатными ModelBlock/CraftingHelper; оно выявило и исправило отсутствующие шаблоны калиток из 0.1.7.

Графический клиент и сетевой сеанс не запускались. Сравнение карт — данные генератора, а не скриншоты Minecraft. Старые чанки не мигрируют.

# Проверка 0.1.7

Сборка успешна; все 13 проверок пройдены. Результаты — `verification-0.1.7.txt`.

`bulkContentCheck`: свойства и выпадение 274 блоков; углы лестниц, соединения заборов, открытие калитки; исходные Creative-категории и 17 яиц; 911 моделей разобраны штатным ModelBlock 1.12, 49 рецептов — Forge CraftingHelper с проверкой непустых результатов/ингредиентов.

`emeraldProgressionCheck`: порядок семи наград, расход ключа, 50 колец за полный комплект, предметы в инвентаре, сохранение NBT через сжатую сериализацию, перенос флагов обработчиком Clone, отсутствие повторной выдачи, отказ при полном инвентаре, проверка владельца контейнера, цикл таблички 60 тиков и запрет восстановления после разрушения. Это тест постоянного NBT и обработчика, не полный сеанс сохранения/загрузки игрока сервером: для последнего нужен запущенный Forge data fixer.

Графический клиент, реальный сетевой обмен окна K, выделенный сервер и управление с клавиатуры здесь не запускались. Статический разбор моделей не доказывает отсутствие визуальных артефактов в сборке с OptiFine.

# Проверка 0.1.6

Сборка успешна; все 11 проверок пройдены. Результаты — `verification-0.1.6.txt`.

Новый `mobiusZonesCheck`: настоящие чанки для всех восьми новых биомов, проверка их верхнего/подстилающего слоя и замены камня после вырезания пещер; старая палитра из 8 зон; свойства лесов и застывшей нефти, Jump Boost и пересчёт взрывостойкости. Выборка `mobiusPortalCheck` расширена до двумерной сетки, поскольку прежняя одна полоса не обязана содержать все 16 зон.

Palette Мобиуса, температура/осадки/цвета и музыкальные события восьми новых биомов сверены с оригинальным JAR. Маппинги 1.20.1 дополнительно проверены: `m_60967_` = jumpFactor, `m_5909_` = getVisualShape, а не коллизия. Справочники: https://mappings.dev/1.20.1/net/minecraft/world/level/block/state/BlockBehaviour$Properties.html и https://mappings.dev/1.20.1/net/minecraft/world/level/block/state/BlockBehaviour.html .

Клиент, реальное воспроизведение музыки и сетевой переход между измерениями здесь не запускались. Изменение карты новых чанков описано в README.

# Проверка 0.1.5

Итог: сборка успешна, все 10 проверок пройдены. Полный перечень результатов — `verification-0.1.5.txt`.

Новая проверка `floraCheck` охватывает пять исходных patch-наборов и 11 видов растений трёх зон; последовательные вероятности выбора и no_op; почву, границы биома, незагруженные чанки и препятствия; обе половины GHZ, ограничения высоты, размещение игроком и взаимное удаление частей. `environmentCheck` сохраняет проверку расхода кольца из 0.1.4.

Ресурсы: новые текстуры и девять файлов анимации цветов/метаданных сопоставлены побайтово с оригинальным JAR. Регистрационные ID сохранены. Предварительная проверка ресурсов: 56 графов блоков, 12 биомов, 17 персонажей и все JSON.

Графический клиент не запускался: внешний вид, анимация атласа в игре и работа в сборке с OptiFine требуют клиентской проверки. Проверка генерации использует детерминированный тестовый мир с плоской поверхностью; точное совпадение карт 1.12.2 и 1.20.1 не заявляется.

Ниже — история проверки 0.1.4 и предыдущих выпусков.

# Verification — 0.1.4 Environment Alpha

## Current release

- Reported Shadow crash: reproduced in the actual GeckoLib 3 `IGeoRenderer.renderCube`, using original geometry. Four foot cubes contain 16 null quads total. `GeoCompatibility` compacts only absent faces; the existing real faces retain their vertices/UVs. The regression executes all 749 cubes across 14 models through the same renderCube method after the fix. No OpenGL draw or shader integration is simulated.
- `environmentCheck`: Survival/Creative ring placement, inventory consumption, no overwrites; all six pillar templates with exact source coordinates and blocks, obstruction preflight; twelve original Ogg files and repeatable Ambient sound objects; head pitch/yaw/reset; population subscriber invoked with a real PopulateChunkEvent.Pre object, preserving cave air, vanilla ores and adjacent vanilla-biome stone.
- Full earlier suite retained: terrain/block metadata, entity/NBT/model parsing, Tails collision/flight, food interactions, all 102 animation clips, food AI scheduling, Mobius frames/terrain/exit placement.
- Resource graph validation includes new plant/column models, textures and all twelve music sound resources.
- The original biome JSON explicitly contains vanilla ore features; replacing every vanilla material would diverge from the original. Chaostone is applied after carving because vanilla cave carvers do not recognize the custom stone.

The graphical client, real sound playback and the user's OptiFine/shader/mod combination have not been run here. Runtime test fixes a demonstrated NPE; visual confirmation remains a gameplay check. World-generation changes apply to new population, not previously populated chunks. Full structures, all plant families and exact 1.20 ore distribution remain incomplete.

The initial attempt to dispatch through EventBus in the plain Java harness failed because Forge's launch-time event transformer had not injected the no-argument event constructor. The harness now invokes the subscriber with the real event object. Production registration uses EVENT_BUS, verified against ForgeEventFactory.onChunkPopulate; full bus dispatch remains a Forge-launch integration check.

Final build/test output is saved in `verification-0.1.4.txt`.

## Historical 0.1.3 verification (superseded Shadow status)

# Verification — 0.1.3 Mobius Alpha

## Current scope and unresolved report

The user's Shadow-on-spawn crash has **not** been reproduced and is **not marked fixed**. No crash-report is available. All 17 constructor/NBT checks and Shadow's clips pass headless tests. A real client report/stack trace is needed to identify the failing path; no speculative catch-all exception suppression was added.

This release is not tested in a graphical client or dedicated server. Tests of dimension terrain and exit preparation do not verify Forge's live networking, player transfer or respawn lifecycle.

## Build result

Java 8; Forge 14.23.5.2860; ForgeGradle 3.0.197; snapshot_20171003-1.12. Release command: `gradle --offline build smokeCheck characterSmokeCheck tailsFlightCheck foodContentCheck animationPlaybackCheck characterFoodCheck mobiusPortalCheck`. Resource validator: `python tools/validate_assets.py`. The final results are also retained in `verification-0.1.3.txt`.

## New regression coverage

- `animationPlaybackCheck`: all 102 clips, 160 frames each, run through the actual GeckoLib 3 AnimationController, AnimationProcessor and interpolator. Finite bone rotations/scales are checked. Missing animation bones use GeckoLib's normal `shouldCrashOnMissing=false`, matching production. This includes all Shadow clips; it does not execute OpenGL rendering.
- `characterFoodCheck`: real AI scheduler issues path requests for Sonic, Tails, Knuckles, Amy, Shadow, Silver, Vector and Rouge. Tests Survival/Creative, both hands, a closer empty-handed player, removal of food, range, Spectator, cocoa metadata and interpolated tail-spin/reset. Navigation is a recording test boundary, so full movement through loaded chunks is not claimed.
- `foodContentCheck`: original checks retained, with Shadow and Silver feeding added (seven characters total).
- `mobiusPortalCheck`: X/Z gold frames of widths 2/4/21; invalid 1/22-wide frames; blocked interior; broken frame removal; deterministic generation of eight Mobius biomes and seed variation; spawn-position query; three actual ChunkGeneratorOverworld chunks; original igniter recipe; exit feet/head/floor, opposite-side fallback, refusal to carve through obstructions.
- Existing terrain, character and Tails flight smoke checks retained.

## Source trace

- Tails spin: original `client/model/Modeltails_flying.java`, `tails.yRot = ageInTicks`, adapted to the Geo tailroot and the rotated flying body.
- Temptation: original entities' TemptGoal declarations; Shadow cocoa SRG `f_42533_` -> 1.12 dye metadata 3, Silver apple `f_42410_`, Vector `BUBBLEGUM`; Rouge diamond/emerald. Mapping lookup: https://mappings.dev/1.20.1/net/minecraft/world/item/Items.html . Vanilla 1.12 EntityAITempt bytecode confirms that it first chooses the nearest player and only then checks the held item; this release selects among matching players. Vanilla temptation already permitted Creative, so Creative exclusion is **not** claimed as the old bug's proven cause.
- Mobius: `MobiusItem`, `MobiusPortalBlock`, `MobiusPortalShape`, `MobiusTeleporter`, `flint_and_ring_recipe.json`. Existing imported terrain is reused for 8/16 source zones. Exit-platform and player-only transport are explicit 1.12 adaptations.

## Required gameplay validation

Spawn Shadow alone and next to other characters and capture any crash report; observe Tails takeoff/spinning/landing; follow each listed lure over terrain; build gold portals in both axes, travel both ways, break/reignite, save/reload in Mobius, test two players and a dedicated server. Check that a chosen dimension ID does not conflict before starting a save.

## Previous release checks (historical 0.1.2)



## Passed on the delivered source

- `gradle build`, `smokeCheck`, `characterSmokeCheck`, `tailsFlightCheck` and `foodContentCheck`: passed (food test rerun after fixing its mock-world spawn point) using Java 8 and ForgeGradle 3.0.197 / Forge 1.12.2-14.23.5.2860 / snapshot_20171003-1.12 mappings.
- `python tools/validate_assets.py`: 39 block model/texture graphs; 12 biome surface references; eight palm template palettes and bounds; 17 character texture/geometry/animation/sound resource graphs; all JSON syntax.
- Terrain smoke: 39 block constructors and metadata roundtrips; 12 biome constructors/registrations; deterministic 1024x1024 custom-world biome map with valid IDs and 11 custom zones in the sample.
- Character smoke: 17 constructors with initialized AI tasks and expected health/dimensions; health/name NBT save/load roundtrips; 14 geometries baked using the actual GeckoLib library; 102 animation clips parsed, including nonempty XYZ keyframes for every source channel; three converted vanilla-model constructors.
- Distribution JAR: all classes use Java 8 bytecode (major version 52); production SRG references after reobfuscation; all 17 entity subclasses, 39 block definitions, correct GeckoLib minimum internal version, biome search command, Russian names and plank recipes; no MDK ExampleMod classes; ZIP CRC checks.

The standalone registry harness emits warnings about the sonicraft prefix because it runs outside a real Forge mod-loading context. They are harness warnings, not a client launch test. Normal Forge registration is performed by the mod's registry event handlers.

## New content checks (0.1.2)

`foodContentCheck` runs against Minecraft/Forge classes, not a reimplementation of the food/recipe logic. It passed:

- All 20 foods: hunger, saturation parameter, stack sizes and actual consumption into the player's FoodStats.
- Five characters: +4 HP, clamping at maximum health, one item consumed in Survival, no consumption in Creative, offhand use, rejected wrong item, original food-temptation mapping (Cream excluded).
- Piko Piko Hammer: durability 10000, enchantability 19, ring repair, attack-speed modifier -3, durability loss on hit.
- Gold ring: collection, block removal, repeated callback does not duplicate reward, Creative removes without granting, Spectator leaves it untouched, no physical collision.
- Mint: can stay on SoniCraft soil and cannot stay without support.
- All five imported crafting recipes: deserialized by actual 1.12.2 recipe classes, matched against a populated crafting grid, expected output counts.
- Original custom models for the hammer/ring and associated block/item JSON parsed by Minecraft's ModelBlock deserializer. This is not a rendered visual check.

`validate_assets.py` also checks all 20 food models, model-parent/texture references, recipe references and all JSON. Existing character definition JSON remains unchanged from 0.1.0/0.1.1.

The first new-content harness run failed because its synthetic WorldProvider had no world-bound spawn point for a player constructor; this was corrected in the test world. The production code did not need a workaround for that harness failure.

## Tails flight checks

`tailsFlightCheck` passed against a small in-memory block world using the actual Minecraft `Entity.move` collision code. It covers the complete ascent/cruise/descent sequence, the normal AI scheduler, saving/loading in flight, restored gravity, cooldown, expiring fall protection, low ceilings, walls, water, unloaded chunks, NoAI/config cancellation, invalid numeric NBT and 0.1.0 saves. The test world has no other entities; a crowded real server and dynamic terrain still need gameplay checks.

## User-tested baseline

The user reported that 0.1.0 starts successfully and that character models and sounds work without observed artifacts. This is evidence for the previous release, not a visual or multiplayer test of the new flight behavior.

## Not tested

No complete Minecraft client or dedicated-server launch of 0.1.2 by the assistant. No rendered-model/UV verification, animation appearance, combat simulation, chunk decoration in a playable world, long-running natural spawning, multiplayer synchronization, or TMD coexistence test. Passing constructors and parsers does not validate those behaviors.

## New-content in-game acceptance checklist

1. Craft 16 rings from four gold nuggets placed as a diamond. Place and collect a ring in Survival; test a full inventory. Repeat in Creative and Spectator.
2. Craft both chili-dog recipes, feed Sonic; give the other four characters their favorite food. Test damaged/full-health heroes, both hands and Creative mode.
3. Test food attraction with Sonic/Tails/Knuckles/Amy. Cream should accept ice cream without gaining a new TemptGoal.
4. Spawn Amy, observe the existing hammer geometry when running/attacking and its absence in idle/walk; check combat audio and drop with doMobLoot enabled/disabled.
5. Use the item hammer, repair it with rings, and inspect the held-item model. Test anvil behavior in an actual client.
6. Check mint placement/removal on vanilla and SoniCraft ground. Other source acquisition systems (shops/structures) are still missing; do not assume every food can yet be obtained in survival.
7. Repeat on dedicated server. No assistant gameplay launch of this release has been performed.

## Existing in-game acceptance checklist

1. Install both supplied mods on Forge 14.23.5.2860 / Minecraft 1.12.2 / Java 8; create a new creative test world.
2. Spawn Tails in open terrain and wait at least four seconds. Observe takeoff, the rotating tails, a short flight and landing. Save/reload while airborne; try a low ceiling and disable `characters.tailsFlight` after restart.
3. Spawn all 17 characters using eggs or `/summon sonicraft:<id>`; inspect models, textures, movement, voices and damage reactions.
4. Name and damage one, save/reload, verify name/health retention.
5. Spawn Sonic and Metal Sonic together, observe melee behavior; confirm ordinary Eggman is neutral until attacked.
6. Test the SoniCraft world type and the normal Overworld. Change the `overworld.biomes` list and weights, restart and inspect newly generated chunks.
7. Repeat on a dedicated server, then separately alongside TMD. Keep `latest.log` for any failure.

## Build dependencies

The source uses public mirrors for legacy Forge tooling. Missing legacy Scala development coordinates are mapped to upstream Scala artifacts; these are compilation dependencies only and are not bundled into the mod JAR. The mod itself contains no Scala code. The original, unchanged GeckoLib Forge 1.12.2 release JAR 3.0.31 is supplied separately; its internal mod version is 3.0.30.



## Обновление 0.1.16 — массовый перенос городских сооружений и бадников

От базовой версии 0.1.15 добавлено **179 новых регистрационных ID** блоков из оригинала. Общее количество основных блоков — **698**. Перенесены модели, текстуры и варианты состояний для 1.12.2 (горизонтальные направления, 6 направлений, оси, часть вариантов `blockstate`, лестницы и плиты). По статическому анализу из 361 исходного NBT-шаблона **234** теперь не содержат *отсутствующих ID блоков Sonicraft* (раньше 152). Это **не 234 готовые постройки**: некоторые содержат несовместимые ванильные блоки/состояния, не подключены к генерации или зависят от механик и jigsaw.

Шесть новых бадников с исходными geo/текстурами/анимациями: **Jaws, Woodrow, Grabber, Grounder, Coconuts и Bumpalo**. Модель и ID каждого настоящие, ИИ приспособлен под 1.12.2 и пока упрощён (особенно плавание Jaws, захват Grabber и снаряды Woodrow/Coconuts). Woodrow и Grabber, а также Jaws и Grounder теперь допустимы при загрузке оригинальных NBT. Оригинальные биомные спавны Grounder/Coconuts/Bumpalo сохранены; никаких придуманных естественных спавнов Jaws/Grabber/Woodrow не добавлено.

Небольшие новые сооружения в генерации новых чанков: в Spring Yard — `spring_yard_funnel`, `spring_yard_bumper_pit`; в Aquatic Ruin — `aquatic_ruin_big_platform_1`; в Chemical Plant — `cpz_tower_1`; в Press Garden — деревья `small_chilly_blossom_tree_*_flowers` и `*_woodrow_*`. Условия высот/биома/пересечения проверяются перед размещением; крупные фабрики/города остаются NBT-шаблонами, а не объявлены полноценно работающей многочастной генерацией.

Отчёты: `STRUCTURE_BULK_016_MANIFEST.json` (в том числе список несовместимых состояний), `BADNIK_016_MANIFEST.json`, `tools/verify_016.py`. Исходная версия 1.20.1 нужна только для восстановления неимпортированных механик; уже перенесённые ресурсы включены.

**Проверено здесь:** чтение 361 NBT, валидность JSON, 698 ресурсных графов блоков, наличие 4203 моделей из blockstates, файлы geo/текстур/анимаций шести мобов, синтаксис 81 Java-файла. **Не проверено:** компиляция Forge, фактический клиентский рендер, серверное сохранение новых состояний, столкновения, боевой ИИ, естественная генерация и совместимость с другими модами. Сборка `.jar` не состоялась: `gradlew` не смог загрузить Gradle 4.9 из-за недоступности `services.gradle.org` (DNS). НЕ использовать `.jar` 0.1.10 как собранный релиз 0.1.16. Для сборки на Java 8: `BUILD_JAR_WINDOWS.cmd` или `BUILD_JAR_LINUX.sh`, возможна GitHub Actions workflow.

## 0.1.22

- Offline: `python tools/verify_022.py /path/to/original.jar` и `python tools/verify_complexes_022.py` проходят.
- 91 Java-файл прошёл синтаксический разбор на установленном JDK (не проверка типов).
- Gradle не загрузил дистрибутив: `java.net.UnknownHostException: services.gradle.org`.
- Forge `batch022Check` **не запускался**. JAR не собран.

## Дополнительная проверка 0.1.27 (source-only)

- `python tools/verify_027.py`: прочитаны все 361 исходных NBT, исходные файлы сверены с оригинальным 1.20.1 JAR, 17 несовместимых ванильных ID имеют явные адаптеры, затронуто 10 структур, обработка front_text подтверждена статически.
- `python tools/verify_026.py`: регрессия 1314 ID, 361 NBT, новых модельных состояний 0.1.26 прошла.
- `python tools/validate_assets.py`: после исправления ошибочной проверки «model/block/<id> обязан быть для любой двери» подтверждены 1314 наборов blockstate + item model, JSON и ссылки на собственные текстуры и модели.
- Прямой запуск Gradle всё ещё останавливается при разрешении `services.gradle.org`. **Никакой компиляции Minecraft Forge 1.12.2, рендера, проверки физики или клиентского запуска в этом окружении не было**.
- Аудит 217 записей 59 видов сущностей NBT показал, что текущий whitelist позволяет загрузить только 52 записи (включая ванильную свинью). Остальные 165 записей пока не переносим автоматически, включая скриптовые датчики, боссов и предметы декора.
