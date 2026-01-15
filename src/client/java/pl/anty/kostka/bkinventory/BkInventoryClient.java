package pl.anty.kostka.bkinventory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.glfw.GLFW;
import pl.anty.kostka.bkinventory.util.ColorUtils;
import pl.anty.kostka.bkinventory.util.InventoryRestoreController;
import pl.anty.kostka.bkinventory.mixin.HandledScreenAccessor;

public class BkInventoryClient implements ClientModInitializer {
    public static final String PREFIX = "&8[&2B&aK &fInventory&8] ";

    private static KeyBinding saveKeyBinding;
    private static KeyBinding restoreKeyBinding;

    @Override
    public void onInitializeClient() {
        saveKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bk-inventory.save",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.bk-inventory"));

        restoreKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bk-inventory.restore",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_E,
                "category.bk-inventory"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null)
                return;

            long window = client.getWindow().getHandle();
            boolean isCtrlPressed = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                    InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_CONTROL);

            InventoryRestoreController.tick(client);

            while (saveKeyBinding.wasPressed()) {
                if (isCtrlPressed) {
                    handleSaveAction(client);
                }
            }

            while (restoreKeyBinding.wasPressed()) {
                if (isCtrlPressed) {
                    if (!InventoryRestoreController.isActive()) {
                        client.player.sendMessage(ColorUtils.translateColorCodes(PREFIX + "&fPrzywracanie układu..."),
                                false);
                        InventoryRestoreController.start();
                    } else {
                        InventoryRestoreController.stop();
                        client.player.sendMessage(ColorUtils.translateColorCodes(PREFIX + "&cZatrzymano przywracanie."),
                                false);
                    }
                }
            }
        });
    }

    private void handleSaveAction(MinecraftClient client) {
        if (client.currentScreen instanceof HandledScreen<?> handledScreen) {
            Slot focusedSlot = ((HandledScreenAccessor) handledScreen).getFocusedSlot();

            if (focusedSlot != null) {
                int invIdx = guiSlotToInvIdx(focusedSlot, client.player.currentScreenHandler);
                if (invIdx != -1) {
                    InventoryRestoreController.saveSlot(invIdx, focusedSlot.getStack());
                    client.player.sendMessage(
                            ColorUtils.translateColorCodes(PREFIX + "&7Slot &b#" + invIdx + " &7został &azapisany&7!"),
                            false);
                } else {
                    client.player.sendMessage(
                            ColorUtils.translateColorCodes(PREFIX + "&cTo nie jest odpowiedni slot do zapisu."), false);
                }
                return;
            }
        }

        InventoryRestoreController.saveFullInventory(client.player.getInventory());
        client.player.sendMessage(
                ColorUtils.translateColorCodes(PREFIX + "&7Cały układ ekwipunku został &azapisany&7!"), false);
    }

    private int guiSlotToInvIdx(Slot slot, net.minecraft.screen.ScreenHandler handler) {
        int totalSlots = handler.slots.size();
        int playerStart = totalSlots - 36;
        if (slot.id < playerStart)
            return -1;

        int rel = slot.id - playerStart;
        if (rel >= 27)
            return rel - 27; // Hotbar
        return rel + 9; // Main
    }
}
