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
 * 蚕蛾蚕丝喷吐 (Dawn of Time Silkmoth Thread Spit Action):
 * 喷吐出极具黏性的柔韧蚕丝，缠绕束缚正前方 8 格内目标，造成 4 点伤害并施加 6 秒迟缓 IV 与虚弱 II 强力禁锢！
 * 冷却 12 秒。
 */
public class DotSilkmothThreadSpitAction implements IAction, ICooldownAction {

    public static final int COOLDOWN = 240;

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
                SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.NEUTRAL, 1.4F, 1.3F);
        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_SPIDER_STEP, SoundCategory.NEUTRAL, 1.2F, 1.6F);

        if (world.isRemote) return;

        long now = world.getTotalWorldTime();
        Long next = this.nextUseAt.get(entity.getUniqueID());
        if (next != null && next > now) {
            return;
        }
        this.nextUseAt.put(entity.getUniqueID(), now + COOLDOWN);

        Vec3d look = entity.getLookVec();
        double range = 8.0D;
        AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(range, 2.0D, range);
        List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb,
                e -> e != null && e.isEntityAlive() && !e.equals(entity));

        DamageSource src = entity instanceof EntityPlayer ?
                DamageSource.causePlayerDamage((EntityPlayer) entity) :
                DamageSource.causeMobDamage(entity);

        for (EntityLivingBase target : targets) {
            Vec3d toTarget = new Vec3d(target.posX - entity.posX, 0, target.posZ - entity.posZ).normalize();
            if (look.x * toTarget.x + look.z * toTarget.z > 0.35D) {
                target.attackEntityFrom(src, 4.0F);
                target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 120, 3));
                target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 120, 1));
            }
        }

        if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer) world;
            for (int i = 1; i <= 7; i++) {
                double px = entity.posX + look.x * (i * 1.1D);
                double py = entity.posY + entity.getEyeHeight() * 0.6D + look.y * (i * 1.1D);
                double pz = entity.posZ + look.z * (i * 1.1D);
                ws.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, px, py, pz, 5, 0.15D, 0.15D, 0.15D, 0.02D);
                ws.spawnParticle(EnumParticleTypes.CLOUD, px, py, pz, 2, 0.1D, 0.1D, 0.1D, 0.01D);
            }
        }
    }
}
