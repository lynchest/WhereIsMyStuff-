package dev.wims;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entrypoint for WhereIsMyStuff? (WIMS).
 * Client-specific initialization is handled in WimsModClient to ensure server-safety.
 */
public class WimsMod implements ModInitializer {
    public static final String MOD_ID = "whereismystuff";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean renderingGhostItem = false;
    public static float ghostItemAlpha = 0.35f;
    public static final java.util.Map<Object, Float> ghostRenderStates = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    public static void log(String message) {
        LOGGER.info("[WIMS-Debug] {}", message);
    }

    @Override
    public void onInitialize() {
        log("WIMS Mod Initialized!");
    }
}
