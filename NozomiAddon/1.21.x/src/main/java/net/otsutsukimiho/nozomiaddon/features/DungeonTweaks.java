package net.otsutsukimiho.nozomiaddon.features;

import com.mojang.authlib.properties.Property;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;

import net.otsutsukimiho.nozomiaddon.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

public class DungeonTweaks implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void setEnabled(boolean enabled) {
        this.enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = true;
    private List<ArmorStandEntity> armorStand = new ArrayList<>();

    public static BooleanSetting noBreakChest = new BooleanSetting("NoBreakChest", false);
    public static BooleanSetting noBreakLever = new BooleanSetting("NoBreakLever", false);
    public static BooleanSetting noBreakSkull = new BooleanSetting("NoBreakSkull", false);
    public static BooleanSetting hideSoulWeaver = new BooleanSetting("Hide SoulWeaver", false);
    @Override
    public List<Settings> getSettings() {
        return List.of(noBreakChest, noBreakLever, noBreakSkull, hideSoulWeaver);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.STRUCTURE_BLOCK);
    }

    public void initClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!DungeonTweaks.hideSoulWeaver.isEnabled()) return;
            if (!DUNGEON.inDungeon) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null) return;
            Box worldBox = new Box(-1e6, -1e6, -1e6, 1e6, 1e6, 1e6);
            armorStand = MinecraftClient.getInstance().world.getEntitiesByClass(ArmorStandEntity.class, worldBox, e -> true);
            for (ArmorStandEntity stand : armorStand) {
                ItemStack helmet = stand.getEquippedStack(EquipmentSlot.HEAD);
                if (helmet == null || helmet.isEmpty()) continue;
                ComponentMap components = helmet.getComponents();
                ProfileComponent profile = components.getOrDefault(DataComponentTypes.PROFILE, null);
                if (profile != null) {
                    if (profile.getGameProfile().properties().containsKey("textures")) {
                        for (Property property : profile.getGameProfile().properties().get("textures")) {
                            if (property == null) continue;
                            String base64Texture = property.value();
                            if (base64Texture != null && !base64Texture.isEmpty()) {
                                if (base64Texture.contains("eyJ0aW1lc3RhbXAiOjE1NTk1ODAzNjI1NTMsInByb2ZpbGVJZCI6ImU3NmYwZDlhZjc4MjQyYzM5NDY2ZDY3MjE3MzBmNDUzIiwicHJvZmlsZU5hbWUiOiJLbGxscmFoIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yZjI0ZWQ2ODc1MzA0ZmE0YTFmMGM3ODViMmNiNmE2YTcyNTYzZTlmM2UyNGVhNTVlMTgxNzg0NTIxMTlhYTY2In19fQ==")) stand.remove(Entity.RemovalReason.DISCARDED);
                            }
                            break;
                        }
                    }
                }
            }

        });
    }

}