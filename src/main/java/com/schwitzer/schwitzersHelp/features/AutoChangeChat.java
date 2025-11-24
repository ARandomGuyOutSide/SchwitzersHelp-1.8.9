package com.schwitzer.schwitzersHelp.features;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoChangeChat {
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    private boolean isAutoChangeChat;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event)
    {
        isAutoChangeChat = config.isAutoChangeChat();
        if(isAutoChangeChat)
        {
            String message = event.message.getUnformattedText().toLowerCase();

            if(message.contains("joined"))
            {
                if(message.contains("party"))
                {
                    ChatUtil.sendMessage("/chat p");
                }
            }
        }
    }

}
