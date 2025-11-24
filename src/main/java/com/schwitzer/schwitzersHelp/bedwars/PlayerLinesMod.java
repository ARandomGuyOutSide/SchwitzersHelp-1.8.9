package com.schwitzer.schwitzersHelp.bedwars;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;

public class PlayerLinesMod {
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {

        boolean playerLines = config.isPlayerLines();
        OneColor playerLinesColor = config.getPlayerLinesColor();

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        // Hole die Position des Spielers (relativ zur Kamera)
        double camX = mc.getRenderManager().viewerPosX;
        double camY = mc.getRenderManager().viewerPosY;
        double camZ = mc.getRenderManager().viewerPosZ;

        if (playerLines) {
            Collection<NetworkPlayerInfo> tabListPlayers = mc.getNetHandler().getPlayerInfoMap();

            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (player == mc.thePlayer) continue;

                boolean isInTabList = tabListPlayers.stream()
                        .anyMatch(info -> info.getGameProfile().getId().equals(player.getGameProfile().getId()));

                if (!isInTabList) continue;

                String playerName = player.getDisplayName().getUnformattedText();
                if (playerName.startsWith("[NPC]")) continue;

                // Endpunkt: Anderer Spieler (relativ zur Kamera)
                Vec3 playerEnd = new Vec3(
                        player.posX - camX,
                        player.posY + player.getEyeHeight() - camY,
                        player.posZ - camZ
                );

                // Zeichne die Linie mit RenderUtil
                RenderUtil.drawLine(playerEnd, playerLinesColor, false);
            }
        }
    }
}