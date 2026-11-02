package aaaa0ggmc.morphext.modpack.action;

import aaaa0ggmc.morphext.integration.metamorph.ICooldownAction;
import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 黄蜂剧毒尾针俯冲突刺 (Biomes O' Plenty Wasp Stinger Dash Action):
 * 振翅极速俯冲突刺 6 格，以致命毒针贯穿沿途目标，造成 7.5 点物理伤害并施加 6 秒剧毒 II 与反胃效果！
 * 冷却 14 秒。
 */
public class BopWaspStingerDashAction implements IAction, ICooldownAction {

    public static final int COOLDOWN = 280;

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

        // 俯冲突刺高速位移
        entity.motionX += look.x * 1.55D;
        entity.motionY += look.y * 1.1D;
        entity.motionZ += look.z * 1.55D;
        entity.fallDistance = 0.0F;
        entity.velocityChanged = true;

        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_SPIDER_STEP, SoundCategory.HOSTILE, 1.6F, 1.5F);
        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.2F, 1.4F);

        if (world.isRemote) return;

        AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(3.0D, 1.5D, 3.0D);
        List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb,
                e -> e != null && e.isEntityAlive() && !e.equals(entity));

        DamageSource src = entity instanceof EntityPlayer ?
                DamageSource.causePlayerDamage((EntityPlayer) entity) :
                DamageSource.causeMobDamage(entity);

        for (EntityLivingBase target : targets) {
            Vec3d toTarget = new Vec3d(target.posX - entity.posX, 0, target.posZ - entity.posZ).normalize();
            if (look.x * toTarget.x + look.z * toTarget.z > 0.25D) {
                target.attackEntityFrom(src, 7.5F);
                target.addPotionEffect(new PotionEffect(MobEffects.POISON, 120, 1));
                target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 80, 0));
                target.knockBack(entity, 0.6F, -toTarget.x, -toTarget.z);
            }
        }

        if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer) world;
            for (int i = 1; i <= 6; i++) {
                double px = entity.posX + look.x * (i * 0.8D);
                double py = entity.posY + entity.getEyeHeight() * 0.5D + look.y * (i * 0.8D);
                double pz = entity.posZ + look.z * (i * 0.8D);
                ws.spawnParticle(EnumParticleTypes.SPELL_MOB, px, py, pz, 3, 0.15D, 0.8D, 0.15D, 1.0D);
                ws.spawnParticle(EnumParticleTypes.CRIT, px, py, pz, 2, 0.1D, 0.1D, 0.1D, 0.05D);
            }
        }
    }
}
