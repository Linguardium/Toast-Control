package dev.shadowsoffire.toastcontrol;


import net.minecraft.client.gui.components.toasts.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

// original class extends Placebo BlockedDeque class

public class ControlledDeque extends ArrayDeque<Toast> {

    public boolean isBlocked(Toast toast) {
        if (ToastConfig.INSTANCE.printClasses.get()) ToastLoader.LOGGER.info(toast.getClass());
        if (ToastConfig.INSTANCE.global.get() || ToastConfig.INSTANCE.globalVanilla.get() && this.isVanillaToast(toast)) return true;
        if (ToastConfig.INSTANCE.globalModded.get() && !this.isVanillaToast(toast)) return true;
        if (ToastControl.BLOCKED_CLASSES.contains(toast.getClass())) return true;
        return toast instanceof AdvancementToast && ToastConfig.INSTANCE.advancements.get() || toast instanceof RecipeToast && ToastConfig.INSTANCE.recipes.get() || toast instanceof SystemToast && ToastConfig.INSTANCE.system.get()
            || toast instanceof TutorialToast && ToastConfig.INSTANCE.tutorial.get();
    }

    private boolean isVanillaToast(Toast toast) {
        return toast instanceof AdvancementToast || toast instanceof RecipeToast || toast instanceof SystemToast || toast instanceof TutorialToast;
    }

    // methods pulled in from placebo https://github.com/Shadows-of-Fire/Placebo/blob/1.20/src/main/java/dev/shadowsoffire/placebo/collections/BlockedDeque.java
    @Override
    public void addFirst(@NotNull Toast toast) {
        if (!isBlocked(toast)) super.addFirst(toast);
    }
    @Override
    public void addLast(@NotNull Toast toast) {
        if (!isBlocked(toast)) super.addFirst(toast);
    }

    @Override
    public boolean add(@NotNull Toast toast) {
        return !isBlocked(toast) && super.add(toast);
    }

    @Override
    public boolean offerFirst(@NotNull Toast toast) {
        return !isBlocked(toast) && super.offerFirst(toast);
    }

    @Override
    public boolean offerLast(@NotNull Toast toast) {
        return !isBlocked(toast) && super.offerLast(toast);
    }

}
