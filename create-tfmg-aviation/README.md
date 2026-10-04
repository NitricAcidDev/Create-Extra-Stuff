# Create: TFMG Aviation

Adds a heated mixing recipe for AeroEngine aviation kerosene using TFMG fuels.

| Ingredients | Machine | Output |
| --- | --- | --- |
| 800 mB Kerosene + 200 mB Heavy Oil (TFMG) | Mechanical Mixer over a heated Basin | 1,000 mB Aviation Kerosene (AeroEngine jet fuel) |

Use a powered Mechanical Mixer, a Basin, and a fueled Blaze Burner below the Basin. Pump both ingredients into the Basin and extract the finished fuel with fluid pipes. Normal heating is sufficient; no Blaze Cake is needed. The output is AeroEngine's existing `aeroengineering:aviation_kerosene` fluid, ready to feed into its engines.

## Installation

Minecraft **1.21.1**, **Java 21**, and **NeoForge 21.1.228 or newer within 21.1** are required. Install the addon JAR in the instance's `mods` folder alongside **Create 6.0.10**, **Create: The Factory Must Grow**, and **AeroEngine**, including the dependencies required by those mods. Add it on both clients and dedicated servers.

The recipe loads automatically in new and existing worlds. No configuration, scripts, or separate datapack installation is needed. It adds an alternative recipe without replacing AeroEngine's original recipe, and appears in JEI when JEI is installed.

## Build

With JDK 21 installed, run `gradlew.bat build` on Windows or `./gradlew build` on Linux/macOS. The addon JAR is written to `build/libs/`. Published dependency JARs placed in `dev-libs/` are used for development runs only and are excluded from the addon.

## Verification

Five Minecraft GameTests passed with NeoForge 21.1.248, Create 6.0.10, TFMG 1.2.0, and the published AeroEngine 1.3.0 JAR (which declares 1.0.2 internally), plus its required physics dependencies. Tests exercise Create's actual Basin recipe matching and processing: exact output and input consumption, heating, insufficient kerosene, insufficient heavy oil, and rejection of diesel substitution. They also confirm AeroEngine's original recipe remains loaded.

To repeat them, place the published TFMG, AeroEngine, Sable, and bundled Create Aeronautics dependency JARs in `dev-libs/`, then run `gradlew.bat runGameTestServer`. Verification classes and the empty test structure are excluded from the release JAR.

## Dependency references

- [AeroEngine](https://modrinth.com/mod/aeroengine)
- [Create: The Factory Must Grow](https://modrinth.com/mod/create-tfmg)
- [Create](https://modrinth.com/mod/create)
