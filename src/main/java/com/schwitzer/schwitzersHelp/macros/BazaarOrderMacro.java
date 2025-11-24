package com.schwitzer.schwitzersHelp.macros;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;

public class BazaarOrderMacro implements Macro {
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    private final String discordWebhook = config.getDiscordWebhook();
    private MacroController.MacroState macroControllerState = MacroController.MacroState.DISABLED;
    private MacroState currentState = MacroState.DISABLED;
    private MacroState previousState = MacroState.DISABLED;

    private int tickcounter = 0;
    private int itemPlaceOption;
    private String iterationOption;
    private boolean isSellOffer;
    private String[] itemNames;
    private String[] itemPrices;
    private boolean customPrices;
    private int iteration;
    private boolean madBestOffer;

    private enum MacroState {
        CHECK_INVENTORY_FOR_ITEMS, OPEN_BAZAAR, CLOSE_BAZAAR, CLICK_ON_ITEM, CLICK_SELL_INSTANT_BUTTON, CLICK_SELL_OFFER_BUTTON, CLICK_CUSTOM_PRICE_BUTTON,
        CLICK_BEST_OFFER_BUTTON, CLICK_CONFIRM_BUTTON, FILL_INVENTORY_FROM_SACKS, ENTER_CUSTOM_PRICE, CONFIRM_CUSTOM_PRICE, DISABLED
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        iteration = 0;
        if (!isSellOffer) {
            currentState = MacroState.OPEN_BAZAAR;
        }
        madBestOffer = false;
        currentState = MacroState.OPEN_BAZAAR;
        previousState = MacroState.DISABLED;
        macroControllerState = MacroController.MacroState.ENABLED;

        itemPlaceOption = config.getItemPlaceOption();
        isSellOffer = config.isSellOffer();
        itemNames = config.getItemNames().split(";");
        itemPrices = config.getItemPrices().split(";");
        customPrices = config.isCustomPrices();

        for (int i = 0; i < itemPrices.length; i++) {
            itemPrices[i] = NumberUtil.convertPriceString(itemPrices[i].trim());
        }

        for (int i = 0; i < itemNames.length; i++) {
            itemNames[i] = itemNames[i].replace("_", " ");
        }
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
        return "Bazaar Order Macro";
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

    private void setState(MacroState newState) {
        previousState = currentState;
        currentState = newState;
    }

    private boolean isGuiOpen() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.currentScreen != null;
    }

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
            case CHECK_INVENTORY_FOR_ITEMS:
                if (InventoryUtil.isItemInInventory(itemNames[iteration]))
                {
                    setState(MacroState.OPEN_BAZAAR);
                }
                else
                {
                    setState(MacroState.FILL_INVENTORY_FROM_SACKS);
                }
                break;

            case OPEN_BAZAAR:
                if (iteration < itemNames.length) {
                    BazaarUtil.openAndSearchItemInBazaar(itemNames[iteration]);
                    tickcounter = TimerUtil.milisecondsToTicks(850);
                    setState(MacroState.CLICK_ON_ITEM);
                }
                break;

            case CLOSE_BAZAAR:
                mc.thePlayer.closeScreen();

                switch (iterationOption)
                {
                    case "fill inventory":
                        setState(MacroState.FILL_INVENTORY_FROM_SACKS);
                        break;
                }
                tickcounter = TimerUtil.milisecondsToTicks(750);
                break;

            case FILL_INVENTORY_FROM_SACKS:
                SkyblockUtil.getItemFromSack(itemNames[iteration], 10000);
                tickcounter = TimerUtil.milisecondsToTicks(650);
                setState(MacroState.OPEN_BAZAAR);
                break;

            case CLICK_ON_ITEM:
                // GUI-Check: Wenn keine GUI offen ist, zurück zur vorherigen Phase
                if (!isGuiOpen()) {
                    setState(previousState);
                    break;
                }

                InventoryUtil.clickItemInInv(InventoryUtil.findItemInInventoryAndContainer(itemNames[iteration]));
                tickcounter = TimerUtil.milisecondsToTicks(750);
                if (!isSellOffer || itemPrices.length == 0) {
                    setState(MacroState.CLICK_SELL_INSTANT_BUTTON);
                } else {
                    setState(MacroState.CLICK_SELL_OFFER_BUTTON);
                }
                tickcounter = TimerUtil.milisecondsToTicks(650);
                break;

            case CLICK_SELL_INSTANT_BUTTON:
                // GUI-Check: Wenn keine GUI offen ist, zurück zur vorherigen Phase
                if (!isGuiOpen()) {
                    setState(previousState);
                    break;
                }

                System.out.println("Sell Offer");
                BazaarUtil.clickSellInstantly();
                setState(MacroState.CLOSE_BAZAAR);
                iterationOption = "fill inventory";
                tickcounter = TimerUtil.milisecondsToTicks(850);
                break;

            case CLICK_SELL_OFFER_BUTTON:
                // GUI-Check: Wenn keine GUI offen ist, zurück zur vorherigen Phase
                if (!isGuiOpen()) {
                    setState(previousState);
                    break;
                }

                BazaarUtil.clickCreateSellOffer();
                System.out.println(itemPrices.length);
                System.out.println(Arrays.toString(Arrays.stream(itemPrices).toArray()));
                if (!customPrices) {
                    System.out.println("Best Offer");
                    setState(MacroState.CLICK_BEST_OFFER_BUTTON);
                } else {
                    System.out.println("Custom Offer");
                    setState(MacroState.CLICK_CUSTOM_PRICE_BUTTON);
                }
                System.out.println("Nothing or both");
                tickcounter = TimerUtil.milisecondsToTicks(850);
                break;

            case CLICK_BEST_OFFER_BUTTON:
                // GUI-Check: Wenn keine GUI offen ist, zurück zur vorherigen Phase
                if (!isGuiOpen()) {
                    setState(previousState);
                    break;
                }

                if (!madBestOffer) {
                    System.out.println("Trying to make best offer -0.1");
                    BazaarUtil.clickBestOfferMinusOne();
                    madBestOffer = true;
                } else {
                    BazaarUtil.clickSameAsBestOffer();
                    System.out.println("Trying to make best offer");
                }
                setState(MacroState.CLICK_CONFIRM_BUTTON);
                tickcounter = TimerUtil.milisecondsToTicks(750);
                break;

            case CLICK_CONFIRM_BUTTON:
                // GUI-Check: Wenn keine GUI offen ist, zurück zur vorherigen Phase
                if (!isGuiOpen()) {
                    setState(previousState);
                    break;
                }

                BazaarUtil.clickConfirmSellOffer();
                tickcounter = TimerUtil.milisecondsToTicks(850);
                iterationOption = "fill inventory";
                setState(MacroState.CLOSE_BAZAAR);
                break;

            case CLICK_CUSTOM_PRICE_BUTTON:
                // GUI-Check: Wenn keine GUI offen ist, zurück zur vorherigen Phase
                if (!isGuiOpen()) {
                    setState(previousState);
                    break;
                }

                InventoryUtil.clickItemInInv(InventoryUtil.findItemInInventoryAndContainer("custom price"));
                tickcounter = TimerUtil.milisecondsToTicks(650);
                setState(MacroState.ENTER_CUSTOM_PRICE);
                break;

            case ENTER_CUSTOM_PRICE:
                // GUI-Check: Wenn keine GUI offen ist, zurück zur vorherigen Phase
                if (!isGuiOpen()) {
                    setState(previousState);
                    break;
                }

                BazaarUtil.enterAmountToSign(itemPrices[iteration]);
                tickcounter = TimerUtil.milisecondsToTicks(350);
                setState(MacroState.CONFIRM_CUSTOM_PRICE);
                break;

            case CONFIRM_CUSTOM_PRICE:
                // GUI-Check: Wenn keine GUI offen ist, zurück zur vorherigen Phase
                if (!isGuiOpen()) {
                    setState(previousState);
                    break;
                }

                BazaarUtil.confirmAmountToSign();
                tickcounter = TimerUtil.milisecondsToTicks(650);
                setState(MacroState.CLICK_CONFIRM_BUTTON);
                break;
        }
    }
}