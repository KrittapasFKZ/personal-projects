package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import java.util.HashMap;
import java.util.Map;

public class DungeonRunOverview extends DraggableHudElement implements FeatureManager.Feature {
    public DungeonRunOverview() {
        super("DungeonRunOverview", 10, 10, 200, 60, 10);
    }

    public void setEnabled(boolean enabled) {
        DungeonRunOverview.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.MAP);
    }

    private static volatile boolean enabled = false;
    public static String dungeonFloor = "§c???";
    public static String bloodOpened = "?";
    public static float portalTime = 0;
    public static long bossEntry = 0;
    public static float bloodTick = 0;
    public static boolean bloodStart = false;
    public static float ticksElapsed = 0;
    public static long dungeonStep = 0;
    public static float realElapsed = 0;
    public static float loss = 0;
    public boolean getDungeon = false;
    public static String zone = "Total";
    public static final Map<String, Float> ticks = new HashMap<>();

    public int countTick = 0;

    public static final String[] WATCHER_DIALOGUE = {
            "Things feel a little more roomy now, eh?",
            "Oh.. hello?",
            "I'm starting to get tired of seeing you around here...",
            "You've managed to scratch and claw your way here, eh?",
            "So you made it this far... interesting.",
            "Ah, we meet again...",
            "Ah, you've finally arrived."
    };

    public void createSplit() {
        ticks.clear();
        ticks.put("Total", 0F);
        ticks.put("Maxor", 0F);
        ticks.put("Storm", 0F);
        ticks.put("Terminals", 0F);
        ticks.put("Goldor", 0F);
        ticks.put("Necron", 0F);
        ticks.put("Dragons", 0F);
        ticks.put("End", 0F);
        ticks.put("Boss", 0F);
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            dungeonFloor = "§c???";
            bloodOpened = "?";
            zone = "Total";
            portalTime = 0;
            bossEntry = 0;
            bloodTick = 0;
            bloodStart = false;
            ticksElapsed = 0;
            dungeonStep = 0;
            getDungeon = false;
            realElapsed = 0;
            loss = 0;
            createSplit();
        });

        ClientTickEvents.END_LEVEL_TICK.register(client -> {
            if (!DUNGEON.inDungeon) return;
            if (!enabled) return;
            if (!getDungeon) {
                String getFloor = DungeonUtils.getCurrentDungeon();
                countTick++;
                if (countTick >= 20) {
                    countTick = 0;
                }
                if (getFloor == null) return;
                dungeonFloor = DungeonUtils.getCurrentDungeon();
                getDungeon = true;
            } else {
                if (dungeonFloor.matches("M\\d+")) {
                    dungeonFloor = "§4§l" + dungeonFloor;
                } else {
                    dungeonFloor = "§a" + dungeonFloor;
                }
            }

            if (DUNGEON.runStarted != -1 && DUNGEON.runEnded == -1) {
                realElapsed = (float) (System.currentTimeMillis() - DUNGEON.runStarted) / 1000;
                loss = ((realElapsed - ticksElapsed / 20) * -1);
            }

        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!enabled) return;
            if (!DUNGEON.inDungeon) return;
            Minecraft client = Minecraft.getInstance();
            if (client == null) return;
            if (client.player == null) return;

            String msg = message.getString();
            if (msg.startsWith("[BOSS] The Watcher: ")) {
                for (String line : WATCHER_DIALOGUE) {
                    if (msg.contains(line)) {
                        if (!bloodStart) bloodStart = true;
                        break;
                    }
                }
            }
            if (msg.contains("[BOSS] The Watcher: That will be enough for now.")) {
                client.gui.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §cBlood Ready!"));
            }
            if (msg.contains("[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!")) {
                zone = "Maxor";
            }
            if (msg.contains("[BOSS] Storm: Pathetic Maxor, just like expected.")) {
                if (dungeonStep == 3) {
                    dungeonStep = 4;
                    client.gui.getChat().addClientSystemMessage(Component.literal(String.format("§d§lNA §f§l» §aMaxor Done: %.2fs", (ticks.getOrDefault("Maxor", 0F) / 20.0))));
                }
                zone = "Storm";
            }
            if (msg.contains("[BOSS] Goldor: Who dares trespass into my domain?")) {
                if (dungeonStep == 4) {
                    dungeonStep = 5;
                    client.gui.getChat().addClientSystemMessage(Component.literal(String.format("§d§lNA §f§l» §bStorm Done: %.2fs", (ticks.getOrDefault("Storm", 0F) / 20.0))));
                }
                zone = "Terminals";
            }
            if (msg.contains("The Core entrance is opening!")) {
                if (dungeonStep == 5) {
                    dungeonStep = 6;
                    client.gui.getChat().addClientSystemMessage(Component.literal(String.format("§d§lNA §f§l» §eTerminals Done: %.2fs", (ticks.getOrDefault("Terminals", 0F) / 20.0))));
                }
                zone = "Goldor";
            }
            if (msg.contains("[BOSS] Necron: You went further than any human before, congratulations.")) {
                if (dungeonStep == 6) {
                    dungeonStep = 7;
                    client.gui.getChat().addClientSystemMessage(Component.literal(String.format("§d§lNA §f§l» §7Goldor Done: %.2fs", (ticks.getOrDefault("Goldor", 0F) / 20.0))));
                }
                zone = "Necron";
            }
            if (msg.contains("[BOSS] Necron: All this, for nothing...")) {
                if (dungeonStep == 7) {
                    dungeonStep = 8;
                    client.gui.getChat().addClientSystemMessage(Component.literal(String.format("§d§lNA §f§l» §cNecron Done: %.2fs", (ticks.getOrDefault("Necron", 0F) / 20.0))));
                }
                zone = "Dragons";
            }
        });
    }

    public static void onPacketReceived() {
        if (!DUNGEON.inDungeon) return;
        if (!enabled) return;
        if (DUNGEON.runStarted != -1 && DUNGEON.runEnded == -1) {
            tick();
        }
    }

    public static void tick () {
        if (!DUNGEON.inDungeon) return;
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        if (client.player == null) return;

        float tps = 1f;
        ticksElapsed += tps;
        ticks.put(zone, ticks.getOrDefault(zone, 0F) + tps);
        if (DUNGEON.bloodOpened == -1) {
            bloodOpened = getTime(System.currentTimeMillis() - DUNGEON.runStarted);
        } else {
            bloodOpened = getTime(DUNGEON.bloodOpened - DUNGEON.runStarted);
        }
        if (bloodStart) {
            bloodTick += tps;
            if (dungeonStep == 0) {
                dungeonStep = 1;
                client.gui.getChat().addClientSystemMessage(Component.literal(String.format("§d§lNA §f§l» §c§lBLOOD DOOR §chas been opened! §f(§a%s§f)", bloodOpened)));
            }
        }
        if (DUNGEON.watcherCleared != -1 && DUNGEON.bossEntry == -1) {
            bloodStart = false;
            portalTime += tps;
            if (dungeonStep == 1) {
                dungeonStep = 2;
                client.gui.getChat().addClientSystemMessage(Component.literal(String.format("§d§lNA §f§l» §cBlood Done! §f(took §a%.2fs§f)", (bloodTick / 20.0))));
            }
        }
        if (DUNGEON.bossEntry != -1) {
            bossEntry = (DUNGEON.bossEntry - DUNGEON.runStarted);
            if (dungeonStep == 2) {
                dungeonStep = 3;
                client.gui.getChat().addClientSystemMessage(Component.literal(String.format("§d§lNA §f§l» §aBoss Entry: §a%s", getTime(bossEntry))));
                if (!dungeonFloor.contains("7")) zone = "Boss";
            }
        }
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        float scale = this.size / 10.0f;
        String text;
        int y = 0;

        var matrices = ctx.pose();
        matrices.pushMatrix();
        ctx.pose().translate(this.x, this.y);
        ctx.pose().scale(scale, scale);

        if (!DUNGEON.inDungeon) {
            if (EditHudScreen.isEditMode()) {
                text = String.format("§e§lRun Overview§r %s §7(§a?§7)", "§c???");
                ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
            }
        } else {
            if (DUNGEON.runStarted == -1) {
                text = String.format("§e§lRun Overview§r %s §7(§a?§7)", dungeonFloor);
                ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
            } else {
                text = String.format("§e§lRun Overview§r %s §7(§a%s§7) §7(§b%.1fs§7)" , dungeonFloor, getTime((long) ((ticksElapsed / 20) * 1000)), loss);
                ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
                y += 12;
                text = String.format(" §8%s Doors §7| §4BR %s", DUNGEON.openedWitherDoors, bloodOpened);
                ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
            }
            if (dungeonStep >= 1) {
                y += 12;
                text = String.format(" §cWatcher Clear: %.2fs", (bloodTick / 20.0));
                ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
            }
            if (dungeonStep >= 2) {
                y += 12;
                text = String.format(" §dPortal: %.2fs", (portalTime / 20.0));
                ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
            }
            if (dungeonStep >= 3) {
                y += 12;
                text = String.format(" §aBoss Entry: %s", getTime(bossEntry));
                ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
            }
            if (dungeonFloor.contains("M7")) {
                if (dungeonStep >= 3) {
                    y += 12;
                    text = String.format(" §aMaxor: %.2fs", (ticks.getOrDefault("Maxor", 0F) / 20.0));
                    ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
                }
                if (dungeonStep >= 4) {
                    y += 12;
                    text = String.format(" §bStorm: %.2fs", (ticks.getOrDefault("Storm", 0F) / 20.0));
                    ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
                }
                if (dungeonStep >= 5) {
                    y += 12;
                    text = String.format(" §eTerminals: %.2fs", (ticks.getOrDefault("Terminals", 0F) / 20.0));
                    ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
                }
                if (dungeonStep >= 6) {
                    y += 12;
                    text = String.format(" §7Goldor: %.2fs", (ticks.getOrDefault("Goldor", 0F) / 20.0));
                    ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
                }
                if (dungeonStep >= 7) {
                    y += 12;
                    text = String.format(" §cNecron: %.2fs", (ticks.getOrDefault("Necron", 0F) / 20.0));
                    ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
                }
                if (dungeonStep >= 8) {
                    y += 12;
                    text = String.format(" §4Dragons: %.2fs", (ticks.getOrDefault("Dragons", 0F) / 20.0));
                    ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
                }
            } else {
                if (dungeonFloor.contains("7")) {
                    if (dungeonStep >= 3) {
                        y += 12;
                        text = String.format(" §aMaxor: %.2fs", (ticks.getOrDefault("Maxor", 0F) / 20.0));
                        ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
                    }
                    if (dungeonStep >= 4) {
                        y += 12;
                        text = String.format(" §bStorm: %.2fs", (ticks.getOrDefault("Storm", 0F) / 20.0));
                        ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
                    }
                    if (dungeonStep >= 5) {
                        y += 12;
                        text = String.format(" §eTerminals: %.2fs", (ticks.getOrDefault("Terminals", 0F) / 20.0));
                        ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
                    }
                    if (dungeonStep >= 6) {
                        y += 12;
                        text = String.format(" §7Goldor: %.2fs", (ticks.getOrDefault("Goldor", 0F) / 20.0));
                        ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
                    }
                    if (dungeonStep >= 7) {
                        y += 12;
                        text = String.format(" §cNecron: %.2fs", (ticks.getOrDefault("Necron", 0F) / 20.0));
                        ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
                    }
                } else {
                    if (dungeonStep >= 3) {
                        y += 12;
                        text = String.format(" §4Boss: %.2fs", (ticks.getOrDefault("Boss", 0F) / 20.0));
                        ctx.text(Minecraft.getInstance().font, Component.literal(text), 0, y, 0xFFFFFFFF, true);
                    }
                }
            }
        }

        ctx.pose().popMatrix();
        this.width = Math.round(200 * scale);
        this.height = Math.round(60 * scale);
    }

    public static String getTime(Long ms) {
        if (ms == null || ms == 0) return "?";
        long minutes = ms / 60000;
        long seconds = (ms / 1000) % 60;
        if (minutes != 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

}