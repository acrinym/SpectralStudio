"""Extract the verified original MIX intro pack to a new local directory.

Only the known archive and exact bundled sample entries are accepted.
No APK execution, firmware flashing or network access.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
import shutil
import tempfile
import zipfile

APK_HASH = "d5f03a44aca91e473cafcf2e74a94df58a7091cfe9d2cede15ea058f10867b30"
ROOT = "assets/defaultSoundPack/"
FILENAME = re.compile(r"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\.m4a")

def digest(path):
    with Path(path).open("rb") as src:
        return hashlib.file_digest(src, "sha256").hexdigest()

def extract_pack(archive, destination, *, expected_hash=APK_HASH):
    archive, destination = Path(archive), Path(destination)
    if digest(archive) != expected_hash:
        raise ValueError("APK SHA-256 does not match the expected original")
    if destination.exists():
        raise FileExistsError(destination)
    destination.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(dir=destination.parent) as tmp:
        stage = Path(tmp) / "pack"
        stage.mkdir()
        with zipfile.ZipFile(archive) as z:
            raw = z.read(ROOT + "info.json")
            metadata = json.loads(raw)
            presets, sounds = metadata["presets"], metadata["sounds"]
            if len(presets) != 1 or len(sounds) != 12:
                raise ValueError("Unexpected bundled pack structure")
            ordered = next(iter(presets.values()))["soundIDs"]
            if len(ordered) != 12 or set(ordered) != set(sounds):
                raise ValueError("Unexpected preset sound IDs")
            files = []
            seen = set()
            (stage / "Sounds").mkdir()
            for sound_id in ordered:
                entry = sounds[sound_id]
                name = entry["soundPath"].rsplit("/", 1)[-1]
                if not FILENAME.fullmatch(name) or name in seen:
                    raise ValueError("Invalid or duplicate sample name")
                seen.add(name)
                member = ROOT + "Sounds/" + name
                if z.namelist().count(member) != 1:
                    raise ValueError("Missing or duplicate archive member")
                path = stage / "Sounds" / name
                with z.open(member) as source, path.open("xb") as target:
                    shutil.copyfileobj(source, target)
                files.append({"id": sound_id, "name": entry["name"], "path": "Sounds/" + name,
                              "sha256": digest(path), "bytes": path.stat().st_size,
                              "type": entry["type"], "category": entry["category"],
                              "quantization": entry["quantization"], "sustainable": entry["sustainable"]})
            (stage / "info.json").write_bytes(raw)
            (stage / "import-manifest.json").write_text(json.dumps({
                "source_apk_sha256": expected_hash, "bpm": metadata["bpm"],
                "metadata_sha256": digest(stage / "info.json"), "samples": files
            }, indent=2)+"\n", encoding="utf-8")
        # Never overwrite an existing destination. Rename within same filesystem.
        if destination.exists():
            raise FileExistsError(destination)
        stage.rename(destination)
    return files

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk", type=Path)
    parser.add_argument("destination", type=Path)
    args = parser.parse_args()
    files = extract_pack(args.apk, args.destination)
    print(f"Imported {len(files)} original samples to {args.destination}")
