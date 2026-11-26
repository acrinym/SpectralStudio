# Native Specdrums protocol evidence

Official SDK https://sdk.sphero.com/documentation/sdk-documents documents an active palette as index/R/G/B tuples, color identification as index/confidence pairs, and color detection notifications containing RGB, confidence and classification ID (255 is unidentified). Default palette index 0 and MIDI palette index 1 are documented. These are API semantics, not a complete transport trace.

Independent reference https://spherov2.readthedocs.io/en/latest/_modules/spherov2/commands/io.html labels IO device 26, enable color tap command 29 and color tap event 30; the implementation explicitly labels these tap commands untested. Do not promote them to verified hardware behavior. The original APK confirms class names; method body extraction and on-device capture remain outstanding.

Next native protocol gate: discover service/characteristics on actual SD01, capture original connection initialization and notifications, correlate firmware, compare packet routing/framing with official API docs https://sites.google.com/sphero.com/sphero-public-sdk/documentation/api-documents . Only implement writes once evidence is recovered. No firmware modifications.
