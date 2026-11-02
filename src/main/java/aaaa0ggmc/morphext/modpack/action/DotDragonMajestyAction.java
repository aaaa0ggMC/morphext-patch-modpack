package aaaa0ggmc.morphext.modpack.action;

import aaaa0ggmc.morphext.integration.metamorph.ICooldownAction;
import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
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
 * 真龙威仪 (Dawn of Time Japanese Dragon Majesty Action):
 * 对应 EntityJapaneseDragon 的龙吟/龙啸音效人设：爆发真龙威压，
 * 16 格内敌人承受 2 点威压冲击、6 秒虚弱 II 与迟缓 I 并被震退，
 * 队友得 6 秒抗性提升庇护。
 * 冷却 22 秒。
 */
public class DotDragonMajestyAction implements IAction, ICooldownAction {

    public static final int COOLDOWN = 440;

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
        if (entity == null || entity.world == null) {
            return;
        }
        World world = entity.world;
        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 1.8F, 0.7F);
        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 1.2F, 0.5F);

        if (world.isRemote) return;

        long now = world.getTotalWorldTime();
        Long next = this.nextUseAt.get(entity.getUniqueID());
        if (next != null && next > now) {
            return;
        }
        this.nextUseAt.put(entity.getUniqueID(), now + COOLDOWN);

        double radius = 16.0D;
        AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(radius, 6.0D, radius);
        List<EntityLivingBase> nearby = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb,
                e -> e != null && e.isEntityAlive() && !e.equals(entity));

        DamageSource src = entity instanceof EntityPlayer ?
                DamageSource.causePlayerDamage((EntityPlayer) entity) :
                DamageSource.causeMobDamage(entity);

        for (EntityLivingBase target : nearby) {
            Vec3d toTarget = new Vec3d(target.posX - entity.posX, 0, target.posZ - entity.posZ).normalize();
            if (target instanceof IMob) {
                // 威压震慑：物理威压冲击 + 虚弱 + 迟缓 + 重震退
                target.attackEntityFrom(src, 2.0F);
                target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 120, 1, false, true));
                target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 0, false, true));
                target.knockBack(entity, 1.2F, -toTarget.x, -toTarget.z);
            } else if (target.isOnSameTeam(entity) || target instanceof EntityPlayer) {
                // 龙威庇护：队友抗性提升
                target.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 120, 0, false, true));
            }
        }

        if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer) world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE,
                    entity.posX, entity.posY + 1.5D, entity.posZ, 3, 0.5D, 0.3D, 0.5D, 0.0D);
            ws.spawnParticle(EnumParticleTypes.CLOUD,
                    entity.posX, entity.posY + 1.0D, entity.posZ, 40, 6.0D, 1.5D, 6.0D, 0.02D);
            ws.spawnParticle(EnumParticleTypes.CRIT,
                    entity.posX, entity.posY + 1.5D, entity.posZ, 35, 7.0D, 2.0D, 7.0D, 0.15D);
        }
    }
}
