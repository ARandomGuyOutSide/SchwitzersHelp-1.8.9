package com.schwitzer.schwitzersHelp.util;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScoreboardUtil {
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final int SIDEBAR_SLOT = 1;

    // Pattern für verschiedene Area-Formate
    private static final Pattern AREA_PATTERN = Pattern.compile(".*\\s*([A-Za-z\\s]+)\\s*.*");


    public static boolean isInAreaOfTheGame(String area) {
        try {
            area = area.toLowerCase();

            Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(SIDEBAR_SLOT);
            List<String> scoreList = scoreboard.getSortedScores(objective)
                    .stream()
                    .limit(15)
                    .map(score ->
                            ScorePlayerTeam.formatPlayerName(
                                    scoreboard.getPlayersTeam(score.getPlayerName()),
                                    score.getPlayerName()))
                    .collect(Collectors.toList());
            Collections.reverse(scoreList);
            for (String s : scoreList) {
                s = ChatUtil.removeColorCodes(s);

                if(s.toLowerCase().replace("?", "").contains(area))
                {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}