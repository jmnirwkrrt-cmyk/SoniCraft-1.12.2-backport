package org.sonicraft.backport;
import net.minecraft.client.model.ModelRenderer;
final class FlickyAnimation112 {
 static class Frame {float t;float[] v;boolean smooth;}
 static class Channel {String bone;boolean rotation;Frame[] frames;}
 static final Channel[] CHANNELS=Content.read("flicky_animation.json",Channel[].class);
 static float sample(Frame[] frames,float time,int axis){int right=0;while(right<frames.length-1&&frames[right].t<time)right++;int left=Math.max(0,right-1);Frame a=frames[left],b=frames[right];float u=a.t==b.t?0:Math.max(0,Math.min(1,(time-a.t)/(b.t-a.t)));float p1=a.v[axis],p2=b.v[axis];if(!b.smooth)return p1+(p2-p1)*u;float p0=frames[Math.max(0,left-1)].v[axis],p3=frames[Math.min(frames.length-1,right+1)].v[axis];return .5f*((2*p1)+(-p0+p2)*u+(2*p0-5*p1+4*p2-p3)*u*u+(-p0+3*p1-3*p2+p3)*u*u*u);}
 static ModelRenderer bone(FlickyModel112 m,String name){switch(name){case "neck":return m.neck;case "left_leg":return m.left_leg;case "right_leg":return m.right_leg;case "left_wing":return m.left_wing;case "right_wing":return m.right_wing;case "root_rotate":return m.root_rotate;default:throw new IllegalArgumentException(name);}}
 static void apply(FlickyModel112 m,float limbSwing,float limbSwingAmount){float time=(limbSwing*.05f)%.64f,weight=Math.min(Math.max(limbSwingAmount,0),1);for(Channel c:CHANNELS){ModelRenderer b=bone(m,c.bone);float x=sample(c.frames,time,0)*weight,y=sample(c.frames,time,1)*weight,z=sample(c.frames,time,2)*weight;if(c.rotation){b.rotateAngleX+=x*(float)Math.PI/180;b.rotateAngleY+=y*(float)Math.PI/180;b.rotateAngleZ+=z*(float)Math.PI/180;}else {b.rotationPointX+=x;b.rotationPointY-=y;b.rotationPointZ+=z;}}}
}
