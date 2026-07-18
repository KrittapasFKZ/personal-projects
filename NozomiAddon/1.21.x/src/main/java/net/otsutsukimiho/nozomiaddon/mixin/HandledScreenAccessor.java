package net.otsutsukimiho.nozomiaddon.mixin;


import net.minecraft.client.gui.screen.ingame.HandledScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {

    @Accessor("x")
    int nozomi$getX();

    @Accessor("y")
    int nozomi$getY();
}