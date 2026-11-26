# SpectralStudio user manual — development edition

This describes the first Android implementation in PR #1, not the retired Sphero application. The application is experimental; real ring and phone playback remain unverified.

## Prepare
Use an Android 8.0+ device with Bluetooth LE and MIDI support. Download the debug artifact from the matching successful Android workflow, extract the ZIP and install app-debug.apk. Allow installation from the chosen trusted file manager if Android requests it. Debug builds from different signing keys may require uninstalling an earlier development build, which removes local mappings and samples; do not uninstall without considering data loss.

For original ring care, consult the linked SD01 product manual. Charge using the appropriate micro-USB cable and a suitable USB power source. Sphero describes roughly one hour to charge and two hours of play; unplug to use. The ring is not waterproof. Do not use damaged hardware or disassemble the sealed battery.

## First session
1. Open SpectralStudio. Test a generated note pad without a ring.
2. Wake the ring by briefly connecting USB power and unplugging it.
3. Close the old MIX app or any other connected MIDI client.
4. Choose Scan for Bluetooth MIDI devices and grant requested Bluetooth permissions (older Android releases use location permission for scanning).
5. Select your ring; the app opens Android's BLE MIDI output. Tap two colors and observe MIDI note and velocity.
6. If no device appears, repeat the wake and scan process. If connection fails, long-press the discovered entry for a GATT inventory. Copy it for diagnostics.

## Original samples
Download the original preserved MIX 1.2.2 APK separately. Use Import original MIX APK and select the file; the archive fingerprint and size must match. Wait for preparation and use all twelve named pads. The original samples remain in app-private storage. The generated-note instrument is available separately.

## Learn a ring mapping
Connect a MIDI-capable ring, select Learn ring-to-pad mapping, select a pad, then tap a physical color. Repeat for other pads. A MIDI channel/note can belong to one pad; assigning it again moves the mapping. Reset clears assignments. These are host mappings, not ring palette writes. Unmapped notes do not arbitrarily trigger original samples.

## Troubleshooting
- No MIDI: try GATT inventory; firmware support differs.
- No sound: test local pads, check media volume and sample-ready count.
- APK rejected: check it is the 99,772,192-byte preserved MIX 1.2.2, not an HTML download page.
- Samples not ready: switch to generated notes; record the preparation status.
- App sent to background: current implementation disconnects the ring; reconnect on return.

## Current limitations
One ring at a time, one-shot original audio, no original loop quantization, no native color calibration, no native RGB/intensity reception, no EDU activities, no recording/export, no firmware updating. Offline after acquiring/installing the artifacts. See the acceptance checklist for physical verification.
