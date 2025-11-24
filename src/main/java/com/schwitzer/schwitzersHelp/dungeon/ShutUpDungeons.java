package com.schwitzer.schwitzersHelp.dungeon;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import com.schwitzer.schwitzersHelp.util.ScoreboardUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ShutUpDungeons {
    private static final SchwitzerHelpConfig cofing = SchwitzerHelpConfig.getInstance();
    private static final Minecraft mc = Minecraft.getMinecraft();

    private boolean shutUpDungeons;
    private boolean evenBossShutUp;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event)
    {
        if(!ScoreboardUtil.isInAreaOfTheGame("the catac")) return;

        shutUpDungeons = cofing.isShutUpDungeons();
        evenBossShutUp = cofing.isEvenBossShutUp();

        if(!shutUpDungeons) return;

        String message = ChatUtil.removeColorCodes(event.message.getUnformattedText()).toLowerCase();

        if (isDungeonSpamMessage(message)) {
            event.setCanceled(true);
            return;
        }

        if (evenBossShutUp && message.startsWith("[boss]")) {
            event.setCanceled(true);
        }

    }

    private boolean isDungeonSpamMessage(String message) {
        // Dungeon Buffs und Abilities
        if (message.startsWith("dungeon buff!") ||
                message.startsWith("your") ||
                message.startsWith("this ability is on cooldown for") ||
                message.contains("is now available!") ||
                message.contains("is ready to use! press") ||
                message.contains("granted you"))
        {
            return true;
        }

        // Essence und Items
        if (message.startsWith("essence!") ||
                message.contains(" has obtained") ||
                message.contains("found a wither essence!") ||
                message.contains("picked up your")) {
            return true;
        }

        // Statuen und Blessings
        if (message.startsWith("[statue]") ||
                message.startsWith("a blessing of")) {
            return true;
        }

        // Verschiedene Meldungen
        if (message.equals("there are blocks in the way!") ||
                message.contains("milestone") || message.startsWith("right click")) {
            return true;
        }

        return false;
    }
}
