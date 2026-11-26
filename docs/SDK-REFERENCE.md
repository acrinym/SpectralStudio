# SDK developer and interoperability reference

## Scope and reproducibility

Source: preserved MIX 1.2.2 APK; SHA-256 d5f03a44aca91e473cafcf2e74a94df58a7091cfe9d2cede15ea058f10867b30. Inventory includes every defined class under com.sphero.android.convenience, com.sphero.core and com.sphero.platform in both DEX files: 307 classes, 1,188 declared methods and 3,891 fields. Generated resource fields, anonymous classes and private internals are counted. Excludes MIX application classes under com.sphero.jams and native shared-library symbols. The bundled SDK may include APIs for other Sphero products; this is not a claim of ring support.

With Python 3.11+:
    python tools/sdk_inventory.py /path/to/Specdrums_Mix_1.2.2.apk sdk-inventory

Outputs SDK-INVENTORY.md and sdk-inventory.json with class hierarchy, interfaces, field types, method signatures, access flags, direct/virtual grouping and code offsets. Zero offset indicates no encoded body. DEX descriptors retain exact parameter/return types. JSON includes original DEX fingerprints.

## Reference chapters and coverage

| Domain | Evidence / current state |
|---|---|
| Discovery and BLE GATT | Android MIDI and diagnostic implementation; native connection lifecycle pending |
| Packet structure | Official API reference, implementation pending |
| Native tap and palettes | Six IDs confirmed in APK; pure Java payload codecs; no native writes |
| Power, charge and sleep | Original SDK symbol inventory and public common SDK docs; device-specific behavior pending |
| Identity, firmware and hardware version | SDK symbols; native queries pending |
| LEDs and indications | Public SDK and symbols; SD01 masks pending |
| Sensors and color | Documented generic color sensor and six-byte tap event evidence; physical verification pending |
| MIDI | Android OS transport implemented; firmware and physical acceptance pending |
| Firmware/update | Preserved archive only; update procedure not implemented |
| Audio and sample pack | MIX archive importer and Android one-shot player; not part of generic hardware SDK |
| Other robot commands | Present in SDK inventory where bundled; do not assume ring support |

## Native command reference

All verified IDs below are on device 0x1A. See NATIVE-COMMANDS for precise DEX code offsets and evidence.

| CID | Command | Payload / response |
|---|---|---|
| 0x1D | Enable tap notifications | One boolean byte |
| 0x1E | Tap notification | Six unsigned bytes: RGB, confidence, classification, intensity |
| 0x44 | Get active palette | No request data; index/R/G/B tuples |
| 0x45 | Set active palette | Index/R/G/B tuples, maximum 12 entries |
| 0x46 | Identify color | RGB and confidence threshold; index/confidence pairs |
| 0x47 | Load palette | One palette index; 0 default, 1 MIDI |

Public SDK additionally describes Save Color Palette, but its ring support and command ID are not established by the six-command DEX inspection. No writes or persistence changes are currently performed.

## Behavioral documentation completion criteria

For each callable feature: document exact signature, hardware applicability, device/command IDs where relevant, request/response types, byte order, units, valid ranges, state prerequisites, error paths, side effects, notification lifecycle, provenance and tests. Record unresolved elements rather than inventing defaults. For physical behavior, capture ring model and firmware, observations, and packet traces. Public API docs and DEX signatures are not equivalent to a complete operational SDK.

The next archaeology phase must cover native library interfaces, transport, remaining command bodies and variant support. Keep original code separate from our independent implementation and observe original licensing.
