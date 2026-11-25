package org.sonicraft.backport;
import java.util.*;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;
public final class FindZoneCommand extends CommandBase {
 @Override public String getName(){return "sonicraftbiome";}
 @Override public String getUsage(ICommandSender s){return "/sonicraftbiome <id> [radius 128..4096]";}
 @Override public int getRequiredPermissionLevel(){return 2;}
 @Override public void execute(MinecraftServer server,ICommandSender sender,String[] args)throws CommandException{
  if(args.length<1||args.length>2)throw new WrongUsageException(getUsage(sender));String id=args[0].replace("sonicraft:","");Biome b=Content.BIOMES.get(id);if(b==null)throw new CommandException("Unknown SoniCraft biome: "+id);int radius=args.length==2?parseInt(args[1],128,4096):2048;BlockPos origin=sender.getPosition();BlockPos found=sender.getEntityWorld().getBiomeProvider().findBiomePosition(origin.getX(),origin.getZ(),radius,Collections.singletonList(b),new Random(0));sender.sendMessage(new TextComponentString(found==null?"Biome not found within "+radius+" blocks":id+": X="+found.getX()+", Z="+found.getZ()));
 }
 @Override public List<String> getTabCompletions(MinecraftServer server,ICommandSender sender,String[] args,BlockPos pos){return args.length==1?getListOfStringsMatchingLastWord(args,Content.BIOMES.keySet()):Collections.emptyList();}
}
