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
 * 地狱黄蜂蜂群集结 (Biomes O' Plenty Wasp Swarm Rally Action):
 * 演绎自 "Nether Wasp hive" 蜂巢人设：振翅释放召集信息素，
 * 自身获得 6 秒速度 II 与力量 I，8 格内队友共享蜂群之力（速度 I + 力量 I），
 * 敌人被蜂群标记发光 5 秒无处可逃。
 * 冷却 20 秒。
 */
public class BopWaspSwarmRallyAction implements IAction, ICooldownAction {

    public static final int COOLDOWN = 400;

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
                SoundEvents.ENTITY_SPIDER_AMBIENT, SoundCategory.HOSTILE, 1.5F, 1.7F);
        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.8F, 1.4F);

        if (world.isRemote) return;

        long now = world.getTotalWorldTime();
        Long next = this.nextUseAt.get(entity.getUniqueID());
        if (next != null && next > now) {
            return;
        }
        this.nextUseAt.put(entity.getUniqueID(), now + COOLDOWN);

        // 自身：蜂群统帅
        entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, 120, 1, false, true));
        entity.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 120, 0, false, true));

        AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D);
        List<EntityLivingBase> nearby = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb,
                e -> e != null && e.isEntityAlive() && !e.equals(entity));
        for (EntityLivingBase target : nearby) {
            if (target instanceof IMob) {
                // 蜂群标记：发光 5 秒
                target.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 100, 0, false, true));
            } else if (target.isOnSameTeam(entity) || target instanceof EntityPlayer) {
                // 队友共享蜂群之力
                target.addPotionEffect(new PotionEffect(MobEffects.SPEED, 120, 0, false, true));
                target.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 120, 0, false, true));
            }
        }

        if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer) world;
            ws.spawnParticle(EnumParticleTypes.SPELL_MOB,
                    entity.posX, entity.posY + 1.0D, entity.posZ, 30, 1.0D, 0.8D, 1.0D, 1.0D);
            ws.spawnParticle(EnumParticleTypes.CRIT,
                    entity.posX, entity.posY + 1.2D, entity.posZ, 20, 0.8D, 0.5D, 0.8D, 0.1D);
        }
    }
}
