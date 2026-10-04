# Create: Trains Interactive

A NeoForge 1.21.1 fork of Create: Compatible Storage with furniture seats, bookshelves, bar and kitchen interactions, and Pick Block on moving trains. Requires **Create 6.0.10**.

## Installation

1. Close Minecraft and put `create-trains-interactive-1.21.1-1.1.1.jar` in the profile's `mods` folder, replacing the older Trains Interactive JAR.
2. Install the same JAR on clients and dedicated servers.
3. Restart Minecraft. When upgrading from versions before 1.1.0, reassemble existing trains to capture the new interaction handlers.

This build is a replacement for Create: Compatible Storage: do not install both. If migrating from Compatible Storage, disassemble contraptions that use its storage before replacing it; the fork deliberately uses its own registry namespace.

## Supported blocks

| Mod | Supported behaviour |
| --- | --- |
| Kaleidoscope Tavern 1.2.0 | All 16 sofas and 16 bar stools are moving seats, with native sitting height and passenger transfer during assembly. |
| Kaleidoscope Tavern 1.2.0 | Bar cabinet and glass bar cabinet allow inserting and retrieving bottles, using the original bottle rules and left/right placement. |
| Kaleidoscope Tavern 1.2.0 | Cellar cabinet, holder, circular rack and tilted rack allow placing/retrieving bottles in the clicked slot; original bottle restrictions and one-bottle limits apply. |
| Kaleidoscope Tavern 1.2.0 | Add ingredients to shakers, pick them up with an empty hand, shake them normally in hand, pour into moving glassware and collect prepared drinks. Native quality restrictions, ingredients, drink colour and effects are retained. Glassware holders use their original four slots. Taps retain delayed extraction across save/reload. Brewing barrels assemble as a complete multiblock and continue their native brewing checks. |
| Kaleidoscope World Liquor 1.1.9 fix | All 16 chairs/stools are captured as persistent seats. Native chair height takes precedence over the installed version's existing seat-position handler. Its ten existing cabinet interaction handlers remain in place. |
| Kaleidoscope World Liquor 1.1.9 fix | Freezers keep native filling, opening/closing, processing and result extraction. Processing progress survives train reload. Its other unclaimed bar blocks receive native block and held-item interactions. |
| Minecraft chiseled bookshelves | Insert and retrieve books from the clicked front slot. Books, occupied-slot visuals and the last clicked slot persist through save/reload and disassembly. |
| Bookshelf Inspector 2.4 (NeoForge 1.21.1) | Its existing HUD shows the selected book's name, enchantments and author on moving chiseled bookshelves. Empty slots hide the overlay; stationary shelves keep their normal behavior. Install Bookshelf Inspector on clients and servers to enable its HUD. |
| Create: On the Move 1.0.0 and Steam 'n' Rails 0.3.0-beta.2 | Prevents duplicate interaction registrations during startup. Existing workstation and bookshelf handlers remain in place; mounted storage retains Create's inventory menus. On the Move still supplies its handlers for unclaimed blocks. On the Move requires NeoForge 21.1.250 or newer. |
| Farmer's Delight 1.3.4 | Cabinets and wooden/bamboo baskets can be opened on contraptions; their inventories participate in Create storage, survive save/reload and restore changed contents on disassembly. Baskets use a five-slot menu. |
| Farmer's Delight 1.3.4 | Cooking pots use their native inventory menu, cook above onboard heat sources and serve meals into bowls. Menus follow the train and close when the passenger leaves range. Stoves and skillets continue native cooking ticks; cutting boards use native recipes and tool wear, and feasts serve food portions. Supported food blocks attach to their floor during assembly. |
| Minecraft Pick Block | Middle-click targets the nearest visible block on a contraption. Creative mode copies its native item; Ctrl + middle-click also copies block entity data. Survival selects a matching item already in your inventory. |
| Other Compatible Storage integrations | Original optional storage support and tags are retained, including Handcrafted, Quark, Storage Delight and supported Let's Do storage. These additional integrations have not been individually tested in this fork. |

Use normal right-click interactions for supported bar and kitchen blocks. An empty hand retrieves shakers, glasses and drinks where their original mod supports it. Prepared shakers pour into empty glassware with right-click. Native placement of supported bar and food items can use adjacent cells on the contraption. A shaker containing a prepared drink pours into empty glassware; clicking another block keeps the shaker in hand. Its native ingredient overlay works on moving trains, and drink tooltips retain bottle quality and signature cocktail effects. With On the Move, release the attack button before mining another train block so removing one block cannot continue into the block underneath.

Active ticking is limited to the listed bar and kitchen machines. Arbitrary machines, full moving redstone networks and jumping/stomping on pressing tubs are not supplied by this integration. On the Move supplies additional functionality, including its own moving-bed handlers, when installed. Client rendering and multiplayer gameplay still need manual verification.

## Extra furniture and storage

`config/create_trains_interactive-common.toml` contains `extraSeats`, an optional list of exact block IDs and seat-entity heights:

```toml
extraSeats = ["example:chair=0.65"]
```

Keep settings identical on clients and servers. Restart and reassemble after adding seats. The built-in Kaleidoscope furniture uses its native sitting geometry; manually configured furniture uses an offset of -0.3 blocks from the configured seat height. For two-block furniture, configure only the lower block; special assembly callbacks are not automatically patched.

Storage integrations can be extended by datapacks using Create's `create:simple_mounted_storage` tag for blocks with a modifiable item-handler capability, or `create_trains_interactive:uncooperative_mounted_storage` for ordinary `BaseContainerBlockEntity` inventories. Tag entries for optional mods should use `required: false`. Display cabinets and processing machines need specific handlers; do not tag them as generic storage.

## Building and verification

Use Java 21 and the included Gradle wrapper. `gradlew build` builds the release JAR. `gradlew runGameTestServer` runs the isolated Minecraft verification suite. Verification classes and test structures are excluded from the release JAR.

For the verification suite, supply copies of the installed Kaleidoscope Tavern, World Liquor, Farmer's Delight, Farmer's Delight Extended, Create Gears and Tavern, and Kotlin for Forge JARs in `dev-libs/`. The Bookshelf Inspector NeoForge 2.4+1.21.1 JAR is also needed in `dev-libs/` to compile its optional HUD integration; add Cloth Config for client verification. Run `gradlew runClient -PverifyInspector=true` to check the client hooks and HUD data handoff. These third-party JARs are not included in the released mod or source archive. Create/Ponder/Flywheel/Registrate and optional compile-time integrations are resolved from their public Maven repositories.

To build the optional On the Move integration, place its 1.0.0 JAR in `dev-libs/on-the-move/`. Add Steam 'n' Rails 0.3.0-beta.2 there for the combined regression check and run `gradlew runGameTestServer -PwithOnTheMove`. These optional dependencies are not bundled in the release.

Checks cover mounted inventory changes and menu slot counts, world reload/disassembly, all 48 seat colours, native sitting heights, native bottle restrictions and slot interactions, and preservation of World Liquor's existing cabinet handlers. Checks also cover chiseled bookshelf slots in all four facing directions, book restrictions, item components and save/reload/disassembly. Tests run in a separate test world; the Inspector integration also checks initial and late tracking, names, enchantments, authors and live book removal. Client rendering and multiplayer visuals have not been manually tested.

Version 1.1.0 passes 25 server GameTests: the twelve original storage, seating, bookshelf and registration tests, plus native shaker mixing/pouring/serving, ingredient limits and quality restrictions, scheduled tap extraction across reload, moving cooking menus and servings, cutting recipes and tool wear, freezer processing across reload, holder slots, stove cooking, feast portions, full barrel assembly and brewing, creative Pick Block data copying, passenger overflow drops and stationary-world isolation. Isolated client checks load the Pick Block and cooking-menu hooks as well as Inspector compatibility. Verification classes and development dependency JARs are excluded from the release.

## Provenance and license

Storage code and generated tags are forked from [Dadamalda's Create: Compatible Storage](https://github.com/DadamaldaDad/create-compatible-storage), branch `neoforge-2`, commit `c90f134f26c3026533f65e03541aa669ab97f2c2` (2.13.0). Original Java package names are retained for provenance; mod metadata and registration/resource namespaces use `create_trains_interactive`. Original storage code remains credited to Dadamalda.

New furniture code implements the same kind of contraption-seat integration described by [Create: Sit on Seats](https://www.curseforge.com/minecraft/mc-mods/create-sit-on-seats). No source from that mod was copied; its published build was for a different loader/version and no public source link was available on the project page examined.

New integration code was written for this project. Kaleidoscope and World Liquor implementation details were inspected to integrate with their installed versions; their JARs, assets and decompiled source are not redistributed here. There is no dependency on the separate **Create: Interactive** mod.

This fork is distributed under GPL-3.0-only. The full license is in `LICENSE.md`, and the source archive contains the corresponding source and build scripts.

Version 1.1.1 passes all 26 server GameTests, including failed-pour preservation on train and stationary counters. `gradlew runClient -PwithOnTheMove -PverifyInspector=true -PverifyGameplay=true` creates a separate test world and reproduces an oversized, missing Create spawn structure. Checks cover recovery in five packets, native delayed carriage model rebuilding, refreshed bogey references, moving shaker ingredients, scope cleanup, mining target/release handling and native drink tooltip effects, colour and bottle quality. Missing structures up to 16 MiB are recovered in 256 KiB packets; clients wait for recovery rather than discarding the carriage or raycasting its missing blocks. This has not been tested with the full Sable/shader modpack or in multiplayer.
