package com.mcjty.fancytrinkets.modules.trinkets.items;

import com.mcjty.fancytrinkets.FancyTrinkets;
import com.mcjty.fancytrinkets.datapack.TrinketDescription;
import com.mcjty.fancytrinkets.keys.KeyBindings;
import com.mcjty.fancytrinkets.modules.effects.EffectInstance;
import com.mcjty.fancytrinkets.modules.effects.IEffect;
import com.mcjty.fancytrinkets.modules.trinkets.ITrinketItem;
import com.mcjty.fancytrinkets.modules.trinkets.TrinketInstance;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.ComponentFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class TrinketItem extends Item implements ITooltipSettings, ITrinketItem {

    public static final String MESSAGE_FANCYTRINKETS_SHIFTMESSAGE = "message.fancytrinkets.shiftmessage";
    public static final String MESSAGE_EFFECT_HEADER = "effect.fancytrinkets.header";

    private final Map<ResourceLocation, TrinketInstance> trinkets = new HashMap<>();

    // Synced from server, all active toggles
    public static Set<String> toggles = new HashSet<>();

    public TrinketItem() {
        super(new Properties()
                .stacksTo(1)
                .tab(FancyTrinkets.setup.getTab()));
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (allowedIn(tab)) {
            for (TrinketInstance trinket : trinkets.values()) {
                ItemStack stack = new ItemStack(this);
                toNBT(stack, trinket);
                list.add(stack);
            }
        }
    }

    public static void toNBT(ItemStack stack, TrinketInstance trinket) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("id", trinket.id().toString());
    }

    public static ResourceLocation getTrinketId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("id")) {
            return new ResourceLocation(tag.getString("id"));
        } else {
            return null;
        }
    }

    @Override
    public void registerTrinketInstance(ServerLevel level, ResourceLocation id, TrinketDescription description) {
        trinkets.put(id, description.build(id, level));
    }

    @Override
    public void forAllEffects(ItemStack stack, Consumer<IEffect> consumer) {
        ResourceLocation trinketId = getTrinketId(stack);
        if (trinketId != null) {
            TrinketInstance instance = trinkets.get(trinketId);
            if (instance != null) {
                for (EffectInstance effect : instance.effects()) {
                    consumer.accept(effect.effect());
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> list, TooltipFlag flags) {
        ResourceLocation trinketId = getTrinketId(stack);
        if (trinketId != null) {
            TrinketInstance instance = trinkets.get(trinketId);
            if (instance != null) {
                MutableComponent name = ComponentFactory.translatable(instance.nameKey()).withStyle(ChatFormatting.AQUA);
                if (list.isEmpty()) {
                    list.add(name);
                } else {
                    list.set(0, name);
                }
                list.add(ComponentFactory.translatable(instance.descriptionKey()));
                for (EffectInstance effectInstance : instance.effects()) {
                    IEffect effect  = effectInstance.effect();
                    MutableComponent translatable = ComponentFactory.translatable("effect." + effectInstance.id().getNamespace() + "." + effectInstance.id().getPath());
                    String toggle = effect.getToggle();
                    ChatFormatting color = ChatFormatting.WHITE;
                    ChatFormatting headerColor = ChatFormatting.AQUA;
                    if (toggle != null) {
                        if (!toggles.contains(toggle)) {
                            color = ChatFormatting.GRAY;
                            headerColor = ChatFormatting.GRAY;
                        }
                    }

                    Integer hotkey = effect.getHotkey();
                    if (hotkey != null) {
                        Component key = KeyBindings.toggles[hotkey - 1].getKey().getDisplayName();
                        Component key2 = ComponentFactory.literal(" [Key ").withStyle(ChatFormatting.YELLOW).append(key).append("]");
                        list.add(ComponentFactory.translatable(MESSAGE_EFFECT_HEADER).withStyle(headerColor)
                                .append(translatable.withStyle(color)).append(key2));
                    } else {
                        list.add(ComponentFactory.translatable(MESSAGE_EFFECT_HEADER).withStyle(headerColor)
                                .append(translatable.withStyle(color)));
                    }
                }
            }
        }
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new CuriosCapabilityProvider(stack, this);
    }
}
