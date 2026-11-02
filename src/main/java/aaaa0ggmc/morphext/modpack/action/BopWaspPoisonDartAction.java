package aaaa0ggmc.morphext.modpack.action;

import aaaa0ggmc.morphext.integration.metamorph.ICooldownAction;
import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 黄蜂剧毒尾针射击 (Biomes O' Plenty Wasp Poison Dart Action):
 * 朝准星瞄准方向精准射出一枚淬毒尾针飞弹，命中造成 5 点直接伤害并施加 5 秒剧毒 II 与迟缓 II！
 * 冷却 12 秒。
 */
public class BopWaspPoisonDartAction implements IAction, ICooldownAction {

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
        entity.swingArm(EnumHand.MAIN_HAND);

        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.2F, 1.6F);

        if (world.isRemote) return;

        long now = world.getTotalWorldTime();
        Long next = this.nextUseAt.get(entity.getUniqueID());
        if (next != null && next > now) {
            return;
        }
        this.nextUseAt.put(entity.getUniqueID(), now + COOLDOWN);

        Vec3d look = entity.getLookVec();
        double spawnX = entity.posX + look.x * 1.2D;
        double spawnY = entity.posY + entity.getEyeHeight() * 0.7D + look.y * 1.2D;
        double spawnZ = entity.posZ + look.z * 1.2D;

        EntitySnowball dart = new EntitySnowball(world, spawnX, spawnY, spawnZ) {
            @Override
            protected void onImpact(RayTraceResult result) {
                if (result.entityHit instanceof EntityLivingBase && !result.entityHit.equals(entity)) {
                    EntityLivingBase hit = (EntityLivingBase) result.entityHit;
                    DamageSource src = entity instanceof EntityPlayer ?
                            DamageSource.causePlayerDamage((EntityPlayer) entity) :
                            DamageSource.causeMobDamage(entity);
                    hit.attackEntityFrom(src, 5.0F);
                    hit.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 1));
                    hit.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
                }

                if (!this.world.isRemote && this.world instanceof WorldServer) {
                    WorldServer ws = (WorldServer) this.world;
                    ws.spawnParticle(EnumParticleTypes.SPELL_MOB,
                            this.posX, this.posY, this.posZ,
                            10, 0.2D, 0.2D, 0.2D, 1.0D);
                    ws.spawnParticle(EnumParticleTypes.SLIME,
                            this.posX, this.posY, this.posZ,
                            6, 0.15D, 0.15D, 0.15D, 0.05D);
                }
                this.setDead();
            }
        };

        dart.shoot(look.x, look.y + 0.03D, look.z, 1.8F, 1.0F);
        world.spawnEntity(dart);
    }
}
