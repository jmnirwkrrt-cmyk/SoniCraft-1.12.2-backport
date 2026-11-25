import hashlib,json,struct,sys,zipfile
from pathlib import Path
root=Path(__file__).resolve().parent
base=Path(sys.argv[1]);out=Path(sys.argv[2]);helper=Path(sys.argv[3]);patch=Path(sys.argv[4])
assert hashlib.sha256(base.read_bytes()).hexdigest()=='f41878fd4d7a53512aadb987c8c5fdece04e125c4a7f40fa62f0d08b8c4486a1'
def utf8_replace(data,old,new):
    buf=bytearray(data[:10]);count=struct.unpack('>H',data[8:10])[0];pos=10;i=1;hits=0
    while i<count:
        tag=data[pos];buf.append(tag);pos+=1
        if tag==1:
            n=struct.unpack('>H',data[pos:pos+2])[0];s=data[pos+2:pos+2+n];pos+=2+n
            if s==old:s=new;hits+=1
            buf.extend(struct.pack('>H',len(s)));buf.extend(s)
        else:
            n={3:4,4:4,5:8,6:8,7:2,8:2,9:4,10:4,11:4,12:4,15:3,16:2,18:4}[tag];buf.extend(data[pos:pos+n]);pos+=n
            if tag in (5,6):i+=1
        i+=1
    buf.extend(data[pos:]);assert hits==1,hits;return bytes(buf)
with zipfile.ZipFile(base) as z,zipfile.ZipFile(helper) as h,zipfile.ZipFile(patch) as p:
    changes={n:h.read(n) for n in h.namelist() if n.endswith('.class')}
    changes.update({n:p.read(n) for n in p.namelist() if n.endswith('.class')})
    old=b'0.1.27-p43-worldfix';new=b'0.1.27-p44-scrap-route'
    changes['org/sonicraft/backport/SonicraftLegacy.class']=utf8_replace(z.read('org/sonicraft/backport/SonicraftLegacy.class'),old,new)
    changes['mcmod.info']=z.read('mcmod.info').replace(old,new)
    for f in (root/'assets').rglob('*'):
        if f.is_file():changes[f.relative_to(root/'assets').as_posix()]=f.read_bytes()
    sources=list((root/'src').rglob('*.java'))+[root/n for n in ('README_RU.md','Patch44.java','Remap.java','LinkCheck.java','pack.py','BUILD.sh','asset_report.json','assets.py','nbt43.py','verify.py')]
    for f in sources:changes['META-INF/sonicraft-p44/'+f.relative_to(root).as_posix()]=f.read_bytes()
    out.parent.mkdir(parents=True,exist_ok=True)
    with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as w:
        for n in z.namelist():
            if n not in changes:w.writestr(n,z.read(n))
        for n,data in changes.items():w.writestr(n,data)
    with zipfile.ZipFile(out) as v:
        assert v.testzip() is None and len(v.namelist())==len(set(v.namelist()))
        untouched=[n for n in z.namelist() if n not in changes]
        assert all(z.read(n)==v.read(n) for n in untouched)
        print('PASS archive,',len(untouched),'P43 entries preserved byte-for-byte')
    dest=out.parent/'SoniCraft_P44_SOURCE_PATCH.zip'
    with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED) as w:
        for f in sources:w.write(f,f.relative_to(root))
        for f in (root/'assets').rglob('*'):
            if f.is_file():w.write(f,f.relative_to(root))
print('SHA256',hashlib.sha256(out.read_bytes()).hexdigest())
