package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.utils.events.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RagTimer extends DraggableHudElement implements FeatureManager.Feature  {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            Hand hand = null;
            if (event.packet instanceof PlayerInteractItemC2SPacket packet) {
                hand = packet.getHand();
            } else if (event.packet instanceof PlayerInteractBlockC2SPacket packet) {
                hand = packet.getHand();
            }

            if (hand != null) {
                ItemStack stack = client.player.getStackInHand(hand);
                if (isRag(stack)) {
                    waitingForServer = true;
                    tryCast = true;
                    waitElapsed = 20f;
                }
            }
        });

        EventBus.register(RagTimer.class, PacketEvent.Receive.class, event -> {
            if (!enabled) return;

            if (event.packet instanceof PlaySoundS2CPacket packet) {
                Identifier soundId = packet.getSound().value().id();
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
            if (MinecraftClient.getInstance().player != null) {
                ItemStack heldItem = MinecraftClient.getInstance().player.getMainHandStack();
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

        ComponentMap components = stack.getComponents();
        NbtComponent nbt = components.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return false;

        NbtCompound tag = nbt.copyNbt();
        if (tag.contains("id")) {
            String id = tag.getString("id", "UNKNOWN");
            return id.equals("RAGNAROCK_AXE");
        }

        return false;
    }

    @Override
    public void render(DrawContext ctx, RenderTickCounter tickCounter) {
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

        var matrices = ctx.getMatrices();
        matrices.pushMatrix();
        ctx.getMatrices().translate(this.x, this.y);
        ctx.getMatrices().scale(scale, scale);

        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(displayText);
        int centerX = 25 - textWidth / 2;

        ctx.drawText(MinecraftClient.getInstance().textRenderer, displayText, centerX, 0, 0xFFFFFFFF, true);

        ctx.getMatrices().popMatrix();
        this.width = Math.round(50 * scale);
        this.height = Math.round(10 * scale);
    }

}