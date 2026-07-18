package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;

import net.minecraft.util.math.Vec3d;
import net.otsutsukimiho.nozomiaddon.utils.*;
import net.otsutsukimiho.nozomiaddon.utils.events.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SAHighlight implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private boolean enabled = false;
    private final List<StarredBox> starredBoxes = new ArrayList<>();

    public static BooleanSetting tpAlert = new BooleanSetting("TP Alert", false);
    public static TextSetting tpMessage = new TextSetting("TP Message", "&c&lSA Jumping!");
    public static SoundSetting customSound = new SoundSetting("AlertSound", "minecraft:entity.blaze.hurt", 1.0f, 1.0f);
    public static ColorSetting highlightColor = new ColorSetting("Color", new Color(255, 0, 0, 255), false);
    @Override
    public List<Settings> getSettings() {
        return List.of(tpAlert, tpMessage, highlightColor, customSound);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("ewogICJ0aW1lc3RhbXAiIDogMTU4OTEzNzY1ODgxOSwKICAicHJvZmlsZUlkIiA6ICJlM2I0NDVjODQ3ZjU0OGZiOGM4ZmEzZjFmN2VmYmE4ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaW5pRGlnZ2VyVGVzdCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zMzk5ZTAwZjQwNDQxMWU0NjVkNzQzODhkZjEzMmQ1MWZlODY4ZWNmODZmMWMwNzNmYWZmYTFkOTE3MmVjMGYzIgogICAgfQogIH0KfQ==");
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            starredBoxes.clear();
        });
        EventBus.register(SAHighlight.class, PacketEvent.Receive.class, event -> {
            if (!enabled) return;
            if (!SAHighlight.tpAlert.isEnabled()) return;
            if (!DUNGEON.inDungeon) return;
            int warningTime = -1;
            if (event.packet instanceof WorldBorderInitializeS2CPacket packet) {
                warningTime = packet.getWarningTime();
            } else if (event.packet instanceof WorldBorderWarningTimeChangedS2CPacket packet) {
                warningTime = packet.getWarningTime();
            }
            if (warningTime == 10000) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    customSound.playTestSound();
                    client.player.sendMessage(Text.literal("§d§lNA §f§l» §c§lShadow Assassin Jumping!"), false);

                    String rawMessage = SAHighlight.tpMessage.getValue();
                    if (rawMessage == null) rawMessage = " ";
                    Text formattedText = ColorUtils.parseColor(rawMessage.trim());

                    client.inGameHud.setTitle(Text.literal(" "));
                    client.inGameHud.setSubtitle(formattedText);
                    client.inGameHud.setTitleTicks(0, 50, 10);
                }
            }
        });
    }

    public void find() {
        if (!enabled) return;
        if (!DUNGEON.inDungeon) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        if (client.player == null) return;

        starredBoxes.removeIf(box -> box.getMob().isRemoved());

        Box worldBox = client.player.getBoundingBox().expand(80);

        List<PlayerEntity> plrs =
                client.world.getEntitiesByClass(
                        PlayerEntity.class,
                        worldBox,
                        plr -> true
                );

        for (PlayerEntity plr : plrs) {
            Text name = plr.getName();
            if (name == null) continue;

            String raw = name.getString();
            if (raw.contains("Shadow Assassin")) {
                boolean exists = starredBoxes.stream().anyMatch(b -> b.getMob() == plr);
                if (!exists) {
                    starredBoxes.add(new StarredBox(plr));
                }
            }
        }

        for (StarredBox box : starredBoxes) {
            box.update();
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        starredBoxes.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static class StarredBox {
        private final PlayerEntity entity;

        public StarredBox(PlayerEntity entity) {
            this.entity = entity;
        }

        public void update() {
            if (entity.isRemoved()) return;

            float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true);
            Vec3d pos = entity.getLerpedPos(tickDelta);

            double x = pos.getX();
            double y = pos.getY();
            double z = pos.getZ();

            int color = SAHighlight.highlightColor.getRGB();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            Box box = new Box(x - 0.5, y, z - 0.5, x + 0.5,y + 2.0, z + 0.5);
            Renderer.addBox(box.expand(-0.2,0,-0.2), r, g, b);
        }

        public PlayerEntity getMob() {
            return entity;
        }
    }

}