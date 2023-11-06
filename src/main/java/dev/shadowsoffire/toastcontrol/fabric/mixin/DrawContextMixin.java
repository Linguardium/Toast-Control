package dev.shadowsoffire.toastcontrol.fabric.mixin;

import dev.shadowsoffire.toastcontrol.ToastConfig;
import dev.shadowsoffire.toastcontrol.fabric.ToastAwareDrawContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.shadowsoffire.toastcontrol.ToastControl.OVERRIDE;
import static dev.shadowsoffire.toastcontrol.ToastControl.REPLACEMENT_BLACKLIST;

@Mixin(GuiGraphics.class)
public abstract class DrawContextMixin implements ToastAwareDrawContext {


    @Shadow abstract void innerBlit(ResourceLocation resourceLocation, int i, int j, int k, int l, int m, float f, float g, float h, float n, float o, float p, float q, float r);

    @Shadow @Final private GuiSpriteManager sprites;

    // While rendering should only be on the render thread, this ensures that our modifications are thread locked to the thread handling the toast rendering.
    ThreadLocal<Boolean> drawingToast = ThreadLocal.withInitial(()->false);

    @Override
    public void setDrawingToast(boolean state) {
        drawingToast.set(state);
    }

    // handles opacity by calling alternate drawGuiTexture that takes color and alpha values if opacity is not opaque
    @Inject(method="innerBlit(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFF)V",cancellable = true,at=@At("HEAD"))
    private void checkTranslucentTransparentOptions(ResourceLocation resourceLocation, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, CallbackInfo ci) {
        if (drawingToast.get()) {
            float opacity = ToastConfig.INSTANCE.opacity.get().floatValue();
            if (opacity < 1.0f) {
                innerBlit(resourceLocation,x1,x2,y1,y2,z,u1,u2,v1,v2,1,1,1,opacity);
                ci.cancel();
            }
        }
    }

    // Targets the drawGuiTexture method and replaces the Identifier it receives with the override if the override is set
    @ModifyVariable(
            method = {
//                    "blitSprite(Lnet/minecraft/resources/ResourceLocation;IIIIIIIII)V",
//                    Above can be uncommented if background images are not all being targeted
                    "blitSprite(Lnet/minecraft/resources/ResourceLocation;IIIII)V"
            },
            at=@At("HEAD"),
            argsOnly = true
    )
    private ResourceLocation useOverrideToastBackground(ResourceLocation original) {
        if (drawingToast.get() && OVERRIDE != null && !REPLACEMENT_BLACKLIST.contains(original)) {
            if (spritesAtlasContains(OVERRIDE)) { return OVERRIDE; }
        }
        return original;
    }

    // because its cleaner this way
    @Unique
    private boolean spritesAtlasContains(ResourceLocation identifier) {
        return sprites.textureAtlas.texturesByName.containsKey(identifier);
    }
}
