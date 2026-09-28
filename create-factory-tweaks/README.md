# Create: Factory Tweaks

By NitricAcid. Configurable TFMG blaze burner fuels and distillation outputs.

Open Mods > Create: Factory Tweaks > Config. Settings use the profile-wide common config so the lock can be changed from the Mods menu before entering a world. Dedicated servers use their own copy; multiplayer clients cannot change the server values. Leave **Lock settings to defaults** enabled for the supplied dataset; disable it for custom settings. Changes apply to future fuel charges and distillation batches without reloading the world. Already charged fuel retains its remaining time.

| Fuel | Heat | Seconds / 100 mB | Average mB/t | Bucket duration |
|---|---|---:|---:|---|
| LPG | SEETHING | 125 | 0.04000 | 20m 50s |
| Naphtha | SEETHING | 185 | 0.02703 | 30m 50s |
| Gasoline | KINDLED | 188 | 0.02660 | 31m 20s |
| Kerosene | KINDLED | 190 | 0.02632 | 31m 40s |
| Diesel | KINDLED | 200 | 0.02500 | 33m 20s |
| Heavy oil | NONE | 0 | 0 | Cannot burn |

Fuel durations are configured per **100 mB**, matching the table. Average consumption is 5 / seconds, and a bucket lasts ten charges. Other charge sizes scale proportionally. Both burner types use the same duration, enabled setting and heat tier. A bucket in a normal burner grants the same ticks as 1,000 mB in a liquid burner, without the old solid-burner capacity clipping. Normal burners reject another fuel bucket once their remaining time exceeds the liquid burner pump threshold (normally 10,000 ticks); forced feeding obeys the same limit. Previously overstacked burners are capped to one longest supported bucket plus that threshold on their next server tick. The separate solid heat-units setting has been removed; heavy oil is disabled in both burner paths. Bucket checks, pipe transfer behavior, insertion rules, sounds and particles remain in place.

**Distillation Presets** defaults to **Vanilla**, meaning original TFMG recipe outputs (including datapack changes). **Adjusted** selects built-in output amounts for each of TFMG's six crude oil and heavy oil distillation recipes. **Custom** opens a folder containing **Use Custom** and the **Distillation editor**. The editor has a separate folder for each recipe and an output value for each fluid that recipe produces. The lock is on the main config screen; when locked, editing controls are greyed out and distillation runs Vanilla while retaining the selected preset for when the lock is turned off.

Distillation recipes are identified by their input and output fluids, so modified amounts in datapacks do not prevent the preset from applying. Unrecognised recipes retain their original outputs. The root PSB files remain unchanged; Factory Tweaks uses the exported fuel artwork with pixel filtering disabled. Original license attribution remains in LICENSE.txt.

Build and regression checks: `gradlew build`.
