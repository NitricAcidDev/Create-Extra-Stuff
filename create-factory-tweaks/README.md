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

**Distillation Presets** defaults to **Vanilla**, meaning original TFMG recipe outputs (including datapack changes). **Adjusted** is a disabled placeholder button for a future custom preset. There are no distillation overrides or output sliders; only burner fuel values have custom defaults.

The former distillation settings are no longer used. The new defaults are used automatically while locked. The root PSB files remain unchanged; Factory Tweaks uses the exported fuel artwork with pixel filtering disabled. Original license attribution remains in LICENSE.txt.

Build and regression checks: `gradlew build`.
