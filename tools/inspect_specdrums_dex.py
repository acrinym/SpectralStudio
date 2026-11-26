"""Read class and method tables from the exact original APK for repeatable command evidence."""
import hashlib
import json
import struct
import sys
import zipfile
from pathlib import Path

EXPECTED = "d5f03a44aca91e473cafcf2e74a94df58a7091cfe9d2cede15ea058f10867b30"
COMMANDS = {"ColorTapNotifyCommand": 0x1e, "EnableColorTapNotifyCommand": 0x1d,
            "GetActiveColorPaletteCommand": 0x44, "SetActiveColorPaletteCommand": 0x45,
            "GetColorIdentificationReportCommand": 0x46, "LoadColorPaletteCommand": 0x47}

def inspect(apk):
    with Path(apk).open("rb") as f:
        if hashlib.file_digest(f,"sha256").hexdigest()!=EXPECTED:
            raise ValueError("Unexpected original APK")
    with zipfile.ZipFile(apk) as z: b=z.read("classes2.dex")
    u4=lambda p: struct.unpack_from("<I",b,p)[0]
    u2=lambda p: struct.unpack_from("<H",b,p)[0]
    def leb(p):
        v=s=0
        for _ in range(5):
            n=b[p];p+=1;v|=(n&127)<<s;s+=7
            if not n&128:return v,p
        raise ValueError("Invalid ULEB")
    strings=[]
    for i in range(u4(56)):
        p=u4(u4(60)+4*i);_,p=leb(p);strings.append(b[p:b.index(0,p)].decode("utf8","replace"))
    types=[strings[u4(u4(68)+4*i)] for i in range(u4(64))]
    methods=[(types[u2(p)],strings[u4(p+4)]) for p in range(u4(92),u4(92)+8*u4(88),8)]
    fields=[(types[u2(p)],types[u2(p+2)],strings[u4(p+4)]) for p in range(u4(84),u4(84)+8*u4(80),8)]
    result={}
    for p in range(u4(100),u4(100)+32*u4(96),32):
        name=types[u4(p)];d=u4(p+24); short=name.rsplit("/",1)[-1].rstrip(";")
        if not d or short not in COMMANDS:continue
        counts=[]
        for _ in range(4):v,d=leb(d);counts.append(v)
        for count in counts[:2]:
            for _ in range(count):_,d=leb(d);_,d=leb(d)
        for count in counts[2:]:
            mi=0
            for _ in range(count):
                delta,d=leb(d);mi+=delta;_,d=leb(d);code,d=leb(d)
                if methods[mi][1]!="<init>" or not code:continue
                words=[u2(x) for x in range(code+16,code+16+2*u4(code+12),2)]
                # Match the constructor's two const/16 values followed by invoke-virtual.
                matches=[]
                for i in range(len(words)-5):
                    if words[i]==0x0013 and words[i+2]==0x0113 and words[i+4]==0x406e:
                        method=methods[words[i+5]]
                        if method[1]=="registerApiCommand":matches.append((words[i+1],words[i+3]))
                if matches!=[(0x1a,COMMANDS[short])]:raise ValueError("Unexpected registerApiCommand call "+short)
                result[short]={"device":matches[0][0],"command":matches[0][1],"constructor_code_offset":hex(code)}
    if set(result)!=set(COMMANDS):raise ValueError("Missing command evidence")
    args=[f[2] for f in fields if f[0].endswith("/ColorTapNotifyListenerArgs;")]
    if set(args)!={"_red","_green","_blue","_confidence","_colorClassificationId","_tapIntensity"}:raise ValueError("Unexpected tap fields")
    return {"dex_sha256":hashlib.sha256(b).hexdigest(),"commands":result,"tap_listener_fields":args}

if __name__=="__main__":
    print(json.dumps(inspect(sys.argv[1]),indent=2))
