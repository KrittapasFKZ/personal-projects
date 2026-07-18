package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
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

public class RagTimer extends DraggableHudElement implements FeatureManager.Feature  {
    public RagTimer() {
        super("RagTimer", 10, 10, 50, 10, 10);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;

    private static float tickElapsed = 0f;
    private static float waitElapsed = 0f;
    private static boolean waitingForServer = false;
    private static boolean tryCast = false;

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.GOLDEN_AXE);
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            waitingForServer = false;
            tryCast = false;
            tickElapsed = 0f;
            waitElapsed = 0f;
        });

        EventBus.register(RagTimer.class, PacketEvent.Send.class, event -> {
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
                if (isRag(stack)) {
                    waitingForServer = true;
                    tryCast = true;
                    waitElapsed = 20f;
                }
            }
        });

        EventBus.register(RagTimer.class, PacketEvent.Receive.class, event -> {
            if (!enabled) return;

            if (event.packet instanceof ClientboundSoundPacket packet) {
                Identifier soundId = packet.getSound().value().location();
                String path = soundId.getPath();

                if (path.contains("block.lever.click") && waitingForServer && waitElapsed >= 1 ) {
                    waitElapsed = 0f;
                    tryCast = false;
                }

                if (path.contains("wolf.death") && waitingForServer) {
                    tickElapsed = 200f;
                    waitElapsed = 0f;
                    tryCast = false;
                }
            }
        });

    }

    public static void onPacketReceived() {
        if (tickElapsed >= 1) tickElapsed -= 1f;
        if (waitElapsed >= 1) {
            waitElapsed -= 1f;
        } else if (waitingForServer && tryCast) {
            waitingForServer = false;
            tryCast = false;
        }
        if (waitingForServer) {
            if (Minecraft.getInstance().player != null) {
                ItemStack heldItem = Minecraft.getInstance().player.getMainHandItem();
                if (!isRag(heldItem)) {
                    waitingForServer = false;
                    tryCast = false;
                    waitElapsed = 0f;
                }
            }
        }
    }

    private static boolean isRag(ItemStack stack) {
        if (stack.isEmpty()) return false;

        DataComponentMap components = stack.getComponents();
        CustomData nbt = components.get(DataComponents.CUSTOM_DATA);
        if (nbt == null) return false;

        CompoundTag tag = nbt.copyTag();
        if (tag.contains("id")) {
            String id = tag.getStringOr("id", "UNKNOWN");
            return id.equals("RAGNAROCK_AXE");
        }

        return false;
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        String displayText;

        if (EditHudScreen.isEditMode()) {
            displayText = "§cRag§f: §a10.0s";
        } else {
            if (tickElapsed >= 1) {
                displayText = String.format("§cRag§f: §a%.2fs", (tickElapsed / 20.0));
            } else  {
                displayText = " ";
            }
        }

        float scale = this.size / 10.0f;

        var matrices = ctx.pose();
        matrices.pushMatrix();
        ctx.pose().translate(this.x, this.y);
        ctx.pose().scale(scale, scale);

        int textWidth = Minecraft.getInstance().font.width(displayText);
        int centerX = 25 - textWidth / 2;

        ctx.text(Minecraft.getInstance().font, displayText, centerX, 0, 0xFFFFFFFF, true);

        ctx.pose().popMatrix();
        this.width = Math.round(50 * scale);
        this.height = Math.round(10 * scale);
    }

}