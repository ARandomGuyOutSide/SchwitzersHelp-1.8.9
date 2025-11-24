package com.schwitzer.schwitzersHelp.dungeon;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.RenderUtil;
import com.schwitzer.schwitzersHelp.util.ScoreboardUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityBat;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DungeonEsp {
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private static Minecraft mc = Minecraft.getMinecraft();

    private boolean keyMobEsp;
    private OneColor keyMobEspColor;
    private boolean batEsp;
    private OneColor batEspColor;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        keyMobEsp = config.isKeyMobEsp();
        keyMobEspColor = config.getKeyMobEspColor();
        batEsp = config.isBatEsp();
        batEspColor = config.getBatEspColor();

        if(!ScoreboardUtil.isInAreaOfTheGame("the catac")) return;

        if(!keyMobEsp && !batEsp) return;

        // Kamera-Position für relative Rendering
        double camX = mc.getRenderManager().viewerPosX;
        double camY = mc.getRenderManager().viewerPosY;
        double camZ = mc.getRenderManager().viewerPosZ;

        for (Object entityObj : mc.theWorld.loadedEntityList) {
            if (entityObj instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) entityObj;

                // Skip player - aber nur diese Iteration, nicht die ganze Methode
                if(entity == mc.thePlayer) continue;

                // Relative Position zur Kamera berechnen
                double relX = entity.posX - camX;
                double relY = entity.posY - camY;
                double relZ = entity.posZ - camZ;

                // Key Mob ESP (Star Mobs)
                if (keyMobEsp && entity.hasCustomName()) {
                    String formattedName = entity.getDisplayName().getFormattedText();

                    if (formattedName.contains("✯") && !formattedName.endsWith("✯") && formattedName.indexOf("✯") == formattedName.lastIndexOf("✯")) {
                        RenderUtil.drawEntityBox(relX, relY - 2, relZ, 0.5, 1.8, keyMobEspColor, 0.0D);
                    }
                }

                // Bat ESP
                if (batEsp && entityObj instanceof EntityBat) {
                    EntityBat bat = (EntityBat) entityObj;
                    RenderUtil.drawEntityBox(relX, relY, relZ, bat.width, bat.height, batEspColor, 0.0D);
                }
            }
        }
    }
}