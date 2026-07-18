import Settings from "../config";
import { registerWhen } from "../utils/utils";

let blessing = {
    Life: {
        Name: "&aLife",
        Level: 0
    },
    Power: {
        Name: "&4Power",
        Level: 0
    },
    Stone: {
        Name: "&7Stone",
        Level: 0
    },
    Wisdom: {
        Name: "&bWisdom",
        Level: 0
    },
    Time: {
        Name: "&5Time",
        Level: 0
    }
};

const romanToNumber = {
    I: 1,
    II: 2,
    III: 3,
    IV: 4,
    V: 5,
    VI: 6,
    VII: 7,
    VIII: 8,
    IX: 9,
    X: 10
};

function addBlessing(level, type) {
    let newlevel = romanToNumber[level]
    blessing[type].Level = blessing[type].Level + newlevel;
    ChatLib.chat(`${Settings.prefix} &d&lBLESSING! ${blessing[type].Name} ${newlevel} &f(&aCurrent: ${blessing[type].Level}&f)`);
    World.playSound("random.orb", 100, 1.25);
};

registerWhen(register("chat", (name, type, level, event) => {
    if (!Settings.dungeonblessing) return;
    addBlessing(level, type)
    cancel(event);
}).setCriteria("DUNGEON BUFF! ${name} found a Blessing of ${type} ${level}!"), () => Settings.dungeonblessing)

registerWhen(register("chat", (name, type, level, time, event) => {
    if (!Settings.dungeonblessing) return;
    addBlessing(level, type)
    cancel(event);
}).setCriteria("DUNGEON BUFF! ${name} found a Blessing of ${type} ${level}! ${time}"), () => Settings.dungeonblessing)

registerWhen(register("chat", (type, level, event) => {
    if (!Settings.dungeonblessing) return;
    addBlessing(level, type)
    cancel(event);
}).setCriteria("DUNGEON BUFF! A Blessing of ${type} ${level} was found!"), () => Settings.dungeonblessing)

registerWhen(register("chat", (type, level, time, event) => {
    if (!Settings.dungeonblessing) return;
    addBlessing(level, type)
    cancel(event);
}).setCriteria("DUNGEON BUFF! A Blessing of ${type} ${level} was found! ${time}"), () => Settings.dungeonblessing)

registerWhen(register("worldUnload", () => {
    blessing = {
        Life: {
            Name: "&aLife",
            Level: 0
        },
        Power: {
            Name: "&4Power",
            Level: 0
        },
        Stone: {
            Name: "&7Stone",
            Level: 0
        },
        Wisdom: {
            Name: "&bWisdom",
            Level: 0
        },
        Time: {
            Name: "&5Time",
            Level: 0
        }
    };
}), () => Settings.dungeonblessing)