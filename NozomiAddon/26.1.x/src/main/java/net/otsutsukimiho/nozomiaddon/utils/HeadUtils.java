package net.otsutsukimiho.nozomiaddon.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

public class HeadUtils {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ItemStack getSkull(String base64) {
        return createHead(base64);
    }

    private static ItemStack createHead(String base64) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        UUID uuid = UUIDUtil.createOfflinePlayerUUID("MaskIcon");
        Multimap<String, Property> multimap = ArrayListMultimap.create();
        multimap.put("textures", new Property("textures", base64));
        PropertyMap propertyMap = new PropertyMap(multimap);
        GameProfile profile = new GameProfile(uuid, "MaskIcon", propertyMap);
        stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
        return stack;
    }
}