package net.otsutsukimiho.nozomiaddon.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class CustomCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger("nozomiaddon");
    private static Map<String, String> shortcuts = new HashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Path DATA_DIR = FabricLoader.getInstance().getConfigDir().resolve("nozomiaddon");
    private static final Path CONFIG_FILE = DATA_DIR.resolve("cmd.json");

    public static void init() {
        load();

        ClientSendMessageEvents.ALLOW_COMMAND.register(CustomCommand::onCommand);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("nacmd")
                    .then(ClientCommands.literal("add")
                            .then(ClientCommands.argument("trigger", StringArgumentType.word())
                                    .then(ClientCommands.argument("command", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                String trigger = StringArgumentType.getString(context, "trigger");
                                                String command = StringArgumentType.getString(context, "command");

                                                if (command.startsWith("/")) command = command.substring(1);
                                                if (trigger.startsWith("/")) trigger = trigger.substring(1);

                                                shortcuts.put(trigger, command);
                                                save();

                                                context.getSource().sendFeedback(Component.literal("§d§lNA §f§l» §aAdded §e/" + trigger + " §a-> §e/" + command));
                                                return 1;
                                            })
                                    )
                            )
                    )
                    .then(ClientCommands.literal("remove")
                            .then(ClientCommands.argument("trigger", StringArgumentType.word())
                                    .executes(context -> {
                                        String trigger = StringArgumentType.getString(context, "trigger");
                                        if (shortcuts.remove(trigger) != null) {
                                            save();
                                            context.getSource().sendFeedback(Component.literal("§d§lNA §f§l» §aRemoved §e/" + trigger));
                                        } else {
                                            context.getSource().sendFeedback(Component.literal("§d§lNA §f§l» §cUnknown CMD."));
                                        }
                                        return 1;
                                    })
                            )
                    )
                    .then(ClientCommands.literal("list")
                            .executes(context -> {
                                context.getSource().sendFeedback(Component.literal("§d§lNA §f§l» §aCommand Lists"));
                                if (shortcuts.isEmpty()) {
                                    context.getSource().sendFeedback(Component.literal("§7(No commands saved)"));
                                } else {
                                    shortcuts.forEach((k, v) -> {
                                        context.getSource().sendFeedback(Component.literal("§d§lNA §f§l» §e/" + k + " §a-> §e/" + v));
                                    });
                                }
                                return 1;
                            })
                    )
                    .then(ClientCommands.literal("reload")
                            .executes(context -> {
                                load();
                                context.getSource().sendFeedback(Component.literal("§d§lNA §f§l» §aConfiguration reloaded!"));
                                return 1;
                            })
                    )
            );
        });
    }

    private static boolean onCommand(String command) {
        if (shortcuts.containsKey(command)) {
            String realCommand = shortcuts.get(command);
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.connection.sendCommand(realCommand);
            }
            return false;
        }
        return true;
    }

    private static void save() {
        try {
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(shortcuts, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save custom commands", e);
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().gui.getChat().addClientSystemMessage(Component.literal("§cFailed to save commands!"));
            }
        }
    }

    private static void load() {
        if (!Files.exists(CONFIG_FILE)) return;

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_FILE)) {
            Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            Map<String, String> loaded = GSON.fromJson(reader, type);

            if (loaded != null) {
                shortcuts = loaded;
                LOGGER.info("Loaded " + shortcuts.size() + " custom commands.");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load custom commands", e);
        }
    }
}