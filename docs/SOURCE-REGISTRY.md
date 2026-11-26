# Original source registry

Reviewed 2026-11-26. Preserve original URLs and identify document versions. Links are reference access, not a claim of redistribution rights.

| Source | Location | Scope / status |
|---|---|---|
| Original SD01 FCC manual | https://fccid.io/SXO-SD01/User-Manual/User-Manual-4068904 | FCC exhibit |
| SD01 product manual, DOC 004437 REV A, 2018 | https://device.report/m/7ca75588dc94d193a7fe0a5bbd1ec8852528edd961aded3d247c89ae311fed22.pdf | Six-page hardware safety and regulatory guide; original copyright retained |
| FCC filings | https://fccid.io/SXO-SD01 | Hardware exhibits |
| Sphero Specdrums FAQ | https://help.sphero.com/sphero-support/specdrums-faq | Original color and battery behavior |
| EDU 12 Pack FAQ | https://help.sphero.com/sphero-support/specdrums-edu-12-pack-faq | Ring pairing, classroom bundle, MIDI |
| Application retirement | https://help.sphero.com/sphero-support/specdrums-edu-and-mix-apps | Official original MIX APK access |
| SDK command docs | https://sdk.sphero.com/documentation/sdk-documents | Shared product API, not exclusively SD01 |
| API docs | https://sdk.sphero.com/documentation/api-documents | Common packet framing, routing, flags, escaping and checksums |
| SDK license | https://sdk.sphero.com/sdk-license | Distinct from SpectralStudio MIT license |
| Original APK and firmware | https://github.com/acrinym/SpectralStudio/releases/tag/preservation-specdrums-mix-1.2.2 | Four preserved binaries; hashes in preservation/manifest.json |

The original manual and public SDK terms contain reproduction restrictions. Link original documents and write independently authored interoperability references; do not silently relicense third-party documentation. No original manuals were mirrored in this commit.

## Outstanding source recovery

Original EDU application archive and complete activity-card set; original MIX and EDU instructional videos; firmware version history and older APKs; full list of published SDK variants; original service/characteristic and packet captures from SD01.

## English SD01 manual preservation checkpoint (2026-11-26)

The owner supplied two independently downloaded English PDFs from FCC/device.report and Geizhals. Both are exactly 758550 bytes and SHA-256 `7ca75588dc94d193a7fe0a5bbd1ec8852528edd961aded3d247c89ae311fed22`. One deduplicated copy is archived privately in the owner's Sphero/Specdrums Drive folder as SPECDRUMS_MANUAL_ENGLISH_SD01_DOC004437_REV_A.pdf. French and Spanish manuals and seven educational activity PDFs are also archived privately. The English manual is DOC 004437 REV A, six pages, model SD01; charging input is rated 5 V DC 500 mA and radio is BLE 4.0. The SDK and API site remain accessible at the original URLs and alternate Google Sites origin, but byte-complete snapshots have not been obtained. The public SDK covers multiple products; it is not an SD01-specific SDK.

## SDK and API archive checkpoint (2026-11-26)

Supersedes the outstanding SDK/API snapshot status above. The owner supplied two original MHTML webpage captures, both parsed successfully with matching page titles and Snapshot-Content-Location headers. The original bytes are preserved privately in Sphero/Specdrums (no public redistribution). Extension .mhtml was added to the archived filenames.

| Artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| Sphero Public SDK - SDK Documents.mhtml | 3197296 | b365db58a1d24e77c3ac7ca4ac42148ee6ca590c2a0ff5234c897b4b4712213c |
| Sphero Public SDK - API Documents.mhtml | 3004177 | a8edb94f76217f62b77f7ffc5900b4c71108514fdca4230092703282d1302116 |

Original origins: https://sdk.sphero.com/documentation/sdk-documents and https://sdk.sphero.com/documentation/api-documents . Captures contain HTML, CSS and PNG resources. These are two individual documentation-page snapshots, not an exhaustive crawl of every linked SDK page. Offline rendering and external link completeness have not been verified. Use these preserved primary sources alongside the original APK for subsequent protocol reconstruction.
