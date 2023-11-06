package dev.shadowsoffire.toastcontrol;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ToastLoader implements ClientModInitializer {

    public static final String MODID = "toastcontrol";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Override
    public void onInitializeClient() {
        ToastControl.configReg();
        ToastControl.keyReg();
        ToastControl.preInit();
        // Fabric supports environment control. Configured as client only in fabric.mod.json
    }

}
