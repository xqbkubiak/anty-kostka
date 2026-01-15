package pl.anty.kostka.bkinventory.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;

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
        for (int i = 0; i < inventory.main.size(); i++) {
            ItemStack stack = inventory.main.get(i);
            if (!stack.isEmpty()) {
                savedLayout.put(i, stack.copy());
            }
        }
    }

    public static void start() {
        if (savedLayout.isEmpty()) return;
        active = true;
        failTicks = 0;
    }

    public static boolean isActive() { return active; }
    public static void stop() { active = false; }

    public static void tick(MinecraftClient client) {
        if (!active || client.player == null || client.interactionManager == null) return;

        ScreenHandler handler = client.player.currentScreenHandler;
        if (handler == null) { active = false; return; }

        long now = System.currentTimeMillis();
        if (now - lastActionMs < 25) return;

        ItemStack cursorStack = handler.getCursorStack();
        
        if (cursorStack.isEmpty()) {
            for (Map.Entry<Integer, ItemStack> entry : savedLayout.entrySet()) {
                int targetInvIdx = entry.getKey();
                ItemStack targetStack = entry.getValue();
                int guiSlotId = invIdxToGuiSlotId(targetInvIdx, handler);
                if (guiSlotId == -1) continue;
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
            client.player.sendMessage(ColorUtils.translateColorCodes("&8[&2B&aK &fInventory&8] &aUkład przywrócony!"), false);
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
                clickSlot(client, handler, invIdxToGuiSlotId(targetInvIdx, handler));
                lastActionMs = now;
            } else {
                int freeSlot = findFreeUselessSlot(handler);
                if (freeSlot != -1) clickSlot(client, handler, freeSlot);
                else active = false;
                lastActionMs = now;
            }
        }
        failTicks++;
        if (failTicks > 100) active = false;
    }

    private static boolean isSameItem(ItemStack s1, ItemStack s2) {
        if (s1.isEmpty() || s2.isEmpty()) return s1.isEmpty() == s2.isEmpty();
        return ItemStack.areItemsAndComponentsEqual(s1, s2);
    }

    private static int invIdxToGuiSlotId(int invIdx, ScreenHandler handler) {
        if (handler instanceof net.minecraft.screen.PlayerScreenHandler) {
            if (invIdx >= 0 && invIdx <= 8) return 36 + invIdx;
            if (invIdx >= 9 && invIdx <= 35) return invIdx;
        } 
        int totalSlots = handler.slots.size();
        if (totalSlots >= 36) {
            int startOfPlayerInv = totalSlots - 36;
            if (invIdx >= 0 && invIdx <= 8) return startOfPlayerInv + 27 + invIdx;
            if (invIdx >= 9 && invIdx <= 35) return startOfPlayerInv + (invIdx - 9);
        }
        return -1;
    }

    private static int findItemInInventory(ScreenHandler handler, ItemStack target) {
        int totalSlots = handler.slots.size();
        int playerStart = totalSlots - 36;
        if (playerStart < 0) return -1;
        for (int i = playerStart; i < totalSlots; i++) {
            if (isSameItem(handler.getSlot(i).getStack(), target)) {
                int invIdx = guiSlotToInvIdx(i, handler);
                if (invIdx != -1) {
                    ItemStack shouldBeHere = savedLayout.get(invIdx);
                    if (shouldBeHere != null && isSameItem(handler.getSlot(i).getStack(), shouldBeHere)) continue;
                }
                return i;
            }
        }
        return -1;
    }

    private static int guiSlotToInvIdx(int guiSlotId, ScreenHandler handler) {
        int totalSlots = handler.slots.size();
        int playerStart = totalSlots - 36;
        if (guiSlotId < playerStart) return -1;
        int rel = guiSlotId - playerStart;
        if (rel >= 27) return rel - 27;
        return rel + 9;
    }

    private static int findFreeUselessSlot(ScreenHandler handler) {
        int totalSlots = handler.slots.size();
        int playerStart = totalSlots - 36;
        for (int i = playerStart; i < totalSlots; i++) {
            int invIdx = guiSlotToInvIdx(i, handler);
            if (handler.getSlot(i).getStack().isEmpty() && (invIdx == -1 || !savedLayout.containsKey(invIdx))) return i;
        }
        for (int i = playerStart; i < totalSlots; i++) {
            int invIdx = guiSlotToInvIdx(i, handler);
            if (invIdx != -1) {
                ItemStack shouldBeHere = savedLayout.get(invIdx);
                if (shouldBeHere == null || !isSameItem(handler.getSlot(i).getStack(), shouldBeHere)) return i;
            }
        }
        return -1;
    }

    private static void clickSlot(MinecraftClient client, ScreenHandler handler, int slotId) {
        client.interactionManager.clickSlot(handler.syncId, slotId, 0, SlotActionType.PICKUP, client.player);
    }
}
