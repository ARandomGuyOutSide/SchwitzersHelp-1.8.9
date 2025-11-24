package com.schwitzer.schwitzersHelp.util;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import static net.minecraft.client.gui.Gui.drawRect;

public class RenderUtil {

    private static SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private final static float lineThickness = 3.5f;


    // Neue überladene drawLine() Methode für Coal Vein Linien
    public static void drawLine(Vec3 end, OneColor lineColor, boolean forceColor) {
        Vec3 start = new Vec3(0, Minecraft.getMinecraft().thePlayer.getEyeHeight(), 0);

        // Debug: Farbe ausgeben
        float[] color = getRenderColor(lineColor, forceColor);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting(); // Wichtig! Lighting kann Farben überschreiben

        // Farbe NACH den OpenGL-Zustandsänderungen setzen
        GlStateManager.color(color[0], color[1], color[2], color[3]);

        // Setze Linienbreite
        GL11.glLineWidth(lineThickness);

        // Zeichne die Linie
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(start.xCoord, start.yCoord, start.zCoord);
        GL11.glVertex3d(end.xCoord, end.yCoord, end.zCoord);
        GL11.glEnd();

        // Setze die OpenGL-Einstellungen zurück
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // Farbe zurücksetzen
        GlStateManager.popMatrix();
    }

    private static float[] getRenderColor(OneColor baseColor, boolean forceColor) {
        if (config.isRainbow() && !forceColor) {
            long time = System.currentTimeMillis();
            float hue = (time % 6000) / 6000.0f;
            int rgb = java.awt.Color.HSBtoRGB(hue, 1f, 1f);
            return new float[]{
                    ((rgb >> 16) & 0xFF) / 255.0f,
                    ((rgb >> 8) & 0xFF) / 255.0f,
                    (rgb & 0xFF) / 255.0f,
                    baseColor.getAlpha() / 255.0f
            };
        }
        return new float[]{
                baseColor.getRed() / 255.0f,
                baseColor.getGreen() / 255.0f,
                baseColor.getBlue() / 255.0f,
                baseColor.getAlpha() / 255.0f
        };
    }

    public static void drawNameTag(String name, double x, double y, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontRenderer = mc.fontRendererObj;
        RenderManager renderManager = mc.getRenderManager();

        float scale = 0.02F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 0.5, z);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();

        int width = fontRenderer.getStringWidth(name) / 2;
        drawRect(-width - 2, -2, width + 2, 10, 0x80000000);
        fontRenderer.drawStringWithShadow(name, -width, 0, 0xFFFFFF);

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    public static void drawBedBox(BlockPos pos, OneColor bed_ESP_color) {
        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(pos);
        Block block = blockState.getBlock();

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (!(block instanceof BlockBed)) {
            return;
        }

        BlockBed.EnumPartType partType = blockState.getValue(BlockBed.PART);
        boolean isFoot = partType == BlockBed.EnumPartType.FOOT;

        if (!isFoot) return; // Only render for foot part

        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        float[] color = getRenderColor(bed_ESP_color, false);
        GlStateManager.color(color[0], color[1], color[2], color[3]);

        EnumFacing facing = blockState.getValue(BlockBed.FACING);

        double width = 1.0D;
        double height = 0.5625D;
        double length = 2.0D;

        double x1, z1, x2, z2;

        switch (facing) {
            case NORTH:
                x1 = x;
                z1 = z - 1;
                x2 = x + width;
                z2 = z + 1;
                break;
            case SOUTH:
                x1 = x;
                z1 = z;
                x2 = x + width;
                z2 = z + length;
                break;
            case WEST:
                x1 = x - 1;
                z1 = z;
                x2 = x + 1;
                z2 = z + width;
                break;
            case EAST:
                x1 = x;
                z1 = z;
                x2 = x + length;
                z2 = z + width;
                break;
            default:
                return;
        }

        // Set line width
        GL11.glLineWidth(lineThickness);

        // Draw the box
        GL11.glBegin(GL11.GL_LINES);

        // Bottom face
        GL11.glVertex3d(x1, y, z1);
        GL11.glVertex3d(x2, y, z1);
        GL11.glVertex3d(x2, y, z1);
        GL11.glVertex3d(x2, y, z2);
        GL11.glVertex3d(x2, y, z2);
        GL11.glVertex3d(x1, y, z2);
        GL11.glVertex3d(x1, y, z2);
        GL11.glVertex3d(x1, y, z1);

        // Top face
        GL11.glVertex3d(x1, y + height, z1);
        GL11.glVertex3d(x2, y + height, z1);
        GL11.glVertex3d(x2, y + height, z1);
        GL11.glVertex3d(x2, y + height, z2);
        GL11.glVertex3d(x2, y + height, z2);
        GL11.glVertex3d(x1, y + height, z2);
        GL11.glVertex3d(x1, y + height, z2);
        GL11.glVertex3d(x1, y + height, z1);

        // Vertical edges
        GL11.glVertex3d(x1, y, z1);
        GL11.glVertex3d(x1, y + height, z1);
        GL11.glVertex3d(x2, y, z1);
        GL11.glVertex3d(x2, y + height, z1);
        GL11.glVertex3d(x2, y, z2);
        GL11.glVertex3d(x2, y + height, z2);
        GL11.glVertex3d(x1, y, z2);
        GL11.glVertex3d(x1, y + height, z2);

        GL11.glEnd();

        // Reset OpenGL settings
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static void drawEntityBox(double x, double y, double z, double width, double height, OneColor color, double yOffset) {
        float[] col = getRenderColor(color, false);

        // OpenGL-Zustand korrekt setzen
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        // Farbe setzen
        GlStateManager.color(col[0], col[1], col[2], col[3]);

        // Linienbreite setzen
        GL11.glLineWidth(lineThickness);

        GL11.glBegin(GL11.GL_LINES);

        // Bottom
        GL11.glVertex3d(x - width, y + yOffset, z - width);
        GL11.glVertex3d(x + width, y + yOffset, z - width);
        GL11.glVertex3d(x + width, y + yOffset, z - width);
        GL11.glVertex3d(x + width, y + yOffset, z + width);
        GL11.glVertex3d(x + width, y + yOffset, z + width);
        GL11.glVertex3d(x - width, y + yOffset, z + width);
        GL11.glVertex3d(x - width, y + yOffset, z + width);
        GL11.glVertex3d(x - width, y + yOffset, z - width);

        // Top
        GL11.glVertex3d(x - width, y + height + yOffset, z - width);
        GL11.glVertex3d(x + width, y + height + yOffset, z - width);
        GL11.glVertex3d(x + width, y + height + yOffset, z - width);
        GL11.glVertex3d(x + width, y + height + yOffset, z + width);
        GL11.glVertex3d(x + width, y + height + yOffset, z + width);
        GL11.glVertex3d(x - width, y + height + yOffset, z + width);
        GL11.glVertex3d(x - width, y + height + yOffset, z + width);
        GL11.glVertex3d(x - width, y + height + yOffset, z - width);

        // Verticals
        GL11.glVertex3d(x - width, y + yOffset, z - width);
        GL11.glVertex3d(x - width, y + height + yOffset, z - width);
        GL11.glVertex3d(x + width, y + yOffset, z - width);
        GL11.glVertex3d(x + width, y + height + yOffset, z - width);
        GL11.glVertex3d(x + width, y + yOffset, z + width);
        GL11.glVertex3d(x + width, y + height + yOffset, z + width);
        GL11.glVertex3d(x - width, y + yOffset, z + width);
        GL11.glVertex3d(x - width, y + height + yOffset, z + width);

        GL11.glEnd();

        // OpenGL-Zustand zurücksetzen
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void drawFilledBox(AxisAlignedBB box, OneColor color) {
        float[] col = getRenderColor(color, false);
        drawFilledBox(box, col[0], col[1], col[2], col[3]);
    }

    public static void drawFilledBox(AxisAlignedBB box, float red, float green, float blue, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();

        boolean onlyOutline = config.isBlockOutline();

        if (!onlyOutline) {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();

            GlStateManager.color(red, green, blue, alpha);
            worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

            worldrenderer.pos(box.minX, box.minY, box.minZ).endVertex();
            worldrenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
            worldrenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
            worldrenderer.pos(box.minX, box.minY, box.maxZ).endVertex();

            worldrenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
            worldrenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
            worldrenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
            worldrenderer.pos(box.minX, box.maxY, box.minZ).endVertex();

            worldrenderer.pos(box.minX, box.minY, box.minZ).endVertex();
            worldrenderer.pos(box.minX, box.maxY, box.minZ).endVertex();
            worldrenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
            worldrenderer.pos(box.maxX, box.minY, box.minZ).endVertex();

            worldrenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
            worldrenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
            worldrenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
            worldrenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();

            worldrenderer.pos(box.minX, box.minY, box.minZ).endVertex();
            worldrenderer.pos(box.minX, box.minY, box.maxZ).endVertex();
            worldrenderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
            worldrenderer.pos(box.minX, box.maxY, box.minZ).endVertex();

            worldrenderer.pos(box.maxX, box.minY, box.minZ).endVertex();
            worldrenderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
            worldrenderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
            worldrenderer.pos(box.maxX, box.minY, box.maxZ).endVertex();

            tessellator.draw();
        }

        if (!(red == 0.0f && green == 0.0f && blue == 0.0f)) {
            if (onlyOutline) {
                GlStateManager.color(red, green, blue, alpha);
            } else {
                float outlineRed = Math.min(1.0f, red + 0.2f);
                float outlineGreen = Math.min(1.0f, green + 0.2f);
                float outlineBlue = Math.min(1.0f, blue + 0.2f);
                GlStateManager.color(outlineRed, outlineGreen, outlineBlue, 1.0f);
            }

            RenderGlobal.drawSelectionBoundingBox(box);
        }

        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    /**
     * Renders multiple custom blocks for ESP
     * @param foundBlocks List of block positions to render
     * @param renderPosX Camera X offset
     * @param renderPosY Camera Y offset
     * @param renderPosZ Camera Z offset
     * @param color Color to render the blocks
     */
    public static void drawCustomBlocks(java.util.List<BlockPos> foundBlocks, double renderPosX, double renderPosY, double renderPosZ, OneColor color) {
        if (foundBlocks == null || foundBlocks.isEmpty()) return;
        
        // Choose color based on config (rainbow or default)
        float[] col = getRenderColor(color, false);
        
        // Render each found block
        for (BlockPos pos : foundBlocks) {
            AxisAlignedBB box = new AxisAlignedBB(
                pos.getX() - renderPosX,
                pos.getY() - renderPosY,
                pos.getZ() - renderPosZ,
                pos.getX() + 1 - renderPosX,
                pos.getY() + 1 - renderPosY,
                pos.getZ() + 1 - renderPosZ
            );
            drawFilledBox(box, col[0], col[1], col[2], col[3]);
        }
    }

    /**
     * Renders multiple entities with custom names for ESP
     * @param entities List of entities to render
     * @param camX Camera X position
     * @param camY Camera Y position
     * @param camZ Camera Z position
     * @param color Color to render the entities
     */
    public static void drawCustomNamedEntities(java.util.List<net.minecraft.entity.EntityLivingBase> entities, double camX, double camY, double camZ, OneColor color) {
        if (entities == null || entities.isEmpty()) return;
        
        // Choose color based on config (rainbow or default)
        float[] renderColor = getRenderColor(color, false);
        
        Minecraft mc = Minecraft.getMinecraft();
        
        // Render each found entity
        for (net.minecraft.entity.EntityLivingBase entity : entities) {
            if (entity == null || entity.isDead || entity == mc.thePlayer) continue;

            // Calculate relative position to camera
            double relativeX = entity.posX - camX;
            double relativeY = entity.posY - camY;
            double relativeZ = entity.posZ - camZ;

            // Render entity box
            drawEntityBox(relativeX, relativeY, relativeZ, entity.width, entity.height, new OneColor(renderColor[0], renderColor[1],renderColor[2], renderColor[3]), 0.0D);
        }
    }
}
