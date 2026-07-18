import Settings from "../config";

register("chat", (item) => {
    if (!Settings.bazaarnotify) return
    Client.showTitle('&e&l[!] &6&lBAZAAR FILLED &e&l[!]&r', `${item}`, 0, 80, 0)
    World.playSound("random.levelup", 100, 2);
}).setCriteria("[Bazaar] Your Buy Order for ${item} was filled!");

register("chat", (item) => {
    if (!Settings.bazaarnotify) return
    Client.showTitle(`&e&l[!] &6&lBAZAAR FILLED &e&l[!]&r'`, `${item}`, 0, 80, 0)
    World.playSound("random.levelup", 100, 2);
}).setCriteria("[Bazaar] Your Sell Offer for ${item} was filled!");