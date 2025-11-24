package com.schwitzer.schwitzersHelp.macros;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import com.schwitzer.schwitzersHelp.util.InventoryUtil;
import com.schwitzer.schwitzersHelp.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.LinkedList;
import java.util.List;

public class CollectStashMacro implements Macro {
    private SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    private MacroController.MacroState macroControllerState = MacroController.MacroState.DISABLED;
    private MacroState currentState = MacroState.OPEN_STASH;
    private MacroState previousState = MacroState.DISABLED;
    private int tickcounter;
    private LinkedList<String> itemList = new LinkedList<>();
    private int indexOfButton;
    private List<String> betterFormOfItemList = new LinkedList<>();
    private List<String> itemBlackList = new LinkedList<>();
    private int atItemIndex;
    private boolean itemsCollected = false;

    private enum MacroState {
        OPEN_STASH, WAIT_FOR_CONTAINER_TO_OPEN, GET_ITEMS, CLOSE_MENU, FILL_INTO_SACKS, CLICK_SUPERCRAFT, OPEN_BAZZAR, OPEN_SUPER_CRAFT,
        SELECT_ITEM, DISABLED, FINAL_SUPERCRAFT_CLICK, CLICK_SUPERCRAFT_AGAIN, AFTER_SUPERCRAFT, CLOSE_SUPERCRAFT_MENU
    }

    // Hilfsmethode zum Entfernen von Farbcodes


    // Neue Hilfsmethode zum Verarbeiten von Ingot-Namen
    private String processItemName(String itemName) {
        if (itemName == null) return null;

        // Prüfen ob der Name mit "_ingot" endet
        if (itemName.endsWith("_ingot")) {
            // "_ingot" entfernen
            return itemName.substring(0, itemName.length() - 6); // 6 = Länge von "_ingot"
        }

        return itemName;
    }

    // Hilfsmethode für Shift+Click
    private void performShiftClick(int slotIndex, int mouseButton, int ShiftButton, String actionDescription) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof GuiContainer && slotIndex >= 0) {
            GuiContainer gui = (GuiContainer) mc.currentScreen;
            ChatUtil.debugMessage("Attempting to " + actionDescription + " slot: " + slotIndex);

            // Shift-Taste aktivieren
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);

            // Click ausführen
            mc.playerController.windowClick(
                    gui.inventorySlots.windowId,
                    slotIndex,
                    mouseButton,
                    ShiftButton, // Click Type (1 = shift click)
                    mc.thePlayer
            );

            // Shift-Taste deaktivieren
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);

            ChatUtil.debugMessage(actionDescription + " executed!");
        } else {
            ChatUtil.debugMessage("Cannot click: GUI not open or button index invalid (" + slotIndex + ")");
        }
    }

    private void setState(MacroState newState) {
        previousState = currentState;
        currentState = newState;
    }

    private boolean isGuiOpen() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.currentScreen != null;
    }

    @Override
    public void onEnable() {
        currentState = MacroState.OPEN_STASH;
        previousState = MacroState.DISABLED;
        macroControllerState = MacroController.MacroState.ENABLED;
        tickcounter = 0;
        atItemIndex = 0;
        itemsCollected = false;
        // Listen leeren beim neuen Start
        itemList.clear();
        betterFormOfItemList.clear();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        currentState = MacroState.DISABLED;
        previousState = MacroState.DISABLED;
        macroControllerState = MacroController.MacroState.DISABLED;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public String getName() {
        return "Stash item collector";
    }

    @Override
    public MacroController.MacroState getState() {
        return macroControllerState;
    }

    @Override
    public void setState(MacroController.MacroState state) {
        this.macroControllerState = state;
        if (state == MacroController.MacroState.DISABLED) {
            onDisable();
        }
    }

    // [Bazaar] You reached your maximum of 28 Bazaar orders!

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player != Minecraft.getMinecraft().thePlayer) return;
        if (macroControllerState != MacroController.MacroState.ENABLED) return;
        if (currentState == MacroState.DISABLED) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (tickcounter > 0) {
            tickcounter--;
            return;
        }

        switch (currentState) {
            case OPEN_STASH:
                ChatUtil.sendMessage("/viewstash material");
                tickcounter = TimerUtil.secondsToTicks(1);
                setState(MacroState.WAIT_FOR_CONTAINER_TO_OPEN);
                break;

            case WAIT_FOR_CONTAINER_TO_OPEN:
                if (mc.currentScreen instanceof GuiContainer) {
                    tickcounter = TimerUtil.milisecondsToTicks(500);
                    setState(MacroState.GET_ITEMS);
                }
                break;

            case GET_ITEMS:
                // GUI-Check: Wenn keine GUI offen ist, zurück zur vorherigen Phase
                if (!isGuiOpen()) {
                    setState(previousState);
                    break;
                }

                GuiContainer guiContainer = (GuiContainer) mc.currentScreen;
                Container container = guiContainer.inventorySlots;

                int playerInventorySize = mc.thePlayer.inventory.getSizeInventory();
                int containerSize = container.inventorySlots.size() - playerInventorySize;

                indexOfButton = -1; // reset each time

                // Nur Items sammeln wenn sie noch nicht gesammelt wurden
                if (!itemsCollected) {
                    ChatUtil.debugMessage("Sammle Items zum ersten Mal...");
                    itemList.clear();
                    betterFormOfItemList.clear();

                    for (int i = 0; i < containerSize + 2; i++) {
                        ItemStack stack = container.getSlot(i).getStack();

                        if (stack != null && stack.getItem() != null) {
                            String registryName = stack.getItem().getRegistryName();
                            String displayName = stack.getDisplayName();
                            String cleanDisplayName = ChatUtil.removeColorCodes(displayName);

                            if (registryName != null && (
                                    registryName.contains("stained_glass_pane") ||
                                            registryName.contains("barrier") ||
                                            registryName.equals("minecraft:barrier")
                            )) {
                                continue;
                            }

                            if (cleanDisplayName != null && (
                                    cleanDisplayName.equals("Close") ||
                                            cleanDisplayName.equals("Fill Inventory") ||
                                            cleanDisplayName.equals("Sell Stash Now")
                            )) {
                                continue;
                            }

                            if (cleanDisplayName != null && cleanDisplayName.contains("Insert Into Sacks")) {
                                indexOfButton = i;
                                ChatUtil.debugMessage("Found 'Insert Into Sacks' at slot: " + i);
                                continue;
                            }

                            if (registryName != null && !registryName.equals(" ")) {
                                String[] data = cleanDisplayName.split(" x");
                                cleanDisplayName = data[0];
                                cleanDisplayName = cleanDisplayName.toLowerCase();
                                cleanDisplayName = cleanDisplayName.replace(" ", "_");

                                // Ingot-Verarbeitung anwenden
                                cleanDisplayName = processItemName(cleanDisplayName);

                                ChatUtil.debugMessage(cleanDisplayName + " at spot: " + i);
                                itemList.add(cleanDisplayName);

                                String enchantedItemName = "enchanted_" + cleanDisplayName;
                                // Prüfen ob das Item bereits auf der Blacklist steht
                                if (!itemBlackList.contains(enchantedItemName)) {
                                    betterFormOfItemList.add(enchantedItemName);
                                    ChatUtil.debugMessage("Item added : " + cleanDisplayName);
                                } else {
                                    ChatUtil.debugMessage("Item skipped (blacklisted): " + cleanDisplayName);
                                }
                            }
                        }
                    }
                    itemsCollected = true;
                    ChatUtil.debugMessage("Insgesamt " + betterFormOfItemList.size() + " Items gefunden! (" + itemBlackList.size() + " auf Blacklist)");
                } else {
                    indexOfButton = InventoryUtil.findItemInInventoryAndContainer("Insert Into Sacks");
                }

                if (indexOfButton >= 0) {
                    ChatUtil.debugMessage("Button found at index: " + indexOfButton);
                } else {
                    ChatUtil.debugMessage("Insert Into Sacks button not found!");
                }

                tickcounter = TimerUtil.milisecondsToTicks(650);
                setState(MacroState.FILL_INTO_SACKS);
                break;

            case FILL_INTO_SACKS:
                // GUI-Check: Wenn keine GUI offen ist, zurück zur vorherigen Phase
                if (!isGuiOpen()) {
                    setState(previousState);
                    break;
                }

                if (mc.currentScreen instanceof GuiContainer && indexOfButton >= 0) {
                    GuiContainer gui = (GuiContainer) mc.currentScreen;
                    ChatUtil.debugMessage("Attempting to click slot: " + indexOfButton);

                    mc.playerController.windowClick(
                            gui.inventorySlots.windowId,
                            indexOfButton,
                            0,
                            0,
                            mc.thePlayer
                    );
                    ChatUtil.debugMessage("Click executed!");
                } else {
                    ChatUtil.debugMessage("Cannot click: GUI not open or button index invalid (" + indexOfButton + ")");
                }

                tickcounter = TimerUtil.milisecondsToTicks(825);
                setState(MacroState.CLOSE_MENU);
                break;

            case CLOSE_MENU:
                mc.thePlayer.closeScreen();

                // Nach dem Schließen des Stash-Menüs: Beginne mit Supercraft für alle Items
                if (betterFormOfItemList.size() > 0) {
                    atItemIndex = 0; // Beginne mit dem ersten Item
                    setState(MacroState.OPEN_SUPER_CRAFT);
                } else {
                    ChatUtil.debugMessage("Keine Items zum Verarbeiten gefunden!");
                    setState(MacroState.DISABLED);
                }

                tickcounter = TimerUtil.milisecondsToTicks(750);
                break;

            case OPEN_SUPER_CRAFT:
                // Prüfen ob noch Items zu verarbeiten sind
                if (atItemIndex >= betterFormOfItemList.size()) {
                    ChatUtil.debugMessage("Alle Items wurden verarbeitet! Starte neue Runde...");
                    // Zurück zum Anfang für die nächste Runde
                    atItemIndex = 0;
                    itemsCollected = false; // Items müssen neu gesammelt werden
                    setState(MacroState.OPEN_STASH);
                    tickcounter = TimerUtil.secondsToTicks(1);
                    return;
                }

                ChatUtil.debugMessage("Verarbeite Item " + (atItemIndex + 1) + " von " + betterFormOfItemList.size() + ": " + betterFormOfItemList.get(atItemIndex));
                ChatUtil.sendMessage("/recipe " + betterFormOfItemList.get(atItemIndex));
                tickcounter = TimerUtil.secondsToTicks(1);
                setState(MacroState.SELECT_ITEM);
                break;

            case SELECT_ITEM:
                // GUI-Check: Wenn keine GUI offen ist, zurück zur vorherigen Phase
                if (!isGuiOpen()) {
                    setState(previousState);
                    break;
                }

                indexOfButton = InventoryUtil.findItemInInventoryAndContainer(betterFormOfItemList.get(atItemIndex).replace("_", " "));

                if (indexOfButton >= 0) {
                    ChatUtil.debugMessage("Item found at index: " + indexOfButton);

                    if (mc.currentScreen instanceof GuiContainer) {
                        GuiContainer gui = (GuiContainer) mc.currentScreen;
                        mc.playerController.windowClick(
                                gui.inventorySlots.windowId,
                                indexOfButton,
                                0,
                                0,
                                mc.thePlayer
                        );
                        ChatUtil.debugMessage("Item selected!");
                    }

                    tickcounter = TimerUtil.secondsToTicks(1);
                    setState(MacroState.CLICK_SUPERCRAFT);
                } else {
                    ChatUtil.debugMessage("Item not found: " + betterFormOfItemList.get(atItemIndex) + " - added to blacklist");
                    // Item zur Blacklist hinzufügen
                    itemBlackList.add(betterFormOfItemList.get(atItemIndex));

                    // GUI schließen
                    mc.thePlayer.closeScreen();

                    // Zum nächsten Item gehen
                    atItemIndex++;

                    // Prüfen ob noch Items zu verarbeiten sind
                    if (atItemIndex < betterFormOfItemList.size()) {
                        ChatUtil.debugMessage("Nächstes Item wird verarbeitet: " + betterFormOfItemList.get(atItemIndex));
                        setState(MacroState.OPEN_SUPER_CRAFT);
                    } else {
                        ChatUtil.debugMessage("Alle Items dieser Runde wurden verarbeitet! Starte neue Runde...");
                        // Zurück zum Anfang für die nächste Runde
                        atItemIndex = 0;
                        itemsCollected = false; // Items müssen neu gesammelt werden
                        setState(MacroState.OPEN_STASH);
                    }

                    tickcounter = TimerUtil.secondsToTicks(1);
                }
                break;

            case CLICK_SUPERCRAFT:
                // GUI-Check: Wenn keine GUI offen ist, zurück zur vorherigen Phase
                if (!isGuiOpen()) {
                    setState(previousState);
                    break;
                }

                indexOfButton = InventoryUtil.findItemInInventoryAndContainer("supercraft");

                if (indexOfButton >= 0) {
                    ChatUtil.debugMessage("Supercraft button found at index: " + indexOfButton);
                    // Verwende die vereinfachte Methode für Shift+Rechtsklick
                    performShiftClick(indexOfButton, 1, 1, "Shift+Right-click");
                } else {
                    ChatUtil.debugMessage("Supercraft button not found!");
                }

                ChatUtil.debugMessage("Menü geschlossen nach Supercraft.");

                tickcounter = TimerUtil.milisecondsToTicks(500);
                setState(MacroState.CLICK_SUPERCRAFT_AGAIN);
                break;

            case CLICK_SUPERCRAFT_AGAIN:
                // GUI-Check: Wenn keine GUI offen ist, zurück zur vorherigen Phase
                if (!isGuiOpen()) {
                    setState(previousState);
                    break;
                }

                performShiftClick(indexOfButton, 0, 0, "Left-click");

                tickcounter = TimerUtil.milisecondsToTicks(350);
                setState(MacroState.AFTER_SUPERCRAFT);

                break;

            case AFTER_SUPERCRAFT:

                mc.thePlayer.closeScreen();

                atItemIndex++; // Nächstes Item

                // Prüfen ob noch Items zu verarbeiten sind
                if (atItemIndex < betterFormOfItemList.size()) {
                    ChatUtil.debugMessage("Nächstes Item wird verarbeitet: " + betterFormOfItemList.get(atItemIndex));
                    setState(MacroState.OPEN_SUPER_CRAFT);
                } else {
                    ChatUtil.debugMessage("Alle Items dieser Runde wurden verarbeitet! Starte neue Runde...");
                    // Zurück zum Anfang für die nächste Runde
                    atItemIndex = 0;
                    itemsCollected = false; // Items müssen neu gesammelt werden
                    setState(MacroState.OPEN_STASH);
                }

                tickcounter = TimerUtil.secondsToTicks(1);
                break;
        }
    }
}