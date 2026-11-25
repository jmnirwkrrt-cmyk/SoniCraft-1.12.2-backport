import pathlib as P,re,zipfile,json,collections
root=P.Path('audit/SoniCraft_Registry/source');out=P.Path('p33')
jar=next(P.Path('p33-input').rglob('*.jar'));z=zipfile.ZipFile(jar);defs=json.loads(z.read('assets/sonicraft/backport/blocks.json'))
reg=next((root/'init').glob('*ModBlocks.java')).read_text()
classes=dict(re.findall(r'REGISTRY\.register\("([\w]+)", \(\) -> new (\w+)\(\)\)',reg))
def old(i):return i.endswith(('_lamp','_light','_lights','_searchlight','_bulb','_torch','_lantern')) or any(x in i for x in ['neon','glowing','luminant_','_lamp_','_light_'])
levels={}
for d in defs:
 i=d['id']; p=root/'block'/(classes.get(i,'')+'.java')
 if not p.is_file():continue
 s=p.read_text();m=re.search(r'super\(([^;]*?BlockBehaviour\.Properties\.m_284310_\(\)[^;]*)\);',s)
 if not m:continue
 expr=m[1];match=re.search(r'\.m_60953_\(\w+ -> (\d+)\)',expr)
 if match:l=int(match[1])
 elif old(i) and 'm_60953_' not in expr and re.search(r'extends (?:Block|RotatedPillarBlock|StairBlock|SlabBlock|WallBlock|FenceBlock|HorizontalDirectionalBlock|DirectionalBlock)\b',s):l=0
 else:continue
 if not 0<=l<=15:raise ValueError(i)
 levels[i]=l
out.joinpath('levels.json').write_text(json.dumps(levels,sort_keys=True,indent=2)+'\n')
groups=collections.defaultdict(list)
for k,v in sorted(levels.items()):groups[v].append(k)
code='package org.sonicraft.backport;\npublic final class P33Light { public static float intensity(String id, boolean fallback){switch(id){\n'
for v,ids in groups.items():code+=''.join('case "'+i+'":\n' for i in ids)+'return '+str(v)+'/15.0f;\n'
out.joinpath('P33Light.java').write_text(code+'default:return fallback?0.9f:0f;}}}\n')
print(len(levels),{v:len(ids) for v,ids in groups.items()});print(list(groups.get(0,[]))[:50])
