package com.schwitzer.schwitzersHelp.helpStuff;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = "helpStuff", name = "Cheats", version = "1.0")
public class MovementHelp {

    private float movementspeed;
    private boolean modifymovementspeed;
    private boolean autojump;
    private boolean fly;
    private boolean noFall;
    private boolean useHypixelSpeed;
    private long lastJumpTime = 0;
    private Minecraft mc = Minecraft.getMinecraft();
    private SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        modifymovementspeed = config.isModify_movementspeed();
        movementspeed = config.getMovementspeed();
        autojump = config.isAuto_jump();
        fly = config.isFly();
        noFall = config.isNoFall();
        useHypixelSpeed = config.isModify_movementspeed_hypixel();

        // Flugmodus
        if (fly) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.motionY = 0.5;
            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.thePlayer.motionY = -0.5;
            } else {
                mc.thePlayer.motionY = 0;
            }
        }

        // Kein-Fallschaden
        if (noFall && mc.thePlayer.fallDistance > 2) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
        }


        // Zeitstempel des letzten Sprungs aktualisieren
        if (mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.onGround) {
            lastJumpTime = System.currentTimeMillis();
        }

        // Modifizierung der Bewegungsgeschwindigkeit
        if (!modifymovementspeed) return;

        if ((mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0)
                && !mc.thePlayer.isSneaking()) {

            if (useHypixelSpeed) {
                double hypixelSpeed = 1.13; // Hypixel-spezifischer Geschwindigkeitswert

                // Geschwindigkeit (gs) berechnen
                double csp = Math.sqrt(
                        mc.thePlayer.motionX * mc.thePlayer.motionX +
                                mc.thePlayer.motionZ * mc.thePlayer.motionZ);

                // Überprüfen, ob der Spieler in den letzten 2 Sekunden gesprungen ist
                long currentTime = System.currentTimeMillis();
                boolean recentlyJumped = (currentTime - lastJumpTime) <= 1000; // 1 Sekunde = 1000 ms

                if (recentlyJumped && csp != 0.0D && mc.thePlayer.onGround && !mc.thePlayer.capabilities.isFlying) {
                    double fixedInput = 1.2;
                    double factor = 0.5; // Faktor, um die Geschwindigkeit zu skalieren
                    double val = 1.0 + factor * (fixedInput - 1.0); // Alternative Formulierung von val

                    // Begrenzung der Geschwindigkeit auf serverfreundliche Werte
                    double maxAllowedSpeed = 0.3; // Maximale Geschwindigkeit auf Hypixel
                    double newSpeed = csp * val;
                    if (newSpeed > maxAllowedSpeed) {
                        val = maxAllowedSpeed / csp;
                    }

                    // Bewegung (ss) anwenden
                    double yaw = mc.thePlayer.rotationYaw;
                    if (mc.thePlayer.moveForward < 0.0F) {
                        yaw += 180.0F;
                    }

                    float moveFactor;
                    if (mc.thePlayer.moveForward < 0.0F) {
                        moveFactor = -0.5F;
                    } else if (mc.thePlayer.moveForward > 0.0F) {
                        moveFactor = 0.5F;
                    } else {
                        moveFactor = 1.0F;
                    }

                    if (mc.thePlayer.moveStrafing > 0.0F) {
                        yaw -= 90.0F * moveFactor;
                    }

                    if (mc.thePlayer.moveStrafing < 0.0F) {
                        yaw += 90.0F * moveFactor;
                    }

                    yaw *= 0.017453292F; // Konvertieren in Radiant

                    mc.thePlayer.motionX = -Math.sin(yaw) * (csp * val);
                    mc.thePlayer.motionZ = Math.cos(yaw) * (csp * val);
                }
            } else {
                // Standard-Berechnung der Geschwindigkeit
                double maxSpeed = movementspeed * 0.25; // Bewegungswert aus der Konfiguration
                float yaw = mc.thePlayer.rotationYaw;

                double radianYaw = Math.toRadians(yaw);
                double forward = mc.thePlayer.moveForward;
                double strafing = mc.thePlayer.moveStrafing;

                double motionX = -Math.sin(radianYaw) * forward * maxSpeed + Math.cos(radianYaw) * strafing * maxSpeed;
                double motionZ = Math.cos(radianYaw) * forward * maxSpeed + Math.sin(radianYaw) * strafing * maxSpeed;

                if (autojump && mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                    mc.thePlayer.motionY *= 0.5;
                }

                mc.thePlayer.motionX = motionX;
                mc.thePlayer.motionZ = motionZ;

                System.out.println(String.format("X motion: %f Y motion: %f Z motion: %f",
                        mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ));
            }
        }
    }
}