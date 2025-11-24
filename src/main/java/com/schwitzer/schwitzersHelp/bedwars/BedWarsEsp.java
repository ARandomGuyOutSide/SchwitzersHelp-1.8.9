package com.schwitzer.schwitzersHelp.bedwars;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

public class BedWarsEsp {
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private static final Minecraft mc = Minecraft.getMinecraft();

    private boolean playerESP;
    private OneColor playerESPColor;
    private boolean bedEsp;
    private OneColor bedEspColor;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        playerESP = config.isPlayerEsp();
        playerESPColor = config.getPlayerEspColor();
        bedEsp = config.isBedEsp();
        bedEspColor = config.getBedEspColor();

        if(!playerESP && !bedEsp) return;

        if (bedEsp) {

            EntityPlayer player = mc.thePlayer;

            // Render-Position berechnen (wie bei Coal ESP)
            double renderPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
            double renderPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
            double renderPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

            int scanRadius = 25;
            Set<BlockPos> processedBeds = new HashSet<>();

            // Translate für relatives Rendering (wie bei Coal ESP)
            GlStateManager.pushMatrix();
            GlStateManager.translate(-renderPosX, -renderPosY, -renderPosZ);

            for (int x = -scanRadius; x <= scanRadius; x++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    for (int y = -scanRadius; y <= scanRadius; y++) {
                        BlockPos pos = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);

                        if (pos.getY() < 0 || pos.getY() > 255) continue;

                        IBlockState blockState = mc.theWorld.getBlockState(pos);
                        Block block = blockState.getBlock();

                        if (block instanceof BlockBed) {
                            BlockBed.EnumPartType partType = blockState.getValue(BlockBed.PART);

                            if (partType == BlockBed.EnumPartType.FOOT && !processedBeds.contains(pos)) {
                                processedBeds.add(pos);
                                // Jetzt mit absoluten Koordinaten, da wir bereits translate gemacht haben
                                RenderUtil.drawBedBox(pos, bedEspColor);
                            }
                        }
                    }
                }
            }

            GlStateManager.popMatrix();
        }

        if (playerESP) {
            // Kamera-Position für relative Berechnung
            double camX = mc.getRenderManager().viewerPosX;
            double camY = mc.getRenderManager().viewerPosY;
            double camZ = mc.getRenderManager().viewerPosZ;

            for (Object playerObj : mc.theWorld.loadedEntityList) {
                if (playerObj instanceof EntityLivingBase) {
                    EntityLivingBase otherPlayer = (EntityLivingBase) playerObj;
                    if (otherPlayer instanceof EntityPlayer) {
                        EntityPlayer otherEntityPlayer = (EntityPlayer) otherPlayer;

                        // Skip own player
                        if (otherEntityPlayer == mc.thePlayer) continue;

                        boolean isInTabList = false;

                        // Get the player's name and check if it starts with "[NPC]"
                        String playerName = otherEntityPlayer.getDisplayName().getUnformattedText();
                        if (playerName.startsWith("[NPC]")) continue; // Skip players that start with "[NPC]"

                        for (NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                            if (playerInfo.getGameProfile().getName().equals(otherEntityPlayer.getGameProfile().getName())) {
                                isInTabList = true;
                                break;
                            }
                        }

                        if (!isInTabList) continue;

                        // Berechne relative Position zur Kamera
                        double relativeX = otherPlayer.posX - camX;
                        double relativeY = otherPlayer.posY - camY;
                        double relativeZ = otherPlayer.posZ - camZ;

                        OneColor color = playerESPColor;
                        RenderUtil.drawEntityBox(relativeX, relativeY, relativeZ, 0.5, 1.8, color, 0);

                        // Nametag Position (etwas über dem Kopf)
                        double nameX = relativeX;
                        double nameY = relativeY + 1.75;
                        double nameZ = relativeZ;
                        RenderUtil.drawNameTag(otherEntityPlayer.getName(), nameX, nameY, nameZ);
                    }
                }
            }
        }
    }
}