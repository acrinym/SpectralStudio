# Physical SD01 acceptance checklist

Use a charged ring and close any original app or competing BLE client. Install the debug APK from the matching CI run.

1. Play all generated pads with Bluetooth disabled.
2. Import original MIX APK using the system picker. Wait for 12/12 prepared samples, audition each, restart and verify pack remains available.
3. Scan for the ring, long-press it, copy the GATT inventory. Save the inventory and note known ring firmware separately. No vendor commands are transmitted.
4. Scan again, tap the ring entry to connect via BLE MIDI, switch to generated notes, and tap two different colors. Record note/channel values.
5. Switch to original MIX, use Learn, choose a pad, tap a color. Repeat for a second color. Verify the mapped sample plays; unassigned notes should not play an arbitrary sample. Restart and verify mappings persist.
6. Disconnect/reconnect; test with Wi-Fi off and Bluetooth enabled.
7. If MIDI fails, supply the GATT inventory and firmware identity. Native protocol support remains open.

Do not claim physical acceptance until steps have been performed with a real ring and actual audible playback. This checklist does not verify original quantized loop behavior.
