package pl.anty.kostka.bkinventory.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Shadow protected int x;
    @Shadow protected int y;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Podświetlanie można dodać tutaj
    }

    private void drawHighlight(DrawContext context, Slot slot, int borderColor, int fillColor) {
        int realX = this.x + slot.x;
        int realY = this.y + slot.y;

        context.fill(realX, realY, realX + 16, realY + 16, fillColor);
        context.fill(realX, realY, realX + 16, realY + 1, borderColor);
        context.fill(realX, realY + 15, realX + 16, realY + 16, borderColor);
        context.fill(realX, realY + 1, realX + 1, realY + 15, borderColor);
        context.fill(realX + 15, realY + 1, realX + 16, realY + 15, borderColor);
    }
}
