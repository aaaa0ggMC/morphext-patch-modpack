package aaaa0ggmc.morphext.modpack.action;

import aaaa0ggmc.morphext.integration.metamorph.ICooldownAction;
import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日本巨龙腾云驾雾 (Dawn of Time Japanese Dragon Cloud Ascend Action):
 * 召唤神圣祥云腾空飞升，获得 6 秒极速与抗性提升，在祥云环绕中御风翱翔！
 * 冷却 15 秒。
 */
public class DotDragonAscendAction implements IAction, ICooldownAction {

    public static final int COOLDOWN = 300;

    private final Map<UUID, Long> nextUseAt = new ConcurrentHashMap<>();

    @Override
    public int getCooldownTicks(EntityLivingBase entity) {
        if (entity == null || entity.world == null) {
            return COOLDOWN;
        }
        Long next = this.nextUseAt.get(entity.getUniqueID());
        if (next == null) {
            return 0;
        }
        long remaining = next - entity.world.getTotalWorldTime();
        return remaining > 0L ? (int) Math.min(remaining, COOLDOWN) : 0;
    }

    @Override
    public void execute(EntityLivingBase entity, AbstractMorph morph) {
        World world = entity.world;
        if (!world.isRemote) {
            long now = world.getTotalWorldTime();
            Long next = this.nextUseAt.get(entity.getUniqueID());
            if (next != null && next > now) {
                return;
            }
            this.nextUseAt.put(entity.getUniqueID(), now + COOLDOWN);
        }
        Vec3d look = entity.getLookVec();

        // 巨龙乘云飞升
        entity.motionX += look.x * 1.45D;
        entity.motionY = Math.max(entity.motionY, 1.25D);
        entity.motionZ += look.z * 1.45D;
        entity.fallDistance = 0.0F;
        entity.velocityChanged = true;

        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.NEUTRAL, 1.8F, 0.8F);
        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER, 0.4F, 1.8F);

        if (world.isRemote) return;

        // 赋予 6 秒疾行 II 与抗性提升 II
        entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, 120, 1, false, false));
        entity.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 120, 1, false, false));
        entity.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 120, 3, false, false));

        if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer) world;
            for (int deg = 0; deg < 360; deg += 30) {
                double rad = Math.toRadians(deg);
                double px = entity.posX + Math.cos(rad) * 2.2D;
                double pz = entity.posZ + Math.sin(rad) * 2.2D;
                ws.spawnParticle(EnumParticleTypes.CLOUD, px, entity.posY + 0.3D, pz, 3, 0.1D, 0.2D, 0.1D, 0.05D);
                ws.spawnParticle(EnumParticleTypes.DRAGON_BREATH, px, entity.posY + 0.5D, pz, 2, 0.1D, 0.1D, 0.1D, 0.02D);
            }
        }
    }
}
