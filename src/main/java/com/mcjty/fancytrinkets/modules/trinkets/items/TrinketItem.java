package com.mcjty.fancytrinkets.modules.trinkets.items;

import com.mcjty.fancytrinkets.FancyTrinkets;
import com.mcjty.fancytrinkets.datapack.TrinketDescription;
import com.mcjty.fancytrinkets.keys.KeyBindings;
import com.mcjty.fancytrinkets.modules.effects.EffectInstance;
import com.mcjty.fancytrinkets.modules.effects.IEffect;
import com.mcjty.fancytrinkets.modules.trinkets.ITrinketItem;
import com.mcjty.fancytrinkets.modules.trinkets.TrinketInstance;
import com.mcjty.fancytrinkets.modules.trinkets.TrinketsModule;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.ComponentFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TrinketItem extends Item implements ITooltipSettings, ITrinketItem {

    public static final String MESSAGE_FANCYTRINKETS_SHIFTMESSAGE = "message.fancytrinkets.shiftmessage";
    public static final String MESSAGE_FANCYTRINKETS_BONUS = "message.fancytrinkets.bonus";

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

    public static void addEffects(ItemStack stack, List<ResourceLocation> effects) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = new ListTag();
        for (ResourceLocation location : effects) {
            list.add(StringTag.valueOf(location.toString()));
        }
        tag.put("effects", list);
    }

    public static List<ResourceLocation> getEffects(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            ListTag effects = tag.getList("effects", Tag.TAG_STRING);
            return effects.stream().map(s -> new ResourceLocation(s.getAsString())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static ItemStack createTrinketStack(String id) {
        return createTrinketStack(new ResourceLocation(FancyTrinkets.MODID, id));
    }

    public static ItemStack createTrinketStack(ResourceLocation id) {
        TrinketsModule.TrinketInfo info = TrinketsModule.TRINKETS.get(id);
        if (info == null) {
            throw new RuntimeException("Cannot find trinket '" + id.toString() + "'!");
        }
        ResourceLocation itemId = info.trinketDescription().item();
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            throw new RuntimeException("Cannot find item for trinket '" + id.toString() + "'!");
        }
        ItemStack result = new ItemStack(item);
        result.getOrCreateTag().putString("id", id.toString());
        return result;
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
                    if (!effectInstance.hidden()) {
                        MutableComponent translatable = ComponentFactory.translatable("effect." + effectInstance.id().getNamespace() + "." + effectInstance.id().getPath());
                        String toggle = effect.getToggle();
                        ChatFormatting color = ChatFormatting.BLUE;
                        ChatFormatting style = ChatFormatting.BLUE;
                        if (toggle != null) {
                            if (!toggles.contains(toggle)) {
                                color = ChatFormatting.GRAY;
                                style  = ChatFormatting.STRIKETHROUGH;
                            }
                        }

                        Integer hotkey = effect.getHotkey();
                        if (hotkey != null) {
                            Component key = KeyBindings.toggles[hotkey - 1].getKey().getDisplayName();
                            Component key2 = ComponentFactory.literal(" [Key ").withStyle(ChatFormatting.YELLOW).append(key).append("]");
                            list.add(ComponentFactory.literal("    ").append(translatable.withStyle(color).withStyle(style).append(key2)));
                        } else {
                            list.add(ComponentFactory.literal("    ").append(translatable.withStyle(color).withStyle(style)));
                        }
                    }
                }
                List<ResourceLocation> effects = getEffects(stack);
                if (!effects.isEmpty()) {
                    list.add(ComponentFactory.translatable(MESSAGE_FANCYTRINKETS_BONUS).withStyle(ChatFormatting.AQUA));
                    for (ResourceLocation effect : effects) {
                        MutableComponent translatable = ComponentFactory.translatable("effect." + effect.getNamespace() + "." + effect.getPath());
                        list.add(ComponentFactory.literal("    ").append(translatable).withStyle(ChatFormatting.GREEN));
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
