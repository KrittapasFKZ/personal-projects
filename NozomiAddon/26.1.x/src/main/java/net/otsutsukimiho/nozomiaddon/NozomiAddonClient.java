package net.otsutsukimiho.nozomiaddon;

import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.otsutsukimiho.nozomiaddon.features.*;
import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.config.*;
import net.otsutsukimiho.nozomiaddon.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NozomiAddonClient implements ClientModInitializer {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final DungeonUtils DUNGEON = new DungeonUtils();
    public static String MOD_VERSION = "???";

    @Override
    public void onInitializeClient() {
        FabricLoader.getInstance().getModContainer("nozomiaddon").ifPresent(mod -> {
            String fullVersion = mod.getMetadata().getVersion().getFriendlyString();
            MOD_VERSION = fullVersion.split("-")[0];
        });

        LOGGER.info("NozomiAddon initialized for Minecraft 26.1.x!");

        new Renderer().onInitializeClient();
        ConfigManager.init();
        FeatureManager.init(ConfigManager.getAllFeatures());
        HudManager.initialize();
        PriceUtils.init();
        CustomCommand.init();
        Refill.init();
        ChatRuleManager.load();
        ItemCommand.register();
        LoadPets.initClient();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            WorldUtils.reset();
            PriceUtils.onWorldUnload();
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
                WorldUtils.onWorldLoad();
            }, "na-worldutils").start();
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("NozomiAddon")
                    .executes(context -> {
                        new Thread(() -> {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ignored) {}
                            Minecraft.getInstance().execute(() -> {
                                if (ClickGUI.displayMode.getMode().equals("V2")) {
                                    Minecraft.getInstance().setScreen(new ModMenuGrid());
                                } else {
                                    Minecraft.getInstance().setScreen(new ModMenuList());
                                }
                            });
                        }, "na-open-delay").start();
                        return 1;
                    })
            );
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("nagetworld")
                    .executes(context -> {
                        WorldUtils.sendWorldToPlayer(Minecraft.getInstance().player);
                        return 1;
                    })
            );
        });

        LevelRenderEvents.END_EXTRACTION.register(context -> {
            WitherHighlight Highlight_Wither = (WitherHighlight) FeatureManager.getRegistered().get("Highlight - WitherBosses");
            StarredMobHighlight Highlight_StarredMob = (StarredMobHighlight) FeatureManager.getRegistered().get("Highlight - Starred Mob");
            BatHighlight Highlight_Bat = (BatHighlight) FeatureManager.getRegistered().get("Highlight - Secret Bat");
            SAHighlight Highlight_SA = (SAHighlight) FeatureManager.getRegistered().get("Highlight - Shadow Assassin");
            DungeonMobDrops Highlight_DungeonMobDrops = (DungeonMobDrops) FeatureManager.getRegistered().get("Dungeon - MobDrop Notify");
            FrozenCorpse Highlight_FrozenCorpse = (FrozenCorpse) FeatureManager.getRegistered().get("Mining - FrozenCorpse");
            GlaciteMobs Highlight_GlaciteMobs = (GlaciteMobs) FeatureManager.getRegistered().get("Mining - GlaciteMobs");
            CustomHighlight Highlight_CustomHighlight = (CustomHighlight) FeatureManager.getRegistered().get("Highlight - CustomHighlight");
            BlockESP Highlight_BlockESP = (BlockESP) FeatureManager.getRegistered().get("Highlight - BlockESP");

            Renderer.clearRenderQueue();

            Highlight_Wither.find();
            Highlight_StarredMob.find();
            Highlight_Bat.find();
            Highlight_SA.find();
            Highlight_DungeonMobDrops.find();
            Highlight_FrozenCorpse.find();
            Highlight_GlaciteMobs.find();
            Highlight_CustomHighlight.find();
            Highlight_BlockESP.find();
        });
    }
}