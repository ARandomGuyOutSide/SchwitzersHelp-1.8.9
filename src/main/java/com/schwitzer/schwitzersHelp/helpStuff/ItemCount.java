package com.schwitzer.schwitzersHelp.helpStuff;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemCount {

    SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    private boolean showItemCount;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        showItemCount = config.isItemCount();

        if (!showItemCount)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        RenderManager renderManager = mc.getRenderManager();

        // Maximale Renderreichweite für Items
        double maxRenderDistance = 16;

        // Schleife durch alle Entities in der Welt
        for (Object obj : mc.theWorld.loadedEntityList) {
            if (obj instanceof EntityItem) {
                EntityItem entityItem = (EntityItem) obj;

                // Entfernung zwischen Spieler und Item berechnen
                double distance = entityItem.getDistanceToEntity(mc.thePlayer);

                // Nur rendern, wenn die Entfernung <= maxRenderDistance ist
                if (distance > maxRenderDistance)
                    continue;

                // Anzahl der Items abrufen
                int count = entityItem.getEntityItem().stackSize;

                // Position berechnen
                Vec3 vec = new Vec3(
                        entityItem.prevPosX + (entityItem.posX - entityItem.prevPosX) * event.partialTicks,
                        entityItem.prevPosY + (entityItem.posY - entityItem.prevPosY) * event.partialTicks,
                        entityItem.prevPosZ + (entityItem.posZ - entityItem.prevPosZ) * event.partialTicks
                );

                double x = vec.xCoord - renderManager.viewerPosX;
                double y = vec.yCoord - renderManager.viewerPosY + 0.5;
                double z = vec.zCoord - renderManager.viewerPosZ;

                // Anzeige rendern
                renderLabel(String.valueOf(count), x, y, z, renderManager);
            }
        }
    }

    private void renderLabel(String text, double x, double y, double z, RenderManager renderManager) {
        Minecraft mc = Minecraft.getMinecraft();

        // OpenGL-Transformationen
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-renderManager.playerViewY, 0, 1, 0);
        GlStateManager.rotate(renderManager.playerViewX, 1, 0, 0);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);

        // Text rendern
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        mc.fontRendererObj.drawString(text, -mc.fontRendererObj.getStringWidth(text) / 2, 0, 0xFFFFFF);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();

        GlStateManager.popMatrix();
    }
}
