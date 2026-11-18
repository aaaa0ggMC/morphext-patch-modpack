package aaaa0ggmc.morphext.modpack;

import aaaa0ggmc.morphext.modpack.passive.DotDragonTideGracePassive;
import aaaa0ggmc.morphext.modpack.passive.DotSilkmothPhototaxisPassive;
import aaaa0ggmc.morphext.spi.IExternalCooldownProvider;
import aaaa0ggmc.morphext.spi.IMorphextPlugin;
import aaaa0ggmc.morphext.spi.MorphextPluginContext;
import mchorse.metamorph.api.MorphManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * 整合包适配补丁 SPI 插件入口（ServiceLoader 自动发现）。
 */
public final class ModpackMorphextPlugin implements IMorphextPlugin {

    @Override
    public String id() {
        return "morphext_patch_modpack";
    }

    @Override
    public void onPreInit(MorphextPluginContext ctx) {
        DotDragonTideGracePassive tideGrace = new DotDragonTideGracePassive();
        ctx.registerAbility(tideGrace);
        MorphManager.INSTANCE.abilities.put(tideGrace.getId(), tideGrace);

        DotSilkmothPhototaxisPassive phototaxis = new DotSilkmothPhototaxisPassive();
        ctx.registerAbility(phototaxis);
        MorphManager.INSTANCE.abilities.put(phototaxis.getId(), phototaxis);

        ctx.registerActionClassifier(action ->
                action != null && "jones.metamorphmax.metamorph.actions.AITarget".equals(action.getClass().getName()));
        ctx.registerExternalCooldownProvider(new IExternalCooldownProvider() {
            private Potion potion;
            private boolean lookedUp = false;

            @Override
            public int getExternalCooldownTicks(EntityPlayer player) {
                if (!lookedUp) {
                    lookedUp = true;
                    try {
                        Class<?> clazz = Class.forName("jones.metamorphmax.init.PotionInit");
                        potion = (Potion) clazz.getField("MOB_ABILITY_COOLDOWN").get(null);
                    } catch (Throwable ignored) {
                    }
                }
                if (potion == null || player == null) {
                    return 0;
                }
                PotionEffect eff = player.getActivePotionEffect(potion);
                return eff == null ? 0 : eff.getDuration();
            }
        });
    }

    @Override
    public void onInit(MorphextPluginContext ctx) {
        ctx.registerMobActAdapter(new ModpackMobActAdapter());
    }
}
