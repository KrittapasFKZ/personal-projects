package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;

import net.otsutsukimiho.nozomiaddon.utils.*;

import java.awt.Color;
import java.util.*;

public class BlockESP implements FeatureManager.Feature {
    private boolean enabled = false;
    private final List<BlockPos> foundBlocks = new ArrayList<>();

    private long lastScanTime = 0;

    private static List<String> getAllBlockNames() {
        List<String> names = new ArrayList<>();
        for (Block block : Registries.BLOCK) {
            String name = block.getName().getString();
            if (!names.contains(name)) {
                names.add(name);
            }
        }
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    public static MultiSelectSetting targetBlocks = new MultiSelectSetting("Target Blocks", getAllBlockNames());
    public static NumberSetting scanRadius = new NumberSetting("Radius", 16, 1, 64, 1);
    public static NumberSetting blockLimit = new NumberSetting("Block Limit", 16, 1, 500, 1);
    public static NumberSetting scanDelay = new NumberSetting("Scan Delay (Ticks)", 20, 10, 100, 1);
    public static ColorSetting highlightColor = new ColorSetting("Color", new Color(255, 255, 255, 255), false);

    @Override
    public List<Settings> getSettings() {
        return List.of(targetBlocks, scanRadius, blockLimit, scanDelay, highlightColor);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.DIAMOND_PICKAXE);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            foundBlocks.clear();
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void initClient() {

    }

    public void find() {
        if (!enabled) {
            foundBlocks.clear();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        long currentTime = System.currentTimeMillis();
        long delayMillis = scanDelay.getValue() * 50L;

        if (currentTime - lastScanTime >= delayMillis) {
            lastScanTime = currentTime;
            scanBlocksOutward(client);
        }

        if (foundBlocks.isEmpty()) return;

        int color = highlightColor.getRGB();
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        for (BlockPos pos : foundBlocks) {
            Box box = new Box(pos);
            Renderer.addBox(box, r, g, b);
        }
    }

    private void scanBlocksOutward(MinecraftClient client) {
        if (client.world == null || client.player == null) return;
        List<String> selectedNames = targetBlocks.getSelectedOptions();

        if (selectedNames.isEmpty()) {
            foundBlocks.clear();
            return;
        }

        List<Block> targetBlockObjects = new ArrayList<>();
        for (Block block : Registries.BLOCK) {
            if (selectedNames.contains(block.getName().getString())) {
                targetBlockObjects.add(block);
            }
        }

        int maxRadius = scanRadius.getValue();
        int maxRadiusSq = maxRadius * maxRadius;
        int limit = blockLimit.getValue();

        BlockPos playerPos = client.player.getBlockPos();
        List<BlockPos> tempBlocks = new ArrayList<>();

        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(playerPos);
        visited.add(playerPos);

        Vec3i[] directions = {
                new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0),
                new Vec3i(0, 1, 0), new Vec3i(0, -1, 0),
                new Vec3i(0, 0, 1), new Vec3i(0, 0, -1)
        };

        while (!queue.isEmpty() && tempBlocks.size() < limit) {
            BlockPos currentPos = queue.poll();

            BlockState state = client.world.getBlockState(currentPos);
            if (!state.isAir() && targetBlockObjects.contains(state.getBlock())) {
                tempBlocks.add(currentPos);
            }

            if (tempBlocks.size() >= limit) {
                break;
            }

            for (Vec3i dir : directions) {
                BlockPos nextPos = currentPos.add(dir);

                if (playerPos.getSquaredDistance(nextPos) <= maxRadiusSq && !visited.contains(nextPos)) {
                    visited.add(nextPos);
                    queue.add(nextPos);
                }
            }
        }

        foundBlocks.clear();
        foundBlocks.addAll(tempBlocks);
    }
}