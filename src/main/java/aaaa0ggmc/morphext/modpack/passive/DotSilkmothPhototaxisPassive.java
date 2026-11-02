package aaaa0ggmc.morphext.modpack.passive;

import aaaa0ggmc.morphext.ability.AbstractPassiveAbility;
import aaaa0ggmc.morphext.ability.MorphContext;
import mchorse.metamorph.api.abilities.IAbility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 蚕蛾趋光 (Dawn of Time Silkmoth Phototaxis Passive):
 * 还原 EntitySilkmoth.changeRotationPos 夜间飞向光照 >= 14 光源、绕桑树飘飞的习性：
 * 1. 趋光：靠近强光（光照 >= 12）时欣然加速（速度 I）并洒落喜悦鳞粉；
 * 2. 夜行：黑夜或弱光（光照 <= 7）中获得夜视与速度 I；
 * 3. 畏晒：白昼直射日光下轻微虚弱 I。
 * 小幅增减益被动，服务端权威。
 * <p>
 * 同时兼备 MorphExtension IPassiveAbility 与 Metamorph 原生 IAbility 双接口。
 */
public class DotSilkmothPhototaxisPassive extends AbstractPassiveAbility implements IAbility {

    public static final String ID = "dot_silkmoth_phototaxis";

    /** 同 tick 双通道去重（IPassiveAbility 与 IAbility 双注册，防止同 tick 双跑）。 */
    private static final Map<UUID, Integer> LAST_DRIVEN_TICK = new ConcurrentHashMap<>();

    public DotSilkmothPhototaxisPassive() {
        super(ID, "morphext.ability." + ID + ".name", "morphext.ability." + ID + ".desc", ItemStack.EMPTY);
    }

    // ==========================================
    //       MorphExtension IPassiveAbility 实现
    // ==========================================

    @Override
    public void onUpdate(EntityPlayer player, MorphContext context) {
        handlePhototaxisTick(player);
    }

    // ==========================================
    //       Metamorph 原生 IAbility 桥接实现
    // ==========================================

    @Override
    public void update(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            handlePhototaxisTick((EntityPlayer) entity);
        }
    }

    @Override
    public void onMorph(EntityLivingBase entity) {
        // 变身瞬间无需初始化
    }

    @Override
    public void onDemorph(EntityLivingBase entity) {
        // 短时效效果自然消散，无需清理
    }

    // ==========================================
    //               核心逻辑封装
    // ==========================================

    public static void handlePhototaxisTick(EntityPlayer player) {
        if (player == null || player.world == null || player.world.isRemote
                || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.ticksExisted % 20 != 0) {
            return;
        }
        Integer lastTick = LAST_DRIVEN_TICK.put(player.getUniqueID(), player.ticksExisted);
        if (lastTick != null && lastTick.intValue() == player.ticksExisted) {
            return;
        }
        if (LAST_DRIVEN_TICK.size() > 64) {
            LAST_DRIVEN_TICK.entrySet().removeIf(e -> e.getValue() < player.ticksExisted - 100);
        }
        World world = player.world;
        BlockPos pos = new BlockPos(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        int light = world.getLight(pos);

        // 与 EntitySilkmoth.changeRotationPos 相同的夜间判定
        long timeOfDay = world.getWorldTime() % 24000L;
        boolean isNight = timeOfDay > 12000L && timeOfDay < 23000L;
        boolean inSunlight = world.isDaytime() && world.canSeeSky(pos)
                && world.getLightFor(EnumSkyBlock.SKY, pos) > 10;

        if (inSunlight && !isNight) {
            // 畏晒：白昼直射日光下轻微虚弱（须先于趋光分支，否则必然 light >= 12 抢先命中而失效）
            player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0, false, true));
        } else if (light >= 12) {
            // 趋光：靠近光源欣然加速
            player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60, 0, false, true));
            if (world instanceof WorldServer) {
                ((WorldServer) world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                        player.posX, player.posY + player.height * 0.5D, player.posZ,
                        3, 0.25D, 0.25D, 0.25D, 0.0D);
            }
        } else if (isNight || light <= 7) {
            // 夜行：夜视 + 速度
            player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 220, 0, false, false));
            player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60, 0, false, true));
        }
    }
}
