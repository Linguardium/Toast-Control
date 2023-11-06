package dev.shadowsoffire.toastcontrol;

import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class ToastConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ToastConfig INSTANCE;
    static {
        Pair<ToastConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ToastConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }

    public final BooleanValue advancements;
    public final BooleanValue recipes;
    public final BooleanValue system;
    public final BooleanValue tutorial;
    public final BooleanValue globalVanilla;
    public final BooleanValue globalModded;
    public final BooleanValue global;
    public final ConfigValue<List<? extends String>> blockedClasses;

    public final IntValue forceTime;
    public final BooleanValue translucent;
    public final BooleanValue transparent;
    public final IntValue toastCount;
    public final IntValue offsetX;
    public final IntValue offsetY;
    public final BooleanValue noSlide;
    public final BooleanValue startLeft;
    public final BooleanValue topDown;

    public final BooleanValue printClasses;

    public final DoubleValue opacity;
    public final ConfigValue<String> globalBackgroundTexture;
    public final ConfigValue<List<? extends String>> textureBlacklist;

    public ToastConfig(ForgeConfigSpec.Builder build) {
        build.comment("Client Configuration").push("client").push("blocked_toasts");

        this.advancements = build.comment("If advancement toasts are blocked.").define("advancements", true);
        this.recipes = build.comment("If recipe toasts are blocked.").define("recipes", true);
        this.system = build.comment("If system toasts are blocked.").define("system", false);
        this.tutorial = build.comment("If tutorial toasts are blocked.").define("tutorial", true);
        this.globalVanilla = build.comment("If all vanilla toasts are blocked.").define("global_vanilla", false);
        this.globalModded = build.comment("If all non-vanilla toasts are blocked.").define("global_modded", false);
        this.global = build.comment("If all toasts are blocked.").define("global", false);
        this.blockedClasses = build.comment("Toast Classes that are blocked from being shown.").defineList("blocked_classes", new ArrayList<>(), Predicates.alwaysTrue());

        build.pop().push("visual_options");
        this.globalBackgroundTexture = build.comment("A custom background texture path that overrides all toast backgrounds.").define("global_background_texture","");
        this.textureBlacklist = build.comment("Texture locations that should not be replaced. These are relative references not absolute: 'minecraft:toast/advancement' not 'minecraft:textures/gui/sprites/toast/advancement.png'").defineList("texture_blacklist",new ArrayList<>(),o->ResourceLocation.isValidResourceLocation((String)o));
        this.forceTime = build.comment("How long a toast must be on the screen for, in ticks.  Use 0 to use the default time.").defineInRange("force_time", 0, 0, 4000);
        this.translucent = build.comment("deprecated. please set opacity above 0.0 and below 1.0 or use a resource pack to control translucency.").define("translucent", false);
        this.transparent = build.comment("deprecated. please set opacity to 0.0 to make toasts transparent").define("transparent", false);
        this.opacity = build.comment("Sets opacity for toast textures. 0.0 is transparent, 1.0 is opaque").defineInRange("opacity",1.0,0.0,1.0);
        this.toastCount = build.comment("How many toasts will be displayed on screen at once.").defineInRange("toast_count", 3, 1, 7);
        this.offsetX = build.comment("The X offset for toasts to be drawn at.").defineInRange("x_offset", 0, -8192, 8192);
        this.offsetY = build.comment("The Y offset for toasts to be drawn at.").defineInRange("y_offset", 0, -8192, 8192);
        this.noSlide = build.comment("If toasts automatically pop into the screen without animations.").define("no_slide", false);
        this.startLeft = build.comment("If toasts show on the left of the screen.").define("start_left", false);
        this.topDown = build.comment("If toasts will come in from the top of the screen, rather than the side.").define("top_down", false);

        build.pop().push("debug");
        this.printClasses = build.comment("If toast classes are printed when they are shown.").define("print_classes", false);
        build.pop().pop();
    }

    public static <T extends ModConfig> void onLoad(T config) {
        if (ToastLoader.MODID.equals(config.getModId())) {
            ToastControl.handleToastReloc();
            ToastControl.handleBlockedClasses();
            Minecraft.getInstance().getToasts().occupiedSlots = new BitSet(INSTANCE.toastCount.get());
            ToastLoader.LOGGER.info("Toast control config reloaded.");
        }
    }

}
