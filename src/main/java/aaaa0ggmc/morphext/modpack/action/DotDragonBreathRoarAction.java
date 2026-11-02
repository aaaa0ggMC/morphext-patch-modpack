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
 * 日本巨龙龙神吐息与威慑龙啸 (Dawn of Time Japanese Dragon Breath & Roar Action):
 * 爆发震撼天地的真龙怒啸，喷薄浩瀚龙炎吐息，身前 10 格锥形范围内敌人承受 10 点混合伤害、
 * 被击退并附加 6 秒引燃与虚弱 II、迟缓 II 威慑诅咒！
 * 冷却 25 秒。
 */
public class DotDragonBreathRoarAction implements IAction, ICooldownAction {

    public static final int COOLDOWN = 500;

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
                SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 1.8F, 0.9F);
        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1.5F, 0.8F);

        if (world.isRemote) return;

        long now = world.getTotalWorldTime();
        Long next = this.nextUseAt.get(entity.getUniqueID());
        if (next != null && next > now) {
            return;
        }
        this.nextUseAt.put(entity.getUniqueID(), now + COOLDOWN);

        Vec3d look = entity.getLookVec();
        double range = 10.0D;
        AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(range, 3.0D, range);
        List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb,
                e -> e != null && e.isEntityAlive() && !e.equals(entity));

        DamageSource src = entity instanceof EntityPlayer ?
                DamageSource.causePlayerDamage((EntityPlayer) entity).setDamageBypassesArmor().setFireDamage() :
                DamageSource.causeMobDamage(entity).setDamageBypassesArmor().setFireDamage();

        for (EntityLivingBase target : targets) {
            Vec3d toTarget = new Vec3d(target.posX - entity.posX, 0, target.posZ - entity.posZ).normalize();
            if (look.x * toTarget.x + look.z * toTarget.z > 0.3D) {
                target.attackEntityFrom(src, 10.0F);
                target.setFire(6);
                target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 160, 1));
                target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 1));
                target.knockBack(entity, 1.2F, -toTarget.x, -toTarget.z);
            }
        }

        if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer) world;
            for (int i = 1; i <= 8; i++) {
                double spread = i * 0.35D;
                double px = entity.posX + look.x * (i * 1.2D);
                double py = entity.posY + entity.getEyeHeight() * 0.8D + look.y * (i * 1.2D);
                double pz = entity.posZ + look.z * (i * 1.2D);

                ws.spawnParticle(EnumParticleTypes.DRAGON_BREATH, px, py, pz,
                        12, spread, 0.3D, spread, 0.05D);
                ws.spawnParticle(EnumParticleTypes.FLAME, px, py, pz,
                        6, spread, 0.2D, spread, 0.03D);
            }
        }
    }
}
