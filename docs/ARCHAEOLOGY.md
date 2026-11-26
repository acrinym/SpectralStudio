# Original MIX archaeology

Source inspected: Specdrums MIX 1.2.2 APK, SHA-256 d5f03a44aca91e473cafcf2e74a94df58a7091cfe9d2cede15ea058f10867b30 (public preservation release). The bundled metadata is assets/defaultSoundPack/info.json. Its one preset has 12 ordered sound IDs; all twelve referenced M4A files are actually bundled under assets/defaultSoundPack/Sounds, alongside an extra nil_sound.wav not in the preset. The pack BPM is 150. The original JSON records numeric type/category/quantization and sustain fields; their exact runtime semantics are not yet verified.

DEX string-table inspection of classes2.dex confirms class descriptors in com/sphero/android/convenience/commands/io for ColorTapNotifyCommand, EnableColorTapNotifyCommand, GetColorIdentificationReportCommand, LoadColorPaletteCommand, SetActiveColorPaletteCommand and GetActiveColorPaletteCommand, plus SpecdrumsToy in the toys package. String identifiers establish the presence of these features, not command bytes or payloads. Next recover method bodies, cross-reference device/command IDs and capture actual ring notifications before implementing writes. Preserve packet evidence with firmware version and direction.

The first Android milestone deliberately uses standard BLE MIDI; Sphero confirms the feature but requires suitable firmware: https://help.sphero.com/sphero-support/specdrums-edu-12-pack-faq . No assumption about MIDI note to color or palette IDs.

## Local original pack import

Run Python 3.11+: python tools/import_mix_pack.py /path/to/Specdrums_Mix_1.2.2.apk /path/to/new-pack-directory

The importer verifies the complete archive fingerprint, preserves metadata verbatim, resolves all preset IDs to bundled audio and writes file-level fingerprints. It neither downloads nor executes the app. Original sample playback inside SpectralStudio remains unimplemented. CI verifies against the original release; it does not re-publish the audio.
