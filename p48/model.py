import re
from pathlib import Path
s=Path('audit/SoniCraft_Registry/source/client/model/Modeleggrobo.java').read_text();out=[];names=[];roots=[]
for line in s.splitlines():
 m=re.search(r'PartDefinition (\w+) = (\w+)\.m_171599_\("[^\"]+", (.*), PartPose\.(m_\d+_)\((.*)\)\);',line)
 if not m:continue
 name,parent,body,pose,args=m.groups();names.append(name);nums=re.findall(r'(-?\d+(?:\.\d+)?f)',args);assert len(nums) in (3,6)
 out += [f'{name}=new ModelRenderer(this);',f'{name}.setRotationPoint({",".join(nums[:3])});']
 if len(nums)==6:out += [f'{name}.rotateAngle{axis}={v};' for axis,v in zip('XYZ',nums[3:])]
 if parent=='partdefinition':roots.append(name)
 else:out.append(f'{parent}.addChild({name});')
 for box in re.finditer(r'\.m_171514_\((\d+), (\d+)\)(.*?)\.m_171488_\((.*?)new CubeDeformation\(([^)]+)\)\)',body):
  u,v,mirror,vals,inflate=box.groups();vals=[v.strip() for v in vals.rstrip(', ').split(',')];assert len(vals)==6
  dims=[str(int(float(v.rstrip('f')))) for v in vals[3:]]
  out += [f'{name}.mirror={str("m_171480_" in mirror).lower()};',f'{name}.setTextureOffset({u},{v});',f'{name}.addBox({",".join(vals[:3]+dims+[inflate])});']
assert len(names)>0
r='package org.sonicraft.backport;\nimport net.minecraft.client.model.*;import net.minecraft.entity.Entity;import net.minecraft.util.math.MathHelper;\npublic class EggroboModel48 extends ModelBase {\npublic ModelRenderer '+','.join(names)+';\npublic EggroboModel48(){textureWidth=128;textureHeight=128;\n'+'\n'.join(out)+'\n}\npublic void render(Entity e,float a,float b,float c,float d,float f,float scale){setRotationAngles(a,b,c,d,f,scale,e);'+''.join(n+'.render(scale);' for n in roots)+'}\npublic void setRotationAngles(float a,float b,float c,float d,float f,float s,Entity e){Head.rotateAngleY=d/57.295776f;Head.rotateAngleX=f/57.295776f;Right_Leg.rotateAngleX=MathHelper.cos(a)*b;Left_Leg.rotateAngleX=-MathHelper.cos(a)*b;Left_Arm.rotateAngleX=MathHelper.cos(a*.6662f)*b;}\n}'

for a,b in [('Head','head'),('Right_Leg','right_leg'),('Left_Leg','left_leg'),('Left_Arm','left_arm')]:r=r.replace(a,b)
Path('p48/src/org/sonicraft/backport/EggroboModel48.java').write_text(r)
print('Original pawn model:',len(names),'parts',len(roots),'roots')
