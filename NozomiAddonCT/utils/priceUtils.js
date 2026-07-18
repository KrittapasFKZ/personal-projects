import request from "requestV2";

let data = JSON.parse(FileLib.read("NozomiAddon", "data/data.json"));
let waitWorld = false

function updateBazaarAndAuction() {
    if (!waitWorld) return
    waitWorld = false
    const now = Date.now();

    if (now - data.lastUpdate < 120000) return;

    data = JSON.parse(FileLib.read("NozomiAddon", "data/data.json"));
    data.lastUpdate = now;
    FileLib.write("NozomiAddon", "data/data.json", JSON.stringify(data, null, 2));

    request({ url: "https://api.hypixel.net/skyblock/bazaar", json: true }).then(data => {
        if (!data.success || !("products" in data)) return;
        let prices = Object.keys(data.products).reduce((a, b) => {
            const p = data.products[b];
            a[b] = {
                buy: p.quick_status.buyPrice,
                sell: p.quick_status.sellPrice
            };
            return a;
        }, {});
        FileLib.write("NozomiAddon", "./data/bz.json", JSON.stringify(prices, null, 4), true);
    }); 

    request({ url: "https://moulberry.codes/lowestbin.json", json: true }).then(data => {
        FileLib.write("NozomiAddon", "./data/auction.json", JSON.stringify(data, null, 4), true);
    });

    request({ url: "https://api.hypixel.net/resources/skyblock/items", json: true }).then(data => {
        if (!data.success || !("items" in data)) return;
        const nameToIdMap = {};
        data.items.forEach(item => {
            const displayName = item.name;
            const itemId = item.id;
            if (displayName && itemId) {
                nameToIdMap[displayName] = itemId;
            }
        });
        FileLib.write("NozomiAddon", "./data/items.json", JSON.stringify(nameToIdMap, null, 4), true);
    });

};

register("step", () => {
    updateBazaarAndAuction();
}).setDelay(5);

register("worldUnload", () => {
    waitWorld = true
});
