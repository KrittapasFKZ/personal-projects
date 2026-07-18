package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.text.Text;

import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.utils.*;
import net.otsutsukimiho.nozomiaddon.utils.events.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TerminalsPhase extends DraggableHudElement implements FeatureManager.Feature {
    public TerminalsPhase() {
        super("TerminalsPhase", 10, 10, 100, 10, 10);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;
    private static boolean phaseStarted = false;
    private static int currentSection = 0;
    private static boolean gateDestroy = false;
    private static int currentTerminals = 0;
    private static int totalTerminals = 0;
    private static long hideAt = 0;
    private static Text titleText = Text.empty();
    private static String currentTerminalType = "terminal";

    private static final Pattern TERMINAL_PATTERN = Pattern.compile(
            "(\\w{1,16}) (activated|completed) a (lever|device|terminal)! \\((\\d)/((7|8))\\).*"
    );

    public static BooleanSetting hideCompleteTitle = new BooleanSetting("HideCompleteTitle", false);
    public static BooleanSetting showGateTitle = new BooleanSetting("showGateTitle", false);
    public static FloatSetting titleScale = new FloatSetting("Title Scale", 4f,1f,6f,0.25f);
    public static BooleanSetting showSection = new BooleanSetting("showSection", false);
    public static TextSetting customMessage_Lever = new TextSetting("Lever Text", "&bLever");
    public static TextSetting customMessage_Device = new TextSetting("Device Text", "&9Device");
    public static TextSetting customMessage_Terminal = new TextSetting("Terminal Text", "&dTerminal");
    private static final Map<String, Supplier<String>> DUMMY_DATA = Map.of(
            "{name}", () -> TerminalsPhase.customMessage_Lever.getValue().replace("&", "§"),
            "{current}", () -> "3",
            "{total}", () -> "7"
    );
    public static TextSetting customMessage = new TextSetting("Main Text", "{name} &f(&e&l{current}&f/&a&l{total}&r&f)", DUMMY_DATA);
    public static SoundSetting customSound1 = new SoundSetting("Entering P3", "minecraft:entity.player.levelup", 1.0f, 1.0f);
    public static SoundSetting customSound2 = new SoundSetting("Section Safe", "minecraft:entity.player.burp", 1.0f, 1.0f);
    public static SoundSetting customSound3 = new SoundSetting("Section Unsafe", "minecraft:block.anvil.land", 1.0f, 1.0f);
    public static SoundSetting customSound4 = new SoundSetting("Gate Destroyed", "minecraft:entity.iron_golem.death", 0.75f, 1.0f);
    public static SoundSetting customSound5 = new SoundSetting("Completed Terminal", "minecraft:entity.experience_orb.pickup", 1.0f, 1.25f);
    public static SoundSetting customSound6 = new SoundSetting("Completed P3", "minecraft:block.anvil.use", 1.0f, 1.0f);
    @Override
    public List<Settings> getSettings() {
        return List.of(hideCompleteTitle, showGateTitle, titleScale, showSection, customMessage, customMessage_Lever, customMessage_Device, customMessage_Terminal, customSound1, customSound2, customSound3, customSound4, customSound5, customSound6);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.COMMAND_BLOCK);
    }

    public void initClient() {
        EventBus.register(TerminalsPhase.class, PacketEvent.Receive.class, event -> {
            if (!DUNGEON.inDungeon) return;
            if (!enabled) return;
            if (!phaseStarted) return;
            String text = null;

            if (event.packet instanceof SubtitleS2CPacket packet) {
                text = packet.text().getString();
            }

            if (text != null) {
                String cleanText = text.replaceAll("§.", "");
                Matcher matcher = TERMINAL_PATTERN.matcher(cleanText);
                if (TerminalsPhase.hideCompleteTitle.isEnabled()) {
                    if (cleanText.contains("The gate has been destroyed!")) {
                        event.cancel();
                    }
                    if (matcher.find()) {
                        event.cancel();
                    }
                }
            }
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            phaseStarted = false;
            gateDestroy = false;
            currentSection = 0;
            currentTerminals = 0;
            totalTerminals = 0;
            hideAt = 0;
            currentTerminalType = "terminal";
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!enabled) return;
            if (!DUNGEON.inDungeon) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) return;
            if (client.player == null) return;

            String msg = message.getString();
            if (msg.contains("[BOSS] Storm: I should have known that I stood no chance") && !phaseStarted) {
                client.player.sendMessage(Text.literal("§d§lNA §f§l» §9§lEntering Terminal Phase!"),false);
                customSound1.playTestSound();
                currentTerminalType = "terminal";
                phaseStarted = true;
                currentSection = 1;
                currentTerminals = 0;
                totalTerminals = 7;
                return;
            }

            if (!phaseStarted) return;
            Matcher matcher = TERMINAL_PATTERN.matcher(msg);

            if (matcher.find()) {
                String player = matcher.group(1);
                String action = matcher.group(2);
                String type = matcher.group(3);
                int current = Integer.parseInt(matcher.group(4));
                int total = Integer.parseInt(matcher.group(5));

                currentTerminalType = type;

                totalTerminals = (currentSection == 2) ? 8 : 7;
                if (current > currentTerminals) currentTerminals = current;

                if (current == totalTerminals && phaseStarted) {
                    if (currentSection <= 3) {
                        if (gateDestroy) {
                            client.player.sendMessage(Text.literal("§d§lNA §f§l» §a§lSAFE! §d§lS§f§l" + currentSection),false);
                            customSound2.playTestSound();
                            titleText = Text.literal("§a§lSAFE!");
                            hideAt = System.currentTimeMillis() + 2000;
                        } else {
                            client.player.sendMessage(Text.literal("§d§lNA §f§l» §c§lGATE NOT DESTROY! §d§lS§f§l" + currentSection),false);
                            customSound3.playTestSound();
                            titleText = Text.literal("§c§lGATE NOT DESTROY!");
                            hideAt = System.currentTimeMillis() + 2000;
                        }
                    }
                    currentSection++;
                    currentTerminals = 0;
                    totalTerminals = (currentSection == 2) ? 8 : 7;
                    gateDestroy = false;
                    return;
                } else {
                    customSound5.playTestSound();
                }
            }

            if (msg.contains("The Core entrance is opening!") && phaseStarted) {
                phaseStarted = false;
                client.player.sendMessage(Text.literal("§d§lNA §f§l» §9§lTerminal Phase Completed!"),false);
                customSound6.playTestSound();
                titleText = Text.literal("§f§l§kA§r §d§lCORE §f§l§kA");
                hideAt = System.currentTimeMillis() + 2000;
                return;
            }

            if (msg.contains("The gate has been destroyed!") && phaseStarted && !gateDestroy) {
                gateDestroy = true;
                client.player.sendMessage(Text.literal("§d§lNA §f§l» §a§lGATE DESTROYED! §d§lS§f§l" + currentSection),false);
                customSound4.playTestSound();
                titleText = Text.literal("§a§lGATE DESTROYED!");
                hideAt = System.currentTimeMillis() + 2000;
            }

        });
    }

    @Override
    public void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        if ((System.currentTimeMillis() < hideAt) && TerminalsPhase.showGateTitle.isEnabled()) {
            MinecraftClient client = MinecraftClient.getInstance();
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            var matrices = ctx.getMatrices();

            matrices.pushMatrix();
            ctx.getMatrices().translate(width / 2.0f, height / 2.0f - 30);
            ctx.getMatrices().scale(TerminalsPhase.titleScale.getValue(), TerminalsPhase.titleScale.getValue());

            ctx.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, titleText, 0, 0, 0xFFFFFFFF);

            ctx.getMatrices().popMatrix();
        }

        if (!EditHudScreen.isEditMode()) {
            if (!DUNGEON.inDungeon) return;
            if (!phaseStarted) return;
        }

        float scale = this.size / 10.0f;

        var matrices = ctx.getMatrices();
        matrices.pushMatrix();
        ctx.getMatrices().translate(this.x, this.y);
        ctx.getMatrices().scale(scale, scale);

        String nameReplacement;
        switch (currentTerminalType) {
            case "lever":
                nameReplacement = TerminalsPhase.customMessage_Lever.getValue();
                break;
            case "device":
                nameReplacement = TerminalsPhase.customMessage_Device.getValue();
                break;
            case "terminal":
            default:
                nameReplacement = TerminalsPhase.customMessage_Terminal.getValue();
                break;
        }

        String rawMessage = TerminalsPhase.customMessage.getValue();
        if (rawMessage == null) rawMessage = " ";

        String newMsg = rawMessage
                .replace("{name}", nameReplacement)
                .replace("{current}", String.valueOf(currentTerminals))
                .replace("{total}", String.valueOf(totalTerminals))
                .trim();

        Text coloredMsg = ColorUtils.parseColor(newMsg);

        Text displayText;
        if (TerminalsPhase.showSection.isEnabled()) {
            displayText = Text.literal("§2§lS§f§l" + currentSection + " ").append(coloredMsg);
        } else {
            displayText = coloredMsg;
        }

        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(displayText);
        int centerX = 40 - textWidth / 2;

        ctx.drawText(MinecraftClient.getInstance().textRenderer, displayText, centerX, 0, 0xFFFFFFFF, true);
        ctx.getMatrices().popMatrix();

        this.width = Math.round(80 * scale);
        this.height = Math.round(10 * scale);
    }

}