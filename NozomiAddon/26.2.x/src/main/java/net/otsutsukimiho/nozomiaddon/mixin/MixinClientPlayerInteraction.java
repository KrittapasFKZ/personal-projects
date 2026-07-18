package net.otsutsukimiho.nozomiaddon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.otsutsukimiho.nozomiaddon.features.DungeonTweaks;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

@Mixin(MultiPlayerGameMode.class)
public class MixinClientPlayerInteraction {

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void preventChestBreak(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        if (!DUNGEON.inDungeon) return;

        boolean holdingDB = client.player.getMainHandItem().getItem() == Items.DIAMOND_PICKAXE;
        if (!holdingDB) return;
        if (DUNGEON.bossEntry != -1) return;
        Block block = client.level.getBlockState(pos).getBlock();

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
