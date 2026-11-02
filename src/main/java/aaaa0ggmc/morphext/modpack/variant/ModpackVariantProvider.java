package aaaa0ggmc.morphext.modpack.variant;

import aaaa0ggmc.morphext.variant.CreativeVariantDef;
import aaaa0ggmc.morphext.variant.ICreativeVariantProvider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * 整合包杂项模组（Biomes O' Plenty、Dawn of Time 等）形态变体提供者。
 */
public class ModpackVariantProvider implements ICreativeVariantProvider {

    @Override
    public List<CreativeVariantDef> getVariants(World world) {
        List<CreativeVariantDef> list = new ArrayList<>();

        // 1. Biomes O' Plenty 黄蜂
        NBTTagCompound queenWasp = new NBTTagCompound();
        queenWasp.setFloat("morphext_scale", 1.75F);
        queenWasp.setString("morphext_variant", "queen_wasp");
        list.add(new CreativeVariantDef("biomesoplenty:wasp", "黄蜂 (蜂后巨蜂) - Giant Queen Wasp", "biomesoplenty", queenWasp, 1.75F));

        // 2. Dawn of Time 蚕蛾幼体
        NBTTagCompound silkworm = new NBTTagCompound();
        silkworm.setFloat("morphext_scale", 0.5F);
        silkworm.setString("morphext_variant", "silkworm_larva");
        list.add(new CreativeVariantDef("dawnoftimebuilder:silkmoth", "蚕蛾 (幼蚕) - Silkworm Larva", "dawnoftimebuilder", silkworm, 0.5F));

        return list;
    }
}
