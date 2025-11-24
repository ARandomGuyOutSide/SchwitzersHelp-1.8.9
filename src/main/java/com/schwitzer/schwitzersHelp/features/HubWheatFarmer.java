package com.schwitzer.schwitzersHelp.features;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.schwitzer.schwitzersHelp.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HubWheatFarmer {
    private BlockPos mostLeftWheatFromPlayer;
    private BlockPos mostRightWheatFromPlayer;
    private final Minecraft mc = Minecraft.getMinecraft();

    /**
     * Scannt die Umgebung des Spielers und findet die äußersten Weizen-Blöcke
     *
     * @return true wenn Weizen gefunden wurde, false sonst
     */
    public boolean scanForWheat() {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return false;

        World world = player.worldObj;
        BlockPos playerPos = player.getPosition();

        // Berechne Blickrichtung aus Yaw (horizontal rotation)
        float yaw = player.rotationYaw;
        double yawRad = Math.toRadians(yaw);
        double lookX = -Math.sin(yawRad);
        double lookZ = Math.cos(yawRad);

        // Berechne die "rechts" und "links" Vektoren basierend auf der Blickrichtung
        double rightX = lookZ;
        double rightZ = -lookX;
        double leftX = -rightX;
        double leftZ = -rightZ;

        mostLeftWheatFromPlayer = null;
        mostRightWheatFromPlayer = null;

        double maxLeftScore = Double.NEGATIVE_INFINITY;
        double maxRightScore = Double.NEGATIVE_INFINITY;

        // Scanne 4 Block Radius vor dem Spieler
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = -1; y <= 2; y++) {
                    BlockPos checkPos = playerPos.add(x, y, z);

                    // Prüfe ob der Block Weizen ist
                    if (!isWheat(world, checkPos)) continue;

                    // Berechne relative Position zum Spieler
                    double relX = checkPos.getX() - playerPos.getX();
                    double relZ = checkPos.getZ() - playerPos.getZ();

                    // Prüfe ob der Block vor dem Spieler liegt (dot product)
                    double forwardDot = relX * lookX + relZ * lookZ;
                    if (forwardDot <= 0) continue; // Block ist hinter dem Spieler

                    // Berechne wie weit links/rechts der Block ist (dot product)
                    double leftScore = relX * leftX + relZ * leftZ;
                    double rightScore = relX * rightX + relZ * rightZ;

                    // Je näher am Spieler, desto höher die Priorität
                    double distance = Math.sqrt(relX * relX + relZ * relZ);
                    double proximityBonus = 1.0 / (distance + 1.0);

                    // Linker Block: maximiere links-Wert mit Nähe-Bonus
                    double leftFinalScore = leftScore + proximityBonus;
                    if (leftFinalScore > maxLeftScore) {
                        maxLeftScore = leftFinalScore;
                        mostLeftWheatFromPlayer = checkPos;
                    }

                    // Rechter Block: maximiere rechts-Wert mit Nähe-Bonus
                    double rightFinalScore = rightScore + proximityBonus;
                    if (rightFinalScore > maxRightScore) {
                        maxRightScore = rightFinalScore;
                        mostRightWheatFromPlayer = checkPos;
                    }
                }
            }
        }

        return mostLeftWheatFromPlayer != null && mostRightWheatFromPlayer != null;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        EntityPlayer player = mc.thePlayer;

        if (mostRightWheatFromPlayer == null || mostLeftWheatFromPlayer == null) return;

        double renderPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
        double renderPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
        double renderPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

        AxisAlignedBB boxLeft = new AxisAlignedBB(
                mostLeftWheatFromPlayer.getX() - renderPosX,
                mostLeftWheatFromPlayer.getY() - renderPosY,
                mostLeftWheatFromPlayer.getZ() - renderPosZ,
                mostLeftWheatFromPlayer.getX() + 1 - renderPosX,
                mostLeftWheatFromPlayer.getY() + 1 - renderPosY,
                mostLeftWheatFromPlayer.getZ() + 1 - renderPosZ
        );

        AxisAlignedBB boxRight = new AxisAlignedBB(
                mostRightWheatFromPlayer.getX() - renderPosX,
                mostRightWheatFromPlayer.getY() - renderPosY,
                mostRightWheatFromPlayer.getZ() - renderPosZ,
                mostRightWheatFromPlayer.getX() + 1 - renderPosX,
                mostRightWheatFromPlayer.getY() + 1 - renderPosY,
                mostRightWheatFromPlayer.getZ() + 1 - renderPosZ
        );

        // Use RenderUtil to render all blocks at once
        RenderUtil.drawFilledBox(boxLeft, 0.0f, 1.0f, 0.0f, 1f);
        RenderUtil.drawFilledBox(boxRight, 1.0f, 0.0f, 0.0f, 1f);

    }

    /**
     * Prüft ob an der Position Weizen ist
     */
    private boolean isWheat(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block == Blocks.wheat;
    }

    /**
     * Gibt die linke Weizen-Position zurück
     */
    public BlockPos getMostLeftWheat() {
        return mostLeftWheatFromPlayer;
    }

    /**
     * Gibt die rechte Weizen-Position zurück
     */
    public BlockPos getMostRightWheat() {
        return mostRightWheatFromPlayer;
    }

    /**
     * Startet den Farming-Prozess
     *
     * @return true wenn erfolgreich abgeschlossen
     */
    public boolean startFarming() {
        if (!scanForWheat()) {
            return false;
        }

        // TODO: Implementiere die Farming-Logik
        // while mostLeft == wheat und mostRight == wheat mach von links nach rechts und von rechts nach links and go forward

        return true;
    }
}