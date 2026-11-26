# SpectralStudio

Independent, offline-first replacement for Sphero Specdrums MIX and EDU, with hardware and software preservation as a core objective. MIT applies to our original code, not third-party material. Preserve original archives byte-for-byte; track provenance and licensing before redistribution. Never flash firmware during initial discovery.

## Development trains

1. Archaeology: recover original SDKs, documentation, archive formats and protocol evidence.
2. Hardware: BLE discovery, connection, version and battery identification, tap events; verify on physical SD01.
3. Compatibility: MIX and EDU behavior inventory and original asset importer.
4. Audio: low-latency samples, custom instruments, loops and recording.
5. MIDI: configurable note and controller mapping, FL Studio integration.
6. Studio: color calibration, performance UI, sequencing and export.
7. Expansion: reusable protocol SDK and desktop/mobile support.
8. Preservation: original artifact inventory, provenance, regression tests and offline operation.

First acceptance gate: a physical ring triggers an audible sound in independent software without the original app. Simulated tests do not count as hardware validation.

No application stack has been finalized. Do not claim packet formats or hardware compatibility before verification. Use GitHub-hosted CI for automated checks; physical Bluetooth tests remain local.
