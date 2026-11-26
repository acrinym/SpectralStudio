# Original sound pack in Android

Download the original MIX APK from the preservation release. In SpectralStudio choose Import original MIX APK, select that file using Android's system picker, and wait until all 12 samples are prepared. Import requires the exact original archive SHA-256 and size. The imported pack is stored privately inside SpectralStudio and reopened on startup. No original audio is embedded in our debug APK.

Choose generated notes or original MIX mode. Tap pads directly, or use Learn ring-to-pad mapping, select a pad, then tap a physical color with a connected MIDI-capable ring. Mappings include channel and note, persist on restart, and can be reset. An unassigned MIDI note in original pack mode does not trigger an arbitrary sample.

This is one-shot playback using Android MediaPlayer; original loop synchronization, sustain, quantization and low-latency performance are not implemented or measured. Ring learning is host-side MIDI mapping, not firmware color calibration. Physical phone playback and hardware acceptance remain required.
