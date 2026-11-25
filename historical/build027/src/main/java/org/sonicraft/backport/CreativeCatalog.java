package org.sonicraft.backport;
import java.util.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.*;
public final class CreativeCatalog {
 static final Map<String,String> CATEGORIES=Content.read("creative_categories.json",Map.class);
 public static final CreativeTabs BLOCKS=new Tab("sonicraft_blocks",Items.BRICK);
 public static final CreativeTabs MISC=new Tab("sonicraft_misc",Items.EMERALD);
 public static final CreativeTabs CHARACTERS=new Tab("sonicraft_characters",Items.SPAWN_EGG);
 public static final CreativeTabs VEHICLES=new Tab("sonicraft_vehicles",Items.MINECART);
 public static final CreativeTabs OPERATORS=new Tab("sonicraft_operators_tab",Items.REDSTONE);
 static CreativeTabs forId(String id){String category=CATEGORIES.get(id);if("sonicraft_misc".equals(category))return MISC;if("sonicraft_characters".equals(category))return CHARACTERS;if("sonicraft_vehicles".equals(category))return VEHICLES;if("sonicraft_operators_tab".equals(category))return OPERATORS;return BLOCKS;}
 static final class Tab extends CreativeTabs {
  final Item icon;Tab(String id,Item icon){super(id);this.icon=icon;}
  @Override public ItemStack getTabIconItem(){return new ItemStack(icon);}
  @Override public void displayAllRelevantItems(NonNullList<ItemStack> out){super.displayAllRelevantItems(out);if(this==CHARACTERS)for(Characters.Definition d:Characters.DEFINITIONS){ItemStack egg=new ItemStack(Items.SPAWN_EGG);ItemMonsterPlacer.applyEntityIdToItemStack(egg,new ResourceLocation("sonicraft",d.id));out.add(egg);}if(this==CHARACTERS)for(String id:Badniks.IDS){ItemStack egg=new ItemStack(Items.SPAWN_EGG);ItemMonsterPlacer.applyEntityIdToItemStack(egg,new ResourceLocation("sonicraft",id));out.add(egg);}}
 }
}
