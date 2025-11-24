package com.schwitzer.schwitzersHelp.util;

import com.schwitzer.schwitzersHelp.mixin.gui.AccessorGuiEditSign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.util.ChatComponentText;

public class BazaarUtil {
    private static Minecraft mc = Minecraft.getMinecraft();

    public static void clickBestOfferMinusOne()
    {
        InventoryUtil.clickItemInInv(InventoryUtil.findItemInInventoryAndContainer("best offer -0.1"));
    }

    public static void clickSameAsBestOffer()
    {
        InventoryUtil.clickItemInInv(InventoryUtil.findItemInInventoryAndContainer("same as best offer"));
    }

    public static void clickConfirmSellOffer()
    {
        InventoryUtil.clickItemInInv(InventoryUtil.findItemInInventoryAndContainer("sell offer"));
    }

    public static void clickConfirmBuyOffer()
    {
        InventoryUtil.clickItemInInv(InventoryUtil.findItemInInventoryAndContainer("buy order"));
    }

    public static void clickCreateSellOffer()
    {
        InventoryUtil.clickItemInInv(InventoryUtil.findItemInInventoryAndContainer("create sell offer"));
    }

    public static void clickSellInstantly()
    {
        InventoryUtil.clickItemInInv(InventoryUtil.findItemInInventoryAndContainer("sell instantly"));
    }

    public static void openAndSearchItemInBazaar(String itemName)
    {
        ChatUtil.sendMessage("/bz " + itemName);
    }

    public static void enterAmountToSign(String text) {
        if (!(mc.currentScreen instanceof GuiEditSign)) return;
        AccessorGuiEditSign guiEditSign = (AccessorGuiEditSign) mc.currentScreen;
        if (guiEditSign.getTileSign() == null || guiEditSign.getTileSign().signText[0].getUnformattedText().equals(text))
            return;

        guiEditSign.getTileSign().signText[0] = new ChatComponentText(text);
    }

    public static void confirmAmountToSign() {
        if (!(mc.currentScreen instanceof GuiEditSign)) return;
        AccessorGuiEditSign guiEditSign = (AccessorGuiEditSign) mc.currentScreen;
        guiEditSign.getTileSign().markDirty();
        mc.displayGuiScreen(null);
    }
}
