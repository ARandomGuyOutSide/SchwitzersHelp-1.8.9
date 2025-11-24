package com.schwitzer.schwitzersHelp.features;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class PlaceBlocksOnCommand {
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private static final Minecraft mc = Minecraft.getMinecraft();

    // Toggle-Status (statisch, damit er zwischen Command-Aufrufen erhalten bleibt)
    private static boolean enabled = false;

    // Für kontinuierliches Platzieren
    private static boolean rightClickHeld = false;
    private static int placeDelay = 0;
    private static final int PLACE_DELAY_TICKS = 1;

    public static void setEnabled(boolean enabled) {
        PlaceBlocksOnCommand.enabled = enabled;
        if (!enabled) {
            rightClickHeld = false; // Reset beim Deaktivieren
        }
    }

    @SubscribeEvent
    public void onMouseClick(MouseEvent event) {
        // Nur weitermachen wenn das Feature aktiviert ist
        if (!enabled) {
            return;
        }

        // Nur bei Rechtsklick (Button 1)
        if (event.button != 1) {
            return;
        }

        // Prüfen ob Spieler existiert und im Spiel ist
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        // Prüfen ob der Spieler einen Block in der Hand hat
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock)) {
            return;
        }

        // Status setzen basierend auf Button-State
        rightClickHeld = event.buttonstate;

        // Bei Drücken sofort einen Block platzieren
        if (event.buttonstate) {
            placeDelay = 0; // Reset delay
            tryPlaceBlock();
        }

        // Event canceln um normales Platzieren zu verhindern
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        // Nur bei END-Phase um doppelte Ausführung zu vermeiden
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        // Nur weitermachen wenn Feature aktiviert und Rechtsklick gehalten wird
        if (!enabled || !rightClickHeld) {
            return;
        }

        // Delay verwalten
        if (placeDelay > 0) {
            placeDelay--;
            return;
        }

        // Block platzieren und Delay reset
        tryPlaceBlock();
        placeDelay = PLACE_DELAY_TICKS;
    }

    private void tryPlaceBlock() {
        // Prüfen ob Spieler existiert und im Spiel ist
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        // Prüfen ob der Spieler einen Block in der Hand hat
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock)) {
            return;
        }

        // Raytracing um zu sehen worauf der Spieler schaut
        MovingObjectPosition rayTrace = mc.objectMouseOver;
        if (rayTrace == null || rayTrace.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }

        // Position des angezielten Blocks
        BlockPos hitPos = rayTrace.getBlockPos();
        EnumFacing hitSide = rayTrace.sideHit;

        // Neue Position berechnen (neben dem angezielten Block, nicht darauf)
        BlockPos newPos = hitPos.offset(hitSide);

        // Prüfen ob die Position frei ist (nicht solid)
        World world = mc.theWorld;
        if (!world.getBlockState(newPos).getBlock().isReplaceable(world, newPos)) {
            return;
        }

        // Prüfen ob der Block in den Spieler platziert werden würde
        if (wouldBlockIntersectPlayer(newPos)) {
            return;
        }

        // Block aus dem gehaltenen Item bekommen
        ItemBlock itemBlock = (ItemBlock) heldItem.getItem();
        Block blockToPlace = itemBlock.getBlock();

        try {
            // Block platzieren
            world.setBlockState(newPos, blockToPlace.getDefaultState());


        } catch (Exception e) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§cFehler beim Platzieren des Blocks!"));
            e.printStackTrace();
            rightClickHeld = false; // Stop bei Fehler
        }
    }

    private boolean wouldBlockIntersectPlayer(BlockPos pos) {
        if (mc.thePlayer == null) {
            return false;
        }

        // Spieler-Bounding-Box bekommen
        double playerX = mc.thePlayer.posX;
        double playerY = mc.thePlayer.posY;
        double playerZ = mc.thePlayer.posZ;

        // Spieler-Hitbox: etwa 0.6 breit, 1.8 hoch
        double playerWidth = 0.6;
        double playerHeight = 1.8;

        // Spieler-Bounding-Box berechnen
        double playerMinX = playerX - playerWidth / 2;
        double playerMaxX = playerX + playerWidth / 2;
        double playerMinY = playerY;
        double playerMaxY = playerY + playerHeight;
        double playerMinZ = playerZ - playerWidth / 2;
        double playerMaxZ = playerZ + playerWidth / 2;

        // Block-Bounding-Box (ein ganzer Block: 1x1x1)
        double blockMinX = pos.getX();
        double blockMaxX = pos.getX() + 1.0;
        double blockMinY = pos.getY();
        double blockMaxY = pos.getY() + 1.0;
        double blockMinZ = pos.getZ();
        double blockMaxZ = pos.getZ() + 1.0;

        // Kollisionsprüfung: Überschneiden sich die Bounding-Boxes?
        boolean xOverlap = playerMaxX > blockMinX && playerMinX < blockMaxX;
        boolean yOverlap = playerMaxY > blockMinY && playerMinY < blockMaxY;
        boolean zOverlap = playerMaxZ > blockMinZ && playerMinZ < blockMaxZ;

        // Kollision nur wenn alle drei Achsen überschneiden
        return xOverlap && yOverlap && zOverlap;
    }
}