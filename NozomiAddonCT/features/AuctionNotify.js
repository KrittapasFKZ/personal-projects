import Settings from "../config";

register("chat", (player, item, coins) => {
    if (!Settings.auctionnotify) return
    Client.showTitle(`&e&l[!] &6&lAUCTION SOLD &e&l[!]&r'`, `${item}`, 0, 80, 0)
    World.playSound("random.levelup", 100, 2);
}).setCriteria("[Auction] ${player} bought ${item} for ${coins} coins CLICK");