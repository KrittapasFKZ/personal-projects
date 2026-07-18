package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.otsutsukimiho.nozomiaddon.features.DungeonTweaks;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteraction {

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void preventChestBreak(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!DUNGEON.inDungeon) return;

        boolean holdingDB = client.player.getMainHandStack().getItem() == Items.DIAMOND_PICKAXE;
        if (!holdingDB) return;
        if (DUNGEON.bossEntry != -1) return;
        Block block = client.world.getBlockState(pos).getBlock();

        if (DungeonTweaks.noBreakChest.isEnabled()) {
            if (block == Blocks.CHEST) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
        if (DungeonTweaks.noBreakLever.isEnabled()) {
            if (block == Blocks.LEVER) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
        if (DungeonTweaks.noBreakSkull.isEnabled()) {
            if (block == Blocks.PLAYER_HEAD || block == Blocks.PLAYER_WALL_HEAD) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }

    }

}
