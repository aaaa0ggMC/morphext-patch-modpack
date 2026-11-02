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
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 蚕蛾作茧自护 (Dawn of Time Silkmoth Cocoon Action):
 * 对应 EntitySilkmoth 的吐丝人设（thread spit 的防御面）：
 * 吐丝结茧裹身 4 秒，获得抗性提升 II 与再生 II，代价是迟缓 II 与挖掘疲劳（茧中不动）。
 * 冷却 20 秒。
 */
public class DotSilkmothCocoonAction implements IAction, ICooldownAction {

    public static final int COOLDOWN = 400;
    public static final int COCOON_TICKS = 80;

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
                SoundEvents.BLOCK_CLOTH_PLACE, SoundCategory.PLAYERS, 1.0F, 1.2F);
        world.playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.PLAYERS, 0.8F, 1.5F);

        if (world.isRemote) return;

        long now = world.getTotalWorldTime();
        Long next = this.nextUseAt.get(entity.getUniqueID());
        if (next != null && next > now) {
            return;
        }
        this.nextUseAt.put(entity.getUniqueID(), now + COOLDOWN);

        // 丝茧庇护：抗性 + 再生，代价是茧中迟缓
        entity.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, COCOON_TICKS, 1, false, true));
        entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, COCOON_TICKS, 1, false, true));
        entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, COCOON_TICKS, 1, false, true));
        entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, COCOON_TICKS, 0, false, true));

        if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer) world;
            ws.spawnParticle(EnumParticleTypes.CLOUD,
                    entity.posX, entity.posY + 0.8D, entity.posZ, 25, 0.5D, 0.5D, 0.5D, 0.01D);
            ws.spawnParticle(EnumParticleTypes.SPELL_MOB,
                    entity.posX, entity.posY + 0.8D, entity.posZ, 20, 0.5D, 0.5D, 0.5D, 0.85D);
        }
    }
}
