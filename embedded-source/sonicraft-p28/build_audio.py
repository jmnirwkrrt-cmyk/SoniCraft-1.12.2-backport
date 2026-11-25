"""Usage: python build_audio.py P27.jar P28.jar. Needs numpy, ffmpeg, libsndfile."""
import copy
import ctypes as C, ctypes.util, hashlib, json, subprocess, sys, tempfile, zipfile
from pathlib import Path
import numpy as np
class Info(C.Structure):
    _fields_=[('frames',C.c_int64),('rate',C.c_int),('channels',C.c_int),('format',C.c_int),('sections',C.c_int),('seekable',C.c_int)]
sf=C.CDLL(ctypes.util.find_library('sndfile'))
sf.sf_open.argtypes=[C.c_char_p,C.c_int,C.POINTER(Info)];sf.sf_open.restype=C.c_void_p
sf.sf_readf_float.argtypes=[C.c_void_p,C.POINTER(C.c_float),C.c_int64];sf.sf_readf_float.restype=C.c_int64
sf.sf_close.argtypes=[C.c_void_p]
def decode(path):
    info=Info(); h=sf.sf_open(bytes(path),0x10,C.byref(info))
    if not h:raise ValueError(str(path))
    try:
        x=np.empty((info.frames,info.channels),np.float32)
        assert sf.sf_readf_float(h,x.ctypes.data_as(C.POINTER(C.c_float)),len(x))==len(x)
        return info.rate,x
    finally:sf.sf_close(h)
def run(base,output):
    assert hashlib.sha256(base.read_bytes()).hexdigest()==BASE_SHA
    rows=[]; assets='assets/sonicraft/sounds/'
    with zipfile.ZipFile(base) as src,tempfile.TemporaryDirectory() as tmp:
        folder=Path(tmp);events=json.loads(src.read('assets/sonicraft/sounds.json'));targets=set()
        for ev,spec in events.items():
            if ev.startswith('mob.') or ev in ('neon_spring.use','slotmachine.active','pikopikohammer.attack'):
                for v in spec['sounds']:
                    targets.add(assets+(v if isinstance(v,str) else v['name']).split(':')[1]+'.ogg')
        with zipfile.ZipFile(output,'w') as dst:
            for inf in src.infolist():
                b=src.read(inf);n=inf.filename
                if n in targets and b[b.index(b'\x01vorbis')+11]==2:
                    p=folder/'before.ogg';p.write_bytes(b);rate,x=decode(p);mix=x.mean(axis=1);old=np.mean(x*x);avg=np.mean(mix*mix)
                    # Avoid erasing a strongly anti-correlated stereo recording on downmix.
                    if avg < old*.25:mix=x[:,np.argmax(np.mean(x*x,axis=0))]
                    mix=mix*min(1.,.99/max(float(np.abs(mix).max()),.00001))
                    q=folder/'after.ogg'
                    subprocess.run(['ffmpeg','-v','error','-y','-f','f32le','-ar',str(rate),'-ac','1','-i','pipe:0','-c:a','libvorbis','-q:a','8',str(q)],input=mix.astype('<f4').tobytes(),check=True)
                    r,y=decode(q);assert r==rate and y.shape==(len(x),1)
                    corr=float(np.corrcoef(mix,y[:,0])[0,1]);assert corr>.97,(n,corr)
                    rows.append(dict(path=n,rate=r,frames=len(x),correlation=round(corr,5)));b=q.read_bytes()
                dst.writestr(copy.copy(inf),b)
            dst.writestr('META-INF/sonicraft-p28/build_audio.py',Path(__file__).read_bytes())
            dst.writestr('META-INF/sonicraft-p28/test_audio.py',Path(__file__).with_name('test_audio.py').read_bytes())
            dst.writestr('META-INF/sonicraft-p28/changes.json',json.dumps(rows,indent=2))
        with zipfile.ZipFile(output) as dst:
            assert dst.testzip() is None
            changed={i.filename for i in src.infolist() if src.read(i)!=dst.read(i.filename)}
            assert changed=={r['path'] for r in rows}
    print(len(rows),'changed, min waveform correlation',min(r['correlation'] for r in rows))
if __name__=='__main__':
    BASE_SHA='e6120d87d2778efdc4a88af8f4f18c55ae9675ed58c9e7477baedc1b0faa5f91'
    run(*map(Path,sys.argv[1:]))
