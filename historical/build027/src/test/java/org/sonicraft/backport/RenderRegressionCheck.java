package org.sonicraft.backport;
import java.util.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.geo.raw.pojo.Converter;
import software.bernie.geckolib3.geo.raw.tree.RawGeometryTree;
import software.bernie.geckolib3.geo.render.GeoBuilder;
import software.bernie.geckolib3.geo.render.built.*;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
/** Executes the exact renderCube method from the reported crash, without an OpenGL draw. */
public final class RenderRegressionCheck {
 static final IGeoRenderer<Object> RENDERER=new IGeoRenderer<Object>(){public GeoModelProvider getGeoModelProvider(){return null;}public ResourceLocation getTextureLocation(Object o){return null;}};
 static void cubes(GeoBone b,List<GeoCube> result){result.addAll(b.childCubes);for(GeoBone child:b.childBones)cubes(child,result);}
 static void draw(GeoCube cube){BufferBuilder buffer=new BufferBuilder(1024);buffer.begin(7,DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);IGeoRenderer.MATRIX_STACK.push();try{RENDERER.renderCube(buffer,cube,1,1,1,1);buffer.finishDrawing();}finally{IGeoRenderer.MATRIX_STACK.pop();}}
 public static void main(String[] args)throws Exception {
  net.minecraft.init.Bootstrap.register();
  int rendered=0;boolean reproduced=false;
  for(Characters.Definition d:Characters.DEFINITIONS){if(d.geo==null)continue;
   GeoModel model=new GeoBuilder().constructGeoModel(RawGeometryTree.parseHierarchy(Converter.fromJsonString(CharacterSmokeCheck.resource("geo/"+d.geo+".geo.json")),new ResourceLocation("sonicraft",d.geo)));
   List<GeoCube> cubes=new ArrayList<>();for(GeoBone b:model.topLevelBones)cubes(b,cubes);
   if(d.id.equals("shadow"))for(GeoCube c:cubes)if(Arrays.asList(c.quads).contains(null)){try{draw(c);}catch(NullPointerException expected){reproduced=true;}break;}
   int removed=GeoCompatibility.prepare(model);if(d.id.equals("shadow")&&removed!=16)throw new AssertionError("Expected 16 omitted Shadow faces, got "+removed);
   if(GeoCompatibility.prepare(model)!=0)throw new AssertionError("Not idempotent");
   for(GeoCube c:cubes){draw(c);rendered++;}
  }
  for(Badniks.Definition d:Badniks.DEFINITIONS){if(d.geo==null)continue;GeoModel model=new GeoBuilder().constructGeoModel(RawGeometryTree.parseHierarchy(Converter.fromJsonString(CharacterSmokeCheck.resource("geo/"+d.geo+".geo.json")),new ResourceLocation("sonicraft",d.geo)));GeoCompatibility.prepare(model);List<GeoCube> cubes=new ArrayList<>();for(GeoBone b:model.topLevelBones)cubes(b,cubes);for(GeoCube c:cubes){draw(c);rendered++;}}
  if(!reproduced)throw new AssertionError("Old Shadow crash not reproduced");
  System.out.println("PASS: original Shadow renderCube NPE reproduced; fix removes exactly 16 omitted faces; "+rendered+" cubes across all 16 models traverse the real renderCube without exception");
 }
}
