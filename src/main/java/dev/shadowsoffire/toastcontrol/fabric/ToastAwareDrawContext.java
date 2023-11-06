package dev.shadowsoffire.toastcontrol.fabric;

// used to track rendering phase to limit modifications to toast rendering
public interface ToastAwareDrawContext {
    public void setDrawingToast(boolean state);
}
