package net.otsutsukimiho.nozomiaddon.utils;

import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.regex.MatchResult;

public class ItemUtils {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String ID = "id";

    public static @NotNull NbtCompound getCustomData(@NotNull ComponentHolder stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
    }

    public static @NotNull Optional<String> getItemIdOptional(@NotNull ComponentHolder stack) {
        NbtCompound customData = getCustomData(stack);
        return customData.getString(ID);
    }

    public static OptionalInt parseOptionalIntFromMatcher(MatchResult matcher, String group) {
        String s = matcher.group(group);
        if (s == null) return OptionalInt.empty();
        return OptionalInt.of(Integer.parseInt(s.replace(",", "")));
    }
}