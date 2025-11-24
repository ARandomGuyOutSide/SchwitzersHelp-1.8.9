package com.schwitzer.schwitzersHelp.dungeon;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import com.schwitzer.schwitzersHelp.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class InformTeammatesInChat {
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private static final Minecraft mc = Minecraft.getMinecraft();

    private boolean sendLeapMessage;
    private boolean sendSavedMessage;
    private String customLeapMessage;

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        GuiUtil.renderTitle();
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event)
    {
        sendLeapMessage = config.isSendLeapMessage();
        sendSavedMessage = config.isSendSavedMessage();
        customLeapMessage = config.getCustomLeapMessage();

        String message = ChatUtil.removeColorCodes(event.message.getUnformattedText()).toLowerCase();

        if(sendLeapMessage)
        {
            if(message.startsWith("you have teleported to"))
            {
                String playerName = message.split(" ")[4];

                playerName = playerName.replace("!", "");

                if(!customLeapMessage.isEmpty())
                {
                    ChatUtil.sendMessage("/pc " + customLeapMessage + " " + playerName);
                }
                else
                {
                    ChatUtil.sendMessage("/pc Leaped to " + playerName);
                }
            }
        }
        if(sendSavedMessage)
        {
            if(message.startsWith("your bonzo's mask saved your life!"))
            {
                ChatUtil.sendMessage("/pc Bonzo popped");
                GuiUtil.drawTitle("Bonzo has been popped", 3000, 2.5f, Color.RED);

            }
            else if(message.startsWith("your spirit mask saved your life!") || message.startsWith("second wind activated!"))
            {
                ChatUtil.sendMessage("/pc Spirit popped");
                GuiUtil.drawTitle("Spirit has been popped", 3000, 2.5f, Color.RED);
            }
            else if(message.startsWith("your phoenix pet saved you from certain death!"))
            {
                ChatUtil.sendMessage("/pc Phoenix popped");
                GuiUtil.drawTitle("Phoenix has been popped", 3000, 2.5f, Color.RED);
            }
        }
    }
}
