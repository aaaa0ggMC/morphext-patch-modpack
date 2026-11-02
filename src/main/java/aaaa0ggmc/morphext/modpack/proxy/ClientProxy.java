package aaaa0ggmc.morphext.modpack.proxy;

import aaaa0ggmc.morphext.client.hand.GenericHandRegistry;
import aaaa0ggmc.morphext.client.hand.HandPreset;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        registerMiscHandPresets();
        aaaa0ggmc.morphext.variant.MorphCreativeInjector.registerProvider(new aaaa0ggmc.morphext.modpack.variant.ModpackVariantProvider());
        LOGGER.info("[morphext-modpack] ModpackVariantProvider registered into MorphCreativeInjector.");
    }

    private void registerMiscHandPresets() {
        try {
            // Biomes O' Plenty Wasp
            HandPreset waspPreset = new HandPreset("Right_Leg_Front", "Left_Leg_Front");
            GenericHandRegistry.registerModel("ModelWasp", waspPreset);
            GenericHandRegistry.registerMorph("biomesoplenty:wasp", waspPreset);
            GenericHandRegistry.registerMorph("biomesoplenty.wasp", waspPreset);
            LOGGER.info("[morphext-modpack] Registered Biomes O' Plenty Wasp hand presets");

            // Dawn of Time Japanese Dragon
            HandPreset dragonPreset = new HandPreset("HarmR", "HarmL");
            GenericHandRegistry.registerModel("ModelJapaneseDragon", dragonPreset);
            GenericHandRegistry.registerMorph("dawnoftimebuilder:japanese_dragon", dragonPreset);
            GenericHandRegistry.registerMorph("dawnoftime:japanese_dragon", dragonPreset);

            // Dawn of Time Silkmoth
            HandPreset silkmothPreset = new HandPreset("wingRight", "wingLeft");
            GenericHandRegistry.registerModel("ModelSilkmoth", silkmothPreset);
            GenericHandRegistry.registerMorph("dawnoftimebuilder:silkmoth", silkmothPreset);
            GenericHandRegistry.registerMorph("dawnoftime:silkmoth", silkmothPreset);

            LOGGER.info("[morphext-modpack] Registered Dawn of Time Dragon & Silkmoth hand presets");
        } catch (Throwable t) {
            LOGGER.warn("[morphext-modpack] Failed to register miscellaneous hand presets", t);
        }
    }
}
