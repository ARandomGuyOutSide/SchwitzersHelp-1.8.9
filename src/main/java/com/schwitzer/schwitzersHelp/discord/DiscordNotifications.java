package com.schwitzer.schwitzersHelp.discord;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordNotifications {
    private static SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static void sendMessageToWebhook(String message, String webhook) throws IOException {
        // JSON-Payload für Discord
        String json = String.format("{\"content\":\"%s\"}", escapeJson(message));

        URL url = new URL(webhook);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "MinecraftMod/1.0");
        connection.setDoOutput(true);

        // JSON-Daten senden
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 204) {
            if(Minecraft.getMinecraft().theWorld != null)
            {
                ChatUtil.debugMessage("Discord-Nachricht erfolgreich gesendet!");
                ChatUtil.sendMessage("Send message to org.polyfrost.schwitzersHelp.discord webhook");
            }
        } else {
            System.out.println("Fehler beim Senden der Discord-Nachricht: " + responseCode);

            // Fehlerdetails lesen
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(errorStream))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println("Discord API Response: " + response.toString());
                }
            }
        }

        connection.disconnect();
    }

    public static void sendEmbedToWebhook(String title, String description, int color, String webhook) throws IOException {
        if(!config.isSendDiscordInformation()) return;
        String json = "{\n" +
                "    \"embeds\": [{\n" +
                "        \"title\": \"" + escapeJson(title) + "\",\n" +
                "        \"description\": \"" + escapeJson(description) + "\",\n" +
                "        \"color\": " + color + "\n" +
                "    }]\n" +
                "}";

        URL url = new URL(webhook);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "MinecraftMod/1.0");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 204) {
            System.out.println("Discord-Embed erfolgreich gesendet!");
        } else {
            System.out.println("Fehler beim Senden des Discord-Embeds: " + responseCode);
        }

        connection.disconnect();
    }

}
