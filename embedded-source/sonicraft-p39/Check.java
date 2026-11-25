package org.sonicraft.backport;
import com.google.gson.Gson;
import java.io.*; import net.minecraft.block.Block;
public class Check {
 public static void main(String[] args)throws Exception {
 Content.BlockDef[] defs=new Gson().fromJson(new InputStreamReader(Check.class.getResourceAsStream("/assets/sonicraft/backport/blocks.json"),"UTF-8"),Content.BlockDef[].class);
 int gels=0,rows=0;
 for(Content.BlockDef d:defs) if(d.id.equals("mega_mack_block_green")||d.id.equals("mega_mack_block_light_blue")){Block b=P38Gel.create(d);if(!(b instanceof P38Gel))throw new AssertionError();gels++;}
 for(Content.BlockDef d:defs){if(d.material==null||BulkBlocks.material(d)==null||BulkBlocks.sound(d)==null)throw new AssertionError(d.id);rows++;}
 if(gels!=2||rows!=1335)throw new AssertionError();System.out.println("PASS: both production gel constructors; "+rows+" material and sound lookups");
 }}
