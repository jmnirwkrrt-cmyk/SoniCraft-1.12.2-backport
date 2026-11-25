package org.sonicraft.backport;
import java.util.*;import net.minecraft.block.*;import net.minecraft.block.state.*;import net.minecraft.entity.*;import net.minecraft.entity.player.*;import net.minecraft.item.*;import net.minecraft.init.SoundEvents;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.world.*;import net.minecraftforge.event.RegistryEvent;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.fml.common.eventhandler.*;import net.minecraftforge.oredict.OreDictionary;
@Mod.EventBusSubscriber(modid="sonicraft")
public final class CharacterMonitor43 extends StructureFacadeBlocks.Horizontal {
 final String character;
 public CharacterMonitor43(Content.BlockDef d){super(d);String s=d.id.substring(9);character=s.equals("amy")?"amy_rose":s.equals("big")?"big_the_cat":s.equals("blaze")?"blaze_the_cat":s;setHardness(1);setLightOpacity(0);}
 public boolean isOpaqueCube(IBlockState s){return false;}public boolean isFullCube(IBlockState s){return false;}
 public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
 public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return new AxisAlignedBB(0,0,0,1,.9,1);}
 public AxisAlignedBB getCollisionBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return NULL_AABB;}
 public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity e){if(e instanceof EntityPlayer)open(w,p,s);}
 public void onBlockClicked(World w,BlockPos p,EntityPlayer player){open(w,p,w.getBlockState(p));}
 public boolean onBlockActivated(World w,BlockPos p,IBlockState s,EntityPlayer player,EnumHand hand,EnumFacing f,float x,float y,float z){open(w,p,s);return true;}
 public boolean removedByPlayer(IBlockState s,World w,BlockPos p,EntityPlayer player,boolean harvest){if(!w.isRemote)open(w,p,s);return false;}
 public void getDrops(NonNullList<ItemStack> drops,IBlockAccess w,BlockPos p,IBlockState s,int fortune){}
 void open(World w,BlockPos p,IBlockState s){
  if(w.isRemote||w.getBlockState(p).getBlock()!=this)return;
  Entity e=EntityList.createEntityByIDFromName(new ResourceLocation("sonicraft",character),w);if(!(e instanceof LegacyCharacter))return;
  Block broken=Content.BLOCKS.get("item_box_broken");if(broken==null)return;
  if(!w.setBlockState(p,broken.getStateFromMeta(getMetaFromState(s)),3))return;
  e.setLocationAndAngles(p.getX()+.5,p.getY(),p.getZ()+.5,w.rand.nextFloat()*360,0);
  ((LegacyCharacter)e).enablePersistence();if(!w.spawnEntity(e)){w.setBlockState(p,s,3);return;}
  w.playSound(null,p,SoundEvents.BLOCK_GLASS_BREAK,SoundCategory.BLOCKS,.8f,1);
  if(w instanceof WorldServer)((WorldServer)w).spawnParticle(EnumParticleTypes.SMOKE_NORMAL,p.getX()+.5,p.getY()+.5,p.getZ()+.5,10,.3,.3,.3,.02);
 }
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void items(RegistryEvent.Register<Item> event){
  Item ring=new Item().setRegistryName("sonicraft","red_star_ring").setUnlocalizedName("sonicraft.red_star_ring");
  Item base=Item.getItemFromBlock(Content.BLOCKS.get("item_box"));if(base!=null)ring.setCreativeTab(base.getCreativeTab());event.getRegistry().register(ring);
  for(Block block:Content.BLOCKS.values())if(block instanceof Leaves43)OreDictionary.registerOre("treeLeaves",new ItemStack(block));
 }
}
