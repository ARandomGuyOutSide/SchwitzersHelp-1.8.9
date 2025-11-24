package com.schwitzer.schwitzersHelp.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class EntityUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    /**
     * Returns the nearest entity colliding with another entity (inside or overlapping its bounding box),
     * while filtering out unwanted entity types.
     *
     * @param e          The entity whose bounding box is checked.
     * @param entityType Optional entity type filter. If null, all entity types are allowed.
     * @return The nearest valid entity colliding with e, or null if none found.
     */
    public static Entity getEntityCuttingOtherEntity(Entity e, Class<?> entityType) {
        List<Entity> possible = mc.theWorld.getEntitiesInAABBexcluding(
                e,
                e.getEntityBoundingBox().expand(0.3D, 2.0D, 0.3D),
                a -> {
                    boolean flag1 = (!a.isDead && !a.equals(mc.thePlayer));
                    boolean flag2 = !(a instanceof EntityArmorStand);
                    boolean flag3 = !(a instanceof net.minecraft.entity.projectile.EntityFireball);
                    boolean flag4 = !(a instanceof net.minecraft.entity.projectile.EntityFishHook);
                    boolean flag5 = (entityType == null || entityType.isInstance(a));
                    return flag2 && flag3 && flag4 && flag5;
                }
        );

        if (!possible.isEmpty()) {
            return Collections.min(possible, Comparator.comparing(e2 -> e2.getDistanceToEntity(e)));
        }
        return null;
    }

    /**
     * Finds entities linked to ArmorStands with matching names, filters them and sorts them
     * by a cost function (distance + rotation).
     *
     * @param entityNames Set of substrings that must appear in the ArmorStand name.
     * @return Sorted list of EntityLivingBase objects.
     */
    public static List<EntityLivingBase> getEntities(Set<String> entityNames) {
        List<EntityLivingBase> entities = new ArrayList<>();

        // Debug: Alle geladenen Entities zählen
        ChatUtil.debugMessage("[EntityUtil] Loaded entities: " + mc.theWorld.loadedEntityList.size());

        // Filter loaded entities -> only ArmorStands with matching names
        mc.theWorld.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityArmorStand)
                .forEach(v -> {
                    String tag = v.getCustomNameTag();
                    ChatUtil.debugMessage("[EntityUtil] ArmorStand found with name: " + tag);

                    if (!v.isDead && tag != null) {
                        String cleanTag = ChatUtil.removeColorCodes(tag).toLowerCase();

                        boolean matches = entityNames.stream().anyMatch(a -> {
                            boolean match = cleanTag.contains(a.toLowerCase());
                            if (match) {
                                ChatUtil.debugMessage("[EntityUtil] Match found: ArmorStand '" + cleanTag + "' contains '" + a + "'");
                            }
                            return match;
                        });

                        if (matches) {
                            Entity livingBase = getEntityCuttingOtherEntity(v, null);
                            if (livingBase instanceof EntityLivingBase && !livingBase.equals(mc.thePlayer)) {
                                ChatUtil.debugMessage("[EntityUtil] Added entity: " + livingBase.getName());
                                entities.add((EntityLivingBase) livingBase);
                            } else {
                                ChatUtil.debugMessage("[EntityUtil] No valid living entity found for this ArmorStand");
                            }
                        }
                    } else {
                        if (tag == null) ChatUtil.debugMessage("[EntityUtil] ArmorStand has no name");
                        if (v.isDead) ChatUtil.debugMessage("[EntityUtil] ArmorStand is dead");
                    }
                });

        Vec3 playerPos = mc.thePlayer.getPositionVector();
        float normalizedYaw = AngleUtil.normalizeAngle(mc.thePlayer.rotationYaw);

        List<EntityLivingBase> sortedEntities = entities.stream()
                .filter(EntityLivingBase::isEntityAlive)
                .sorted(Comparator.comparingDouble(ent -> {
                            Vec3 entPos = ent.getPositionVector();
                            double distanceCost = playerPos.distanceTo(entPos);
                            double angleCost = Math.abs(AngleUtil.getNeededYawChange(normalizedYaw, AngleUtil.getRotationYaw(entPos)));

                            int devMKillDist = 80;
                            int devMKillRot = 20;

                            return distanceCost * ((float) devMKillDist / 100f) + angleCost * ((float) devMKillRot / 100f);
                        }
                )).collect(Collectors.toList());

        ChatUtil.debugMessage("[EntityUtil] Found " + sortedEntities.size() + " matching entities after sorting");

        for (EntityLivingBase e : sortedEntities) {
            ChatUtil.debugMessage("[EntityUtil] Sorted entity: " + e.getName() + " at distance " + playerPos.distanceTo(e.getPositionVector()));
        }

        return sortedEntities;
    }


    public static float getDistanceToTarget(Entity entity)
    {
        Vec3 playerPos = mc.thePlayer.getPositionVector();
        Vec3 entityPos = entity.getPositionVector();

        ChatUtil.formatedChatMessage("Distance to entity is " + (float) playerPos.distanceTo(entityPos));

        return (float) playerPos.distanceTo(entityPos);
    }

    /**
     * Example method stub – you need to replace this with your actual implementation.
     * Parses the health value out of an ArmorStand name.
     */
    private static int getHealthFromStandName(String customNameTag) {
        // TODO: Implement real parsing logic based on your server format
        return 10;
    }
}
