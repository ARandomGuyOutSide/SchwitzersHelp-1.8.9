package com.schwitzer.schwitzersHelp.helpStuff;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@Mod(modid = "aimassist", version = "1.0", name = "AimAssist")
public class AimAssist {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private boolean showCircle;

    private boolean aimAssist;
    private float speed = 3.0F;
    private float distance = 5.0F;
    private float fov = 90.0F;

    private boolean clickAim;
    private boolean weaponOnly;
    private boolean aimInvis;
    private boolean blatantMode;
    private boolean ignoreFriends;

    private List<Entity> friends = new ArrayList<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        aimAssist = config.isAim_Assist();
        speed = config.getAimAssistSpeed();
        distance = config.getAimAssistDistance();
        fov = config.getAimAssistFov();
        clickAim = config.isClick_aim();
        blatantMode = config.isBlatant_mode();
        weaponOnly = config.isWeapon_only();

        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!aimAssist) return;
        if (weaponOnly && !isPlayerHoldingWeapon()) return;

        // Falls nichts aktiv ist → Beenden
        if (!clickAim && !blatantMode) return;

        // Falls ClickAim aktiviert ist, aber die Maustaste NICHT gedrückt wird → Beenden
        if (clickAim && !Mouse.isButtonDown(0)) return;

        EntityPlayer target = (EntityPlayer) getEnemy();
        if (target != null) {
            double targetYaw = normalizeYaw(getTargetYaw(target));
            double targetPitch = getTargetPitch(target);

            if (blatantMode) {
                aimAtTarget(targetYaw, targetPitch);
            } else {
                smoothAim(targetYaw, targetPitch);
            }
        }
    }



    private void aimAtTarget(double targetYaw, double targetPitch) {
        mc.thePlayer.rotationYaw = (float) targetYaw;
        mc.thePlayer.rotationPitch = (float) targetPitch;
    }

    private void smoothAim(double targetYaw, double targetPitch) {
        double yawDiff = normalizeYaw(targetYaw - mc.thePlayer.rotationYaw);
        double pitchDiff = targetPitch - mc.thePlayer.rotationPitch;

        float yawSpeed = speed * 0.15f;
        float pitchSpeed = speed * 0.15f;

        yawDiff = Math.max(-yawSpeed, Math.min(yawDiff, yawSpeed));
        pitchDiff = Math.max(-pitchSpeed, Math.min(pitchDiff, pitchSpeed));

        mc.thePlayer.rotationYaw += yawDiff;
        mc.thePlayer.rotationPitch += pitchDiff;
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        showCircle = config.isShow_circle();

        if (!showCircle) return;
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) return;

        drawFovCircle();
    }

    private void drawFovCircle() {
        ScaledResolution sr = new ScaledResolution(mc);
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;

        float modFov = config.getAimAssistFov();
        float gameFov = mc.gameSettings.fovSetting;
        float baseScale = sr.getScaledWidth() / 3.0f;
        float scaleFactor = (float) (2.5 - (gameFov / 110.0f));

        float radius = (baseScale * scaleFactor * (modFov / 90.0f)) / 8;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0f, 0.0f, 0.0f, 0.5f);

        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < 360; i += 5) {
            double angle = Math.toRadians(i);
            double x = centerX + Math.cos(angle) * radius;
            double y = centerY + Math.sin(angle) * radius;
            GL11.glVertex2d(x, y);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private double getTargetYaw(Entity target) {
        double diffX = target.posX - mc.thePlayer.posX;
        double diffZ = target.posZ - mc.thePlayer.posZ;
        return -Math.toDegrees(Math.atan2(diffX, diffZ));
    }

    private double getTargetPitch(Entity target) {
        double diffY = (target.posY + target.getEyeHeight()) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double distanceXZ = Math.sqrt(Math.pow(target.posX - mc.thePlayer.posX, 2) + Math.pow(target.posZ - mc.thePlayer.posZ, 2));
        return -Math.toDegrees(Math.atan2(diffY, distanceXZ));
    }

    private double normalizeYaw(double yaw) {
        yaw %= 360;
        if (yaw > 180) yaw -= 360;
        if (yaw < -180) yaw += 360;
        return yaw;
    }

    private boolean isPlayerHoldingWeapon() {
        if (mc.thePlayer.getHeldItem() == null) return false;

        return mc.thePlayer.getHeldItem().getItem() instanceof net.minecraft.item.ItemSword;
    }

    public Entity getEnemy() {
        ignoreFriends = config.isIgnore_friends();
        aimInvis = config.isAim_invis();

        if (mc.theWorld == null) return null;

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer) continue;
            if (player.isDead) continue;
            if (!aimInvis && player.isInvisible()) continue;
            if (ignoreFriends && isFriend(player)) continue;
            if (mc.thePlayer.getDistanceToEntity(player) > distance) continue;
            if (!blatantMode && !isInFov(player, fov)) continue;

            return player;
        }
        return null;
    }

    private boolean isInFov(Entity target, float fov) {
        double angle = normalizeYaw(getTargetYaw(target) - mc.thePlayer.rotationYaw);
        return angle >= -fov / 2 && angle <= fov / 2;
    }

    public boolean isFriend(Entity entity) {
        return friends.contains(entity);
    }

    public void addFriend(Entity entity) {
        if (!friends.contains(entity)) {
            friends.add(entity);
        }
    }

    public boolean removeFriend(Entity entity) {
        return friends.remove(entity);
    }

    public List<Entity> getFriends() {
        return friends;
    }
}
