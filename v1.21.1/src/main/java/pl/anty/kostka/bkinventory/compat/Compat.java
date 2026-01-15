package pl.anty.kostka.bkinventory.compat;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class Compat {
    public static Style withUnderline(Style style, boolean val) {
        return style.withUnderline(val);
    }

    public static Style createLinkStyle(Style baseStyle, String url) {
        return baseStyle
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§7Otwórz: §a" + url)));
    }

    public static int getMainInventorySize(PlayerInventory inventory) {
        return inventory.main.size();
    }

    public static ItemStack getMainStack(PlayerInventory inventory, int index) {
        return inventory.main.get(index);
    }

    public static ItemStack getOffHandStack(PlayerInventory inventory) {
        return inventory.offHand.get(0);
    }

    public static KeyBinding registerSaveKeyBinding() {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bk-inventory.save",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.bk-inventory"));
    }

    public static KeyBinding registerRestoreKeyBinding() {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bk-inventory.restore",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.bk-inventory"));
    }

    public static boolean isCtrlPressed(MinecraftClient client) {
        long window = client.getWindow().getHandle();
        return InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }
}
