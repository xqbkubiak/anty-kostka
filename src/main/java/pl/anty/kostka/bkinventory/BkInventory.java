package pl.anty.kostka.bkinventory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.anty.kostka.bkinventory.util.ColorUtils;

import static net.minecraft.server.command.CommandManager.literal;

public class BkInventory implements ModInitializer {
    public static final String MOD_ID = "bk-inventory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String PREFIX = "&8[&2B&aK &fInventory&8] ";

    @Override
    public void onInitialize() {
        LOGGER.info("BK-Inventory initialized!");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("bks")
                    .then(literal("save")
                            .executes(context -> {
                                context.getSource()
                                        .sendFeedback(
                                                () -> ColorUtils.translateColorCodes(
                                                        PREFIX + "&fUżyj skrótu &aCtrl+I&f, aby zapisać układ!"),
                                                false);
                                return 1;
                            }))
                    .then(literal("help")
                            .executes(context -> {
                                context.getSource()
                                        .sendFeedback(() -> ColorUtils.translateColorCodes(PREFIX + "&7Pomoc:"), false);
                                context.getSource().sendFeedback(() -> ColorUtils.translateColorCodes(
                                        "&8- &aCtrl+I &8- &7Zapisuje układ (najechanie na slot zapisuje tylko ten slot)"),
                                        false);
                                context.getSource()
                                        .sendFeedback(() -> ColorUtils
                                                .translateColorCodes("&8- &aCtrl+O &8- &7Przywraca zapisany układ"),
                                                false);
                                return 1;
                            })));
        });
    }
}
