import gzip,io,struct
fmt={1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}
def loads(raw):
 f=io.BytesIO(gzip.decompress(raw))
 def num(k):return struct.unpack('>'+k,f.read(struct.calcsize('>'+k)))[0]
 def read(t):
  if t in fmt:return num(fmt[t])
  if t==8:return f.read(num('H')).decode()
  if t==10:
   v={}
   while k:=num('B'):
    key=read(8);v[key]=(k,read(k))
   return v
  if t==9:
   k=num('B');return k,[read(k) for _ in range(num('i'))]
  if t in (7,11,12):return f.read(num('i')*{7:1,11:4,12:8}[t])
  raise ValueError(t)
 assert f.read(1)==b'\x0a';name=read(8);root=read(10);assert not f.read();return root

def enc(t,v):
 if t in fmt:return struct.pack('>'+fmt[t],v)
 if t==8:
  b=v.encode();return struct.pack('>H',len(b))+b
 if t==10:return b''.join(bytes([k])+enc(8,n)+enc(k,x) for n,(k,x) in v.items())+b'\x00'
 if t==9:
  k,l=v;return struct.pack('>Bi',k,len(l))+b''.join(enc(k,x) for x in l)
 return struct.pack('>i',len(v)//{7:1,11:4,12:8}[t])+v

def dumps(root):return gzip.compress(b'\x0a'+enc(8,'')+enc(10,root),mtime=0)
def ls(d,k):return d[k][1][1]
def pos(b):return tuple(ls(b,'pos'))
