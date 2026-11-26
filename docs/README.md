# SpectralStudio documentation library

## Users and operators
- [User manual](USER-MANUAL.md)
- [First player](ANDROID-FIRST-PLAYER.md)
- [Original sound pack import](ANDROID-ORIGINAL-PACK.md)
- [Hardware acceptance checklist](HARDWARE-ACCEPTANCE.md)
- [Original SD01 hardware documentation and reference registry](SOURCE-REGISTRY.md)

## Developers and archaeologists
- [SDK reference and coverage](SDK-REFERENCE.md)
- [Native command evidence](NATIVE-COMMANDS.md)
- [Protocol evidence](PROTOCOL-EVIDENCE.md)
- [Original application archaeology](ARCHAEOLOGY.md)
- [Development roadmap](ROADMAP.md)

The original SDK symbol catalog is generated from the verified preserved APK by tools/sdk_inventory.py. See SDK-REFERENCE for reproducible commands and scope. GitHub Actions publishes the generated Markdown and JSON as build artifacts. No proprietary implementation is included in the generator.

Every behavioral claim should distinguish original published documentation, DEX evidence, independent implementation, tests, and physical validation. Do not infer device support merely because a class is present.
