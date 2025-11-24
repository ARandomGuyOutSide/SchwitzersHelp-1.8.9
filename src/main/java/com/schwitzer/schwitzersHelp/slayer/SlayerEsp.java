package com.schwitzer.schwitzersHelp.slayer;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.EntityUtil;
import com.schwitzer.schwitzersHelp.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SlayerEsp {
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private static Minecraft mc = Minecraft.getMinecraft();

    private boolean slayerEsp;
    private OneColor slayerEspColor;
    private boolean carryMode;
    private String carryName;

    private Set<String> namesToSearch = new HashSet<>(Arrays.asList(
            "Graveyard Zombie",
            "Zombie Villager"
    ));

    @SubscribeEvent
    public void onLastWorldRender(RenderWorldLastEvent event)
    {
        slayerEsp = config.isSlayerEsp();
        slayerEspColor = config.getSlayerEspColor();

        if (slayerEsp) {

            double camX = mc.getRenderManager().viewerPosX;
            double camY = mc.getRenderManager().viewerPosY;
            double camZ = mc.getRenderManager().viewerPosZ;

            if(carryMode)
            {
                carryName = config.getSlayerCarryNames();
            }

            try {
                for(EntityLivingBase entity : EntityUtil.getEntities(namesToSearch))
                {
                    double relativeX = entity.posX - camX;
                    double relativeY = entity.posY - camY;
                    double relativeZ = entity.posZ - camZ;

                    RenderUtil.drawEntityBox(relativeX, relativeY, relativeZ, entity.width, entity.height, new OneColor(5, 5, 5, 255), 0);
                }
            }catch (NullPointerException ignored)
            {
            }
        }
    }
}
