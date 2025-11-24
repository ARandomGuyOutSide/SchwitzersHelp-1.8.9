package com.schwitzer.schwitzersHelp.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class BossBarUtil {
    private static long lastCheckTime = 0;
    private static String lastBossName = null;
    private static float lastBossHealth = 0f;

    private static class BossBar
    {
        String title;
        double value;

        private BossBar(String title, double value)
        {
            this.title = title;
            this.value = value;
        }

        @Override
        public String toString() {
            return "BossBar{" +
                    "title='" + title + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

    @SubscribeEvent
    public void onRenderBossHealth(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.BOSSHEALTH) {
            // Boss Bar wird gerendert - speichere die Infos
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld != null) {
                for (Object entity : mc.theWorld.loadedEntityList) {
                    if (entity instanceof IBossDisplayData) {
                        IBossDisplayData boss = (IBossDisplayData) entity;
                        lastBossName = boss.getDisplayName().getFormattedText();
                        lastBossHealth = boss.getHealth() / boss.getMaxHealth();
                    }
                }
            }
        }
    }

    public static BossBar getBossBarInfo() {
        Minecraft mc = Minecraft.getMinecraft();

        if (lastBossName != null) {
            int percentage = Math.round(lastBossHealth * 100);

            BossBar bossBar = new BossBar(ChatUtil.removeColorCodes(lastBossName), percentage);

            return bossBar;
        } else {
            System.out.println("No bossbar found");
            return null;
        }
    }
}