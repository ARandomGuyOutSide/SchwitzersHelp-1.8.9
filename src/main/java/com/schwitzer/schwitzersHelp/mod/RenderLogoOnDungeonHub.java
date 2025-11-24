package com.schwitzer.schwitzersHelp.mod;

import com.schwitzer.schwitzersHelp.util.ScoreboardUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RenderLogoOnDungeonHub {

    // S coordinates
    private BlockPos[] sCoordinates = {
            new BlockPos(-51, 154, 2),
            new BlockPos(-51, 154, 3),
            new BlockPos(-51, 154, 4),
            new BlockPos(-51, 153, 4),
            new BlockPos(-51, 152, 4),
            new BlockPos(-51, 152, 3),
            new BlockPos(-51, 152, 2),
            new BlockPos(-51, 151, 2),
            new BlockPos(-51, 150, 2),
            new BlockPos(-51, 150, 3),
            new BlockPos(-51, 150, 4)
    };

    // H coordinates
    private BlockPos[] hCoordinates = {
            new BlockPos(-51, 154, -1),
            new BlockPos(-51, 153, -1),
            new BlockPos(-51, 152, -1),
            new BlockPos(-51, 151, -1),
            new BlockPos(-51, 150, -1),
            new BlockPos(-51, 152, -2),
            new BlockPos(-51, 152, -3),
            new BlockPos(-51, 152, -4),
            new BlockPos(-51, 153, -4),
            new BlockPos(-51, 154, -4),
            new BlockPos(-51, 151, -4),
            new BlockPos(-51, 150, -4)
    };

    private boolean logoLoaded = false;
    private int tickCounter = 0;

    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinWorldEvent event) {
        // Reset flag when player joins world
        if (event.entity == Minecraft.getMinecraft().thePlayer) {
            logoLoaded = false;
            tickCounter = 0;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        // Wait a bit before checking/loading
        tickCounter++;
        if (tickCounter < 20) return;

        try {
            if (!logoLoaded && ScoreboardUtil.isInAreaOfTheGame("dung")) {
                loadLogo(mc.theWorld);
                logoLoaded = true;
            }
        } catch (Exception e) {
            // Safely handle any errors
            System.out.println("Error loading logo: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        // Reset flag when world unloads
        logoLoaded = false;
        tickCounter = 0;
    }

    private void loadLogo(World world) {
        if (world.isRemote) { // Only on client side
            try {
                // Place S letter blocks
                for (BlockPos pos : sCoordinates) {
                    if (world.isBlockLoaded(pos)) {
                        world.setBlockState(pos, Blocks.redstone_block.getDefaultState(), 3);
                    }
                }

                // Place H letter blocks
                for (BlockPos pos : hCoordinates) {
                    if (world.isBlockLoaded(pos)) {
                        world.setBlockState(pos, Blocks.redstone_block.getDefaultState(), 3);
                    }
                }

                System.out.println("Logo loaded successfully!");

            } catch (Exception e) {
                System.out.println("Error placing blocks: " + e.getMessage());
            }
        }
    }
}