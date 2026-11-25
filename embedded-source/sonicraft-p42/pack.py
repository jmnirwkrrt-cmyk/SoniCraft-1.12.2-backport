import hashlib,json,struct,sys,zipfile
from pathlib import Path
root=Path(__file__).resolve().parent
base=Path(sys.argv[1]);out=Path(sys.argv[2]);helper=Path(sys.argv[3]);patched=Path(sys.argv[4])
assert hashlib.sha256(base.read_bytes()).hexdigest()=='0969d05deb2d0c81a312fc5623d642579cd999715fa68337c30a2a87b596dacc'
# Refuse other baselines so previous patches cannot be silently replaced.
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
with zipfile.ZipFile(base) as z,zipfile.ZipFile(helper) as h:
    changes={n:h.read(n) for n in h.namelist() if n.endswith('.class')}
    changes['org/sonicraft/backport/LegacyCharacter.class']=patched.read_bytes()
    old=b'0.1.27-p41-frontier';new=b'0.1.27-p42-companions'
    changes['org/sonicraft/backport/SonicraftLegacy.class']=utf8_replace(z.read('org/sonicraft/backport/SonicraftLegacy.class'),old,new)
    metadata=z.read('mcmod.info');assert metadata.count(old)==1;changes['mcmod.info']=metadata.replace(old,new)
    translations={
      'bond':('We are friends! Following you.','Теперь мы друзья! Иду за тобой.'),
      'follow':('Following you.','Иду за тобой.'),
      'wander':('Wandering freely.','Буду свободно бродить.'),
      'other':('Already bonded to another player.','Уже дружит с другим игроком.'),
      'cooldown':('Cuddle available in %s seconds.','Можно потискать через %s сек.'),
      'space':('No safe space nearby. Still in your arms.','Рядом нет безопасного места. Остаётся на руках.'),
      'compat':('Could not fully reset Carry On. Check its version.','Не удалось полностью сбросить Carry On. Проверьте его версию.')}
    for idx,lang in enumerate(('en_us','ru_ru')):
        n='assets/sonicraft/lang/'+lang+'.lang';changes[n]=z.read(n)+('\n'+''.join('sonicraft.companion.'+k+'='+v[idx]+'\n' for k,v in translations.items())).encode('utf8')
    changes['META-INF/sonicraft-p42/README_RU.md']=(root/'README_RU.md').read_bytes()
    for f in list((root/'src').rglob('*.java'))+[root/n for n in ('Patch.java','Remap.java','LinkCheck.java','pack.py','BUILD.sh')]:
        changes['META-INF/sonicraft-p42/'+f.relative_to(root).as_posix()]=f.read_bytes()
    with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as w:
        for n in z.namelist():
            if n not in changes:w.writestr(n,z.read(n))
        for n,b in changes.items():w.writestr(n,b)
    with zipfile.ZipFile(out) as v:
        assert v.testzip() is None and len(v.namelist())==len(set(v.namelist()))
        untouched=[n for n in z.namelist() if n not in changes]
        assert all(z.read(n)==v.read(n) for n in untouched)
        print('PASS: ZIP integrity, unique entries,',len(untouched),'P41 files preserved byte-for-byte')
print('SHA256',hashlib.sha256(out.read_bytes()).hexdigest())
