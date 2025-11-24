package com.schwitzer.schwitzersHelp.minigames;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MinigameEsp {
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private static Minecraft mc = Minecraft.getMinecraft();

    private boolean zombiesGameEsp;
    private OneColor zombiesGameEspColor;
    private boolean chaosDragonEsp;
    private OneColor chaosDragonEspColor;
    private boolean dragonLine;
    private OneColor dragonLineColor;

    @SubscribeEvent
    public void onLastWorldRender(RenderWorldLastEvent event) {
        zombiesGameEsp = config.isZombieGameEsp();
        zombiesGameEspColor = config.getZombiesGameColor();
        chaosDragonEsp = config.isChaosDragonEsp();
        chaosDragonEspColor = config.getChaosDragonEspColor();
        dragonLine = config.isDragonline();
        dragonLineColor = config.getDragonLineColor();

        if(!zombiesGameEsp && !chaosDragonEsp && !dragonLine) return;

        double camX = mc.getRenderManager().viewerPosX;
        double camY = mc.getRenderManager().viewerPosY;
        double camZ = mc.getRenderManager().viewerPosZ;

        for (Object entityObj : mc.theWorld.loadedEntityList) {
            if (entityObj instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) entityObj;

                if (entity == mc.thePlayer) continue;

                // Relative Koordinaten für ALLE Entities berechnen
                double relX = entity.posX - camX;
                double relY = entity.posY - camY;
                double relZ = entity.posZ - camZ;

                if (zombiesGameEsp) {
                    if (entityObj instanceof EntityZombie ||
                            entityObj instanceof EntityBlaze ||
                            entityObj instanceof EntityEndermite ||
                            entityObj instanceof EntitySkeleton ||
                            entityObj instanceof EntitySilverfish ||
                            entityObj instanceof EntityWolf) {

                        RenderUtil.drawEntityBox(relX, relY, relZ, entity.width / 2.0, entity.height, zombiesGameEspColor, 0.0D);
                    }
                }

                if (chaosDragonEsp) {
                    if (entityObj instanceof EntityDragon) {
                        EntityDragon dragon = (EntityDragon) entityObj;
                        RenderUtil.drawEntityBox(relX, relY, relZ, dragon.width / 2.0, dragon.height, chaosDragonEspColor, 0.0D);
                    }
                }

                // Dragon Line - nur wenn die aktuelle Entity ein Drache ist
                if (dragonLine && entityObj instanceof EntityDragon) {
                    EntityDragon dragon = (EntityDragon) entityObj;

                    // Endpunkt: Drache (relativ zur Kamera)
                    Vec3 dragonPos = new Vec3(
                            dragon.posX - camX,
                            dragon.posY - camY + 3,
                            dragon.posZ - camZ
                    );

                    // Zeichne die Linie vom Spieler zum Drachen
                    RenderUtil.drawLine(dragonPos, dragonLineColor, false);
                }
            }
        }
    }
}