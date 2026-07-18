package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import java.util.List;

public class BlockSkyBlockMenu implements FeatureManager.Feature {
    private boolean enabled = false;

    public static BooleanSetting onlyInDungeon = new BooleanSetting("Only In Dungeon", false);
    public static BooleanSetting blockNetherStar = new BooleanSetting("Block Nether Star", false);
    public static BooleanSetting blockDungeonMap = new BooleanSetting("Block Dungeon Map", false);
    public static BooleanSetting overlayMessage = new BooleanSetting("Overlay Message", true);
    public static BooleanSetting playSound = new BooleanSetting("Play Sound", true);

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<Settings> getSettings() {
        return List.of(onlyInDungeon, blockNetherStar, blockDungeonMap, overlayMessage, playSound);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.NETHER_STAR);
    }

    @Override
    public void initClient() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!enabled) return;
            if (onlyInDungeon.isEnabled() && !DUNGEON.inDungeon) return;
            if (client.player == null) return;

            if (client.player.getInventory().getSelectedSlot() == 8) {

                ItemStack heldItem = client.player.getInventory().getItem(8);
                boolean isNetherStar = heldItem.getItem() == Items.NETHER_STAR;
                boolean isMap = heldItem.getItem() == Items.FILLED_MAP || heldItem.getItem() == Items.MAP;
                boolean shouldBlock = (isNetherStar && blockNetherStar.isEnabled()) || (isMap && blockDungeonMap.isEnabled());

                if (shouldBlock) {
                    boolean blocked = false;

                    while (client.options.keyUse.consumeClick()) {
                        blocked = true;
                    }

                    while (client.options.keyAttack.consumeClick()) {
                        blocked = true;
                    }

                    client.options.keyUse.setDown(false);
                    client.options.keyAttack.setDown(false);

                    if (blocked) {
                        if (overlayMessage.isEnabled()) client.gui.hud.setOverlayMessage(Component.literal("§cSkyBlock Menu is blocked!"), false);
                        if (playSound.isEnabled()) client.player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1f, 1f);
                    }
                }
            }
        });
    }
}