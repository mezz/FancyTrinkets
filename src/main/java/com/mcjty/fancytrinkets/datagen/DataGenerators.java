package com.mcjty.fancytrinkets.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.mcjty.fancytrinkets.FancyTrinkets.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(event.includeClient(), new Items(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new LanguageProvider(generator, "en_us"));
        generator.addProvider(event.includeClient(), new BlockStates(generator, event.getExistingFileHelper()));
        BlockTags blockTags = new BlockTags(generator, event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ItemTags(generator, blockTags, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new Recipes(generator));
        generator.addProvider(event.includeServer(), new StandardEffects(generator));
        generator.addProvider(event.includeServer(), new StandardTrinkets(generator));
        generator.addProvider(event.includeServer(), new StandardBonusTables(generator));
    }
}
