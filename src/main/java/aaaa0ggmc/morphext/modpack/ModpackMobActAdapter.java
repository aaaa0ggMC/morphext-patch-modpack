package aaaa0ggmc.morphext.modpack;

import aaaa0ggmc.morphext.integration.metamorph.SafeMobActAction;
import aaaa0ggmc.morphext.modpack.profiler.ProfilerBridge;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/**
 * 整合包杂项生物专属 Mob Act 原生动作适配器。
 */
public class ModpackMobActAdapter implements SafeMobActAction.IMobActAdapter {

    @Override
    public boolean handleMobAct(EntityLivingBase player, AbstractMorph morph, EntityLiving dummy, @Nullable EntityLivingBase targetEnemy, Vec3d lookTarget) {
        if (morph == null || morph.name == null) return false;
        String name = morph.name.toLowerCase();

        long start = ProfilerBridge.begin();
        try {
            // 1. 超多生物群系 黄蜂 (Biomes O' Plenty Wasp)
            if (name.contains("wasp")) {
                String actId = player.isSneaking() ? "bop_wasp_poison_dart" : "bop_wasp_stinger_dash";
                return executeAction(actId, player, morph);
            }

            // 2. 黎明之纪 日本巨龙 (Dawn of Time Japanese Dragon)
            if (name.contains("dragon") || name.contains("japanese_dragon")) {
                String actId = player.isSneaking() ? "dot_dragon_ascend" : "dot_dragon_breath_roar";
                return executeAction(actId, player, morph);
            }

            // 3. 黎明之纪 蚕蛾 (Dawn of Time Silkmoth)
            if (name.contains("silkmoth")) {
                String actId = player.isSneaking() ? "dot_silkmoth_thread_spit" : "dot_silkmoth_powder_cloud";
                return executeAction(actId, player, morph);
            }

            return false;
        } finally {
            ProfilerBridge.end("modpack:mob_act", start);
        }
    }

    private boolean executeAction(String actionId, EntityLivingBase player, AbstractMorph morph) {
        IAction act = MorphManager.INSTANCE.actions.get(actionId);
        if (act != null) {
            act.execute(player, morph);
            return true;
        }
        return false;
    }
}
