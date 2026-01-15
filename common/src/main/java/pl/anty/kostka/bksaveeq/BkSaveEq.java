package pl.anty.kostka.bksaveeq;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BkSaveEq implements ModInitializer {
    public static final String MOD_ID = "bk-saveeq";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("BK-SaveEQ initialized for Minecraft 1.21.4!");
    }
}
