from build_audio import *
al=C.CDLL(ctypes.util.find_library('openal'));I=C.c_int;P=C.c_void_p;F=C.c_float;U=C.c_uint
def fn(n,ret,*args):
    f=getattr(al,n);f.restype=ret;f.argtypes=args;return f
device=fn('alcLoopbackOpenDeviceSOFT',P,C.c_char_p)(None);assert device
create=fn('alcCreateContext',P,P,C.POINTER(I));current=fn('alcMakeContextCurrent',I,P);destroy=fn('alcDestroyContext',None,P)
genbuf=fn('alGenBuffers',None,I,C.POINTER(U));gensrc=fn('alGenSources',None,I,C.POINTER(U));data=fn('alBufferData',None,U,I,P,I,I);sourcei=fn('alSourcei',None,U,I,I)
position=fn('alSource3f',None,U,I,F,F,F);play=fn('alSourcePlay',None,U);render=fn('alcRenderSamplesSOFT',None,P,P,I);err=fn('alGetError',I)
def simulation(x,rate,side):
    # ALC_FREQUENCY, ALC_FORMAT_CHANNELS_SOFT: stereo, ALC_FORMAT_TYPE_SOFT: float
    attrs=(I*7)(0x1007,48000,0x1990,0x1501,0x1991,0x1406,0);ctx=create(device,attrs);assert ctx and current(ctx)
    pcm=(np.clip(x,-1,1)*32767).astype('<i2');buf=U();s=U();genbuf(1,C.byref(buf));gensrc(1,C.byref(s))
    data(buf,0x1101 if x.shape[1]==1 else 0x1103,pcm.ctypes.data,pcm.nbytes,rate);sourcei(s,0x1009,buf.value);position(s,0x1004,side,0,-.1);play(s)
    out=np.zeros((int(len(x)*48000/rate)+1024,2),np.float32);render(device,out.ctypes.data,len(out));assert err()==0
    energy=np.sum(out*out,axis=0);assert energy.sum()>0
    fn('alDeleteSources',None,I,C.POINTER(U))(1,C.byref(s));fn('alDeleteBuffers',None,I,C.POINTER(U))(1,C.byref(buf));current(None);destroy(ctx)
    return float(energy[0]/energy.sum())
with tempfile.TemporaryDirectory() as tmp:
 for asset in ['neon_spring.use','entity.tails.hurt','slotmachineactive']:
    pairs=[]
    for path in sys.argv[1:]:
        with zipfile.ZipFile(path) as z:b=z.read('assets/sonicraft/sounds/'+asset+'.ogg')
        p=Path(tmp)/'sample.ogg';p.write_bytes(b);rate,x=decode(p);pairs.append([round(simulation(x,rate,s),4) for s in [-1,1]])
    print(asset,pairs);assert abs(pairs[0][0]-pairs[0][1])<.1 and pairs[1][0]>.85 and pairs[1][1]<.15
fn('alcCloseDevice',I,P)(device)
