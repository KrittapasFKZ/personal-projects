package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.utils.events.*;

public class ShieldCD extends DraggableHudElement implements FeatureManager.Feature {
    public ShieldCD() {
        super("ShieldCD", 10, 10, 50, 10, 10);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;

    private static float tickElapsed = 0;
    private boolean waitingForServer = false;

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.WRITABLE_BOOK);
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            waitingForServer = false;
            tickElapsed = 0;
        });
        EventBus.register(ShieldCD.class, PacketEvent.Send.class, event -> {
            if (!enabled) return;
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;

            InteractionHand hand = null;
            if (event.packet instanceof ServerboundUseItemPacket packet) {
                hand = packet.getHand();
            } else if (event.packet instanceof ServerboundUseItemOnPacket packet) {
                hand = packet.getHand();
            }

            if (hand != null) {
                ItemStack stack = client.player.getItemInHand(hand);
                if (isWitherImpactWeapon(stack)) {
                    waitingForServer = true;
                }
            }
        });

        EventBus.register(ShieldCD.class, PacketEvent.Receive.class, event -> {
            if (!enabled) return;
            if (!waitingForServer) return;

            if (event.packet instanceof ClientboundSoundPacket packet) {
                Identifier soundId = packet.getSound().value().location();
                String path = soundId.getPath();

                if (path.contains("cure")) {
                    tickElapsed = 100f;
                }
            }
        });
    }

    public static void onPacketReceived() {
        if (tickElapsed >= 1) tickElapsed -= 1f;
    }

    private static boolean isWitherImpactWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;

        DataComponentMap components = stack.getComponents();
        CustomData nbt = components.get(DataComponents.CUSTOM_DATA);
        if (nbt == null) return false;

        CompoundTag tag = nbt.copyTag();
        if (tag.contains("ability_scroll")) {
            ListTag scrolls = tag.getListOrEmpty("ability_scroll");
            return scrolls.contains(StringTag.valueOf("WITHER_SHIELD_SCROLL"));
        }

        return false;
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        String displayText;

        if (tickElapsed >= 1) {
            displayText = String.format("§6Shield§f: §c%.2fs", (tickElapsed / 20.0));
        } else {
            displayText = "§6Shield§f: §aREADY";
        }

        float scale = this.size / 10.0f;

        var matrices = ctx.pose();
        matrices.pushMatrix();
        ctx.pose().translate(this.x, this.y);
        ctx.pose().scale(scale, scale);

        ctx.text(Minecraft.getInstance().font, displayText, 0, 0, 0xFFFFFFFF, true);

        ctx.pose().popMatrix();
        this.width = Math.round(70 * scale);
        this.height = Math.round(10 * scale);
    }

}