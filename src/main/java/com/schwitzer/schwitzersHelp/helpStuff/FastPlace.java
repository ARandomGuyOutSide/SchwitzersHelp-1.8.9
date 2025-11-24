package com.schwitzer.schwitzersHelp.helpStuff;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;

@Mod(modid = "FastPlace", version = "1.0", name = "FastPlace")
public class FastPlace {

    private BlockPos lastPlacedPos = null; // Position des zuletzt platzierten Blocks
    private Minecraft mc = Minecraft.getMinecraft();
    private boolean doFastPlace;

    private static Field rightClickDelayTimerField = null; // Das Feld für die Verzögerung
    SchwitzerHelpConfig config;

    /// Checken was da abgeht xd

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        config = SchwitzerHelpConfig.getInstance();

        // Reflection für das Feld "rightClickDelayTimer" einrichten
        try {
            rightClickDelayTimerField = mc.getClass().getDeclaredField("field_71467_ac");
        } catch (NoSuchFieldException e1) {
            try {
                rightClickDelayTimerField = mc.getClass().getDeclaredField("rightClickDelayTimer");
            } catch (NoSuchFieldException e2) {
                e2.printStackTrace();
            }
        }

        if (rightClickDelayTimerField != null) {
            rightClickDelayTimerField.setAccessible(true);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        doFastPlace = config.isFastPlace();

        if (!doFastPlace || rightClickDelayTimerField == null) {
            return;
        }

        // Verzögerung für Rechtsklick manipulieren
        try {
            // "rightClickDelayTimer" auf 0 setzen, um Verzögerungen zu vermeiden
            rightClickDelayTimerField.set(mc, 0);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // Überprüfen, ob rechte Maustaste gedrückt wird
        if (Mouse.isButtonDown(1) && !mc.thePlayer.capabilities.isFlying) {
            ItemStack heldItem = mc.thePlayer.getHeldItem();

            // Prüfen, ob der Spieler einen Block in der Hand hält
            if (heldItem != null && heldItem.getItem() instanceof ItemBlock) {
                // Blockplatzierung ohne Verzögerung
                attemptPlaceBlock();
            }
        }
    }

    private void attemptPlaceBlock() {
        // Informationen über den angezielten Block abrufen
        MovingObjectPosition target = mc.objectMouseOver;

        if (target != null && target.typeOfHit == MovingObjectType.BLOCK) {
            BlockPos targetPos = target.getBlockPos();
            EnumFacing sideHit = target.sideHit;

            // Blockplatzierung nur seitlich (keine Ober- oder Unterseite)
            if (sideHit != EnumFacing.UP && sideHit != EnumFacing.DOWN) {
                Block blockAtTarget = mc.theWorld.getBlockState(targetPos).getBlock();

                // Prüfen, ob die Blockplatzierung möglich ist (kein Liquid oder Luft)
                if (blockAtTarget != Blocks.air && !(blockAtTarget instanceof BlockLiquid)) {
                    // Platzierung nur, wenn es eine neue Position ist
                    if (!targetPos.equals(lastPlacedPos)) {
                        placeBlock(targetPos, sideHit);
                    }
                }
            }
        }
    }

    private void placeBlock(BlockPos pos, EnumFacing sideHit) {
        ItemStack heldItem = mc.thePlayer.getHeldItem();

        if (mc.playerController.onPlayerRightClick(
                mc.thePlayer, mc.theWorld, heldItem, pos, sideHit, mc.objectMouseOver.hitVec)) {

            // Animation und Rückmeldung
            mc.thePlayer.swingItem();
            mc.getItemRenderer().resetEquippedProgress();

            // Aktualisierung der letzten Position
            lastPlacedPos = pos; // Die Position wird weiterhin gespeichert, um Doppelplatzierungen zu verhindern
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        // Optional: Logik für Block-Highlighting einfügen, falls gewünscht
    }
}
