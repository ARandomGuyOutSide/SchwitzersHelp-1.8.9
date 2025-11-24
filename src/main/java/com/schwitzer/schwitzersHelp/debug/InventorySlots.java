package com.schwitzer.schwitzersHelp.debug;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InventorySlots {

    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    private static final Logger LOGGER = LogManager.getLogger("InventoryLogger");
    private int tickCounter = 0;
    private final int TICK_INTERVAL = 40; // 40 ticks = 2 Sekunden (20 TPS)

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if(!config.isDebugMode()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        tickCounter++;
        if (tickCounter >= TICK_INTERVAL) {
            tickCounter = 0;
            //logInventories();
        }
    }
}