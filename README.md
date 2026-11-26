# SpectralStudio

Open-source Specdrums replacement, hardware preservation and musical experimentation platform.

## First development milestone
Android BLE MIDI ring discovery and a local procedural instrument. No cloud, no account, no firmware updates. See [physical ring test instructions](docs/ANDROID-FIRST-PLAYER.md). GitHub Actions builds a debug APK. This is not yet full MIX/EDU compatibility; hardware acceptance is pending.

## Original MIX pack and ring learning
[Import and playback instructions](docs/ANDROID-ORIGINAL-PACK.md). Import the exact preserved APK into the app; original samples are not bundled. Learn MIDI channel/note mappings using physical colors. Long-press a discovered device for a read-only GATT service inventory and copy the report to assist native protocol research. See [protocol evidence](docs/PROTOCOL-EVIDENCE.md).

## Preservation
[Original MIX 1.2.2 and firmware archive](https://github.com/acrinym/SpectralStudio/releases/tag/preservation-specdrums-mix-1.2.2) — separate third-party licensing. [Fingerprints](preservation/manifest.json).

[Development roadmap](docs/ROADMAP.md). MIT covers our own code.
