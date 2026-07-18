package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.otsutsukimiho.nozomiaddon.utils.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FeatureManager {
    private static final Map<String, Feature> FEATURES = new LinkedHashMap<>();
    static {
        FEATURES.put("Mining - FrozenCorpse", new FrozenCorpse());
        FEATURES.put("Mining - GlaciteMobs", new GlaciteMobs());
        FEATURES.put("HUD - MaskTimer", new MaskTimer());
        FEATURES.put("HUD - InvincibleTimer", new InvincibleTimer());
        FEATURES.put("HUD - PetOverlay", new PetOverlay());
        FEATURES.put("HUD - RealtimeClock", new Clock());
        FEATURES.put("HUD - PartyFinder", new PartyFinder());
        FEATURES.put("HUD - ShieldCD", new ShieldCD());
        FEATURES.put("HUD - RagTimer", new RagTimer());
        FEATURES.put("HUD - CryptNotify", new CryptNotify());
        FEATURES.put("Performance - HitBox", new HitBox());
        FEATURES.put("Performance - EntityScale", new EntityScale());
        FEATURES.put("Performance - HideDamage", new HideDamage());
        FEATURES.put("Performance - HideArmor", new HideArmor());
        FEATURES.put("Performance - HidePlayers", new HidePlayers());
        FEATURES.put("Other - ClickGUI", new ClickGUI());
        FEATURES.put("Other - RenderMode", new RenderMode());
        FEATURES.put("Other - DisableHotbarScroll", new DisableHotbarScroll());
        FEATURES.put("General - Tweaks", new Tweaks());
        FEATURES.put("General - RefillCommand", new RefillCommand());
        FEATURES.put("General - AutoRefill", new AutoRefill());
        FEATURES.put("General - AutoClick", new AutoClick());
        FEATURES.put("General - TimeChanger", new TimeChanger());
        FEATURES.put("General - CommandKeyBind", new CustomMacros());
        FEATURES.put("General - ChatRules", new CustomChatRules());
        FEATURES.put("General - HideWardrobe", new HideWardrobe());
        FEATURES.put("General - HidePetMenu", new HidePetMenu());
        FEATURES.put("General - BlockSkyBlockMenu", new BlockSkyBlockMenu());
        FEATURES.put("Highlight - WitherBosses", new WitherHighlight());
        FEATURES.put("Highlight - Starred Mob", new StarredMobHighlight());
        FEATURES.put("Highlight - Shadow Assassin", new SAHighlight());
        FEATURES.put("Highlight - Secret Bat", new BatHighlight());
        FEATURES.put("Highlight - BlockESP", new BlockESP());
        FEATURES.put("Highlight - CustomHighlight", new CustomHighlight());
        FEATURES.put("Dungeon - DungeonTweaks", new DungeonTweaks());
        FEATURES.put("Dungeon - Run Overview", new DungeonRunOverview());
        FEATURES.put("Dungeon - Key Notify", new KeyNotify());
        FEATURES.put("Dungeon - LeapMenu", new LeapMenu());
        FEATURES.put("Dungeon - MobDrop Notify", new DungeonMobDrops());
        FEATURES.put("Dungeon - Chest Profit", new DungeonChestProfit());
        FEATURES.put("Dungeon - WitherBoss Health", new WitherBossBar());
        FEATURES.put("Dungeon - Storm Tick", new StormTick());
        FEATURES.put("Dungeon - Blood Tick", new BloodTick());
        FEATURES.put("Dungeon - QTerminals", new TerminalsSolver());
        FEATURES.put("Dungeon - TerminalsPhase", new TerminalsPhase());
        FEATURES.put("Dungeon - FuckDiorite", new DioritePillar());
        FEATURES.put("Dungeon - DeathMessage", new DeathMessage());
        FEATURES.put("Dungeon - Rag Alert", new RagAlert());
        FEATURES.put("Dungeon - Hide PlayerAfterLeap", new HidePlayerLeap());
    }

    public static void init(Map<String, Boolean> configStates) {
        for (String fullName : FEATURES.keySet()) {
            String simpleName = fullName;
            if (fullName.contains(" - ")) {
                simpleName = fullName.split(" - ", 2)[1];
            }

            boolean enabled = configStates != null && configStates.getOrDefault(simpleName, false);

            Feature f = FEATURES.get(fullName);
            f.initClient();
            f.setEnabled(enabled);
        }
    }

    public static void setEnabled(String name, boolean enabled) {
        Feature f = FEATURES.get(name);
        if (f != null) f.setEnabled(enabled);
    }

    public static Map<String, Feature> getRegistered() {
        return new LinkedHashMap<>(FEATURES);
    }

    public interface Feature {
        void initClient();
        void setEnabled(boolean enabled);
        boolean isEnabled();
        default List<Settings> getSettings() {
            return new ArrayList<>();
        }
        default ItemStack getIcon() {
            return new ItemStack(Items.BARRIER);
        }
        default String getDescription() {
            return "";
        }
    }
}
