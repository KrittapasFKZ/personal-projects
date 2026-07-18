package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AutoRefill implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;
    private static boolean debounce = false;
    private static boolean kuudraStart = false;
    private static float currentTick = 0f;

    public static ModeSetting onStart = new ModeSetting("onInstanceStarted", "Dungeon",  "Kuudra", "Dungeon", "Dungeon and Kuudra");
    public static NumberSetting intervalSeconds = new NumberSetting("Interval Seconds", 15, 10, 60, 1);
    public static CheckMarkSetting intervalEPearl = new CheckMarkSetting("Ender Pearl", false);
    public static CheckMarkSetting intervalSuperB = new CheckMarkSetting("Superboom", false);
    public static CheckMarkSetting intervalJerry = new CheckMarkSetting("Inflatable Jerry", false);
    public static CheckMarkSetting intervalDecoy = new CheckMarkSetting("Decoy", false);
    public static CheckMarkSetting intervalToxic = new CheckMarkSetting("Toxic Arrow Poison", false);
    public static CheckMarkSetting intervalTwilight = new CheckMarkSetting("Twilight Arrow Poison", false);
    @Override
    public List<Settings> getSettings() {
        return List.of(onStart, intervalSeconds, intervalEPearl, intervalSuperB, intervalJerry, intervalDecoy, intervalToxic, intervalTwilight);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("ewogICJ0aW1lc3RhbXAiIDogMTU5Nzk4NDQ2MTMxOCwKICAicHJvZmlsZUlkIiA6ICJiNzQ3OWJhZTI5YzQ0YjIzYmE1NjI4MzM3OGYwZTNjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTeWxlZXgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBhMDc3ZTI0OGQxNDI3NzJlYTgwMDg2NGY4YzU3OGI5ZDM2ODg1YjI5ZGFmODM2YjY0YTcwNjg4MmI2ZWMxMCIKICAgIH0KICB9Cn0=");
    }

    public static void onPacketReceived() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        client.execute(() -> {
            if (client.player == null) return;

            if (DUNGEON.inDungeon) {
                if (DUNGEON.bossEntry != -1) return;
                if (DUNGEON.runStarted != -1 && debounce) {
                    currentTick += 1f;
                    int targetTick = AutoRefill.intervalSeconds.getValue();
                    if ((currentTick / 20f) >= targetTick) {
                        currentTick = 0f;
                        procRefill(client);
                    }
                }
            }
            if (kuudraStart) {
                currentTick += 1f;
                int targetTick = AutoRefill.intervalSeconds.getValue();
                if ((currentTick / 20f) >= targetTick) {
                    currentTick = 0f;
                    procRefill(client);
                }
            }
        });
    }

    public static void procRefill(MinecraftClient client) {
        if (client == null || client.player == null) return;

        int pearls = 0;
        int superboom = 0;
        int jerry = 0;
        int decoy = 0;
        int toxic = 0;
        int twilight = 0;

        for (ItemStack stack : client.player.getInventory()) {
            if (stack.isEmpty()) continue;
            if (stack.getItem().toString().contains("ender_pearl")) {
                pearls += stack.getCount();
            }
            if (stack.getName().getString().contains("Superboom TNT")) {
                superboom += stack.getCount();
            }
            if (stack.getName().getString().contains("Inflatable Jerry")) {
                jerry += stack.getCount();
            }
            if (stack.getName().getString().contains("Decoy")) {
                decoy += stack.getCount();
            }
            if (stack.getName().getString().contains("Toxic Arrow Poison")) {
                toxic += stack.getCount();
            }
            if (stack.getName().getString().contains("Twilight Arrow Poison")) {
                twilight += stack.getCount();
            }
        }

        int needPearl = 16 - pearls;
        int needTNT = 64 - superboom;
        int needJerry = 64 - jerry;
        int needDecoy = 64 - decoy;
        int needToxic = 64 - toxic;
        int needTwilight = 64 - twilight;

        boolean getPearl = needPearl >= 1;
        boolean getTNT = needTNT >= 1;
        boolean getJerry = needJerry >= 1;
        boolean getDecoy = needDecoy >= 1;
        boolean getToxic = needToxic >= 1;
        boolean getTwilight = needTwilight >= 1;

        StringBuilder text = new StringBuilder();
        long currentDelay = 0;

        if (getPearl && AutoRefill.intervalEPearl.isEnabled()) {
            text.append("§5x").append(needPearl).append(" Pearl");
            Refill.commandQueue.add(new Refill.QueuedCommand(System.currentTimeMillis(), "gfs ender_pearl " + needPearl));
            currentDelay += 2000;
        }

        if (getTNT && AutoRefill.intervalSuperB.isEnabled()) {
            if (!text.isEmpty()) text.append(", ");
            text.append("§9x").append(needTNT).append(" TNT");
            Refill.commandQueue.add(new Refill.QueuedCommand(System.currentTimeMillis() + currentDelay, "gfs SUPERBOOM_TNT " + needTNT));
            currentDelay += 2000;
        }

        if (getJerry && AutoRefill.intervalJerry.isEnabled()) {
            if (!text.isEmpty()) text.append(", ");
            text.append("§fx").append(needJerry).append(" Jerry");
            Refill.commandQueue.add(new Refill.QueuedCommand(System.currentTimeMillis() + currentDelay, "gfs INFLATABLE_JERRY " + needJerry));
            currentDelay += 2000;
        }

        if (getDecoy && AutoRefill.intervalDecoy.isEnabled()) {
            if (!text.isEmpty()) text.append(", ");
            text.append("§ax").append(needDecoy).append(" Decoy");
            Refill.commandQueue.add(new Refill.QueuedCommand(System.currentTimeMillis() + currentDelay, "gfs DUNGEON_DECOY " + needDecoy));
            currentDelay += 2000;
        }

        if (getToxic && AutoRefill.intervalToxic.isEnabled()) {
            if (!text.isEmpty()) text.append(", ");
            text.append("§2x").append(needToxic).append(" Toxic Arrow Poison");
            Refill.commandQueue.add(new Refill.QueuedCommand(System.currentTimeMillis() + currentDelay, "gfs TOXIC_ARROW_POISON " + needToxic));
            currentDelay += 2000;
        }

        if (getTwilight && AutoRefill.intervalTwilight.isEnabled()) {
            if (!text.isEmpty()) text.append(", ");
            text.append("§dx").append(needTwilight).append(" Twilight Arrow Poison");
            Refill.commandQueue.add(new Refill.QueuedCommand(System.currentTimeMillis() + currentDelay, "gfs TWILIGHT_ARROW_POISON " + needTwilight));
            currentDelay += 2000;
        }

        if (!text.isEmpty()) {
            client.player.sendMessage(Text.literal("§d§lNA §f§l» §e§lAUTO! §aRefilling: " + text), false);
        }
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            debounce = false;
            kuudraStart = false;
        });
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (!enabled) return;
            if (debounce) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            if (DUNGEON.inDungeon && DUNGEON.runStarted != -1 && onStart.getMode().contains("Dungeon")) {
                debounce = true;
                procRefill(client);
            }

        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null | !enabled | debounce) return;
            if (!WorldUtils.getWorld().contains("Kuudra")) return;
            if (!onStart.getMode().contains("Kuudra")) return;

            String msg = message.getString();
            if (msg.contains("Okay adventurers, I will go and fish up Kuudra!")) {
                debounce = true;
                kuudraStart = true;
                procRefill(client);
            }
        });
    }

}