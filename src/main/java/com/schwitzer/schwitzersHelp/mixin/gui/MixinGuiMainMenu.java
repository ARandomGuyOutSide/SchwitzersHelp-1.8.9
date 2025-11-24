package com.schwitzer.schwitzersHelp.mixin.gui;

import com.schwitzer.schwitzersHelp.gui.AutoUpdaterGUI;
import net.minecraft.client.gui.GuiMainMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu {
    @Shadow
    private String splashText;

    @Final
    @Inject(method = "updateScreen", at = @At("RETURN"))
    private void initGui(CallbackInfo ci) {

        if (!AutoUpdaterGUI.checkedForUpdates) {
            AutoUpdaterGUI.checkedForUpdates = true;
            System.out.println("DEBUG: Calling getLatestVersion()");
            AutoUpdaterGUI.getLatestVersion();
            System.out.println("DEBUG: isOutdated = " + AutoUpdaterGUI.isOutdated);
            System.out.println("DEBUG: latestVersion = " + AutoUpdaterGUI.latestVersion);

            if (AutoUpdaterGUI.isOutdated) {
                System.out.println("DEBUG: Showing GUI");
                AutoUpdaterGUI.showGUI();
            }
        }

        if (AutoUpdaterGUI.isOutdated) {
            this.splashText = "Update SchwitzersHelp!"; // Fixed text
        }
    }
}