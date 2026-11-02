package aaaa0ggmc.morphext.modpack.action;

import aaaa0ggmc.morphext.integration.metamorph.ICooldownAction;
import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.EntityLivingBase;
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
 * 地狱黄蜂毒巢蜂雾 (Biomes O' Plenty Wasp Hive Venom Action):
 * 演绎自 Nether Wasp hive 蜂巢毒液人设：向目标区域喷吐一团毒巢蜂雾，
 * 3.5 格内敌人承受 2.5 点魔法毒伤（无视护甲）、4 秒剧毒 II 与 3 秒反胃。
 * 冷却 15 秒。
 */
public class BopWaspHiveVenomAction implements IAction, ICooldownAction {

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
        if (entity == null || entity.world == null) {
            return;
        }
        World world = entity.world;
        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_WITCH_THROW, SoundCategory.HOSTILE, 1.0F, 1.2F);
        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.HOSTILE, 1.0F, 1.4F);

        if (world.isRemote) return;

        long now = world.getTotalWorldTime();
        Long next = this.nextUseAt.get(entity.getUniqueID());
        if (next != null && next > now) {
            return;
        }

        // 蜂雾落点：正前方 4 格（或正前方最近敌人处）
        Vec3d look = entity.getLookVec();
        double cx = entity.posX + look.x * 4.0D;
        double cy = entity.posY + entity.getEyeHeight() * 0.5D + look.y * 4.0D;
        double cz = entity.posZ + look.z * 4.0D;

        AxisAlignedBB search = entity.getEntityBoundingBox().grow(8.0D, 3.0D, 8.0D);
        List<EntityLivingBase> candidates = world.getEntitiesWithinAABB(EntityLivingBase.class, search,
                e -> e != null && e.isEntityAlive() && !e.equals(entity));
        double bestDot = 0.5D;
        for (EntityLivingBase t : candidates) {
            Vec3d toTarget = new Vec3d(t.posX - entity.posX, 0, t.posZ - entity.posZ).normalize();
            double dot = look.x * toTarget.x + look.z * toTarget.z;
            if (dot > bestDot) {
                bestDot = dot;
                cx = t.posX;
                cy = t.posY + t.height * 0.5D;
                cz = t.posZ;
            }
        }
        this.nextUseAt.put(entity.getUniqueID(), now + COOLDOWN);

        // 毒伤语义：改走魔法伤害来源（药水/魔法类，无视护甲），不再计入普通玩家近战伤害
        DamageSource src = DamageSource.causeIndirectMagicDamage(entity, entity);

        AxisAlignedBB cloud = new AxisAlignedBB(cx - 3.5D, cy - 2.5D, cz - 3.5D,
                cx + 3.5D, cy + 2.5D, cz + 3.5D);
        List<EntityLivingBase> victims = world.getEntitiesWithinAABB(EntityLivingBase.class, cloud,
                e -> e != null && e.isEntityAlive() && !e.equals(entity));
        for (EntityLivingBase victim : victims) {
            victim.attackEntityFrom(src, 2.5F);
            victim.addPotionEffect(new PotionEffect(MobEffects.POISON, 80, 1));
            victim.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 60, 0));
        }

        if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer) world;
            ws.spawnParticle(EnumParticleTypes.SPELL_MOB,
                    cx, cy, cz, 30, 3.0D, 1.8D, 3.0D, 0.0D);
            ws.spawnParticle(EnumParticleTypes.SLIME,
                    cx, cy, cz, 15, 2.5D, 1.5D, 2.5D, 0.0D);
        }
    }
}
