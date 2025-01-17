package com.mcjty.fancytrinkets;

import com.mcjty.fancytrinkets.curios.CuriosSetup;
import com.mcjty.fancytrinkets.datapack.CustomRegistries;
import com.mcjty.fancytrinkets.keys.KeyInputHandler;
import com.mcjty.fancytrinkets.modules.effects.EffectsModule;
import com.mcjty.fancytrinkets.modules.trinkets.TrinketsModule;
import com.mcjty.fancytrinkets.modules.xpcrafter.XpCrafterModule;
import com.mcjty.fancytrinkets.setup.ClientEventHandlers;
import com.mcjty.fancytrinkets.setup.Config;
import com.mcjty.fancytrinkets.setup.ModSetup;
import com.mcjty.fancytrinkets.setup.Registration;
import mcjty.lib.modules.Modules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.mcjty.fancytrinkets.FancyTrinkets.MODID;

@Mod(MODID)
public class FancyTrinkets {

    public static final String MODID = "fancytrinkets";

    @SuppressWarnings("PublicField")
    public static ModSetup setup = new ModSetup();
    private final Modules modules = new Modules();

    public FancyTrinkets() {
        Config.register();
        setupModules();
        Registration.register();
        CustomRegistries.init();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(setup::init);
        bus.addListener(modules::init);
        bus.addListener(this::onInterModEnqueueEvent);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            bus.addListener(modules::initClient);
            bus.addListener(ClientEventHandlers::onRegisterKeyMappings);
            MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
        });
    }

    private void setupModules() {
        modules.register(new EffectsModule());
        modules.register(new TrinketsModule());
        modules.register(new XpCrafterModule());
    }

    private void onInterModEnqueueEvent(InterModEnqueueEvent event) {
        CuriosSetup.setupCurios();
    }
}
