import Settings from "../config";
import { createSkull } from "../../tska/utils/InventoryUtils"
import { registerHudElement } from "../utils/HUDManager";
import { registerWhen } from "../utils/utils";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { getWorld } from "../utils/worlds";
import { getPet } from "./PetOverlay";

const SpiritMask = createSkull("eyJ0aW1lc3RhbXAiOjE1MDUyMjI5OTg3MzQsInByb2ZpbGVJZCI6IjBiZTU2MmUxNzIyODQ3YmQ5MDY3MWYxNzNjNjA5NmNhIiwicHJvZmlsZU5hbWUiOiJ4Y29vbHgzIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsibWV0YWRhdGEiOnsibW9kZWwiOiJzbGltIn0sInVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWJiZTcyMWQ3YWQ4YWI5NjVmMDhjYmVjMGI4MzRmNzc5YjUxOTdmNzlkYTRhZWEzZDEzZDI1M2VjZTlkZWMyIn19fQ==")
const BonzoMask = createSkull("eyJ0aW1lc3RhbXAiOjE1ODc5MDgzMDU4MjYsInByb2ZpbGVJZCI6IjJkYzc3YWU3OTQ2MzQ4MDI5NDI4MGM4NDIyNzRiNTY3IiwicHJvZmlsZU5hbWUiOiJzYWR5MDYxMCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI3MTZlY2JmNWI4ZGEwMGIwNWYzMTZlYzZhZjYxZThiZDAyODA1YjIxZWI4ZTQ0MDE1MTQ2OGRjNjU2NTQ5YyJ9fX0=")
const Phoenix = createSkull("ewogICJ0aW1lc3RhbXAiIDogMTY0Mjg2NTc3MTM5MSwKICAicHJvZmlsZUlkIiA6ICJiYjdjY2E3MTA0MzQ0NDEyOGQzMDg5ZTEzYmRmYWI1OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJsYXVyZW5jaW8zMDMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjZiMWI1OWJjODkwYzljOTc1Mjc3ODdkZGUyMDYwMGM4Yjg2ZjZiOTkxMmQ1MWE2YmZjZGIwZTRjMmFhM2M5NyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9")

let SpiritText = "&6Spirit Mask"
let BonzoText = "&9Bonzo's Mask"
let PhoenixText = "&cPhoenix"

let inDungeon = false

let bonzotime = 0
let spirittime = 0
let phoenixtime = 0

let bonzosound = false
let spiritsound = false
let phoenixsound = false
let globaldebounce = false

let isDragging = false;
let dragOffsetX = 0;
let dragOffsetY = 0;

const checkInDungeon = () => { return inDungeon; }

// title announce

const showTitle = (title, chat) => {
    if (Settings.masktimer) {
        if (Settings.masktimer_showtitle) {
            Client.showTitle("", `${title}`, 0, 70, 0)
        };
        ChatLib.chat(`${chat}`)
        World.playSound("random.orb", 100, 1.25);
    };
};

register("step", () => {
    bonzotime--
    spirittime--
    phoenixtime--
    if (bonzotime <= 0 && bonzosound) {
        if (!Settings.masktimer) return;
        showTitle(`&9Bonzo's Mask &a&lREADY!`, `${Settings.prefix} &9Bonzo's Mask &a&lREADY!`)
        bonzosound = false
        if (globaldebounce) globaldebounce = false
    }
    if (spirittime <= 0 && spiritsound) {
        if (!Settings.masktimer) return;
        showTitle(`&6Spirit Mask &a&lREADY!`, `${Settings.prefix} &6Spirit Mask &a&lREADY!`)
        spiritsound = false
        if (globaldebounce) globaldebounce = false
    }
    if (phoenixtime <= 0 && phoenixsound) {
        if (!Settings.masktimer) return;
        showTitle(`&cPhoenix &a&lREADY!`, `${Settings.prefix} &cPhoenix &a&lREADY!`)
        phoenixsound = false
        if (globaldebounce) globaldebounce = false
    }
    if (bonzotime >= 1 && spirittime >= 1 && phoenixtime >= 1) {
        if (!Settings.masktimer) return;
        if (!globaldebounce) {
            globaldebounce = true
            Client.scheduleTask(15, () => {
                showTitle(`&r`, `${Settings.prefix} &c&lYou ran out of Invincible items!`)
                ChatLib.command("pc NA » Im out of lives! Chill!!!", false);
                World.playSound("mob.villager.no", 100, 1);
            });
        };
    };
    inDungeon = isInDungeon()
}).setFps(10);

registerWhen(register("renderWorld", () => {
    if (!Settings.masktimer) return;
    let checkMask = Player.armor.getHelmet()?.getName()?.removeFormatting();
    let checkPet = getPet().removeFormatting();
    if (checkMask.includes("Spirit Mask")) {
        SpiritText = "&f&l> Spirit Mask"
    } else {
        SpiritText = "&6Spirit Mask"
    };
    if (checkMask.includes("Bonzo's Mask")) {
        BonzoText = "&f&l> Bonzo's Mask"
    } else {
        BonzoText = "&9Bonzo's Mask"
    };
    if (checkPet.includes("Phoenix")) {
        PhoenixText = "&f&l> Phoenix"
    } else {
        PhoenixText = "&cPhoenix"
    };
}), () => Settings.masktimer);

// bonzo

registerWhen(register("chat", () => {
    if (!Settings.masktimer) return;
    bonzotime = parseInt(1800)
    if (Settings.masktimer_showtitle) {
        showTitle(`&9Bonzo Mask &e&lPROCCED!`, `${Settings.prefix} &9Bonzo Mask &e&lPROCCED!`);
    };
    bonzosound = true
}).setCriteria(/Your (⚚)? Bonzo's Mask saved your life!/), () => Settings.masktimer);

registerWhen(register("chat", () => {
    if (!Settings.masktimer) return;
    bonzotime = parseInt(1800)
    if (Settings.masktimer_showtitle) {
        showTitle(`&9Bonzo Mask &e&lPROCCED!`, `${Settings.prefix} &9Bonzo Mask &e&lPROCCED!`);
    };
    bonzosound = true
}).setCriteria(/Your Bonzo's Mask saved your life!/), () => Settings.masktimer);

// spirit

registerWhen(register("chat", () => {
    if (!Settings.masktimer) return;
    spirittime = parseInt(300)
    if (Settings.masktimer_showtitle) {
        showTitle(`&6Spirit Mask &e&lPROCCED!`, `${Settings.prefix} &6Spirit Mask &e&lPROCCED!`);
    };
    spiritsound = true
}).setCriteria("Second Wind Activated! Your Spirit Mask saved your life!"), () => Settings.masktimer);

// phoenix

registerWhen(register("chat", () => {
    phoenixtime = parseInt(600)
    if (Settings.masktimer_showtitle) {
        showTitle(`&cPhoenix &e&lPROCCED!`, `${Settings.prefix} &cPhoenix &e&lPROCCED!`);
    };
    phoenixsound = true
}).setCriteria("Your Phoenix Pet saved you from certain death!"), () => Settings.masktimer);

function isInDungeon() {
    let yesDungeon = Dungeon.inDungeon
    return yesDungeon
}

registerHudElement(() => {
    if (!Settings.masktimer) return;
    let x = Number(Settings.masktimer_pos_x);
    let y = Number(Settings.masktimer_pos_y);
    let scale = Number(Settings.masktimer_scale);
    if (Settings.editUI) {
        if (Settings.editUIName.includes("MaskTimer")) {
            Renderer.drawRect(Renderer.color(0, 255, 0, 100), x - 3, y - 3, 130 * Settings.masktimer_scale, 42 * Settings.masktimer_scale);
        } else {
            Renderer.drawRect(Renderer.color(255, 255, 255, 150), x - 3, y - 3, 130 * Settings.masktimer_scale, 42 * Settings.masktimer_scale);
        };
    };
    if (inDungeon) {
        Renderer.retainTransforms(true)
        Renderer.translate(x, y)
        Renderer.scale(scale)
        BonzoMask.draw(0, 0), Renderer.drawStringWithShadow((bonzotime <= 0) ? `${BonzoText}: &a&lREADY` : `${BonzoText}: &7${(bonzotime / 10).toFixed(1)}`, 16, 4)
        SpiritMask.draw(0, 12), Renderer.drawStringWithShadow((spirittime <= 0) ? `${SpiritText}: &a&lREADY` : `${SpiritText}: &7${(spirittime / 10).toFixed(1)}`, 16, 5)
        Phoenix.draw(0, 12), Renderer.drawStringWithShadow((phoenixtime <= 0) ? `${PhoenixText}: &a&lREADY` : `${PhoenixText}: &7${(phoenixtime / 10).toFixed(1)}`, 16, 5)
        Renderer.retainTransforms(false)
    } else if (getWorld() == "Kuudra") {
        Renderer.retainTransforms(true)
        Renderer.translate(x, y)
        Renderer.scale(scale)
        SpiritMask.draw(0, 0), Renderer.drawStringWithShadow((spirittime <= 0) ? `${SpiritText}: &a&lREADY` : `${SpiritText} &7${(spirittime / 10).toFixed(1)}`, 16, 5)
        Phoenix.draw(0, 12), Renderer.drawStringWithShadow((phoenixtime <= 0) ? `${PhoenixText}: &a&lREADY` : `${PhoenixText}: &7${(phoenixtime / 10).toFixed(1)}`, 16, 5)
        Renderer.retainTransforms(false)
    };
}, () => Settings.masktimer);

register("clicked", (x, y, button, isDown) => {
    if (!Settings.masktimer) return;
    if (!Settings.editUI) return;
    if (!isDown) {
        Settings.editUIName = Settings.editUIName.replace("MaskTimer", " ").trim();
        isDragging = false;
        return;
    };

    let textX = Number(Settings.masktimer_pos_x);
    let textY = Number(Settings.masktimer_pos_y);
    let textWidth = 130 * Settings.masktimer_scale
    let textHeight = 42 * Settings.masktimer_scale

    if (button === 0) {
        if (x >= textX && x <= textX + textWidth && y >= textY && y <= textY + textHeight) {
            isDragging = true;
            dragOffsetX = x - textX;
            dragOffsetY = y - textY;
            if (Settings.editUIName == "") Settings.editUIName += "MaskTimer"
        };
    } else if (button === 1) {
        if (!Settings.editUIName.includes("MaskTimer")) return
        World.playSound("tile.piston.in", 100, 1);
        Settings.masktimer_pos_x = "0";
        Settings.masktimer_pos_y = "0";
        Settings.masktimer_scale = "1";
        Settings.save()
    };
});

register("dragged", (dx, dy, x, y) => {
    if (!isDragging) return;
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("MaskTimer")) return
    Settings.masktimer_pos_x = x - dragOffsetX;
    Settings.masktimer_pos_y = y - dragOffsetY;
});

register("scrolled", (x, y, dir) => {
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("MaskTimer")) return
    Settings.masktimer_scale = Number(Settings.masktimer_scale);
    if (dir == 1) Settings.masktimer_scale += 0.05
    else Settings.masktimer_scale -= 0.05
})

register("worldUnload", () => {
    inDungeon = false
});

export { checkInDungeon }