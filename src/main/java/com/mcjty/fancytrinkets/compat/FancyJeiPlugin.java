package com.mcjty.fancytrinkets.compat;

import com.mcjty.fancytrinkets.FancyTrinkets;
import com.mcjty.fancytrinkets.datapack.CustomRegistries;
import com.mcjty.fancytrinkets.datapack.TrinketDescription;
import com.mcjty.fancytrinkets.modules.trinkets.TrinketsModule;
import com.mcjty.fancytrinkets.modules.xpcrafter.XpCrafterModule;
import com.mcjty.fancytrinkets.modules.xpcrafter.blocks.ExperienceCrafterBE;
import com.mcjty.fancytrinkets.modules.xpcrafter.recipe.XpRecipe;
import com.mcjty.fancytrinkets.setup.Registration;
import mcjty.lib.container.GenericContainer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

@JeiPlugin
public class FancyJeiPlugin implements IModPlugin {

    private static final ResourceLocation ID = new ResourceLocation(FancyTrinkets.MODID, "jeiplugin");

    public static final RecipeType<XpRecipe> XP_RECIPE_TYPE = RecipeType.create(FancyTrinkets.MODID, "xp_recipe", XpRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

//    @Override
//    public void registerIngredients(IModIngredientRegistration registration) {
//        registration.register();
//    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        for (Registration.TrinketInfo info : Registration.TRINKET_ITEMS.values()) {
            registration.useNbtForSubtypes(info.item().get());
            registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, info.item().get(), new XpRecipeSubtypeInterpreter());
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(XpCrafterModule.EXPERIENCE_CRAFTER.get()), XP_RECIPE_TYPE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        registration.addRecipeCategories(new XpRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<XpRecipe> recipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(XpCrafterModule.XP_RECIPE_TYPE.get());
        registration.addRecipes(XP_RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(GenericContainer.class, XpCrafterModule.CONTAINER_EXPERIENCE_CRAFTER.get(), XP_RECIPE_TYPE,
                ExperienceCrafterBE.SLOT_GRID, XpRecipe.RECIPE_DIMENSION * XpRecipe.RECIPE_DIMENSION,
                ExperienceCrafterBE.SLOT_GRID + XpRecipe.RECIPE_DIMENSION * XpRecipe.RECIPE_DIMENSION, 36);
    }
}
