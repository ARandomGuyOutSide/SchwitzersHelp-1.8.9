package com.schwitzer.schwitzersHelp.util;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class ChatUtil {

    private static Minecraft mc = Minecraft.getMinecraft();
    private static SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    public static void formatedChatMessage(String message) {
        mc.thePlayer.addChatMessage(new ChatComponentText("§0[§1SchwitzersHelp§0]§f >> " + message));
    }

    public static void debugMessage(String message) {
        if (config.isDebugMode())
            mc.thePlayer.addChatMessage(new ChatComponentText("§0[§1SchwitzersHelp§0]§f (DEBUG) >> " + message));
    }

    public static String removeColorCodes(String text) {
        if (text == null) return null;
        // Entfernt alle Minecraft-Farbcodes (§ + ein Zeichen)
        text = text.replaceAll("§[0-9a-fk-or]", "");
        // Entfernt Text in Klammern inklusive der Klammern selbst
        text = text.replaceAll("\\s*\\([^)]*\\)", "");
        return text;
    }

    public static void sendMessage(String message)
    {
        mc.thePlayer.sendChatMessage(message);
    }

}
