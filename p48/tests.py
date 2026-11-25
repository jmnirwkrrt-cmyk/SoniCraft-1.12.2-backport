import tempfile,struct,zlib,gzip,hashlib,subprocess,sys
from pathlib import Path
from nbt43 import *
from migrate_mobius import scan,rewrite
with tempfile.TemporaryDirectory() as tmp:
 p=Path(tmp);world=p/'world';world.mkdir();(world/'DIM11230/region').mkdir(parents=True)
 root={'Data':(10,{'Player':(10,{'Dimension':(3,11230),'Spawns':(9,(10,[{'Dim':(3,11230)}]))}),'WorldGenSeed':(4,11230)}),'ForgeDimensionData':(10,{'11230':(10,{'x':(3,1)})})}
 (world/'level.dat').write_bytes(dumps(root));cfg=p/'sonicraft.cfg';cfg.write_text('dimensions {\n I:mobiusId=11230\n}\n')
 chunk={'Level':(10,{'Entities':(9,(10,[{'Dimension':(3,11230),'Health':(5,20.0)}]))})};payload=zlib.compress(gzip.decompress(dumps(chunk)));raw=struct.pack('>I',len(payload)+1)+b'\x02'+payload
 header=bytearray(8192);header[:4]=(513).to_bytes(4,'big');(world/'DIM11230/region/r.0.0.mca').write_bytes(header+raw+b'\0'*(4096-len(raw)))
 before={str(f.relative_to(world)):f.read_bytes() for f in world.rglob('*') if f.is_file()};config_before=cfg.read_bytes()
 cmd=[sys.executable,str(Path(__file__).parent/'migrate_mobius.py'),str(world),str(cfg)]
 subprocess.run(cmd,check=True,stdout=subprocess.DEVNULL);assert before=={str(f.relative_to(world)):f.read_bytes() for f in world.rglob('*') if f.is_file()}
 subprocess.run(cmd+['--copy-to',str(p/'copy')],check=True,stdout=subprocess.DEVNULL)
 assert (p/'copy/DIM1123/region/r.0.0.mca').exists() and not (p/'copy/DIM11230').exists()
 assert not scan(p/'copy',11230,1123,False)
 out=loads((p/'copy/level.dat').read_bytes());assert out['Data'][1]['WorldGenSeed'][1]==11230
 assert out['Data'][1]['Player'][1]['Dimension'][1]==1123;assert '1123' in out['ForgeDimensionData'][1]
 assert cfg.read_bytes()==config_before;assert all((world/n).read_bytes()==v for n,v in before.items())
 fail=subprocess.run(cmd+['--copy-to',str(p/'copy')],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL);assert fail.returncode!=0
print('PASS migration dry-run, copy-only, player/respawn/chunk refs, dimension data, source preservation, collision refusal')
