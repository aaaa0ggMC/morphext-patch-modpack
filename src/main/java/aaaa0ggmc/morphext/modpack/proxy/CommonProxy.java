package aaaa0ggmc.morphext.modpack.proxy;

import aaaa0ggmc.morphext.integration.metamorph.SafeMobActAction;
import aaaa0ggmc.morphext.modpack.ModpackMobActAdapter;
import aaaa0ggmc.morphext.modpack.action.*;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.abilities.IAction;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonProxy {
    protected static final Logger LOGGER = LogManager.getLogger("morphext-patch-modpack");

    public void preInit(FMLPreInitializationEvent event) {
        registerActions();
        registerPassives();
    }

    public void init(FMLInitializationEvent event) {
        SafeMobActAction.registerAdapter(new ModpackMobActAdapter());
        LOGGER.info("[morphext-modpack] ModpackMobActAdapter registered successfully.");
    }

    public void postInit(FMLPostInitializationEvent event) {}

    private void registerActions() {
        register("bop_wasp_stinger_dash", new BopWaspStingerDashAction());
        register("bop_wasp_poison_dart", new BopWaspPoisonDartAction());
        register("bop_wasp_swarm_rally", new BopWaspSwarmRallyAction());
        register("bop_wasp_hive_venom", new BopWaspHiveVenomAction());
        register("dot_dragon_ascend", new DotDragonAscendAction());
        register("dot_dragon_breath_roar", new DotDragonBreathRoarAction());
        register("dot_dragon_majesty", new DotDragonMajestyAction());
        register("dot_silkmoth_thread_spit", new DotSilkmothThreadSpitAction());
        register("dot_silkmoth_powder_cloud", new DotSilkmothPowderCloudAction());
        register("dot_silkmoth_cocoon", new DotSilkmothCocoonAction());

        LOGGER.info("[morphext-modpack] Registered 10 modpack mob actions into MorphManager");
    }

    private void registerPassives() {
        try {
            // 被动能力注册 (兼容 MorphExtension AbilityRegistry 与 Metamorph 注册表)
            aaaa0ggmc.morphext.modpack.passive.DotDragonTideGracePassive tideGrace =
                    new aaaa0ggmc.morphext.modpack.passive.DotDragonTideGracePassive();
            aaaa0ggmc.morphext.ability.AbilityRegistry.register(tideGrace);
            MorphManager.INSTANCE.abilities.put(tideGrace.getId(), tideGrace);

            aaaa0ggmc.morphext.modpack.passive.DotSilkmothPhototaxisPassive phototaxis =
                    new aaaa0ggmc.morphext.modpack.passive.DotSilkmothPhototaxisPassive();
            aaaa0ggmc.morphext.ability.AbilityRegistry.register(phototaxis);
            MorphManager.INSTANCE.abilities.put(phototaxis.getId(), phototaxis);

            LOGGER.info("[morphext-modpack] Registered modpack passive abilities into AbilityRegistry.");
        } catch (Throwable t) {
            LOGGER.warn("[morphext-modpack] Failed to register passive abilities", t);
        }
    }

    private void register(String id, IAction action) {
        MorphManager.INSTANCE.actions.put(id, action);
        MorphManager.INSTANCE.actions.put("morphext:" + id, action);
    }
}
