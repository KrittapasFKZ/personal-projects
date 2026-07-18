package net.otsutsukimiho.nozomiaddon.utils;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.otsutsukimiho.nozomiaddon.gui.ItemBrowserScreen;

public class ItemCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("naitem").executes(context -> {
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException ignored) {}
                                    MinecraftClient.getInstance().execute(() -> {
                                        MinecraftClient.getInstance().setScreen(new ItemBrowserScreen());
                                    });
                                }, "na-open-delay").start();
                                return 1;
                            })
            );
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("nai").executes(context -> {
                        new Thread(() -> {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ignored) {}
                            MinecraftClient.getInstance().execute(() -> {
                                MinecraftClient.getInstance().setScreen(new ItemBrowserScreen());
                            });
                        }, "na-open-delay").start();
                        return 1;
                    })
            );
        });
    }
}