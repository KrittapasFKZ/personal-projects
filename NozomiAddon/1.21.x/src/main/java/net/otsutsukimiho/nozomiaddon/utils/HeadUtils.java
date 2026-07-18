package net.otsutsukimiho.nozomiaddon.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Uuids;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HeadUtils {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ItemStack getSkull(String base64) {
        return createHead(base64);
    }

    private static ItemStack createHead(String base64) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        UUID uuid = Uuids.getOfflinePlayerUuid("MaskIcon");
        Multimap<String, Property> multimap = ArrayListMultimap.create();
        multimap.put("textures", new Property("textures", base64));
        PropertyMap propertyMap = new PropertyMap(multimap);
        GameProfile profile = new GameProfile(uuid, "MaskIcon", propertyMap);
        stack.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(profile));
        return stack;
    }
}