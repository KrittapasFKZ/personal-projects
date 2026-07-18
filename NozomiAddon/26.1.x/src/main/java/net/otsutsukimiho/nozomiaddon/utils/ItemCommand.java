package net.otsutsukimiho.nozomiaddon.utils;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;
import net.otsutsukimiho.nozomiaddon.gui.ItemBrowserScreen;

public class ItemCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("naitem").executes(context -> {
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException ignored) {}
                                    Minecraft.getInstance().execute(() -> {
                                        Minecraft.getInstance().setScreen(new ItemBrowserScreen());
                                    });
                                }, "na-open-delay").start();
                                return 1;
                            })
            );
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("nai").executes(context -> {
                        new Thread(() -> {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ignored) {}
                            Minecraft.getInstance().execute(() -> {
                                Minecraft.getInstance().setScreen(new ItemBrowserScreen());
                            });
                        }, "na-open-delay").start();
                        return 1;
                    })
            );
        });
    }
}