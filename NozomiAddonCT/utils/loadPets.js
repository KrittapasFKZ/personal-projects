import Settings from "../config";

let collectedPets = [];
let currentPetPage = 1;
let totalPetPages = 1;

let pets = 0

register("guiOpened", (event) => {
    Client.scheduleTask(5, () => {
        let inv = Player.getContainer();
        if (!inv) return;

        let nugget = inv.getStackInSlot(4)
        let nuggetName = nugget.getName().removeFormatting()
        let nuggetNBT = nugget.getNBT()
        let nuggetID = nuggetNBT.getString("id")
        if (nuggetName == "Pets" && nuggetID == "minecraft:bone") { } else return;

        let invName = inv.getName();
        let newName = invName.removeFormatting()

        let match = newName.match(/Pets? \((\d+)\/(\d+)\)/);
        if (match) {
            currentPetPage = parseInt(match[1]);
            totalPetPages = parseInt(match[2]);
        } else if (newName.includes("Pet") || newName.includes("Pets")) {
            currentPetPage = 1;
            totalPetPages = 1;
        } else {
            collectedPets = [];
            return;
        }

        let slots = inv.getItems();

        slots.forEach((item, index) => {
            if (!item || !item.getName()) return;

            let petName = item.getName()
            let lore = item.getLore();
            let match = petName.match(/§\w+\[Lvl (\d+)] (.+)/);
            if (!match) return;

            let hasPetSkin
            if (petName.includes(`✦`)) { hasPetSkin = true } else { hasPetSkin = false }

            let cleanname = petName.removeFormatting()
            let nostar = cleanname.replace("⭐", "");
            let noskin = nostar.replace(" ✦", "");
            let nolevel = noskin.trim().replace(/\[Lvl \d+\]/, "");
            let matchCos = nolevel.trim().match(/\[(\d+)✦] (.+)/);

            let FinalName = nolevel
            if (matchCos) {
                FinalName = `${matchCos[2]} ✦`
            };

            let skinTexture = null;
            try {
                let nbt = item.getNBT();
                let skullOwner = nbt.get("tag")?.get("SkullOwner");
                let textures = skullOwner?.get("Properties")?.get("textures");
                let jsonObj = String(textures);
                let match = jsonObj?.match(/Value:"([^"]+)"/);
                if (match) {
                    skinTexture = match[1];
                };
            } catch (e) {
                skinTexture = null;
            }

            let level = parseInt(match[1]);
            let rarity = getRarityFromLore(lore);

            let heldItem = "§cNone";
            for (let i = 0; i < lore.length; i++) {
                if (lore[i].includes("Held Item: ")) {
                    heldItem = lore[i].split("Held Item: ")[1];
                    break;
                }
            };

            collectedPets.push({
                slot: index,
                name: FinalName.trim(),
                level: level,
                rarity: rarity,
                item: heldItem || "§cNone",
                appliedSkin: hasPetSkin,
                texture: skinTexture || "None",
            });
        });

        setTimeout(() => {
            if (currentPetPage === totalPetPages) {
                let seen = new Set();
                let uniquePets = [];

                for (let pet of collectedPets) {
                    let key = `${pet.slot}-${pet.name}-${pet.level}-${pet.rarity}-${pet.item}`;
                    if (!seen.has(key)) {
                        seen.add(key);
                        uniquePets.push(pet);
                    }
                }

                let json = JSON.stringify(uniquePets, null, 2);
                FileLib.write("NozomiAddon", "./data/pets.json", json);

                if (pets == uniquePets.length) { } else {
                    pets = uniquePets.length
                    ChatLib.chat(`${Settings.prefix} &aSaved &f${uniquePets.length} &apets!`);
                };
                collectedPets = [];
            }
        }, 100);
    });
});

function getRarityFromLore(lore) {
    let found = false
    for (let line of lore) {
        if (line.includes("Rarity:")) {
            return line.removeFormatting().split("Rarity: ")[1];
        }
    }
    if (lore[0]?.includes("§b") && !found) { found = true; return "Divine" };
    if (lore[0]?.includes("§d") && !found) { found = true; return "Mythic" };
    if (lore[0]?.includes("§6") && !found) { found = true; return "Legendary" };
    if (lore[0]?.includes("§5") && !found) { found = true; return "Epic" };
    if (lore[0]?.includes("§9") && !found) { found = true; return "Rare" };
    if (lore[0]?.includes("§a") && !found) { found = true; return "Uncommon" };
    if (lore[0]?.includes("§f") && !found) { found = true; return "Common" };
    return "Unknown";
}