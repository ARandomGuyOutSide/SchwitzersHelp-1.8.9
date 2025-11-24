package com.schwitzer.schwitzersHelp.helpStuff;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SafeWalk {

    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean safeWalk;

    private final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        safeWalk = config.isSafeWalk();

        if (!safeWalk)
            return;

        // Überprüfe, ob der Spieler sich in einem GUI befindet
        if (mc.currentScreen != null || mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        final BlockPos BP = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ);

        // Überprüfe, ob sich Luft unter dem Spieler befindet
        boolean isAirBelow = mc.theWorld.getBlockState(BP).getBlock() == Blocks.air &&
                mc.theWorld.getBlockState(BP.down()).getBlock() == Blocks.air;

        if (mc.thePlayer.onGround && isAirBelow) {
            // Simuliert das Drücken der Shift-Taste, verhindert aber echte Sneak-Animation
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
        } else {
            // Setzt das Sneaken zurück, wenn wieder ein Block unter dem Spieler ist
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(),
                    GameSettings.isKeyDown(mc.gameSettings.keyBindSneak));
        }
    }
}
