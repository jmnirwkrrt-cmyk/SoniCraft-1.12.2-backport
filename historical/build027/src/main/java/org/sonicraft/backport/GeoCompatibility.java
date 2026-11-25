package org.sonicraft.backport;
import java.util.*;
import software.bernie.geckolib3.geo.render.built.*;
/** GeckoLib 3's baker emits null quads for deliberately omitted per-face UVs.
 * Its IGeoRenderer dereferences every quad. Keep only real faces, without inventing UVs. */
final class GeoCompatibility {
 static int prepare(GeoModel model){int removed=0;for(GeoBone b:model.topLevelBones)removed+=prepare(b);return removed;}
 private static int prepare(GeoBone bone){int removed=0;for(GeoCube cube:bone.childCubes){int count=0;for(GeoQuad q:cube.quads)if(q!=null)count++;if(count!=cube.quads.length){removed+=cube.quads.length-count;GeoQuad[] real=new GeoQuad[count];int i=0;for(GeoQuad q:cube.quads)if(q!=null)real[i++]=q;cube.quads=real;}}for(GeoBone child:bone.childBones)removed+=prepare(child);return removed;}
}
