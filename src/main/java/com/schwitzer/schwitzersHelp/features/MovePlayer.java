package com.schwitzer.schwitzersHelp.features;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.schwitzer.schwitzersHelp.util.*;
import com.schwitzer.schwitzersHelp.util.PathFinder.PathFinding;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import scala.collection.parallel.ParIterableLike;

import java.util.*;

public class MovePlayer {
    private static BlockPos[] totalGoals = null;
    private static HashMap<BlockPos, String> totalGoalsWithNames = null;
    private static List<BlockPos> goalsList = null;
    private static boolean useEntityFinding = false;
    private static int currentGoalIndex = 0;
    private static EntityLivingBase mobToLookAt = null;
    private static Set<String> mobsToSearchFor = null;
    private static boolean waitingForMobAim = false;
    private static boolean isRotatingToMobAfterGoal = false; // NEW: Track rotation state
    private static PlayerInputUtil playerInputUtil;


    private static List<BlockPos> path;
    private static int currentNodeIndex = 0;
    private static boolean isMoving = false;
    private static final double REACH_DISTANCE = 0.99;
    private static final int MAX_ITERATIONS = 50000;

    // NEW: Different rotation speeds for initial vs subsequent nodes
    private static final float INITIAL_ROTATION_SPEED = 15f; // Fast initial aim
    public static final float NORMAL_ROTATION_SPEED = 5f;   // Slower for subsequent nodes
    private static boolean hasAimedAtFirstNode = false;      // Track if we've aimed at first node

    // Anti-stuck mechanism
    private static BlockPos lastPosition = null;
    private static int stuckCounter = 0;
    private static final int MAX_STUCK_TICKS = 60; // 3 seconds at 20 TPS
    private static final double MIN_MOVEMENT_THRESHOLD = 0.1; // Minimum distance to consider as movement

    // Movement state tracking to prevent oscillation
    private static int lastMovementDirection = 0; // 0=none, 1=forward, 2=back, 3=left, 4=right
    private static int movementDirectionCounter = 0;

    public static BlockPos[] getCastleWalkingCoords() {
        BlockPos[] goals = {
                new BlockPos(-12, 70, -53),
                new BlockPos(-63, 70, -44),
                new BlockPos(-95, 72, -34),
                new BlockPos(-122, 72, -4),
                new BlockPos(-150, 70, 26),
                new BlockPos(-189, 87, 51)
        };
        return goals;
    }

    public static BlockPos[] getCryptWalkingCoords() {
        BlockPos[] goals = {
                new BlockPos(-43, 70, -70),
                new BlockPos(-70, 70, -42),
                new BlockPos(-111, 71, -62),
                new BlockPos(-161, 73, -84),
                new BlockPos(-162, 61, -100)
        };
        return goals;
    }

    public static void movePlayerTo(BlockPos playerPos, BlockPos[] goals) {
        currentGoalIndex = 0;
        totalGoals = goals;
        totalGoalsWithNames = null;
        goalsList = null;
        useEntityFinding = false;

        path = PathFinding.findPath(playerPos, totalGoals[currentGoalIndex], MAX_ITERATIONS);

        if (path == null || path.isEmpty()) {
            ChatUtil.formatedChatMessage("No path found for next goal!");
            stopMoving();
            return;
        }


        currentNodeIndex = 0;
        isMoving = true;
        hasAimedAtFirstNode = false; // Reset the flag

        // Reset anti-stuck mechanism
        lastPosition = null;
        stuckCounter = 0;
        lastMovementDirection = 0;
        movementDirectionCounter = 0;
        isRotatingToMobAfterGoal = false;
        waitingForMobAim = false;

        ChatUtil.formatedChatMessage("Player at: " + playerPos);
        ChatUtil.formatedChatMessage("Moving to: " + goals[currentGoalIndex] + " within " + path.size() + " Nodes");
    }

    public static void movePlayerTo(BlockPos playerPos, BlockPos[] goals, Set<String> targets) {
        movePlayerTo(playerPos, goals);

        // Store the mobs to look at
        mobsToSearchFor = targets;

        playerInputUtil = new PlayerInputUtil();
    }


    public static boolean movePlayerToEntity(Entity entity) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player == null || entity == null) {
            ChatUtil.formatedChatMessage("Player or entity is null!");
            return false;
        }

        BlockPos playerPos = new BlockPos(player.posX, player.posY, player.posZ);
        BlockPos entityPos = new BlockPos(entity.posX, entity.posY, entity.posZ);

        // Find the best reachable position around the entity
        BlockPos targetPosition = findBestPositionAroundEntity(entityPos, playerPos);

        if (targetPosition == null) {
            ChatUtil.formatedChatMessage("No reachable position found around the entity!");
            return false;
        }

        // Create a single-goal array with the target position
        BlockPos[] goals = {targetPosition};

        // Use existing movePlayerTo method
        movePlayerTo(playerPos, goals);
        return true;
    }


    public static void stopMoving() {
        isMoving = false;
        path = null;
        currentNodeIndex = 0;
        hasAimedAtFirstNode = false; // Reset flag when stopping
        lastPosition = null;
        stuckCounter = 0;
        lastMovementDirection = 0;
        movementDirectionCounter = 0;
        totalGoals = null;
        totalGoalsWithNames = null;
        goalsList = null;
        useEntityFinding = false;
        isRotatingToMobAfterGoal = false;
        waitingForMobAim = false;
        stopAllMovement();
        RotatePlayerTo.stopRotation();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !isMoving || path == null) return;

        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;


        // NEW: Check if we're in the rotation phase after reaching a goal
        if (isRotatingToMobAfterGoal) {
            ChatUtil.debugMessage("[ROTATION MODE] Active - stopping all movement");
            stopAllMovement(); // Stop all movement while rotating


            if (mobToLookAt != null) {
                ChatUtil.debugMessage("[ROTATION MODE] Found mob: " + mobToLookAt.getName() + " at distance: " +
                        String.format("%.2f", player.getDistanceToEntity(mobToLookAt)));

                Vec3 mobPos = new Vec3(
                        mobToLookAt.posX,
                        mobToLookAt.posY + (mobToLookAt.height / 2.0),
                        mobToLookAt.posZ
                );

                // Rotate towards the mob
                RotatePlayerTo.lookAtPosition(mobPos, NORMAL_ROTATION_SPEED);

                // Check if we're looking at the mob
                double yawToMob = getYawToPosition(player, mobPos);
                double yawDiff = Math.abs(MathHelper.wrapAngleTo180_float(player.rotationYaw - (float) yawToMob));

                ChatUtil.debugMessage("[ROTATION MODE] Yaw difference: " + String.format("%.2f", yawDiff) +
                        " degrees (target: < 5)");

                if (yawDiff < 3) { // Within 5 degrees
                    ChatUtil.debugMessage("[ROTATION MODE] ✓ Finished looking at mob, proceeding to next goal");

                    isRotatingToMobAfterGoal = false;

                    if (mobToLookAt != null) {
                        PlayerInputUtil.leftClick();
                    }

                    mobToLookAt = null;
                    // Move to next goal
                    proceedToNextGoal(player);
                }
            } else {
                ChatUtil.debugMessage("[ROTATION MODE] ✗ No mob found, proceeding to next goal");
                isRotatingToMobAfterGoal = false;
                proceedToNextGoal(player);
            }

            return; // Don't execute normal movement logic while rotating
        }


        if (currentNodeIndex >= path.size()) {

            ChatUtil.debugMessage("=== GOAL REACHED ===");
            ChatUtil.debugMessage("Current goal index: " + currentGoalIndex);
            ChatUtil.debugMessage("useEntityFinding: " + useEntityFinding);
            ChatUtil.debugMessage("mobToLookAt is null: " + (mobToLookAt == null));

            if (mobToLookAt == null && mobsToSearchFor != null && !mobsToSearchFor.isEmpty()) {
                List<EntityLivingBase> foundMobs = EntityUtil.getEntities(mobsToSearchFor);
                if (!foundMobs.isEmpty()) {
                    mobToLookAt = foundMobs.get(0);
                    ChatUtil.debugMessage("[MobFinder] Found mob: " + mobToLookAt.getName());
                } else {
                    ChatUtil.debugMessage("[MobFinder] No mobs found matching: " + mobsToSearchFor);
                }
            }

            // If using entity finding, try to find and move to the entity
            if (useEntityFinding && totalGoalsWithNames != null && goalsList != null) {
                BlockPos currentGoalPos = goalsList.get(currentGoalIndex);
                String entityName = totalGoalsWithNames.get(currentGoalPos);

                if (entityName != null && !entityName.isEmpty()) {
                    ChatUtil.formatedChatMessage("Looking for entity: " + entityName);

                    // Use EntityUtil.getEntities() to find entities by name
                    Set<String> searchNames = new HashSet<>();
                    searchNames.add(entityName.toLowerCase());
                    List<EntityLivingBase> foundEntities = EntityUtil.getEntities(searchNames);

                    if (!foundEntities.isEmpty()) {
                        Entity foundEntity = foundEntities.get(0); // Get the closest/best match
                        ChatUtil.formatedChatMessage("Found entity: " + entityName + ", moving to it...");
                        boolean success = movePlayerToEntity(foundEntity);
                        if (success) {
                            return; // Wait for entity movement to complete
                        } else {
                            ChatUtil.formatedChatMessage("Could not find path to entity: " + entityName);
                        }
                    } else {
                        ChatUtil.formatedChatMessage("Entity not found: " + entityName);
                    }
                }
            }

            // NEW: Check if we need to rotate to a mob after reaching the goal
            if (mobToLookAt != null) {
                stopAllMovement();
                isRotatingToMobAfterGoal = true;
                return; // Start rotation phase in next tick
            } else {
                ChatUtil.debugMessage("✗ No mobs to look at - proceeding directly to next goal");
            }

            // No mob to rotate to, proceed to next goal immediately
            proceedToNextGoal(player);
            return;
        }


        // Anti-stuck mechanism
        BlockPos currentPlayerPos = new BlockPos(player.posX, player.posY, player.posZ);
        if (lastPosition != null) {
            double movementDistance = Math.sqrt(currentPlayerPos.distanceSq(lastPosition));
            if (movementDistance < MIN_MOVEMENT_THRESHOLD) {
                stuckCounter++;
                if (stuckCounter >= MAX_STUCK_TICKS) {
                    ChatUtil.debugMessage("Player seems stuck, skipping to next node...");
                    currentNodeIndex++;
                    stuckCounter = 0;
                    if (currentNodeIndex >= path.size()) {
                        ChatUtil.debugMessage("Destination reached after unstuck!");
                        stopMoving();
                        return;
                    }
                }
            } else {
                stuckCounter = 0; // Reset counter if player moved
            }
        }
        lastPosition = currentPlayerPos;

        BlockPos currentTarget = path.get(currentNodeIndex);
        BlockPos lookTarget = findLookTarget(player, path, currentNodeIndex);

        // Create look position at player eye height in the direction of the target
        Vec3 lookPosition = calculateLookPosition(player, lookTarget);

        // Decide if we should apply the fast initial rotation
        float rotationSpeed;
        if (!hasAimedAtFirstNode && currentNodeIndex < 3) {
            // Only use fast speed when aiming at the very first node
            rotationSpeed = INITIAL_ROTATION_SPEED;
            // Check if we are already looking close enough -> then mark as done
            double yawDiff = Math.abs(player.rotationYaw - getYawToPosition(player, lookPosition));
            if (yawDiff < 5) {
                hasAimedAtFirstNode = true;
            }
        } else {
            rotationSpeed = NORMAL_ROTATION_SPEED;
        }

        RotatePlayerTo.lookAtPosition(lookPosition, rotationSpeed);

        moveTowardsTarget(player, currentTarget);

        // Jump logic (only if not going over slabs)
        if (shouldJump(player, currentTarget)) {
            performJump();
        }

        double distanceToTarget = player.getDistance(
                currentTarget.getX() + 0.5,
                currentTarget.getY(),
                currentTarget.getZ() + 0.5
        );

        // Check if block is a slab or stair
        Block targetBlock = Minecraft.getMinecraft().theWorld.getBlockState(currentTarget).getBlock();
        boolean isSlabOrStair = isSlabOrStair(targetBlock);

        // Player height difference
        double heightDiff = player.posY - currentTarget.getY();

        // Node reached if within distance OR if slab/stair and player is up to 2 blocks higher
        if (distanceToTarget <= REACH_DISTANCE || (isSlabOrStair && heightDiff <= 2.0 && heightDiff >= 0.0)) {
            stuckCounter = 0; // Reset stuck counter when reaching a node
            currentNodeIndex++;
            ChatUtil.debugMessage("Reached node " + currentNodeIndex + "/" + path.size());
        }
    }

    /**
     * NEW: Helper method to proceed to the next goal
     */
    private void proceedToNextGoal(EntityPlayerSP player) {
        currentGoalIndex++;

        // Check if we have more goals
        int totalGoalCount = useEntityFinding ? goalsList.size() : totalGoals.length;
        if (currentGoalIndex >= totalGoalCount) {
            ChatUtil.formatedChatMessage("All goals completed!");
            stopMoving();
            return;
        }

        // Calculate path to next goal
        BlockPos playerPosNow = new BlockPos(player.posX, player.posY, player.posZ);
        BlockPos nextGoal = useEntityFinding ? goalsList.get(currentGoalIndex) : totalGoals[currentGoalIndex];
        path = PathFinding.findPath(playerPosNow, nextGoal, MAX_ITERATIONS);

        if (path == null || path.isEmpty()) {
            ChatUtil.formatedChatMessage("No path found for next goal!");
            stopMoving();
            return;
        }
        currentNodeIndex = 0;
        hasAimedAtFirstNode = false;
        ChatUtil.debugMessage("Moving to: " + nextGoal + " within " + path.size() + " Nodes");
    }

    /**
     * Calculates a look position at player eye height in the direction of the target.
     * If target is more than 3 blocks away, aims in the direction of the target.
     * If target is closer, aims at the target position itself.
     * Always maintains player eye height.
     */
    private Vec3 calculateLookPosition(EntityPlayerSP player, BlockPos target) {
        double playerEyeHeight = player.posY + player.getEyeHeight();

        // Calculate horizontal distance to target
        double dx = target.getX() + 0.5 - player.posX;
        double dz = target.getZ() + 0.5 - player.posZ;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        if (horizontalDistance > 3.0) {
            // More than 3 blocks away: aim in the direction at eye height
            // Normalize the direction vector
            double dirX = dx / horizontalDistance;
            double dirZ = dz / horizontalDistance;

            // Create a point far away in that direction at eye height
            // Using a distance of 100 blocks to ensure proper direction
            double lookX = player.posX + dirX * 100;
            double lookZ = player.posZ + dirZ * 100;

            return new Vec3(lookX, playerEyeHeight, lookZ);
        } else {
            // Within 3 blocks: look at the target position at eye height
            return new Vec3(target.getX() + 0.5, playerEyeHeight, target.getZ() + 0.5);
        }
    }

    /**
     * Calculates the yaw angle to look at a specific position
     */
    private float getYawToPosition(EntityPlayerSP player, Vec3 pos) {
        double dx = pos.xCoord - player.posX;
        double dz = pos.zCoord - player.posZ;
        return (float) (Math.toDegrees(Math.atan2(-dx, dz)));
    }

    private static BlockPos findBestPositionAroundEntity(BlockPos entityPos, BlockPos playerPos) {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return null;

        ArrayList<BlockPos> validPositions = new ArrayList<>();

        // Check all positions in a 4-block radius around the entity
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = -2; y <= 2; y++) { // Check a few blocks above and below
                    BlockPos checkPos = entityPos.add(x, y, z);

                    // Skip if too close (less than 1 block) or too far (more than 4 blocks)
                    double distance = Math.sqrt(checkPos.distanceSq(entityPos));
                    if (distance < 1.0 || distance > 4.0) continue;

                    // Check if this position is reachable and safe
                    if (isPositionValidForPlayer(world, checkPos)) {
                        validPositions.add(checkPos);
                    }
                }
            }
        }

        if (validPositions.isEmpty()) {
            return null;
        }

        // Sort positions by distance to player (closest first)
        validPositions.sort((pos1, pos2) -> {
            double dist1 = playerPos.distanceSq(pos1);
            double dist2 = playerPos.distanceSq(pos2);
            return Double.compare(dist1, dist2);
        });

        // Try to find a path to each position, starting with the closest
        for (BlockPos pos : validPositions) {
            List<BlockPos> testPath = PathFinding.findPath(playerPos, pos, MAX_ITERATIONS);
            if (testPath != null && !testPath.isEmpty()) {
                ChatUtil.formatedChatMessage("Found valid position at: " + pos + " (distance: " +
                        String.format("%.1f", Math.sqrt(pos.distanceSq(entityPos))) + " blocks from entity)");
                return pos;
            }
        }

        return null;
    }

    private static boolean isPositionValidForPlayer(World world, BlockPos pos) {
        // Check the block the player would stand on
        Block groundBlock = world.getBlockState(pos.down()).getBlock();
        Block feetBlock = world.getBlockState(pos).getBlock();
        Block headBlock = world.getBlockState(pos.up()).getBlock();

        // Player needs solid ground to stand on (or slab/stairs)
        boolean hasSolidGround = groundBlock.isFullBlock() || isSlabOrStair(groundBlock);

        // Player needs air or passable blocks at feet and head level
        boolean feetClear = feetBlock.isAir(world, pos) || !feetBlock.isFullBlock();
        boolean headClear = headBlock.isAir(world, pos.up()) || !headBlock.isFullBlock();

        // Additional safety checks
        boolean notInVoid = pos.getY() > 0;
        boolean notTooHigh = pos.getY() < 256;

        return hasSolidGround && feetClear && headClear && notInVoid && notTooHigh;
    }

    private boolean shouldJump(EntityPlayerSP player, BlockPos currentTarget) {
        int floorY = player.getPosition().getY();
        double heightDiff = currentTarget.getY() - floorY;

        // 1) Wenn die Höhe nicht groß genug ist, niemals springen
        if (heightDiff <= 0.5) {
            return false;
        }

        // 2) Wenn vor uns Slabs/Stairs liegen (du kannst da hochlaufen), dann auch nicht springen
        boolean slopeAhead = detectSlopeAhead(player, currentTarget, floorY);
        if (slopeAhead) {
            return false;
        }
        return true;
    }

    /**
     * Prüft, ob auf dem nächsten Schritt (in Richtung currentTarget) oder
     * in der Ziel-Spalte eine Slab/treppe (Stair) liegt – unten oder oben.
     */
    private boolean detectSlopeAhead(EntityPlayerSP player, BlockPos currentTarget, int baseY) {
        BlockPos start = new BlockPos(
                MathHelper.floor_double(player.posX),
                baseY,
                MathHelper.floor_double(player.posZ)
        );
        BlockPos end = new BlockPos(currentTarget.getX(), baseY, currentTarget.getZ());

        // Check all blocks in the box between start and end (ground + one block above)
        Iterable<BlockPos> blocksBetween = BlockPos.getAllInBox(
                new BlockPos(Math.min(start.getX(), end.getX()), baseY, Math.min(start.getZ(), end.getZ())),
                new BlockPos(Math.max(start.getX(), end.getX()), baseY + 1, Math.max(start.getZ(), end.getZ()))
        );

        for (BlockPos pos : blocksBetween) {
            Block b = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
            if (isSlabOrStair(b)) {
                return true; // slab or stair found -> treat as slope
            }
        }
        return false;
    }

    private static boolean isSlabOrStair(Block b) {
        return (b instanceof BlockSlab) || (b instanceof BlockStairs);
    }

    private void performJump() {
        new Thread(() -> {
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindJump.getKeyCode(), true);
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindJump.getKeyCode(), false);
        }).start();
    }

    private float getYawToBlock(EntityPlayerSP player, BlockPos pos) {
        double dx = pos.getX() + 0.5 - player.posX;
        double dz = pos.getZ() + 0.5 - player.posZ;
        return (float) (Math.toDegrees(Math.atan2(-dx, dz)));
    }

    // Findet den nächsten Node der mehr als 5 Blöcke entfernt ist für das Schauen
    private BlockPos findLookTarget(EntityPlayerSP player, List<BlockPos> path, int currentIndex) {
        BlockPos playerPos = new BlockPos(player.posX, player.posY, player.posZ);

        // Durchsuche den Path ab dem aktuellen Index
        for (int i = currentIndex; i < path.size(); i++) {
            BlockPos node = path.get(i);
            double distance = playerPos.distanceSq(node);

            // Wenn der Node mehr als 5 Blöcke entfernt ist, verwende ihn als Look-Target
            if (distance > 9) { // 3² = 9 für distanceSq
                return node;
            }
        }

        // Falls kein Node mehr als 5 Blöcke entfernt ist, verwende den aktuellen Ziel-Node
        return path.get(currentIndex);
    }

    // Enhanced movement method with sprint support and anti-oscillation logic
    private void moveTowardsTarget(EntityPlayerSP player, BlockPos target) {
        double deltaX = target.getX() + 0.5 - player.posX;
        double deltaZ = target.getZ() + 0.5 - player.posZ;

        // Calculate distance to target
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        if (distance < 0.2) return; // Too close, no movement needed

        // Reset all movement keys
        stopAllMovement();

        // Calculate angle to target (in Minecraft coordinates)
        double targetYaw = Math.toDegrees(Math.atan2(-deltaX, deltaZ));
        double playerYaw = player.rotationYaw;

        // Normalize both angles to 0-360
        while (targetYaw < 0) targetYaw += 360;
        while (playerYaw < 0) playerYaw += 360;
        while (targetYaw >= 360) targetYaw -= 360;
        while (playerYaw >= 360) playerYaw -= 360;

        // Calculate angle difference (-180 to +180)
        double angleDiff = targetYaw - playerYaw;
        if (angleDiff > 180) angleDiff -= 360;
        if (angleDiff < -180) angleDiff += 360;

        double absAngleDiff = Math.abs(angleDiff);
        boolean shouldSprint = true;
        int currentMovementDirection = 0;

        // Use hysteresis to prevent oscillation between movement directions
        // Different thresholds based on current movement direction
        double forwardThreshold = (lastMovementDirection == 1) ? 55.0 : 35.0; // Wider range if already moving forward
        double backwardThreshold = (lastMovementDirection == 2) ? 125.0 : 145.0; // Wider range if already moving backward

        // Determine movement direction with hysteresis
        if (absAngleDiff <= forwardThreshold) {
            // Target is in front of the player (or close enough to front)
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), true);
            currentMovementDirection = 1;
        } else if (absAngleDiff >= backwardThreshold) {
            // Target is behind the player (or close enough to back)
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), true);
            currentMovementDirection = 2;
            shouldSprint = false; // Don't sprint backwards
        } else {
            // Target is to the side - use strafe movement
            if (angleDiff > 0) {
                // Target is to the right
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), true);
                currentMovementDirection = 4;

                // Add slight forward bias for diagonal movement if close to forward threshold
                if (absAngleDiff < 65) {
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), true);
                }
            } else {
                // Target is to the left
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), true);
                currentMovementDirection = 3;

                // Add slight forward bias for diagonal movement if close to forward threshold
                if (absAngleDiff < 65) {
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), true);
                }
            }
        }

        // Track movement direction changes to prevent rapid oscillation
        if (currentMovementDirection != lastMovementDirection) {
            movementDirectionCounter = 0;
            lastMovementDirection = currentMovementDirection;
        } else {
            movementDirectionCounter++;
        }

        // If we're rapidly changing directions, stick with the current direction for a few ticks
        if (movementDirectionCounter < 5 && currentMovementDirection != lastMovementDirection) {
            // Keep using the previous movement direction for stability
            currentMovementDirection = lastMovementDirection;

            // Re-apply the previous movement
            stopAllMovement();
            switch (lastMovementDirection) {
                case 1: // Forward
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), true);
                    break;
                case 2: // Back
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), true);
                    shouldSprint = false;
                    break;
                case 3: // Left
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), true);
                    break;
                case 4: // Right
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), true);
                    break;
            }
        }

        // Enable sprinting if conditions are met
        if (shouldSprint && canSprint(player, distance, absAngleDiff)) {
            if (!player.isSprinting()) {
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSprint.getKeyCode(), true);
                // Small delay to ensure sprint activates
                new Thread(() -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignored) {
                    }
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSprint.getKeyCode(), false);
                }).start();
            }
        }
    }

    // Check if player should sprint
    private boolean canSprint(EntityPlayerSP player, double distanceToTarget, double angleDiff) {
        // Don't sprint if too close to target
        if (distanceToTarget < 1.5) return false;

        // Don't sprint if not looking roughly in the right direction
        if (angleDiff > 60) return false;

        // Don't sprint if player is hungry (can't sprint)
        if (player.getFoodStats().getFoodLevel() <= 6) return false;

        // Don't sprint if player is in water or lava
        if (player.isInWater() || player.isInLava()) return false;

        // Don't sprint if player is sneaking
        if (player.isSneaking()) return false;

        return true;
    }

    // Alternative movement method with Player-Rotation consideration (kept for reference)
    private void moveTowardsTargetWithRotation(EntityPlayerSP player, BlockPos target) {
        double deltaX = target.getX() + 0.5 - player.posX;
        double deltaZ = target.getZ() + 0.5 - player.posZ;

        // Calculate angle to target
        double targetAngle = Math.toDegrees(Math.atan2(-deltaX, deltaZ));
        double playerYaw = player.rotationYaw;

        // Normalize angles
        while (targetAngle < 0) targetAngle += 360;
        while (playerYaw < 0) playerYaw += 360;

        // Calculate angle difference
        double angleDiff = targetAngle - playerYaw;
        while (angleDiff > 180) angleDiff -= 360;
        while (angleDiff < -180) angleDiff += 360;

        // Reset all movement keys
        stopAllMovement();

        // Movement based on angle to target
        if (Math.abs(angleDiff) < 45) {
            // Target is in front of player
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), true);
        } else if (Math.abs(angleDiff) > 135) {
            // Target is behind player
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), true);
        } else if (angleDiff > 0) {
            // Target is to the right of player
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), true);
        } else {
            // Target is to the left of player
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), true);
        }
    }

    private static void stopAllMovement() {
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
    }

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        if (path == null || path.isEmpty()) return;

        double renderPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
        double renderPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
        double renderPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

        for (int i = 0; i < path.size(); i++) {
            BlockPos blockPos = path.get(i);
            AxisAlignedBB box = new AxisAlignedBB(
                    blockPos.getX() - renderPosX,
                    blockPos.getY() - renderPosY,
                    blockPos.getZ() - renderPosZ,
                    blockPos.getX() + 1 - renderPosX,
                    blockPos.getY() + 1 - renderPosY,
                    blockPos.getZ() + 1 - renderPosZ
            );

            // Highlight current target node in a different color
            if (i == currentNodeIndex && isMoving) {
                RenderUtil.drawFilledBox(box, new OneColor(255, 0, 0, 150)); // Red for current target
            } else if (i < currentNodeIndex) {
                RenderUtil.drawFilledBox(box, new OneColor(0, 255, 0, 100)); // Green for completed nodes
            } else {
                RenderUtil.drawFilledBox(box, new OneColor(0, 0, 255, 100)); // Blue for future nodes
            }
        }
    }

    // Utility method to check if currently moving
    public static boolean isMoving() {
        return isMoving;
    }

    // Method to get current progress
    public static String getProgress() {
        if (!isMoving || path == null) return "Not moving";
        return "Node " + (currentNodeIndex + 1) + "/" + path.size();
    }
}