package pl.anty.kostka.bkinventory.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import pl.anty.kostka.bkinventory.compat.Compat;
import pl.anty.kostka.bkinventory.util.ColorUtils;

import java.util.HashMap;
import java.util.Map;

public class InventoryRestoreController {
    private static final Map<Integer, ItemStack> savedLayout = new HashMap<>();
    private static boolean active = false;
    private static long lastActionMs = 0;
    private static int failTicks = 0;

    public static void saveSlot(int inventoryIndex, ItemStack stack) {
        if (stack.isEmpty()) {
            savedLayout.remove(inventoryIndex);
        } else {
            savedLayout.put(inventoryIndex, stack.copy());
        }
    }

    public static void saveFullInventory(PlayerInventory inventory) {
        savedLayout.clear();
        for (int i = 0; i < Compat.getMainInventorySize(inventory); i++) {
            ItemStack stack = Compat.getMainStack(inventory, i);
            if (!stack.isEmpty()) {
                savedLayout.put(i, stack.copy());
            }
        }
        // Save Off-hand (index 40)
        ItemStack offHand = Compat.getOffHandStack(inventory);
        if (!offHand.isEmpty()) {
            savedLayout.put(40, offHand.copy());
        }
    }

    public static void start() {
        if (savedLayout.isEmpty()) {
            return;
        }
        active = true;
        failTicks = 0;
    }

    public static boolean isActive() {
        return active;
    }

    public static void stop() {
        active = false;
    }

    public static void tick(MinecraftClient client) {
        if (!active || client.player == null || client.interactionManager == null)
            return;

        ScreenHandler handler = client.player.currentScreenHandler;
        if (handler == null) {
            active = false;
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastActionMs < 25)
            return;

        ItemStack cursorStack = handler.getCursorStack();

        if (cursorStack.isEmpty()) {
            for (Map.Entry<Integer, ItemStack> entry : savedLayout.entrySet()) {
                int targetInvIdx = entry.getKey();
                ItemStack targetStack = entry.getValue();

                int guiSlotId = invIdxToGuiSlotId(targetInvIdx, handler);
                if (guiSlotId == -1)
                    continue;

                ItemStack currentStack = handler.getSlot(guiSlotId).getStack();

                if (!isSameItem(currentStack, targetStack)) {
                    int sourceGuiSlotId = findItemInInventory(handler, targetStack);
                    if (sourceGuiSlotId != -1) {
                        clickSlot(client, handler, sourceGuiSlotId);
                        lastActionMs = now;
                        return;
                    }
                }
            }

            active = false;
            client.player.sendMessage(ColorUtils.translateColorCodes("&8[&2B&aK &fInventory&8] &aUkład przywrócony!"),
                    false);
        } else {
            int targetInvIdx = -1;
            for (Map.Entry<Integer, ItemStack> entry : savedLayout.entrySet()) {
                if (isSameItem(cursorStack, entry.getValue())) {
                    int guiId = invIdxToGuiSlotId(entry.getKey(), handler);
                    if (guiId != -1 && !isSameItem(handler.getSlot(guiId).getStack(), entry.getValue())) {
                        targetInvIdx = entry.getKey();
                        break;
                    }
                }
            }

            if (targetInvIdx != -1) {
                int guiSlotId = invIdxToGuiSlotId(targetInvIdx, handler);
                clickSlot(client, handler, guiSlotId);
                lastActionMs = now;
            } else {
                int freeSlot = findFreeUselessSlot(handler);
                if (freeSlot != -1) {
                    clickSlot(client, handler, freeSlot);
                } else {
                    active = false;
                }
                lastActionMs = now;
            }
        }

        failTicks++;
        if (failTicks > 100) {
            active = false;
        }
    }

    private static boolean isSameItem(ItemStack s1, ItemStack s2) {
        if (s1.isEmpty() || s2.isEmpty())
            return s1.isEmpty() == s2.isEmpty();
        return ItemStack.areItemsAndComponentsEqual(s1, s2);
    }

    private static int invIdxToGuiSlotId(int invIdx, ScreenHandler handler) {

        if (handler instanceof net.minecraft.screen.PlayerScreenHandler) {
            if (invIdx >= 0 && invIdx <= 8)
                return 36 + invIdx;
            if (invIdx >= 9 && invIdx <= 35)
                return invIdx;
            if (invIdx == 40)
                return 45;
        }

        int totalSlots = handler.slots.size();
        if (totalSlots >= 36) {
            int startOfPlayerInv = totalSlots - 36;
            if (invIdx >= 0 && invIdx <= 8)
                return startOfPlayerInv + 27 + invIdx;
            if (invIdx >= 9 && invIdx <= 35)
                return startOfPlayerInv + (invIdx - 9);
        }

        return -1;
    }

    private static int findItemInInventory(ScreenHandler handler, ItemStack target) {
        for (int i = 0; i < handler.slots.size(); i++) {
            Slot slot = handler.getSlot(i);
            if (slot.inventory instanceof PlayerInventory && slot.hasStack()) {
                int invIdx = guiSlotToInvIdx(i, handler);

                int playerIdx = slot.getIndex();
                if (playerIdx >= 36 && playerIdx <= 39)
                    continue;

                if (isSameItem(slot.getStack(), target)) {
                    if (invIdx != -1) {
                        ItemStack shouldBeHere = savedLayout.get(invIdx);
                        if (shouldBeHere != null && isSameItem(slot.getStack(), shouldBeHere)) {
                            continue;
                        }
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    private static int guiSlotToInvIdx(int guiSlotId, ScreenHandler handler) {
        if (guiSlotId < 0 || guiSlotId >= handler.slots.size())
            return -1;
        Slot slot = handler.getSlot(guiSlotId);

        if (slot.inventory instanceof PlayerInventory) {
            int index = slot.getIndex();
            if (index >= 0 && index <= 35)
                return index;
            if (index == 40)
                return 40;
        }
        return -1;
    }

    private static int findFreeUselessSlot(ScreenHandler handler) {
        for (int i = 0; i < handler.slots.size(); i++) {
            Slot slot = handler.getSlot(i);
            if (slot.inventory instanceof PlayerInventory) {
                int playerIdx = slot.getIndex();
                if (playerIdx >= 36 && playerIdx <= 39)
                    continue;

                int invIdx = guiSlotToInvIdx(i, handler);
                if (!slot.hasStack() && (invIdx == -1 || !savedLayout.containsKey(invIdx))) {
                    return i;
                }
            }
        }
        for (int i = 0; i < handler.slots.size(); i++) {
            Slot slot = handler.getSlot(i);
            if (slot.inventory instanceof PlayerInventory) {
                int playerIdx = slot.getIndex();
                if (playerIdx >= 36 && playerIdx <= 39)
                    continue;

                int invIdx = guiSlotToInvIdx(i, handler);
                if (invIdx != -1) {
                    ItemStack shouldBeHere = savedLayout.get(invIdx);
                    if (shouldBeHere == null || !isSameItem(slot.getStack(), shouldBeHere)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private static void clickSlot(MinecraftClient client, ScreenHandler handler, int slotId) {
        client.interactionManager.clickSlot(handler.syncId, slotId, 0, SlotActionType.PICKUP, client.player);
    }
}
