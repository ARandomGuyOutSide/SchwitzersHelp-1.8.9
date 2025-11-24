package com.schwitzer.schwitzersHelp.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;

public class AngleUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static float normalizeAngle(float yaw) {
        float newYaw = yaw % 360F;
        if (newYaw < -180F) {
            newYaw += 360F;
        }
        if (newYaw > 180F) {
            newYaw -= 360F;
        }
        return newYaw;
    }

    public static float getNeededYawChange(float start, float end) {
        return normalizeAngle(end - start);
    }

    public static float getRotationYaw(Vec3 to) {
        return (float) -Math.toDegrees(Math.atan2(to.xCoord - mc.thePlayer.posX, to.zCoord - mc.thePlayer.posZ));
    }
}
