package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DioritePillar implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private boolean enabled = false;
    private int tickCounter = 0;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.DIORITE);
    }

    private static final Block[] PILLAR_COLORS = {
            Blocks.LIME_STAINED_GLASS,
            Blocks.YELLOW_STAINED_GLASS,
            Blocks.PURPLE_STAINED_GLASS,
            Blocks.RED_STAINED_GLASS
    };

    private static final BlockPos[] PILLAR_CENTERS = {
            new BlockPos(46, 169, 41),
            new BlockPos(46, 169, 65),
            new BlockPos(100, 169, 65),
            new BlockPos(100, 169, 41)
    };

    private final List<List<BlockPos>> pillarCoordinates = new ArrayList<>();

    public DioritePillar() {
        for (BlockPos pillarCenter : PILLAR_CENTERS) {
            List<BlockPos> list = new ArrayList<>();
            BlockPos center = pillarCenter;
            for (int dx = center.getX() - 3; dx <= center.getX() + 3; dx++) {
                for (int dy = center.getY(); dy <= center.getY() + 37; dy++) {
                    for (int dz = center.getZ() - 3; dz <= center.getZ() + 3; dz++) {
                        list.add(new BlockPos(dx, dy, dz));
                    }
                }
            }
            pillarCoordinates.add(list);
        }
    }

    public void initClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled || client.world == null) return;
            if (!DUNGEON.inDungeon) return;
            if (!DungeonRunOverview.dungeonFloor.contains("7")) return;

            tickCounter++;
            if (tickCounter >= 5) {
                tickCounter = 0;
                replaceDiorite(client);
            }
        });
    }

    private void replaceDiorite(MinecraftClient client) {
        if (client.world == null) return;

        for (int pillarIndex = 0; pillarIndex < 4; pillarIndex++) {
            List<BlockPos> coordinates = pillarCoordinates.get(pillarIndex);

            for (BlockPos pos : coordinates) {
                BlockState state = client.world.getBlockState(pos);
                Block block = state.getBlock();

                if (block == Blocks.DIORITE || block == Blocks.POLISHED_DIORITE) {
                    setGlass(client, pos, pillarIndex);
                }
            }
        }
    }

    private void setGlass(MinecraftClient client, BlockPos pos, int pillarIndex) {
        if (client.world == null) return;
        Block replacementGlass;
        replacementGlass = PILLAR_COLORS[pillarIndex];
        client.world.setBlockState(pos, replacementGlass.getDefaultState());
    }
}