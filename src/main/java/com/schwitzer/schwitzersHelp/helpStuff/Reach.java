package com.schwitzer.schwitzersHelp.helpStuff;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Random;

public class Reach {

    private static Minecraft mc = Minecraft.getMinecraft();

    private static SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    private static boolean reach;

    private static float minReach = 2f;
    private static float maxReach;

    @SubscribeEvent
    public void onMouseClick(MouseEvent event) {
        if (event.button >= 0 && event.buttonstate){
            performReachAction();
        }
    }

    public static boolean performReachAction() {
        maxReach = config.getReach_distance();
        reach = config.isReach();

        if(!reach)
            return false;

        Random random = new Random();

        double reachDistance = minReach + (maxReach - minReach) * random.nextFloat();
        Object[] targetData = findTargetWithinReach(reachDistance, 0.0D);
        if (targetData == null) return false;

        Entity targetEntity = (Entity) targetData[0];
        mc.objectMouseOver = new MovingObjectPosition(targetEntity, (Vec3) targetData[1]);
        mc.pointedEntity = targetEntity;
        return true;
    }

    private static Object[] findTargetWithinReach(double reachDistance, double expandSize) {

        Entity viewEntity = mc.getRenderViewEntity();
        Entity target = null;
        if (viewEntity == null) return null;

        mc.mcProfiler.startSection("pick");
        Vec3 eyesPosition = viewEntity.getPositionEyes(1.0F);
        Vec3 lookDirection = viewEntity.getLook(1.0F);
        Vec3 reachEnd = eyesPosition.addVector(lookDirection.xCoord * reachDistance, lookDirection.yCoord * reachDistance, lookDirection.zCoord * reachDistance);
        Vec3 hitVector = null;

        List<Entity> entities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(viewEntity, viewEntity.getEntityBoundingBox()
                .addCoord(lookDirection.xCoord * reachDistance, lookDirection.yCoord * reachDistance, lookDirection.zCoord * reachDistance)
                .expand(1.0D, 1.0D, 1.0D));

        double closestDistance = reachDistance;

        for (Entity entity : entities) {
            if (entity.canBeCollidedWith()) {
                float collisionMargin = (float) (entity.getCollisionBorderSize() * getExpansionFactor(entity));
                AxisAlignedBB expandedBB = entity.getEntityBoundingBox().expand(collisionMargin, collisionMargin, collisionMargin).expand(expandSize, expandSize, expandSize);
                MovingObjectPosition intercept = expandedBB.calculateIntercept(eyesPosition, reachEnd);

                if (expandedBB.isVecInside(eyesPosition)) {
                    if (0.0D < closestDistance || closestDistance == 0.0D) {
                        target = entity;
                        hitVector = intercept == null ? eyesPosition : intercept.hitVec;
                        closestDistance = 0.0D;
                    }
                } else if (intercept != null) {
                    double distanceToHit = eyesPosition.distanceTo(intercept.hitVec);
                    if (distanceToHit < closestDistance || closestDistance == 0.0D) {
                        if (entity == viewEntity.ridingEntity) {
                            if (closestDistance == 0.0D) {
                                target = entity;
                                hitVector = intercept.hitVec;
                            }
                        } else {
                            target = entity;
                            hitVector = intercept.hitVec;
                            closestDistance = distanceToHit;
                        }
                    }
                }
            }
        }

        if (closestDistance < reachDistance && !(target instanceof EntityLivingBase) && !(target instanceof EntityItemFrame)) {
            target = null;
        }

        mc.mcProfiler.endSection();
        return target != null && hitVector != null ? new Object[]{target, hitVector} : null;
    }

    private static double getExpansionFactor(Entity entity) {
        return 1.5D;
    }
}
