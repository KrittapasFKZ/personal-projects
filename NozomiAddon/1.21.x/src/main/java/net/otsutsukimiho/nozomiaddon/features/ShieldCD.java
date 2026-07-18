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
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.utils.events.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShieldCD extends DraggableHudElement implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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
                if (isWitherImpactWeapon(stack)) {
                    waitingForServer = true;
                }
            }
        });

        EventBus.register(ShieldCD.class, PacketEvent.Receive.class, event -> {
            if (!enabled) return;
            if (!waitingForServer) return;

            if (event.packet instanceof PlaySoundS2CPacket packet) {
                Identifier soundId = packet.getSound().value().id();
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

        ComponentMap components = stack.getComponents();
        NbtComponent nbt = components.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return false;

        NbtCompound tag = nbt.copyNbt();
        if (tag.contains("ability_scroll")) {
            NbtList scrolls = tag.getListOrEmpty("ability_scroll");
            return scrolls.contains(NbtString.of("WITHER_SHIELD_SCROLL"));
        }

        return false;
    }

    @Override
    public void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        String displayText;

        if (tickElapsed >= 1) {
            displayText = String.format("§6Shield§f: §c%.2fs", (tickElapsed / 20.0));
        } else {
            displayText = "§6Shield§f: §aREADY";
        }

        float scale = this.size / 10.0f;

        var matrices = ctx.getMatrices();
        matrices.pushMatrix();
        ctx.getMatrices().translate(this.x, this.y);
        ctx.getMatrices().scale(scale, scale);

        ctx.drawText(MinecraftClient.getInstance().textRenderer, displayText, 0, 0, 0xFFFFFFFF, true);

        ctx.getMatrices().popMatrix();
        this.width = Math.round(70 * scale);
        this.height = Math.round(10 * scale);
    }

}