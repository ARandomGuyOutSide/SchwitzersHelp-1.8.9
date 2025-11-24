package com.schwitzer.schwitzersHelp.failsaves;

import com.schwitzer.schwitzersHelp.macros.Macro;
import com.schwitzer.schwitzersHelp.macros.MacroController;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Worldchange {
    private Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        if (MacroController.isAnyMacroRunning()) {
            if (mc.thePlayer == null || mc.theWorld == null)
                return;

            Macro runningMacro = MacroController.getCurrentlyRunningMacro();

        }
    }
}