package net.otsutsukimiho.nozomiaddon.utils;

import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.regex.MatchResult;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;

public class ItemUtils {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String ID = "id";

    public static @NotNull CompoundTag getCustomData(@NotNull DataComponentHolder stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static @NotNull Optional<String> getItemIdOptional(@NotNull DataComponentHolder stack) {
        CompoundTag customData = getCustomData(stack);
        return customData.getString(ID);
    }

    public static OptionalInt parseOptionalIntFromMatcher(MatchResult matcher, String group) {
        String s = matcher.group(group);
        if (s == null) return OptionalInt.empty();
        return OptionalInt.of(Integer.parseInt(s.replace(",", "")));
    }
}