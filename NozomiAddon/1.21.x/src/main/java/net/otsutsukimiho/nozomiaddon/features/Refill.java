package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Refill {

    public static class QueuedCommand {
        long executeTime;
        String command;

        public QueuedCommand(long executeTime, String command) {
            this.executeTime = executeTime;
            this.command = command;
        }
    }

    public static final List<QueuedCommand> commandQueue = new ArrayList<>();

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("narf")
                    .executes(context -> {
                        triggerRefill();
                        return 1;
                    })
            );

            dispatcher.register(ClientCommandManager.literal("refill")
                    .executes(context -> {
                        triggerRefill();
                        return 1;
                    })
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (!commandQueue.isEmpty()) {
                long now = System.currentTimeMillis();
                Iterator<QueuedCommand> iterator = commandQueue.iterator();

                while (iterator.hasNext()) {
                    QueuedCommand task = iterator.next();
                    if (now >= task.executeTime) {
                        client.player.networkHandler.sendChatCommand(task.command);
                        iterator.remove();
                    }
                }
            }

        });
    }

    private static void triggerRefill() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

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

        if (getPearl && RefillCommand.triggerEPearl.isEnabled()) {
            text.append("§5x").append(needPearl).append(" Pearl");
            commandQueue.add(new QueuedCommand(System.currentTimeMillis(), "gfs ender_pearl " + needPearl));
            currentDelay += 2000;
        }

        if (getTNT && RefillCommand.triggerSuperB.isEnabled()) {
            if (!text.isEmpty()) text.append(", ");
            text.append("§9x").append(needTNT).append(" TNT");
            commandQueue.add(new QueuedCommand(System.currentTimeMillis() + currentDelay, "gfs SUPERBOOM_TNT " + needTNT));
            currentDelay += 2000;
        }

        if (getJerry && RefillCommand.triggerJerry.isEnabled()) {
            if (!text.isEmpty()) text.append(", ");
            text.append("§fx").append(needJerry).append(" Jerry");
            commandQueue.add(new QueuedCommand(System.currentTimeMillis() + currentDelay, "gfs INFLATABLE_JERRY " + needJerry));
            currentDelay += 2000;
        }

        if (getDecoy && RefillCommand.triggerDecoy.isEnabled()) {
            if (!text.isEmpty()) text.append(", ");
            text.append("§ax").append(needDecoy).append(" Decoy");
            commandQueue.add(new QueuedCommand(System.currentTimeMillis() + currentDelay, "gfs DUNGEON_DECOY " + needDecoy));
            currentDelay += 2000;
        }

        if (getToxic && RefillCommand.triggerToxic.isEnabled()) {
            if (!text.isEmpty()) text.append(", ");
            text.append("§2x").append(needToxic).append(" Toxic Arrow Poison");
            Refill.commandQueue.add(new Refill.QueuedCommand(System.currentTimeMillis() + currentDelay, "gfs TOXIC_ARROW_POISON " + needToxic));
            currentDelay += 2000;
        }

        if (getTwilight && RefillCommand.triggerTwilight.isEnabled()) {
            if (!text.isEmpty()) text.append(", ");
            text.append("§dx").append(needTwilight).append(" Twilight Arrow Poison");
            Refill.commandQueue.add(new Refill.QueuedCommand(System.currentTimeMillis() + currentDelay, "gfs TWILIGHT_ARROW_POISON " + needTwilight));
            currentDelay += 2000;
        }

        if (!text.isEmpty()) {
            client.player.sendMessage(Text.literal("§d§lNA §f§l» §aRefilling " + text + "§a!"), false);
        } else {
            client.player.sendMessage(Text.literal("§d§lNA §f§l» §cNo items to refill!"), false);
        }
    }

}