import hashlib, json, struct, zipfile, re, sys
from pathlib import Path
EXPECTED='d5f03a44aca91e473cafcf2e74a94df58a7091cfe9d2cede15ea058f10867b30'
PREFIXES=('Lcom/sphero/android/convenience/','Lcom/sphero/core/','Lcom/sphero/platform/')
def parse(b, dex):
    u4=lambda p:struct.unpack_from('<I',b,p)[0]
    u2=lambda p:struct.unpack_from('<H',b,p)[0]
    def leb(p):
        v=0
        for i in range(5):
            n=b[p];p+=1;v|=(n&127)<<(7*i)
            if not n&128:return v,p
        raise ValueError('invalid ULEB128')
    strings=[]
    for i in range(u4(56)):
        p=u4(u4(60)+4*i);_,p=leb(p);strings.append(b[p:b.index(0,p)].decode('utf8','replace'))
    types=[strings[u4(u4(68)+4*i)] for i in range(u4(64))]
    protos=[]
    for p in range(u4(76),u4(76)+12*u4(72),12):
        q=u4(p+8);args=[types[u2(q+4+2*i)] for i in range(u4(q))] if q else []
        protos.append('('+''.join(args)+')'+types[u4(p+4)])
    fields=[(types[u2(p)],strings[u4(p+4)],types[u2(p+2)]) for p in range(u4(84),u4(84)+8*u4(80),8)]
    methods=[(types[u2(p)],strings[u4(p+4)],protos[u2(p+2)]) for p in range(u4(92),u4(92)+8*u4(88),8)]
    rows=[]
    for p in range(u4(100),u4(100)+32*u4(96),32):
        name=types[u4(p)]
        if not name.startswith(PREFIXES):continue
        parent=u4(p+8); interfaces=u4(p+12);d=u4(p+24)
        row={'class':name,'dex':dex,'access':u4(p+4),'superclass':None if parent==0xffffffff else types[parent],'interfaces':[types[u2(interfaces+4+2*i)] for i in range(u4(interfaces))] if interfaces else [],'fields':[],'methods':[]}
        if d:
            counts=[]
            for _ in range(4):v,d=leb(d);counts.append(v)
            for count,kind in zip(counts[:2],('static','instance')):
                idx=0
                for _ in range(count):
                    delta,d=leb(d);idx+=delta;flags,d=leb(d);owner,n,t=fields[idx];assert owner==name
                    row['fields'].append({'name':n,'type':t,'access':flags,'kind':kind})
            for count,kind in zip(counts[2:],('direct','virtual')):
                idx=0
                for _ in range(count):
                    delta,d=leb(d);idx+=delta;flags,d=leb(d);code,d=leb(d);owner,n,sig=methods[idx];assert owner==name
                    row['methods'].append({'name':n,'signature':sig,'access':flags,'kind':kind,'code_offset':code})
        rows.append(row)
    return rows
def main(apk,out):
    with Path(apk).open('rb') as f:
        if hashlib.file_digest(f,'sha256').hexdigest()!=EXPECTED:raise ValueError('APK fingerprint mismatch')
    rows=[]; hashes={}
    with zipfile.ZipFile(apk) as z:
        for name in sorted(z.namelist()):
            if re.fullmatch(r'classes(?:\d+)?\.dex',name):
                b=z.read(name);hashes[name]=hashlib.sha256(b).hexdigest();rows.extend(parse(b,name))
    rows.sort(key=lambda r:r['class']); counts={'classes':len(rows),'fields':sum(len(r['fields']) for r in rows),'methods':sum(len(r['methods']) for r in rows)}
    assert counts=={'classes':307,'fields':3891,'methods':1188},counts
    out=Path(out);out.mkdir(parents=True,exist_ok=True)
    (out/'sdk-inventory.json').write_text(json.dumps({'apk_sha256':EXPECTED,'dex_sha256':hashes,'counts':counts,'classes':rows},indent=2)+'\n',encoding='utf-8')
    lines=['# Original Android SDK symbol inventory','','Source: verified MIX 1.2.2 APK. Signatures are DEX descriptors; JSON offsets are decimal byte positions. Presence does not establish SD01 support or runtime semantics.','',str(counts),'']
    for r in rows:
        lines += ['## '+r['class'],'',f"DEX: {r['dex']}; access: {r['access']:#x}; parent: {r['superclass']}; interfaces: {', '.join(r['interfaces'])}",'']
        lines += [f"- Field {f['name']}: {f['type']} — {f['kind']}, flags {f['access']:#x}" for f in r['fields']]
        lines += [f"- Method {m['name']}{m['signature']} — {m['kind']}, flags {m['access']:#x}, code {m['code_offset']:#x}" for m in r['methods']]
        lines+=['']
    (out/'SDK-INVENTORY.md').write_text('\n'.join(lines),encoding='utf-8');print(counts)
if __name__=='__main__':main(*sys.argv[1:])
