package org.sonicraft.backport;
import java.util.*;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.Rotation;

/** Operator-only diagnostic and controlled paste of original SoniCraft NBT structures. */
public final class StructureCommand extends CommandBase {
 static final String[] PALETTED=Content.read("paletted_026.json",String[].class);
 @Override public String getName(){return "sonistruct";}
 @Override public String getUsage(ICommandSender sender){return "/sonistruct list | palette [prefix] | check <name> | place <name> [x y z] | dungeon [preview] [x y z] [seed] | city <casino|spring> [preview] [x y z] [seed] | complex <name> [audit|preview] [x y z] [seed]";}
 @Override public int getRequiredPermissionLevel(){return 2;}
 @Override public void execute(MinecraftServer server,ICommandSender sender,String[] args)throws CommandException{
  if(args.length==1&&args[0].equals("list")){
   sender.sendMessage(new TextComponentString("Available source templates: "+StructurePort.CATALOG.size()+"; /sonistruct check <name>"));return;
  }
  if(args.length>=1&&args[0].equals("palette")){
   if(args.length>2)throw new WrongUsageException(getUsage(sender));
   String prefix=args.length==2?args[1]:"";
   int total=0;java.util.List<String> sample=new ArrayList<>();
   for(String name:PALETTED)if(name.startsWith(prefix)){total++;if(sample.size()<14)sample.add(name);}
   sender.sendMessage(new TextComponentString("Templates with SoniCraft palette coverage: "+total+" / "+StructurePort.CATALOG.size()+" (prefix '"+prefix+"'); mechanics and newer vanilla IDs may still need adaptation."));
   sender.sendMessage(new TextComponentString(String.join(", ",sample)+(total>sample.size()?" ...":"")));
   return;
  }
  // 0.1.21: manually assembled linked original complexes. No live worldgen.
  if(args.length>=1&&args[0].equals("complex")){
   if(args.length==2&&args[1].equals("status")){
    sender.sendMessage(new TextComponentString(SourceComplexes.auditAll()));return;
   }
   if(args.length==2&&args[1].equals("list")){
    sender.sendMessage(new TextComponentString("Source complexes: "+String.join(", ",new TreeSet<>(SourceComplexes.complexes()))+". Use /sonistruct complex <name> preview [x y z] [seed]."));
    return;
   }
   if(args.length<2||!SourceComplexes.complexes().contains(args[1]))throw new WrongUsageException(getUsage(sender));
   if(args.length==3&&args[2].equals("audit")){
    sender.sendMessage(new TextComponentString(SourceComplexes.audit(args[1])));return;
   }
   boolean preview=args.length>2&&args[2].equals("preview");int start=preview?3:2;
   if(args.length!=start&&args.length!=start+3&&args.length!=start+4)throw new WrongUsageException(getUsage(sender));
   BlockPos at=args.length>=start+3?new BlockPos(parseInt(args[start]),parseInt(args[start+1]),parseInt(args[start+2])):sender.getPosition().add(4,0,4);
   long seed=args.length==start+4?parseLong(args[start+3]):sender.getEntityWorld().getSeed()^at.toLong();
   MarbleDungeon.Plan plan;
   try{plan=SourceComplexes.plan(args[1],at,seed);}
   catch(IllegalStateException ex){throw new CommandException("Complex root cannot be placed: "+ex.getMessage());}
   sender.sendMessage(new TextComponentString("Source SoniCraft complex ("+args[1]+"): "+plan.summary()));
   if(preview)return;
   if(!SourceComplexes.place(sender.getEntityWorld(),plan))throw new CommandException("Complex rejected (unloaded chunks, unported blocks, incompatible terrain/tile entities, height limits); no blocks changed.");
   sender.sendMessage(new TextComponentString("Placed source sections; original boss scripts, fluid handling and some mechanisms remain unfinished."));
   return;
  }
  if(args.length>=1&&args[0].equals("city")){
   if(args.length<2||!UrbanJigsaw.cities().contains(args[1]))throw new WrongUsageException(getUsage(sender));
   boolean preview=args.length>2&&args[2].equals("preview");int start=preview?3:2;
   if(args.length!=start&&args.length!=start+3&&args.length!=start+4)throw new WrongUsageException(getUsage(sender));
   BlockPos at=args.length>=start+3?new BlockPos(parseInt(args[start]),parseInt(args[start+1]),parseInt(args[start+2])):sender.getPosition().add(4,0,4);
   long seed=args.length==start+4?parseLong(args[start+3]):sender.getEntityWorld().getSeed()^at.toLong();
   MarbleDungeon.Plan plan=UrbanJigsaw.plan(args[1],at,seed);
   sender.sendMessage(new TextComponentString("SoniCraft urban jigsaw ("+args[1]+"): "+plan.summary()));
   if(preview)return;
   if(!UrbanJigsaw.place(sender.getEntityWorld(),plan))throw new CommandException("Urban complex rejected: chunks unloaded, terrain obstructed, protected tile entities, or size limit; nothing changed.");
   sender.sendMessage(new TextComponentString("City sections placed. Source machinery, bosses and automatic worldgen are NOT fully implemented."));
   return;
  }
  if(args.length>=1&&args[0].equals("dungeon")){
   boolean preview=args.length>1&&args[1].equals("preview");int start=preview?2:1;
   if(args.length!=start&&args.length!=start+3&&args.length!=start+4)throw new WrongUsageException(getUsage(sender));
   BlockPos at=args.length>=start+3?new BlockPos(parseInt(args[start]),parseInt(args[start+1]),parseInt(args[start+2])):sender.getPosition().add(2,0,2);
   long seed=args.length==start+4?parseLong(args[start+3]):server.getWorld(sender.getEntityWorld().provider.getDimension()).getSeed()^at.toLong();
   MarbleDungeon.Plan plan=MarbleDungeon.plan(at,seed);
   sender.sendMessage(new TextComponentString("Marble Cave: "+plan.summary()+". Coordinates denote ENTRANCE floor, not cave bottom."));
   if(preview)return;
   if(!MarbleDungeon.place(sender.getEntityWorld(),plan))throw new CommandException("Dungeon rejected: required chunks unloaded, blocked terrain, protected tile entities or out-of-bounds. Nothing changed.");
   sender.sendMessage(new TextComponentString("Marble Cave placed. SOURCE ENTITIES/MECHANISMS are still incomplete."));
   return;
  }
  if(args.length<2||!StructurePort.CATALOG.contains(args[1]))throw new WrongUsageException(getUsage(sender));
  StructurePort.Template t=StructurePort.get(args[1]);
  if(args[0].equals("check")){
   sender.sendMessage(new TextComponentString(t.report()));return;
  }
  if(!args[0].equals("place"))throw new WrongUsageException(getUsage(sender));
  if(!t.usable())throw new CommandException("Cannot paste: missing source blocks "+t.missing);
  if(!t.deferred.isEmpty())sender.sendMessage(new TextComponentString("WARNING: incomplete or adapted mechanisms: "+t.deferred));
  if(t.entities>t.spawns.size()||t.markers!=0)
   sender.sendMessage(new TextComponentString("WARNING: some source entities or jigsaw connections are not imported; this is only an isolated template."));
  if(t.blockEntities!=0)sender.sendMessage(new TextComponentString("Compatible loot/inventory NBT is restored; other block entity data is ignored."));
  BlockPos pos=args.length==5?new BlockPos(parseInt(args[2]),parseInt(args[3]),parseInt(args[4])):sender.getPosition().add(2,0,2);
  if(!StructurePort.place(sender.getEntityWorld(),pos,t,Rotation.NONE,false))throw new CommandException("Placement rejected: missing chunks, protected blocks, Y limit or template too large");
  sender.sendMessage(new TextComponentString("Placed "+t.name+" at "+pos));
 }
 @Override public List<String> getTabCompletions(MinecraftServer server,ICommandSender sender,String[] args,BlockPos targetPos){
  if(args.length==1)return getListOfStringsMatchingLastWord(args,"list","palette","check","place","dungeon","city","complex");
  if(args.length==2&&args[0].equals("palette"))return getListOfStringsMatchingLastWord(args,PALETTED);
  if(args.length==2&&args[0].equals("complex"))return getListOfStringsMatchingLastWord(args,"list","status","casino","spring","chemical","tailslab","labyrinth","hydrocity","hidden","scrap","press","night","studiopolis","starlight","oilocean");
  if(args.length==3&&args[0].equals("complex"))return getListOfStringsMatchingLastWord(args,"preview","audit");
  if(args.length==2&&args[0].equals("city"))return getListOfStringsMatchingLastWord(args,UrbanJigsaw.cities());
  if(args.length==3&&args[0].equals("city"))return getListOfStringsMatchingLastWord(args,"preview");
  if(args.length==2&&args[0].equals("dungeon"))return getListOfStringsMatchingLastWord(args,"preview");
  if(args.length==2)return getListOfStringsMatchingLastWord(args,StructurePort.CATALOG);
  return Collections.emptyList();
 }
}
