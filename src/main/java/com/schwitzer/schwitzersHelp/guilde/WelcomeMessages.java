package com.schwitzer.schwitzersHelp.guilde;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import com.schwitzer.schwitzersHelp.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class WelcomeMessages {

    private SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private double tickcounter;
    private String pendingPlayerName = null;

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (event.player != Minecraft.getMinecraft().thePlayer) return;

        if (tickcounter > 0) {
            tickcounter--;

            if (tickcounter <= 0 && pendingPlayerName != null) {
                ChatUtil.sendMessage("/gc Willkommen " + pendingPlayerName + " in der GooDz Gilde! h/");
                pendingPlayerName = null;
            }
        }
    }

    @SubscribeEvent
    public void onMessage(ClientChatReceivedEvent event) {
        if (config.isSendMessageOnGuildJoin()) {
            String message = event.message.getUnformattedText().toLowerCase();

            if (message.contains("joined the guild")) {
                message = event.message.getUnformattedText();

                String[] data = message.split(" ");

                if (data.length > 1 && data.length < 6) {
                    String name;
                    if(data.length == 4)
                        name = data[0];
                    else
                        name = data[1];


                    ChatUtil.debugMessage(message);
                    ChatUtil.debugMessage("name : " + name);

                    // Timer starten (5 Sekunden)
                    tickcounter = TimerUtil.secondsToTicks(5);
                    pendingPlayerName = name;
                }
            }
        }
    }
}
