package com.schwitzer.schwitzersHelp.util;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for detecting Hypixel Skyblock locations
 */
public class HypixelUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final int SIDEBAR_SLOT = 1;
    
    /**
     * Checks if the player is currently on Hypixel server
     * @return true if on Hypixel
     */
    public static boolean isOnHypixel() {
        try {
            if (mc.theWorld == null || mc.getCurrentServerData() == null) {
                return false;
            }
            
            String serverIP = mc.getCurrentServerData().serverIP.toLowerCase();
            return serverIP.contains("hypixel.net") || serverIP.contains("hypixel.io");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the player is currently on their Private Island
     * @return true if on Private Island
     */
    public static boolean isOnPrivateIsland() {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        if (world == null) return false;

        Scoreboard scoreboard = world.getScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return false;

        Collection<Score> scores = scoreboard.getSortedScores(objective);
        for (Score score : scores) {
            String line = ScorePlayerTeam.formatPlayerName(
                    scoreboard.getPlayersTeam(score.getPlayerName()),
                    score.getPlayerName()
            );

            // Remove all formatting codes for cleaner comparison
            String cleanLine = line.replaceAll("§.", "");

            // Check if we're on Private Island (looks for "Your Island")
            if (cleanLine.contains("Your Isl")) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Gets the current Skyblock area/location
     * @return location string or null if not detectable
     */
    public static String getCurrentLocation() {
        try {
            
            List<String> scoreboardLines = getScoreboardLines();
            for (String line : scoreboardLines) {
                String cleanLine = ChatUtil.removeColorCodes(line).trim();
                
                // Look for location indicators (usually contains special characters)
                if (cleanLine.contains("⏣") || cleanLine.contains("ф") || cleanLine.contains("α")) {
                    return cleanLine;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Gets all scoreboard lines as a list
     * @return list of scoreboard lines
     */
    private static List<String> getScoreboardLines() {
        try {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(SIDEBAR_SLOT);
            
            if (objective == null) {
                return Collections.emptyList();
            }
            
            List<String> scoreList = scoreboard.getSortedScores(objective)
                    .stream()
                    .limit(15)
                    .map(score ->
                            ScorePlayerTeam.formatPlayerName(
                                    scoreboard.getPlayersTeam(score.getPlayerName()),
                                    score.getPlayerName()))
                    .collect(Collectors.toList());
            
            Collections.reverse(scoreList);
            return scoreList;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * Checks if the player is in a specific Skyblock area
     * @param areaName the area name to check for (case-insensitive)
     * @return true if in the specified area
     */
    public static boolean isInArea(String areaName) {
        try {
            String currentLocation = getCurrentLocation();
            if (currentLocation == null) {
                return false;
            }
            
            return currentLocation.toLowerCase().contains(areaName.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }
}