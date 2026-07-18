import Settings from "../config";

let timerTimeout = null;

const parseTimeInput = (input) => {
    const match = input.match(/^(\d+)([smh])$/i);
    if (!match) return null;

    const value = parseInt(match[1]);
    const unit = match[2].toLowerCase();

    if (unit === "s") return value;
    if (unit === "m") return value * 60;
    if (unit === "h") return value * 3600;
    return null;
};

register("command", (...args) => {
    if (args.length < 1) {
        ChatLib.chat(`${Settings.prefix} &cUsage: /timer 5s / 2m / 1h`);
        return;
    }

    const seconds = parseTimeInput(args[0]);
    if (seconds === null) {
        ChatLib.chat(`${Settings.prefix} &cInvalid format! Use like &b /timer 10s&c, &b2m&c, or &b1h&c`);
        return;
    }

    if (timerTimeout) clearTimeout(timerTimeout);

    ChatLib.chat(`${Settings.prefix} &bTimer set for &a${seconds}s`);

    timerTimeout = setTimeout(() => {
        ChatLib.chat(`${Settings.prefix} &aTimer ended! (${args[0]})`);
        Client.showTitle("", `&aTimer ended!`, 0, 40, 1)
        for (let i = 0; i <= 5; i++) {
            setTimeout(() => {
                World.playSound("random.orb", 100, 2);
            }, i * 200);
        };
    }, seconds * 1000);
}).setName("timer");
