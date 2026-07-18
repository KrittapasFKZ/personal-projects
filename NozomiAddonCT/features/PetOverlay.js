import Settings from "../config";
import { createSkull } from "../../tska/utils/InventoryUtils"
import { registerHudElement } from "../utils/HUDManager";
import { registerWhen } from "../utils/utils";

let petIcon = createSkull()
let placeholder = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2UzY2QyN2M5YjQ0NTY3MmJkOGQ5MjA1YTg3N2Y2NDRlNzQ2MjdjMmUzYzVmMTY2OWJlNDk5N2E0ZWJhOWQzYSJ9fX0="

let currentPet = "&r"
let displayName = "&r"
let pettexture = false
let debounce = false
let getRarityColor = {
    "Common": "&f",
    "Uncommon": "&a",
    "Rare": "&9",
    "Epic": "&5",
    "Legendary": "&6",
    "Mythic": "&d",
    "Divine": "&b",
};

const getPet = () => { return currentPet };

function getData(petName) {
    let petFile = FileLib.read("NozomiAddon", "data/pets.json")
    let petData = JSON.parse(petFile)
    let pet = petData.find(p => p.name === petName);
    if (!pet) return null;
    return pet;
};

const petChange = (new_pet) => {
    if (currentPet == new_pet) return
    let cleanname = new_pet.removeFormatting()
    let rawName = cleanname.trim().replace(/\[Lvl \d+\]/, "");
    let matchCos = rawName.trim().match(/\[(\d+)✦] (.+)/);
    let petBaseName = rawName
    if (matchCos) {
        petBaseName = `${matchCos[2]} ✦`
    } else {
        if (!petBaseName.includes("Golden Dragon")) {
            petBaseName = rawName.replace(" ✦", "");
        }
    }
    currentPet = petBaseName.trim();
    if (currentPet == "&r") {
        World.playSound("mob.cat.meow", 100, 0.5);
        displayName = `&r`
        pettexture = false
        if (Settings.petOverlay_title) {
            Client.showTitle("", `&cNo Pet`, 0, 10, 0)
        };
        if (Settings.petOverlay_chat) {
            if (!debounce) {
                debounce = true;
                ChatLib.command(`pc NA » Pet Changed to None`, false);
                setTimeout(() => {
                    debounce = false
                }, 500);
            };
        };
        petIcon = createSkull(placeholder)
    } else {
        let cleanname = currentPet.removeFormatting();
        let data = getData(cleanname);
        if (!data) {
            ChatLib.chat(`${Settings.prefix} &cNo data for this pet!`);
            return
        };
        currentPet = `${getRarityColor[data.rarity]}${currentPet}`
        World.playSound("mob.cat.meow", 100, 1);
        pettexture = true
        displayName = `&7[Lvl ${data.level}] ${currentPet}\n&7(${data.item}&7)`
        if (Settings.petOverlay_title) {
            Client.showTitle("", `${currentPet}`, 0, 10, 0)
        };
        if (Settings.petOverlay_chat) {
            if (!debounce) {
                debounce = true;
                ChatLib.command(`pc NA » Pet Changed to ${currentPet.removeFormatting()}`, false);
                setTimeout(() => {
                    debounce = false
                }, 500);
            };
        };
        if (data.texture) {
            petIcon = createSkull(data.texture)
        } else {
            petIcon = createSkull(placeholder)
        }
    };
};

let isDragging = false;
let dragOffsetX = 0;
let dragOffsetY = 0;

registerHudElement(() => {
    /// Pet Info
    if (!Settings.petOverlay) return;
    let x = Number(Settings.petOverlay_pos_x);
    let y = Number(Settings.petOverlay_pos_y);
    if (Settings.editUI) {
        if (Settings.editUIName.includes("PetOverlay")) {
            Renderer.drawRect(Renderer.color(0, 255, 0, 100), x - 3, y - 3, 150 * Settings.petOverlay_scale, 30 * Settings.petOverlay_scale);
        } else {
            Renderer.drawRect(Renderer.color(255, 255, 255, 150), x - 3, y - 3, 150 * Settings.petOverlay_scale, 30 * Settings.petOverlay_scale);
        };
    };
    Renderer.retainTransforms(true)
    Renderer.translate(x, y)
    Renderer.scale(Number(Settings.petOverlay_scale))
    if (pettexture) petIcon.draw(0, 0)
    Renderer.drawStringWithShadow(displayName, 16, 2)
    Renderer.retainTransforms(false)
}, () => Settings.petOverlay);

register("clicked", (x, y, button, isDown) => {
    if (!Settings.petOverlay) return;
    if (!Settings.editUI) return
    if (!isDown) {
        Settings.editUIName = Settings.editUIName.replace("PetOverlay", " ").trim();
        isDragging = false;
        return;
    };

    let textX = Number(Settings.petOverlay_pos_x);
    let textY = Number(Settings.petOverlay_pos_y);
    let textWidth = 150 * Settings.petOverlay_scale
    let textHeight = 30 * Settings.petOverlay_scale

    if (button === 0) {
        if (x >= textX && x <= textX + textWidth && y >= textY && y <= textY + textHeight) {
            isDragging = true;
            dragOffsetX = x - textX;
            dragOffsetY = y - textY;
            if (Settings.editUIName == "") Settings.editUIName += "PetOverlay"
        };
    } else if (button === 1) {
        if (!Settings.editUIName.includes("PetOverlay")) return
        World.playSound("tile.piston.in", 100, 1);
        Settings.petOverlay_pos_x = "0";
        Settings.petOverlay_pos_y = "0";
        Settings.petOverlay_scale = "1";
        Settings.save()
    };
});

register("dragged", (dx, dy, x, y) => {
    if (!isDragging) return;
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("PetOverlay")) return
    Settings.petOverlay_pos_x = x - dragOffsetX;
    Settings.petOverlay_pos_y = y - dragOffsetY;
});

register("scrolled", (x, y, dir) => {
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("PetOverlay")) return
    Settings.petOverlay_scale = Number(Settings.petOverlay_scale)
    if (dir == 1) Settings.petOverlay_scale += 0.05
    else Settings.petOverlay_scale -= 0.05
})

register("chat", (newPet) => {
    petChange(newPet);
}).setCriteria("&r&aYou summoned your &r${newPet}&r&a!&r")

register("chat", () => {
    petChange("&r");
}).setCriteria("&r&aYou despawned your &r${newPet}&r&a!&r")

register("chat", (newPet) => {
    petChange(newPet);
}).setCriteria("&cAutopet &eequipped your &7${newPet}&e! &a&lVIEW RULE&r")

export { getPet };