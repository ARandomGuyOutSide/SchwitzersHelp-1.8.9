package com.schwitzer.schwitzersHelp.failsaves;

import com.schwitzer.schwitzersHelp.macros.BazaarOrderMacro;
import com.schwitzer.schwitzersHelp.macros.Macro;
import com.schwitzer.schwitzersHelp.macros.MacroController;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BazaarFailsaves {
    private Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (MacroController.isAnyMacroRunning()) {
            if (mc.thePlayer == null || mc.theWorld == null)
                return;

            Macro runningMacro = MacroController.getCurrentlyRunningMacro();

            if (runningMacro instanceof BazaarOrderMacro) {
                String macroName = runningMacro.getName();

                String chatmessage = ChatUtil.removeColorCodes(event.message.getFormattedText()).toLowerCase();

                if(chatmessage.contains("you have reached"))
                {
                    runningMacro.setState(MacroController.MacroState.DISABLED);
                    ChatUtil.formatedChatMessage(macroName + " was disabled due to the bazaar limit being reached");
                }
            }

        }
    }
}
