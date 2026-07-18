import Settings from "./config";

import "./features/Drops";
import "./features/Slayers";
import "./features/MaskTimer";
import "./features/MaskTimerInvincibleTimer";
import "./features/GummyPolarBear";
import "./features/PetOverlay";
import "./features/DungeonSlowSS";
import "./features/DungeonRNGJumpscare";
import "./features/DungeonNotifyGate";
import "./features/DungeonChestProfit";
import "./features/DungeonMelody";
import "./features/DungeonMelodyWarning";
import "./features/DungeonMobDrop";
import "./features/DungeonStorm";
import "./features/DungeonHideParticles";
import "./features/DungeonWitherHighlight";
import "./features/DungeonMobHighlight";
import "./features/DungeonFelHighlight";
import "./features/DungeonBatHighlight";
import "./features/DungeonLividHighlight";
import "./features/DungeonMageHighlight";
import "./features/DungeonBlessing";
import "./features/DungeonDeathMessage";
import "./features/DungeonAutoDraft";
import "./features/DungeonKeyHighligh";
import "./features/DungeonPositionMessage";
import "./features/DungeonBossBar";
import "./features/DungeonBlood";
import "./features/DungeonRunOverview";
import "./features/DungeonRagTimer";
import "./features/DungeonHideDamage";
import "./features/DungeonP3Timer";
import "./features/DungeonShadowFury";
import "./features/DungeonLeapAnnounce";
import "./features/DungeonCryptReminder";
import "./features/KuudraChestProfit";
import "./features/KuudraKeyPrice";
import "./features/KuudraSupplies";
import "./features/KuudraRunOverview";
import "./features/KuudraRNGJumpscare";
import "./features/KuudraChestOpen";
import "./features/RiftUbix";
import "./features/BazaarNotify";
import "./features/AuctionNotify";
import "./features/BGBlur";
import "./features/RecipePrice";
import "./features/Timer";
import "./features/MiningPowder";
import "./features/FishingTrophyFish";
import "./features/MiningCorpse";
import "./features/TrapperHelper";
import "./features/Clock";
import "./features/ThunderBottle";
import "./features/Refill";
import "./features/HideParticles";
import "./features/PlaySound";
import "./features/ChatEmotes";
import "./features/PartyFinder";
import "./features/checkGodRoll";
import "./features/checkItemID";
import "./features/checkItemPrice";
import "./features/MiscNoBlindness";
import "./features/DamageSplash";

import "./utils/priceUtils";
import "./utils/HUDManager";
import "./utils/loadPets";
import "./utils/worlds";

register("renderWorld", () => {
    if (Settings.prefixsettings) {
        Settings.prefix = "&d&lNA &f&l»";
    } else {
        Settings.prefix = "&d&lNozomiAddon &f&l»";
    }
});

register("command", () =>
    Settings.openGUI()
).setName("na");

let a = register("worldLoad", () => {
    ChatLib.chat(`${Settings.prefix} &aModule loaded! &e(&b${Settings.version}&e)`)
    a.unregister();
});