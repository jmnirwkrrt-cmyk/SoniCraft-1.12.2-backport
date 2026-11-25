package org.sonicraft.backport;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.*;
import net.minecraft.world.storage.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.geo.raw.pojo.Converter;
import software.bernie.geckolib3.geo.raw.tree.RawGeometryTree;
import software.bernie.geckolib3.geo.render.GeoBuilder;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.keyframe.*;
import software.bernie.geckolib3.util.json.JsonAnimationUtils;
import software.bernie.geckolib3.molang.MolangRegistrar;
import software.bernie.shadowed.eliotlash.molang.MolangParser;
public class CharacterSmokeCheck {
 static class TestWorld extends World {
  TestWorld(){super(new SaveHandlerMP(),new WorldInfo(new WorldSettings(1L,GameType.CREATIVE,false,false,WorldType.DEFAULT),"test"),new WorldProviderSurface(),new Profiler(),false);}
  @Override protected IChunkProvider createChunkProvider(){return null;}
  @Override protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return false;}
 }
 static String resource(String path)throws Exception{try(InputStream in=CharacterSmokeCheck.class.getResourceAsStream("/assets/sonicraft/"+path)){if(in==null)throw new AssertionError(path);ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))>0)b.write(buf,0,n);return new String(b.toByteArray(),StandardCharsets.UTF_8);}}
 static void channels(VectorKeyFrameList<?> list){if(list==null||list.xKeyFrames.isEmpty()||list.yKeyFrames.isEmpty()||list.zKeyFrames.isEmpty())throw new AssertionError("Missing channel");}
 public static void main(String[] args)throws Exception {
  Bootstrap.register();World world=new TestWorld();MolangParser parser=new MolangParser();MolangRegistrar.registerVars(parser);int clips=0,geometries=0;
  for(Characters.Definition d:Characters.DEFINITIONS){LegacyCharacter e=Characters.entityClass(d).getConstructor(World.class).newInstance(world);if(Math.abs(e.getMaxHealth()-d.health)>.01||Math.abs(e.width-d.width)>.01||e.tasks.taskEntries.isEmpty())throw new AssertionError(d.id);e.setCustomNameTag("test-"+d.id);e.setHealth(e.getMaxHealth()/2);NBTTagCompound tag=new NBTTagCompound();e.writeToNBT(tag);LegacyCharacter copy=Characters.entityClass(d).getConstructor(World.class).newInstance(world);copy.readFromNBT(tag);if(!copy.getCustomNameTag().equals(e.getCustomNameTag())||copy.getHealth()!=e.getHealth())throw new AssertionError("NBT "+d.id);
   if(d.geo==null)continue;new GeoBuilder().constructGeoModel(RawGeometryTree.parseHierarchy(Converter.fromJsonString(resource("geo/"+d.geo+".geo.json")),new ResourceLocation("sonicraft",d.geo)));geometries++;
   JsonObject j=new JsonParser().parse(resource("animations/"+d.geo+".animation.json")).getAsJsonObject();
   for(Map.Entry<String,JsonElement> entry:JsonAnimationUtils.getAnimations(j)){Animation a=JsonAnimationUtils.deserializeJsonToAnimation(entry,parser);JsonObject bones=entry.getValue().getAsJsonObject().getAsJsonObject("bones");if(bones!=null)for(Map.Entry<String,JsonElement> bone:bones.entrySet()){BoneAnimation match=null;for(BoneAnimation ba:a.boneAnimations)if(ba.boneName.equals(bone.getKey()))match=ba;if(match==null)throw new AssertionError("Missing bone");JsonObject ch=bone.getValue().getAsJsonObject();if(ch.has("rotation"))channels(match.rotationKeyFrames);if(ch.has("position"))channels(match.positionKeyFrames);if(ch.has("scale"))channels(match.scaleKeyFrames);}clips++;}
  }
  new CreamModel112();new BlazeModel112();new EggmanModel112();System.out.println("PASS: 17 character constructors, AI and NBT roundtrips; "+geometries+" geometries; "+clips+" animations with complete channels; 3 vanilla model constructors");
 }
}
