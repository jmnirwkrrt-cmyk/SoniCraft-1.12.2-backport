package org.sonicraft.backport.core;
import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"org.sonicraft.backport.core."})
public final class CuddlePlugin implements IFMLLoadingPlugin {
 public static boolean srg=true;
 public String[] getASMTransformerClass(){return new String[]{"org.sonicraft.backport.core.CuddleTransformer"};}
 public String getModContainerClass(){return null;} public String getSetupClass(){return null;}
 public void injectData(Map<String,Object> data){srg=Boolean.TRUE.equals(data.get("runtimeDeobfuscationEnabled"));}
 public String getAccessTransformerClass(){return null;}
}
