# First physical ring acceptance path

Sphero confirms BLE MIDI support for Specdrums (firmware-dependent): https://help.sphero.com/sphero-support/specdrums-edu-12-pack-faq . Android provides the BLE MIDI transport via MidiManager.openBluetoothDevice: https://developer.android.com/reference/android/media/midi/MidiManager . The app intentionally uses that OS API rather than inventing a Sphero packet format.

Build on GitHub Actions or with JDK 17, Android SDK 35 and Gradle 8.9: gradle :app:assembleDebug. Download the debug APK artifact, install it, wake the ring via USB and unplug, close original MIX, grant Bluetooth permissions, scan, and select the ring. First test the local pads. Then tap physical colors and record the displayed MIDI notes and audible results. Try at least two colors, repeated taps, disconnect/reconnect, and airplane mode with Bluetooth re-enabled.

No proprietary commands, firmware writes or network access are implemented. Firmware that does not expose BLE MIDI may not work. The pads are not a calibrated palette; one-shot generated notes are not full MIX parity. Real hardware acceptance remains unverified. Native color protocol, calibration, note sustain, multiple rings, sample packs and recording remain next work.

Original APK and firmware: https://github.com/acrinym/SpectralStudio/releases/tag/preservation-specdrums-mix-1.2.2 . Original source https://help.sphero.com/sphero-support/specdrums-edu-and-mix-apps . Original assets retain their own terms.
