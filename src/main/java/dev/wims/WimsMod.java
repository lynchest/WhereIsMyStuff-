package dev.wims;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entrypoint for WhereIsMyStuff? (WIMS) client-side mod.
 */
public class WimsMod implements ModInitializer {
    public static final String MOD_ID = "whereismystuff";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("WIMS (WhereIsMyStuff?) Mod Initialized!");
    }
}
