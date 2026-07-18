package net.otsutsukimiho.nozomiaddon.features;

import com.mojang.authlib.properties.Property;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.phys.AABB;
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
    private List<ArmorStand> armorStand = new ArrayList<>();

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

            Minecraft client = Minecraft.getInstance();
            if (client.level == null) return;
            AABB worldBox = new AABB(-1e6, -1e6, -1e6, 1e6, 1e6, 1e6);
            armorStand = Minecraft.getInstance().level.getEntitiesOfClass(ArmorStand.class, worldBox, e -> true);
            for (ArmorStand stand : armorStand) {
                ItemStack helmet = stand.getItemBySlot(EquipmentSlot.HEAD);
                if (helmet == null || helmet.isEmpty()) continue;
                DataComponentMap components = helmet.getComponents();
                ResolvableProfile profile = components.getOrDefault(DataComponents.PROFILE, null);
                if (profile != null) {
                    if (profile.partialProfile().properties().containsKey("textures")) {
                        for (Property property : profile.partialProfile().properties().get("textures")) {
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