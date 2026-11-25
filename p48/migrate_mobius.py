#!/usr/bin/env python3
"""Offline, copy-only SoniCraft dimension migration. Python 3.10+. Never edits inputs."""
import argparse,gzip,json,re,shutil,struct,zlib
from pathlib import Path
from nbt43 import loads,dumps
KEYS={'Dim','Dimension','dimension','SpawnDimension','spawnDimension','DimensionId','dimensionId','dimensionID','dimId','DimId','DestinationDimension','destinationDimension','targetDimension','TargetDimension','LastDimension','previousDimension'}
def rewrite(root,old,new):
 changed=0
 def visit(t,v,key=''):
  nonlocal changed
  if t==10:
   for k,(ty,x) in list(v.items()):
    if key in {'DimensionData','ForgeDimensionData'} and k==str(old):
     if str(new) in v:raise ValueError('Existing target dimension data')
     del v[k];v[str(new)]=(ty,x);changed+=1
    if k in KEYS and ty in (3,4) and x==old:v[k]=(ty,new);changed+=1
    else:visit(ty,x,k)
  elif t==9:
   ty,items=v
   for x in items:visit(ty,x)
 visit(10,root)
 return changed

def region(path,old,new,apply):
 raw=path.read_bytes()
 if len(raw)<8192 or len(raw)%4096:raise ValueError('Invalid region: '+str(path))
 header=bytearray(raw[:8192]);out=bytearray(header);changed=0
 for i in range(1024):
  entry=int.from_bytes(raw[i*4:i*4+4],'big');offset=entry>>8;sectors=entry&255
  if not offset:continue
  at=offset*4096;n=struct.unpack('>I',raw[at:at+4])[0];kind=raw[at+4]
  if offset<2 or n<1 or n+4>sectors*4096 or at+n+4>len(raw):raise ValueError('Corrupt chunk '+str(path))
  payload=raw[at+5:at+4+n]
  if kind==1:nbt=loads(payload)
  elif kind==2:nbt=loads(gzip.compress(zlib.decompress(payload)))
  elif kind==3:nbt=loads(gzip.compress(payload))
  else:raise ValueError('Unsupported compression '+str(kind))
  edits=rewrite(nbt,old,new);changed+=edits
  if edits:kind=2;payload=zlib.compress(gzip.decompress(dumps(nbt)))
  chunk=struct.pack('>I',len(payload)+1)+bytes([kind])+payload;count=(len(chunk)+4095)//4096
  if count>255:raise ValueError('Chunk too large')
  start=len(out)//4096;header[i*4:i*4+4]=((start<<8)|count).to_bytes(4,'big');out.extend(chunk);out.extend(b'\0'*(count*4096-len(chunk)))
 if changed and apply:out[:8192]=header;path.write_bytes(out)
 return changed

def scan(world,old,new,apply):
 report=[]
 for p in sorted(world.rglob('*')):
  if not p.is_file():continue
  if p.suffix=='.mca':n=region(p,old,new,apply)
  elif p.suffix=='.dat' or p.name=='level.dat_old':
   raw=p.read_bytes()
   if not raw.startswith(b'\x1f\x8b'):continue
   nbt=loads(raw);n=rewrite(nbt,old,new)
   if n and apply:p.write_bytes(dumps(nbt))
  else:continue
  if n:report.append({'file':str(p.relative_to(world)),'references':n})
 return report

def main():
 a=argparse.ArgumentParser(description=__doc__);a.add_argument('world',type=Path);a.add_argument('config',type=Path);a.add_argument('--old',type=int,default=11230);a.add_argument('--new',type=int,default=1123);a.add_argument('--copy-to',type=Path);args=a.parse_args()
 world=args.world.resolve();cfg=args.config.resolve();old=args.old;new=args.new
 if not world.joinpath('level.dat').is_file() or not (world/('DIM'+str(old))).is_dir():a.error('Expected level.dat and DIM'+str(old))
 if not 2<=new<10000 or new==old or (world/('DIM'+str(new))).exists():a.error('Target ID must be free, 2..9999')
 text=cfg.read_text(encoding='utf-8');pattern=r'(?m)^(\s*I:mobiusId\s*=\s*)'+str(old)+r'\s*$'
 replacement,count=re.subn(pattern,lambda m:m[1]+str(new),text)
 if count!=1:a.error('Config must contain exactly one I:mobiusId='+str(old))
 target=args.copy_to.resolve() if args.copy_to else None
 if target and (target.exists() or target==world or world in target.parents):a.error('--copy-to must be new and outside the input world')
 # Validate every NBT/region BEFORE writing. User must close Minecraft/server first.
 report=scan(world,old,new,False)
 result={'mode':'dry-run','old':old,'new':new,'changed_references':report,'requires':'Stop Minecraft/server; verify target ID is unused by other mods and AR planets.'}
 if target:
  shutil.copytree(world,target)
  scan(target,old,new,True)
  (target/('DIM'+str(old))).rename(target/('DIM'+str(new)))
  (target/'sonicraft.cfg.P48-migrated').write_text(replacement,encoding='utf-8')
  result.update(mode='copy-created',world=str(target),config=str(target/'sonicraft.cfg.P48-migrated'))
  (target/'MOBIUS_MIGRATION.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
 print(json.dumps(result,ensure_ascii=False,indent=2))
if __name__=='__main__':main()
