import json
from pathlib import Path
import tempfile
import unittest
import zipfile
from tools.import_mix_pack import extract_pack, digest, ROOT

class ImportTests(unittest.TestCase):
    def fixture(self, directory, bad=False):
        path=directory/"test.apk"
        sounds={}
        with zipfile.ZipFile(path,"w") as z:
            for i in range(12):
                name=f"00000000-0000-0000-0000-{i:012x}.m4a"
                sounds[str(i)]={"name":str(i),"soundPath":("bad.exe" if bad and i==4 else "remote/"+name),
                    "type":0,"category":0,"quantization":9,"sustainable":True}
                z.writestr(ROOT+"Sounds/"+name,bytes([i])*10)
            z.writestr(ROOT+"info.json",json.dumps({"bpm":150,"sounds":sounds,
                "presets":{"default":{"soundIDs":list(sounds)}}}))
        return path
    def test_extract(self):
        with tempfile.TemporaryDirectory() as tmp:
            p=Path(tmp); apk=self.fixture(p); out=p/"pack"
            result=extract_pack(apk,out,expected_hash=digest(apk))
            self.assertEqual(len(result),12)
            for f in result:
                self.assertEqual(digest(out/f["path"]),f["sha256"])
            with self.assertRaises(FileExistsError):
                extract_pack(apk,out,expected_hash=digest(apk))
    def test_wrong_hash(self):
        with tempfile.TemporaryDirectory() as tmp:
            p=Path(tmp); apk=self.fixture(p)
            with self.assertRaises(ValueError): extract_pack(apk,p/"out")
            self.assertFalse((p/"out").exists())
    def test_invalid_entry(self):
        with tempfile.TemporaryDirectory() as tmp:
            p=Path(tmp); apk=self.fixture(p,True)
            with self.assertRaises(ValueError): extract_pack(apk,p/"out",expected_hash=digest(apk))
            self.assertFalse((p/"out").exists())
if __name__=="__main__": unittest.main()
