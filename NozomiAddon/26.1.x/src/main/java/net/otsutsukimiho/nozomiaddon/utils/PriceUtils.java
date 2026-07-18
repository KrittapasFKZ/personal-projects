package net.otsutsukimiho.nozomiaddon.utils;

import com.google.gson.*;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.file.Files;
import java.nio.file.Path;

public class PriceUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static final Path DATA_DIR =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("nozomiaddon");

    private static final Path DATA_FILE = DATA_DIR.resolve("lastUpdate.json");
    private static final Path BZ_FILE = DATA_DIR.resolve("bz.json");
    private static final Path AUCTION_FILE = DATA_DIR.resolve("auction.json");
    private static final Path ITEMS_FILE = DATA_DIR.resolve("items.json");

    private static boolean waitWorld = false;
    private static long lastTickCheck = 0;
    private static JsonObject BZ_CACHE;
    private static JsonObject AUCTION_CACHE;
    private static JsonObject ITEM_ID_CACHE;

    private static JsonObject data;

    public static void init() {
        loadData();
        reloadCaches();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long now = System.currentTimeMillis();
            if (now - lastTickCheck < 60000) return;
            lastTickCheck = now;
            updateBazaarAndAuction();
        });
    }

    public static void onWorldUnload() {
        waitWorld = true;
    }

    private static void reloadCaches() {
        try {
            if (Files.exists(BZ_FILE)) BZ_CACHE = JsonParser.parseString(Files.readString(BZ_FILE)).getAsJsonObject();
            if (Files.exists(AUCTION_FILE)) AUCTION_CACHE = JsonParser.parseString(Files.readString(AUCTION_FILE)).getAsJsonObject();
            if (Files.exists(ITEMS_FILE)) ITEM_ID_CACHE = JsonParser.parseString(Files.readString(ITEMS_FILE)).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateBazaarAndAuction() {
        if (!waitWorld) return;
        waitWorld = false;

        long now = System.currentTimeMillis();
        long lastUpdate = data.get("lastUpdate").getAsLong();

        if (now - lastUpdate < 120_000) return;

        data.addProperty("lastUpdate", now);
        saveJson(DATA_FILE, data);

        fetchBazaar();
        fetchLowestBin();
        fetchItems();
    }

    private static void fetchBazaar() {
        requestJson("https://api.hypixel.net/skyblock/bazaar", json -> {
            if (!json.get("success").getAsBoolean()) return;

            JsonObject products = json.getAsJsonObject("products");
            JsonObject prices = new JsonObject();

            for (String key : products.keySet()) {
                JsonObject quick = products
                        .getAsJsonObject(key)
                        .getAsJsonObject("quick_status");

                JsonObject obj = new JsonObject();
                obj.addProperty("buy", quick.get("buyPrice").getAsDouble());
                obj.addProperty("sell", quick.get("sellPrice").getAsDouble());

                prices.add(key, obj);
            }

            saveJson(BZ_FILE, prices);
        });
    }

    private static void fetchLowestBin() {
        requestJson("https://moulberry.codes/lowestbin.json", json ->
                saveJson(AUCTION_FILE, json)
        );
    }

    private static void fetchItems() {
        requestJson("https://api.hypixel.net/resources/skyblock/items", json -> {
            if (!json.get("success").getAsBoolean()) return;

            JsonArray items = json.getAsJsonArray("items");
            JsonObject nameToId = new JsonObject();

            for (JsonElement e : items) {
                JsonObject item = e.getAsJsonObject();
                if (item.has("name") && item.has("id")) {
                    nameToId.addProperty(
                            item.get("name").getAsString(),
                            item.get("id").getAsString()
                    );
                }
            }

            saveJson(ITEMS_FILE, nameToId);
        });
    }

    private static void requestJson(String url, java.util.function.Consumer<JsonObject> cb) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();

        HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(JsonParser::parseString)
                .thenApply(JsonElement::getAsJsonObject)
                .thenAccept(cb)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private static void loadData() {
        try {
            Files.createDirectories(DATA_DIR);

            if (!Files.exists(DATA_FILE)) {
                data = new JsonObject();
                data.addProperty("lastUpdate", 0L);
                saveJson(DATA_FILE, data);
            } else {
                data = JsonParser.parseString(Files.readString(DATA_FILE))
                        .getAsJsonObject();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveJson(Path path, JsonElement json) {
        try {
            Files.writeString(path, GSON.toJson(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double getBazaarBuyPrice(String itemId) {
        if (BZ_CACHE == null || !BZ_CACHE.has(itemId)) return -1;
        return BZ_CACHE.getAsJsonObject(itemId).get("buy").getAsDouble();
    }

    public static double getBazaarSellPrice(String itemId) {
        if (BZ_CACHE == null || !BZ_CACHE.has(itemId)) return -1;
        return BZ_CACHE.getAsJsonObject(itemId).get("sell").getAsDouble();
    }

    public static double getLowestBin(String itemId) {
        if (AUCTION_CACHE == null || !AUCTION_CACHE.has(itemId)) return -1;
        return AUCTION_CACHE.get(itemId).getAsDouble();
    }

}
