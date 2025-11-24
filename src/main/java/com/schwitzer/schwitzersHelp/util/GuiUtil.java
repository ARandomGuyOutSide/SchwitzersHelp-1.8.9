package com.schwitzer.schwitzersHelp.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class GuiUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    // Titel-System
    private static TimerUtil titleTimer;
    private static String titleText = "";
    private static float titleScale = 2.0f;
    private static Color titleColor = Color.WHITE;
    private static long titleDuration = 0; // in Millisekunden

    public static void drawTitle(String text, int duration, float scale, Color color) {
        titleText = text;
        titleScale = scale;
        titleColor = color;
        titleDuration = duration;

        // Timer starten
        if (titleTimer == null) {
            titleTimer = new TimerUtil();
        }
        titleTimer.startedAt = System.currentTimeMillis();
    }

    public static void renderTitle() {
        // Nur rendern wenn Text vorhanden und Timer läuft
        if (!titleText.isEmpty() && titleTimer != null && titleTimer.startedAt != 0) {

            // Prüfen ob die Zeit abgelaufen ist
            if (titleTimer.hasPassed(titleDuration)) {
                // Titel beenden
                clearTitle();
                return;
            }

            // Titel rendern
            ScaledResolution res = new ScaledResolution(mc);
            FontRenderer fr = mc.fontRendererObj;

            GlStateManager.pushMatrix();
            GlStateManager.translate(res.getScaledWidth() / 2f, res.getScaledHeight() / 4f, 0);
            GlStateManager.scale(titleScale, titleScale, titleScale);

            int x = -fr.getStringWidth(titleText) / 2;
            fr.drawStringWithShadow(titleText, x, 0, titleColor.getRGB());

            GlStateManager.popMatrix();
        }
    }

    public static void clearTitle() {
        if (titleTimer != null) {
            titleTimer.reset();
        }
        titleText = "";
        titleDuration = 0;
    }

    public static boolean isTitleActive() {
        return !titleText.isEmpty() && titleTimer != null && titleTimer.startedAt != 0;
    }
}