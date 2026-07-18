import Settings from "../config";

const dmgRegex = /^\D?\d[\d,.]+.*$/;
let seenUUIDs = new Set();

function abbreviateNumber(num) {
    num = parseInt(num.replace(/,/g, ""));
    if (isNaN(num)) return num;

    if (num >= 1_000_000_000) return (num / 1_000_000_000).toFixed(2) + "B";
    if (num >= 1_000_000) return (num / 1_000_000).toFixed(2) + "M";
    if (num >= 1_000) return (num / 1_000).toFixed(0) + "K";
    return num.toString();
}

function colorizeByChar(str, color) {
    let rol = false
    let out = ""
    if (str.includes("❤")) {
        rol = true
    };
    if (str.includes("✧")) {
        const colors = ["§f", "§f", "§e", "§6", "§c", "§c"];
        let i = 0;
        for (let ch of str) {
            out += colors[i % colors.length] + ch;
            i++;
        }
        if (rol) {
            out = out.replace("❤", "§d❤").trim()
            return `${out.trim()}`;
        }
        return out;
    } else {
        if (str.includes("✯")) {
            const colors = ["§f", "§f", "§e", "§6", "§c", "§c"];
            let i = 0;
            for (let ch of str) {
                out += colors[i % colors.length] + ch;
                i++;
            }
            if (rol) {
                out = out.replace("❤", "§d❤").trim()
                return `${out.trim()}`;
            }
            return out;
        } else {
            return `${color}${str}`;
        };
    };
}

export const rename = (e) => {
    if (!Settings.damagesplash) return;
    let name = e.getName()
    let cleanname = name.removeFormatting()
    let color = `${name[0]}${name[1]}`
    let uuid = e.getUUID();
    if (dmgRegex.test(cleanname)) {
        let identifier = `${uuid}`;
        if (!seenUUIDs.has(identifier)) {
            seenUUIDs.add(identifier);
            let match = cleanname.match(/[\d,.]+/);
            if (!match) return;

            let abbr = abbreviateNumber(match[0]);
            let newName = cleanname.replace(match[0], abbr);
            let finalName = colorizeByChar(newName, color)

            e.getEntity().func_96094_a(`${finalName}`);
        }
    }
}

register("worldUnload", () => {
    seenUUIDs.clear();
});