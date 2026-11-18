package aaaa0ggmc.morphext.modpack;

import aaaa0ggmc.morphext.faction.FactionEnmity;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.abilities.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Metamorph Max 第三方能力的行为修正（由 morphext-patch-modpack 承载，不污染核心）。
 */
public final class MetamorphMaxOverrides {
    private static final Logger LOGGER = LogManager.getLogger("morphext-patch-modpack");

    public static void register() {
        if (!Loader.isModLoaded("metamorph") || MorphManager.INSTANCE == null
                || MorphManager.INSTANCE.abilities == null) {
            return;
        }
        overrideIfPresent("wolf_enemy", new FactionMediatedWolfEnemy());
    }

    private static void overrideIfPresent(String id, mchorse.metamorph.api.abilities.IAbility replacement) {
        if (MorphManager.INSTANCE.abilities.containsKey(id)) {
            MorphManager.INSTANCE.abilities.put(id, replacement);
            LOGGER.info("[morphext-modpack] overrode Metamorph ability '{}' with faction-mediated implementation", id);
        }
    }

    private MetamorphMaxOverrides() {
    }

    public static final class FactionMediatedWolfEnemy extends Ability {
        @Override
        public void update(EntityLivingBase target) {
            if (!(target instanceof EntityPlayer)) {
                return;
            }
            EntityPlayer player = (EntityPlayer) target;
            if (player.isCreative()) {
                return;
            }
            for (EntityWolf wolf : player.world.getEntitiesWithinAABB(EntityWolf.class,
                    new AxisAlignedBB(player.posX, player.posY, player.posZ,
                            player.posX + 1.0D, player.posY + 1.0D, player.posZ + 1.0D)
                            .grow(35.0D, 10.0D, 35.0D))) {
                if (wolf.getAttackTarget() != null) {
                    continue;
                }
                if (!FactionEnmity.isValidTargetFor(player, wolf)) {
                    continue;
                }
                wolf.setAttackTarget(player);
            }
        }
    }
}
