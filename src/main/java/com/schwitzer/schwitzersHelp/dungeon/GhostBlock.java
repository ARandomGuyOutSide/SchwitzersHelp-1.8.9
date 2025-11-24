package com.schwitzer.schwitzersHelp.dungeon;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class GhostBlock {
    private static final String WITHER_ESSENCE_ID = "e0f3e929-869e-3dca-9504-54c666ee6f23";
    private static final String READSTONE_HEAD_ID = "21f0372e-249a-4f95-bd8e-ea8414b7e577";

    private long lastGhostBlockTime = 0;

    private final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    @SubscribeEvent
    public void onRightClick(InputEvent.MouseInputEvent event) {
        if(config.isGhost_block())
        {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer == null || mc.theWorld == null)
                return;

            int delay = config.getGhostBlockDelay();
            long currentTime = System.currentTimeMillis();

            if (mc.gameSettings.keyBindUseItem.isKeyDown()
                    && mc.thePlayer.getHeldItem() != null
                    && mc.thePlayer.getHeldItem().getItem() instanceof ItemPickaxe) {

                MovingObjectPosition mop = mc.thePlayer.rayTrace(config.getGhostBlockDistance(), 1.0F);

                if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    // Only create ghost block if enough time has passed
                    if (currentTime - lastGhostBlockTime >= delay) {
                        BlockPos pos = mop.getBlockPos();
                        WorldClient worldClient = mc.theWorld;

                        // Check if block is a chest, button or lever
                        Block block = worldClient.getBlockState(pos).getBlock();
                        if (block == Blocks.chest || block == Blocks.trapped_chest || block == Blocks.ender_chest ||
                                block == Blocks.stone_button || block == Blocks.wooden_button || block == Blocks.lever) {
                            return;
                        }

                        // Check if the block is a wither essence skull
                        if (config.isGhostBlockEssence()) {
                            if (block == Blocks.skull) {
                                TileEntitySkull tileEntitySkull = (TileEntitySkull) worldClient.getTileEntity(pos);
                                if (tileEntitySkull != null && tileEntitySkull.getPlayerProfile() != null &&
                                        tileEntitySkull.getPlayerProfile().getId() != null) {

                                    String skullId = tileEntitySkull.getPlayerProfile().getId().toString();

                                    // Verhindere Ghostblock für bestimmte Skulls (z.B. Wither oder Redstone Head)
                                    if (WITHER_ESSENCE_ID.equals(skullId) || READSTONE_HEAD_ID.equals(skullId)) {
                                        return;
                                    }
                                }
                            }
                        }


                        mc.thePlayer.swingItem();
                        worldClient.setBlockToAir(pos); // Removes the block client-side
                        worldClient.markBlockForUpdate(pos); // Updates the block visually

                        // Update the last ghost block time
                        lastGhostBlockTime = currentTime;
                    }
                }
            }
        }

    }
}