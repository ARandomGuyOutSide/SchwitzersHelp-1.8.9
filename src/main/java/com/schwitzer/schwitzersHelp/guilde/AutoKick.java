package com.schwitzer.schwitzersHelp.guilde;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import com.schwitzer.schwitzersHelp.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AutoKick {
    private static final String API_KEY = "https://api.dergruenkohl.com/kicks/goodz4";
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private List<PlayerToKick> playersToKick = new ArrayList<>();
    private boolean hasKickedThisSession = false;

    private int ticksAfterJoin = 0;
    private boolean hasJoined = false;


    private static class PlayerToKick {
        String uuid;
        String reason;

        PlayerToKick(String uuid, String reason) {
            this.uuid = uuid;
            this.reason = reason;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(config.isAutoKickEnabled())
        {
            if(!hasKickedThisSession && isKickDay())
            {
                if (Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().thePlayer != null) {
                    if (!hasJoined) {
                        hasJoined = true;
                        ticksAfterJoin = 0;
                    }

                    ticksAfterJoin++;
                    if (ticksAfterJoin >= TimerUtil.secondsToTicks(5)) {
                        getUsersToKick();
                        kickPlayers();

                        MinecraftForge.EVENT_BUS.unregister(this);
                        ChatUtil.debugMessage("Kicked and unregistered module");
                    }
                } else {
                    hasJoined = false;
                }
            }
        }


    }

    private void getUsersToKick() {
        try {
            URL url = new URL(API_KEY);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            String responseBody = response.toString();

            parseResponse(responseBody);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseResponse(String jsonResponse) {
        try {
            JsonParser parser = new JsonParser();
            JsonArray jsonArray = parser.parse(jsonResponse).getAsJsonArray();

            playersToKick.clear();

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject player = jsonArray.get(i).getAsJsonObject();

                String uuid = player.get("uuid").getAsString();
                String guildRank = player.get("guildRank").getAsString();

                String reason = determineKickReason(guildRank);

                playersToKick.add(new PlayerToKick(uuid, reason));

                System.out.println("UUID: " + uuid + " | Grund: " + reason);
            }

        } catch (Exception e) {
            System.out.println("Fehler beim Parsen der JSON-Response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String determineKickReason(String guildRank) {
        switch (guildRank.toLowerCase()) {
            case "member":
                return "Inaktiv 5+";
            case "stammspieler":
                return "Inaktiv 7+";
            case "loyal":
                return "Inaktiv 30+";
            default:
                return "Inaktiv";
        }
    }

    private boolean isKickDay() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        switch (today) {
            case MONDAY:
                return config.isKickOnMonday();
            case TUESDAY:
                return config.isKickOnTuesday();
            case WEDNESDAY:
                return config.isKickOnWednesday();
            case THURSDAY:
                return config.isKickOnThursday();
            case FRIDAY:
                return config.isKickOnFriday();
            case SATURDAY:
                return config.isKickOnSaturday();
            case SUNDAY:
                return config.isKickOnSunday();
            default:
                return false;
        }
    }

    private void kickPlayers() {
        if(playersToKick.size() >= 10)
        {
            ChatUtil.formatedChatMessage("Do it your self you lazy fuck");
            return;
        }
        new Thread(() -> {
            for (PlayerToKick player : playersToKick) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/g kick " + player.uuid + " " + player.reason);

                try {
                    Thread.sleep(500); // 500ms warten
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            hasKickedThisSession = true;
        }).start();
    }
}