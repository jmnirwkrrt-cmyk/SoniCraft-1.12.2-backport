package org.sonicraft.backport;
import net.minecraft.world.World;import net.minecraft.util.math.BlockPos;import java.util.Map;
class Util41 {
 static boolean pop(World w,BlockPos p){if(!w.func_175698_g(p))return false;MechanismSounds.play(w,p,"balloon.pop");return true;}
 static boolean known(Map map,Object value){return value instanceof String&&(map.containsValue(value)||P34Sites.extra((String)value));}
 static String marker(String k){return "metal_sonic_spawn_trigger".equals(k)?"metal_sonic_boss":"death_egg_robot_spawn_trigger".equals(k)?"death_egg_robot":null;}
 static boolean trigger(java.util.Set set,Object k){return set.contains(k)||"metal_sonic_spawn_trigger".equals(k)||"death_egg_robot_spawn_trigger".equals(k);}
 static int height(String k){if("deathegg".equals(k))return 48;if("bigarms".equals(k))return 12;if("metalpit".equals(k))return -6;if("aquaticlake".equals(k)||"seasidelake".equals(k))return -24;return P32Sites.roadShift(k);}
}
