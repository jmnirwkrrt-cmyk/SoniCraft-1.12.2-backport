# Exercise the *packaged instructions* with small field/world adapters, not a Minecraft runtime.
import re,subprocess,pathlib,zipfile,json,math
jar=next(pathlib.Path('.').glob('*p29-springs.jar'))
class Obj:
 def __init__(self,**kw):self.__dict__.update(kw)
def program(cls):
 s=subprocess.check_output(['java','-m','jdk.jdeps/com.sun.tools.javap.Main','-c','-p','-classpath',str(jar),'org.sonicraft.backport.'+cls],text=True)
 return {name:{int(i):(op,arg or '',comment or '') for i,op,arg,comment in re.findall(r'^ *(\d+): +(\w+)(?: +([^/\n]*?))? *(?:// (.*))?$',body,re.M)} for name,body in re.findall(r'^  [^\n]*? ([\w$]+)\([^\n]*?\);\n    Code:\n(.*?)(?=\n\n|\Z)',s,re.M|re.S) if 'Code:' not in body}
spring=program('MarbleCaveBlocks$Spring');dash=program('ZoneMachines$Dash')
dirs=['DOWN','UP','NORTH','SOUTH','WEST','EAST']; vectors={'NORTH':(0,-1),'SOUTH':(0,1),'WEST':(-1,0),'EAST':(1,0),'UP':(0,0),'DOWN':(0,0)}
def run(code,*args):
 stack=[];loc=dict(enumerate(args));seq=sorted(code);pc=seq[0];push=stack.append;pop=stack.pop
 for _ in range(1000):
  op,arg,desc=code[pc];ni=seq.index(pc)+1;nextpc=seq[ni] if ni<len(seq) else None
  if 'load' in op and op[0] in 'aid':push(loc[int(op.split('_')[1] if '_' in op else arg)])
  elif 'store' in op and op[0] in 'aid':loc[int(op.split('_')[1] if '_' in op else arg)]=pop()
  elif op.startswith(('iconst_','dconst_','fconst_')):push(int(op[-1]))
  elif op=='bipush':push(int(arg))
  elif op.startswith('ldc'):push(desc[7:] if desc.startswith('String ') else float(desc.split()[-1].rstrip('d')))
  elif op=='getstatic':push(desc.split(':')[0].split('.')[-1].split()[-1])
  elif op in ('getfield','putfield'):
   k=desc.split(':')[0].split('.')[-1].split()[-1]
   if op=='getfield':push(getattr(pop(),k))
   else:v=pop();setattr(pop(),k,v)
  elif op in ('dneg',):push(-pop())
  elif op in ('iand','ishl','iushr','irem','ior','isub','dmul','dadd'):
   b=pop();a=pop();push({'iand':lambda:a&b,'ishl':lambda:a<<b,'iushr':lambda:a>>b,'irem':lambda:a%b,'ior':lambda:a|b,'isub':lambda:a-b,'dmul':lambda:a*b,'dadd':lambda:a+b}[op]())
  elif op.startswith('invoke'):
   m=desc.split(':')[0].split('.')[-1].split()[-1];arity=len(re.findall(r'L[^;]+;|[ZBSCDFIJ]',desc.split('(')[1].split(')')[0]));v=[pop() for i in range(arity)][::-1];o=None if op=='invokestatic' else pop()
   if m in ('valueOf','intValue'):r=v[0] if v else o
   elif m=='func_176223_P':r=o.default
   elif m=='func_82600_a':r=dirs[v[0]]
   elif m=='func_176745_a':r=dirs.index(o)
   elif m=='func_177226_a':r=dict(o);r[v[0]]=v[1]
   elif m=='func_177229_b':r=o[v[0]]
   elif m=='func_177230_c':r=o['block']
   elif m=='func_180495_p':r=o.state
   elif m=='func_180501_a':o.state=v[1];r=True
   elif m=='func_175684_a':o.ticks.append(v[2])
   elif m=='func_70093_af':r=False
   elif m=='func_70089_S' or m.startswith('access$'):r=True
   elif m=='getEntityData':r=o.data
   elif m=='func_74762_e':r=o.get(v[0],0)
   elif m=='func_74768_a':o[v[0]]=v[1]
   elif m=='func_82601_c':r=vectors[o][0]
   elif m=='func_82599_e':r=vectors[o][1]
   elif m=='max':r=max(v)
   elif m=='spring':v[0].sounds+=1
   else:raise ValueError(m)
   if not desc.endswith(')V'):push(r)
  elif op=='return':return
  elif op in ('areturn','ireturn'):return pop()
  elif op in ('checkcast','i2d'):pass
  elif op=='pop':pop()
  elif op=='goto':nextpc=int(arg)
  elif op in ('ifeq','ifne'):v=pop();nextpc=int(arg) if (v==0)==(op=='ifeq') else nextpc
  elif op in ('if_acmpne','if_icmpge'):
   b=pop();a=pop();nextpc=int(arg) if (a!=b if op=='if_acmpne' else a>=b) else nextpc
  else:raise ValueError(op)
  pc=nextpc
 raise ValueError('loop')
b=Obj(yellow=False);b.default={'block':b,'STATUS':0}
for i in range(6):
 for f in range(2):
  m=i|8*f;s=run(spring['func_176203_a'],b,m);assert s['field_176387_N']==dirs[i] and s['STATUS']==f and run(spring['func_176201_c'],b,s)==m
w=Obj(ticks=[],sounds=0,field_72995_K=False,state=run(spring['func_176203_a'],b,1));e=Obj(data={},field_70173_aa=10,field_70181_x=-.2);run(spring['launch'],b,w,None,w.state,e);assert e.field_70181_x==1.4 and w.state['STATUS']==1 and w.ticks==[10]
run(spring['launch'],b,w,None,b.default,e);assert w.sounds==1 and w.ticks==[10]
run(spring['func_180650_b'],b,w,None,w.state,None);assert w.state['STATUS']==0 and w.state['field_176387_N']=='UP'
for direction in dirs[2:]:
 for y in [-.5,0.,.7]:
  e.field_70181_x=y;run(dash['boost'],b,w,None,{'FACING':direction},e);dx,dz=vectors[direction];assert (e.field_70159_w,e.field_70181_x,e.field_70179_y)==(-1.4*dx,y,-1.4*dz)
print('PASS: actual disassembled code: 12 metadata round trips, activation, stale callback, 10-tick scheduling, reset, four dash directions at three vertical speeds; simulated world, not Forge.')
