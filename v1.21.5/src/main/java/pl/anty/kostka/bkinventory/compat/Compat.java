package pl.anty.kostka.bkinventory.compat;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import org.lwjgl.glfw.GLFW;

public class Compat {
    public static Style withUnderline(Style style, boolean val) {
        return style.withUnderline(val);
    }

    public static Style createLinkStyle(Style baseStyle, String url) {
        // ClickEvent/HoverEvent became abstract in 1.21.5+
        return baseStyle;
    }

    public static int getMainInventorySize(PlayerInventory inventory) {
        return 36;
    }

    public static ItemStack getMainStack(PlayerInventory inventory, int index) {
        return inventory.getStack(index);
    }

    public static ItemStack getOffHandStack(PlayerInventory inventory) {
        return inventory.getStack(40);
    }

    public static KeyBinding registerSaveKeyBinding() {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bk-inventory.save",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.bk-inventory"));
    }

    public static KeyBinding registerRestoreKeyBinding() {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bk-inventory.restore",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_E,
                "category.bk-inventory"));
    }

    public static boolean isCtrlPressed(MinecraftClient client) {
        long window = client.getWindow().getHandle();
        return InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }
}
