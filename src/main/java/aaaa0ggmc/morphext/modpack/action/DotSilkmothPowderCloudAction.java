package aaaa0ggmc.morphext.modpack.action;

import aaaa0ggmc.morphext.integration.metamorph.ICooldownAction;
import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 蚕蛾致盲磷粉云 (Dawn of Time Silkmoth Powder Cloud Action):
 * 剧烈振翅在周身 6 格弥漫扩散耀眼的致盲鳞粉迷雾，使周围所有敌怪陷入 6 秒致盲与发光失序状态，
 * 自身获得 4 秒隐身与加速安然脱险！
 * 冷却 16 秒。
 */
public class DotSilkmothPowderCloudAction implements IAction, ICooldownAction {

    public static final int COOLDOWN = 320;

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
        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.NEUTRAL, 1.5F, 1.2F);
        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_FIREWORK_BLAST, SoundCategory.NEUTRAL, 1.2F, 1.5F);

        if (world.isRemote) return;

        long now = world.getTotalWorldTime();
        Long next = this.nextUseAt.get(entity.getUniqueID());
        if (next != null && next > now) {
            return;
        }
        this.nextUseAt.put(entity.getUniqueID(), now + COOLDOWN);

        // 蚕蛾自身隐匿脱逃
        entity.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 80, 0, false, false));
        entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, 80, 1, false, false));

        double radius = 6.0D;
        AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(radius, 3.0D, radius);
        List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb,
                e -> e != null && e.isEntityAlive() && !e.equals(entity));

        for (EntityLivingBase target : targets) {
            target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 120, 0));
            target.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 120, 0));

            // 混乱敌怪仇恨
            if (target instanceof EntityLiving) {
                ((EntityLiving) target).setAttackTarget(null);
                ((EntityLiving) target).setRevengeTarget(null);
            }
        }

        if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer) world;
            ws.spawnParticle(EnumParticleTypes.SPELL_INSTANT,
                    entity.posX, entity.posY + 0.3D, entity.posZ,
                    30, 1.5D, 0.8D, 1.5D, 0.05D);
            ws.spawnParticle(EnumParticleTypes.END_ROD,
                    entity.posX, entity.posY + 0.5D, entity.posZ,
                    20, 1.2D, 0.6D, 1.2D, 0.03D);
        }
    }
}
