package dev.shadowsoffire.toastcontrol;

import dev.shadowsoffire.toastcontrol.BetterToastComponent.BetterToastInstance;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.config.ModConfig;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ToastControl {

    public static final KeyMapping CLEAR = new KeyMapping("key.toastcontrol.clear", GLFW.GLFW_KEY_J, "key.toastcontrol.category");
    public static ResourceLocation OVERRIDE = null;
    public static List<ResourceLocation> REPLACEMENT_BLACKLIST = new ArrayList<>();

    public static void keys() {
        if (CLEAR.isDown()) Minecraft.getInstance().getToasts().clear();
    }

    public static void keyReg() {
        KeyBindingHelper.registerKeyBinding(CLEAR);
    }

    public static void preInit() {
        Minecraft.getInstance().toast=new BetterToastComponent();
        ClientTickEvents.END_CLIENT_TICK.register(ToastControl::clientTick);
        handleToastReloc();
        handleBlockedClasses();
        handleBlockedTextures();
    }

    public static void configReg() {
        // register config
        ForgeConfigRegistry.INSTANCE.register("toastcontrol", ModConfig.Type.COMMON, ToastConfig.SPEC);

        // register config handlers
        ModConfigEvents.loading("toastcontrol").register(ToastConfig::onLoad);
        ModConfigEvents.reloading("toastcontrol").register(ToastConfig::onLoad);

    }
    static void handleBlockedTextures() {
        // Background textures are per-toast now and use different fields for each.
        REPLACEMENT_BLACKLIST=new ArrayList<>(ToastConfig.INSTANCE.textureBlacklist.get().stream().map(ResourceLocation::tryParse).filter(Objects::nonNull).toList());
        if (OVERRIDE != null) { REPLACEMENT_BLACKLIST.add(OVERRIDE); }
    }
    static void handleToastReloc() {
        String overrideResourceLocationString = ToastConfig.INSTANCE.globalBackgroundTexture.get();
        if (overrideResourceLocationString != null && !overrideResourceLocationString.isBlank() ) { OVERRIDE = ResourceLocation.tryParse(overrideResourceLocationString); }
        // Toasts have individual textures now replacable via resource pack
        // included textures have been removed.
    }

    public static final List<Class<?>> BLOCKED_CLASSES = new ArrayList<>();

    static void handleBlockedClasses() {
        BLOCKED_CLASSES.clear();
        for (String s : ToastConfig.INSTANCE.blockedClasses.get()) {
            try {
                Class<?> c = Class.forName(s);
                BLOCKED_CLASSES.add(c);
            }
            catch (ClassNotFoundException e) {
                ToastLoader.LOGGER.error("Invalid class string provided to toast control: {}", s);
            }
        }
    }

    public static List<BetterToastInstance<?>> tracker = new ArrayList<>();

    public static void clientTick(Minecraft e) {
            tracker.removeIf(BetterToastInstance::tick);
            keys();
    }

}
