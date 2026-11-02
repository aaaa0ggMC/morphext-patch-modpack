package aaaa0ggmc.morphext.modpack;

import aaaa0ggmc.morphext.modpack.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = MorphExtModpack.MODID,
        name = MorphExtModpack.NAME,
        version = MorphExtModpack.VERSION,
        dependencies = "required-after:aaaa0ggmc_morph_extension;after:biomesoplenty;after:dawnoftimebuilder;after:metamorph",
        acceptableRemoteVersions = "*"
)
public class MorphExtModpack {
    public static final String MODID = "morphext_patch_modpack";
    public static final String NAME = "Morph Extension - Modpack Patch";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.Instance(MODID)
    public static MorphExtModpack INSTANCE;

    @SidedProxy(
            clientSide = "aaaa0ggmc.morphext.modpack.proxy.ClientProxy",
            serverSide = "aaaa0ggmc.morphext.modpack.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("[morphext-modpack] Initializing {} v{}...", NAME, VERSION);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        LOGGER.info("[morphext-modpack] Initialized miscellaneous modpack mob adaptations.");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
