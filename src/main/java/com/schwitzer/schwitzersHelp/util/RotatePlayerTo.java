package com.schwitzer.schwitzersHelp.util;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RotatePlayerTo {
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static BlockPos targetBlock = null;
    private static Entity targetEntity = null;
    private static Vec3 targetPosition = null;
    private static float speed = 5f;
    private static float offset = 0;

    /**
     * Setzt ein Zielblock, zu dem der Spieler rotieren soll.
     */
    public static void lookAtBlock(BlockPos block, float rotationSpeed) {
        targetBlock = block;
        targetEntity = null;
        targetPosition = null;
        speed = rotationSpeed;
    }

    /**
     * Setzt eine Zielposition (Vec3), zu der der Spieler rotieren soll.
     */
    public static void lookAtPosition(Vec3 position, float rotationSpeed) {
        targetPosition = position;
        targetBlock = null;
        targetEntity = null;
        speed = rotationSpeed;
    }

    /**
     * Setzt Zielblock und Ziel-Entiät auf null
     */
    public static void stopRotation()
    {
        targetBlock = null;
        targetEntity = null;
        targetPosition = null;
    }

    /**
     * Setzt eine Ziel-Entität, zu der der Spieler rotieren soll.
     */
    public static void lookAtEntity(Entity entity, float rotationSpeed, float yOffset) {
        targetEntity = entity;
        targetBlock = null;
        targetPosition = null;
        speed = rotationSpeed;
        offset = yOffset;
    }

    /**
     * Wird automatisch bei jedem Client-Tick aufgerufen.
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (targetBlock != null) {
            double x = targetBlock.getX() + 0.5;
            double y = targetBlock.getY() + 0.5;
            double z = targetBlock.getZ() + 0.5;
            rotateTo(x, y, z);

            if (isLookingAt(x, y, z)) {
                targetBlock = null;
            }
        }

        if (targetPosition != null) {
            rotateTo(targetPosition.xCoord, targetPosition.yCoord, targetPosition.zCoord);

            if (isLookingAt(targetPosition.xCoord, targetPosition.yCoord, targetPosition.zCoord)) {
                targetPosition = null;
            }
        }

        if (targetEntity != null) {
            double x = targetEntity.posX;
            double y = targetEntity.posY + targetEntity.getEyeHeight() / 2 + offset;
            double z = targetEntity.posZ;
            rotateTo(x, y, z);

            if (isLookingAt(x, y, z)) {
                targetEntity = null;
            }
        }
    }

    private static void rotateTo(double x, double y, double z) {
        double dx = x - mc.thePlayer.posX;
        double dy = y - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dz = z - mc.thePlayer.posZ;

        float[] angles = calculateAngles(dx, dy, dz);

        mc.thePlayer.rotationYaw = updateRotation(mc.thePlayer.rotationYaw, angles[0], speed);
        mc.thePlayer.rotationPitch = updateRotation(mc.thePlayer.rotationPitch, angles[1], speed);
    }

    private static float[] calculateAngles(double dx, double dy, double dz) {
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        return new float[]{yaw, pitch};
    }

    private static float updateRotation(float current, float target, float speed) {
        float delta = wrapDegrees(target - current);
        if (delta > speed) delta = speed;
        if (delta < -speed) delta = -speed;
        return current + delta;
    }

    private static float wrapDegrees(float value) {
        value %= 360.0F;
        if (value >= 180.0F) value -= 360.0F;
        if (value < -180.0F) value += 360.0F;
        return value;
    }

    private static boolean isLookingAt(double x, double y, double z) {
        double dx = x - mc.thePlayer.posX;
        double dy = y - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dz = z - mc.thePlayer.posZ;
        float[] angles = calculateAngles(dx, dy, dz);

        float yawDiff = Math.abs(wrapDegrees(mc.thePlayer.rotationYaw - angles[0]));
        float pitchDiff = Math.abs(wrapDegrees(mc.thePlayer.rotationPitch - angles[1]));

        return yawDiff < 1.0F && pitchDiff < 1.0F;
    }

    public static BlockPos getTargetBlock() {
        return targetBlock;
    }

    public static Vec3 getTargetPosition() {
        return targetPosition;
    }

    public static Entity getTargetEntity() {
        return targetEntity;
    }
}