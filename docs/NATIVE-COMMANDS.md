# Original native command archaeology

Evidence: original MIX 1.2.2 APK, classes2.dex SHA-256 ba8a5330fd1e81e2e94b0511429412fd67a3a8c241224dd277f135d1f1043bf8. Run tools/inspect_specdrums_dex.py against the preserved APK to independently locate constructor registerApiCommand calls.

| Command | DID | CID | Constructor code offset |
| --- | --- | --- | --- |
| EnableColorTapNotifyCommand | 1a | 1d | 158dac |
| ColorTapNotifyCommand | 1a | 1e | 158b6c |
| GetActiveColorPaletteCommand | 1a | 44 | 158f58 |
| SetActiveColorPaletteCommand | 1a | 45 | 1598a8 |
| GetColorIdentificationReportCommand | 1a | 46 | 1591bc |
| LoadColorPaletteCommand | 1a | 47 | 1596b0 |

The tap handler at 0x158bd0 takes six successive one-byte array ranges and converts each to a short using PrivateUtilities.toShort. It passes the results in sequence to ColorTapNotifyListenerArgs. Its constructor at 0x15db6c stores red, green, blue, confidence, classification and tap intensity in that order; the getter field references independently identify the names. This is stronger evidence than a string-table or third-party command listing. The enable payload serializer at 0x158d80 produces a single byte. Palette and identification tuple semantics are documented in https://sdk.sphero.com/documentation/sdk-documents .

The six-byte tap payload codec and palette/identification codecs are implemented as pure Java. They do not send any BLE commands. Strict lengths and unsigned byte conversions are tested. Tap classification 255 is treated as unknown consistent with the documented color sensor classification sentinel; confirmation for actual tap events and tap-intensity range/units remains a hardware acceptance task.

Still needed: original ring's GATT UUIDs and connection setup, transport frame flags, notification subscription and packet capture, verification of request/response routing, native tap reception, and palette modification with backup/restore. These constructor IDs and payloads are APK-derived, not live hardware measurements.
