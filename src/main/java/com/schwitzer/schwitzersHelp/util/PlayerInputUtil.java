package com.schwitzer.schwitzersHelp.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Method;

public class PlayerInputUtil {
    private TimerUtil timerUtil;
    private static Minecraft mc = Minecraft.getMinecraft();

    private boolean leftClickOnceWasActive;

    public PlayerInputUtil() {
        timerUtil = new TimerUtil();

    }

    public static boolean invoke(Object object, String methodName) {
        try {
            final Method method = object.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(object);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }


    public static void leftClick() {
        if (!invoke(mc, "func_147116_af")) {
            invoke(mc, "clickMouse");
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (mc.thePlayer == null) return;

        if(leftClickOnceWasActive)
        {
            System.out.println("Left click was active");
            if(timerUtil.hasPassed(300))
            {
                System.out.println("Timer has ended");
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                leftClickOnceWasActive = false;
                timerUtil.reset();
                System.out.println("set left click to false");
            }
        }
    }
}
