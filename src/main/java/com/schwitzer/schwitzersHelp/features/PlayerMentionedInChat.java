package com.schwitzer.schwitzersHelp.features;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.discord.DiscordNotifications;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import com.schwitzer.schwitzersHelp.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.io.IOException;

public class PlayerMentionedInChat {
    private final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player != Minecraft.getMinecraft().thePlayer) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        GuiUtil.renderTitle();
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event)
    {
        if(!config.isChatMention()) return;

        String username = mc.thePlayer.getName();
        String message = event.message.getFormattedText();

        // Check if @username is in the message
        if (message.contains("@" + username)) {
            // @username rot einfärben
            String formattedMessage = message.replace(
                    "@" + username,
                    EnumChatFormatting.RED + "@" + username + EnumChatFormatting.RESET
            );

            if(config.isSendDiscordInformation())
            {
                try {
                    DiscordNotifications.sendEmbedToWebhook("@everyone You have been mentioned in chat", ChatUtil.removeColorCodes(message), 65280, config.getDiscordWebhook());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            // Ursprüngliche Nachricht blockieren
            event.setCanceled(true);

            // Neue, formatierte Nachricht in Chat einfügen
            mc.thePlayer.addChatMessage(new ChatComponentText(formattedMessage));

            GuiUtil.drawTitle("You have been mentioned in chat", config.getChatMentionTitleDuration() * 1000, 2.5f, Color.RED);
        }
    }
}