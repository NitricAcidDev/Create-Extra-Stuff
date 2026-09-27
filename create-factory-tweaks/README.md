# Create: Factory Tweaks

By NitricAcid. Configurable TFMG blaze burner fuels and distillation outputs.

Open Mods > Create: Factory Tweaks > Config. Settings are server configs and synchronize to clients. Leave **Lock settings to defaults** enabled for the supplied dataset; disable it for custom settings. Changes apply to future fuel charges and distillation batches without reloading the world. Already charged fuel retains its remaining time.

| Fuel | Heat | Seconds / 100 mB | Average mB/t | Bucket duration |
|---|---|---:|---:|---|
| LPG | SEETHING | 125 | 0.04000 | 20m 50s |
| Naphtha | SEETHING | 185 | 0.02703 | 30m 50s |
| Gasoline | KINDLED | 188 | 0.02660 | 31m 20s |
| Kerosene | KINDLED | 190 | 0.02632 | 31m 40s |
| Diesel | KINDLED | 200 | 0.02500 | 33m 20s |
| Heavy oil | NONE | 0 | 0 | Cannot burn |

Fuel durations are configured per **100 mB**, matching the table. Average consumption is 5 / seconds, and a bucket lasts ten charges. Other charge sizes scale proportionally. Both burner types use the same duration, enabled setting and heat tier. A bucket in a normal burner grants the same ticks as 1,000 mB in a liquid burner, without the old solid-burner capacity clipping. The separate solid heat-units setting has been removed; heavy oil is disabled in both burner paths. Bucket checks, pipe transfer behavior, insertion rules, sounds and particles remain in place.

**Distillation Outputs** has seven settings: heavy oil, diesel, kerosene, naphtha, gasoline, LPG and lubrication oil. Each is a reference output; every recognized distillation variant uses `round(original output * reference / original reference)`, with a minimum of 1 mB. Original references are 120, 60, 30, 10, 60, 60 and 25 mB respectively. Defaults are 100, 60, 30, 30, 60, 60 and 25 mB.

For example, default heavy oil output is 100 mB from full crude-oil distillation, 125 mB from light crude-oil distillation and 83 mB from secondary heavy-oil distillation. Default naphtha is 30 mB from crude oil and 15 mB from heavy oil. Inputs, recipe selection and output ordering are unchanged. All six bundled TFMG 1.3.1 recipe signatures are supported; custom recipes with different signatures retain their own values.

The simplified config replaces the former per-recipe settings and bucket-duration keys. The new defaults are used automatically while locked. The root PSB files remain unchanged; Factory Tweaks uses the exported fuel artwork with pixel filtering disabled. Original license attribution remains in LICENSE.txt.

Build and regression checks: `gradlew build`.
