package com.schwitzer.schwitzersHelp.features;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DisconnectWhenISayIt {

    private final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!config.isFunMode()) return;
        if (event.type != 0) return;

        String rawMessage = event.message.getUnformattedText();

        // Check if message is from Party or Guild chat
        String chatType = "normal";
        String actualMessage = "";

        if (rawMessage.contains(">")) {
            int greaterThanIndex = rawMessage.indexOf('>');

            // Prüfe den Text vor dem ">" auf "Guild" oder "Party"
            String beforeGreaterThan = rawMessage.substring(0, greaterThanIndex).toLowerCase();

            if (beforeGreaterThan.contains("guild")) {
                chatType = "guild";
            } else if (beforeGreaterThan.contains("party")) {
                chatType = "party";
            }
        }
        int colonIndex = rawMessage.indexOf(':');
        if (colonIndex == -1 || colonIndex + 1 >= rawMessage.length()) return;
        actualMessage = rawMessage.substring(colonIndex + 1).trim();

        if (actualMessage.isEmpty()) return;

        String[] words = actualMessage.split("\\s+"); // Verwendet Regex für bessere Trennung

        if (words.length < 3) return;

        String commandPart = words[0] + " " + words[1] + " " + words[2];

        String expectedCommand = String.format("!schwitza %s disconnect",
                Minecraft.getMinecraft().getSession().getUsername());


        if (commandPart.equalsIgnoreCase(expectedCommand)) {

            // Send different messages based on chat type
            String responseMessage;
            if (chatType.equals("party")) {
                responseMessage = "/pc ok chef";
            } else if (chatType.equals("guild")) {
                responseMessage = "/gc ok chef";
            } else {
                responseMessage = "ok chef";
            }

            ChatUtil.sendMessage(responseMessage);

            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Kurz warten damit die Nachricht gesendet wird
                    Minecraft.getMinecraft().getNetHandler().getNetworkManager().closeChannel(
                            new net.minecraft.util.ChatComponentText("Disconnected by command")
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}