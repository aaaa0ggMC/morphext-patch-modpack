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
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 龙神司水 (Dawn of Time Japanese Dragon Tide Grace Passive):
 * 日本龙/龙神在文化上是司水之神（Ryūjin）：
 * 1. 水下呼吸恒常生效；
 * 2. 破浪而行：身处水中时获得速度 I 并泛起水花气泡。
 * 小幅增益被动，服务端权威（文化演绎，非 EntityJapaneseDragon 源码行为）。
 * <p>
 * 同时兼备 MorphExtension IPassiveAbility 与 Metamorph 原生 IAbility 双接口。
 */
public class DotDragonTideGracePassive extends AbstractPassiveAbility implements IAbility {

    public static final String ID = "dot_dragon_tide_grace";

    /** 同 tick 双通道去重（IPassiveAbility 与 IAbility 双注册，防止同 tick 双跑）。 */
    private static final Map<UUID, Integer> LAST_DRIVEN_TICK = new ConcurrentHashMap<>();

    public DotDragonTideGracePassive() {
        super(ID, "morphext.ability." + ID + ".name", "morphext.ability." + ID + ".desc", ItemStack.EMPTY);
    }

    // ==========================================
    //       MorphExtension IPassiveAbility 实现
    // ==========================================

    @Override
    public void onUpdate(EntityPlayer player, MorphContext context) {
        handleTideGraceTick(player);
    }

    // ==========================================
    //       Metamorph 原生 IAbility 桥接实现
    // ==========================================

    @Override
    public void update(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            handleTideGraceTick((EntityPlayer) entity);
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

    public static void handleTideGraceTick(EntityPlayer player) {
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

        // 1. 水下呼吸恒常生效
        player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 300, 0, false, false));

        // 2. 破浪而行
        if (player.isInWater()) {
            player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60, 0, false, true));
            if (world instanceof WorldServer) {
                ((WorldServer) world).spawnParticle(EnumParticleTypes.WATER_BUBBLE,
                        player.posX, player.posY + 0.5D, player.posZ,
                        6, 0.3D, 0.3D, 0.3D, 0.02D);
            }
        }
    }
}
