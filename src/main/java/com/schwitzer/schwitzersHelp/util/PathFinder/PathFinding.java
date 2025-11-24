package com.schwitzer.schwitzersHelp.util.PathFinder;

import com.schwitzer.schwitzersHelp.util.ChatUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.*;

public class PathFinding {
    private static Minecraft mc = Minecraft.getMinecraft();
    private static List<BlockPos> path = null;

    // Configuration for wall penalty and freespace evaluation
    private static final double FREESPACE_WEIGHT = 0.5; // How important is freespace (0-1)
    private static final int FREESPACE_RADIUS = 1; // Radius for freespace check
    private static final double WALL_PENALTY_MULTIPLIER = 1.5; // How much to penalize cramped spaces

    private static class Node {
        BlockPos pos;
        Node parent;
        double gCost; // cost from start
        double hCost; // heuristic cost to target
        double fCost; // g + h

        public Node(BlockPos pos, Node parent, double gCost, double hCost) {
            this.pos = pos;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }
    }

    public static List<BlockPos> findPath(BlockPos start, BlockPos goal, int maxIterations) {

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        Map<BlockPos, Double> gScores = new HashMap<>();
        Set<BlockPos> closedSet = new HashSet<>();
        Node startNode = new Node(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);
        gScores.put(start, 0.0);
        int iterations = 0;

        while (!openSet.isEmpty() && iterations < maxIterations) {
            iterations++;
            Node current = openSet.poll();

            if (current.pos.equals(goal)) {
                path = removeBadNodes(reconstructPath(current));
                return path;
            }

            closedSet.add(current.pos);

            for (BlockPos neighbor : getNeighbors(current.pos)) {
                if (closedSet.contains(neighbor)) continue;

                double moveCost = calculateMoveCostWithWallPenalty(current.pos, neighbor);
                double tentativeG = gScores.get(current.pos) + moveCost;

                if (tentativeG < gScores.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    double extendedHeuristic = heuristicWithFreespace(neighbor, goal);
                    Node neighborNode = new Node(neighbor, current, tentativeG, extendedHeuristic);
                    gScores.put(neighbor, tentativeG);
                    openSet.add(neighborNode);
                }
            }
        }

        return Collections.emptyList(); // no path found
    }


    private static boolean checkIfGoalIsLoaded(BlockPos goal)
    {
        return heuristic(mc.thePlayer.getPosition(), goal) < 150.0;
    }

    // Enhanced movement cost calculation that includes wall penalty
    private static double calculateMoveCostWithWallPenalty(BlockPos from, BlockPos to) {
        Block fromBlock = mc.theWorld.getBlockState(from).getBlock();

        boolean isDiagonal = Math.abs(to.getX() - from.getX()) == 1 &&
                Math.abs(to.getZ() - from.getZ()) == 1;
        int dy = to.getY() - from.getY();

        double baseCost;

        if (dy == 1) { // Moving up
            if (isBlockSlabOrStair(fromBlock)) {
                baseCost = isDiagonal ? 1.414 : 1.0; // sqrt(2) for diagonal, 1 for straight
            } else {
                // Jumping up is more expensive, especially diagonally
                baseCost = isDiagonal ? 5.5 : 3.0; // Make diagonal jumps significantly more expensive
            }
        } else if (dy == 0) { // Same level
            baseCost = isDiagonal ? 1.414 : 1.0; // Standard diagonal cost (sqrt(2))
        } else if (dy == -1) { // Moving down
            baseCost = isDiagonal ? 2.5 : 1.8; // Falling down diagonally is a bit more expensive
        } else {
            // Larger height differences
            baseCost = Math.abs(dy) * (isDiagonal ? 3.5 : 2.5);
        }

        // Calculate wall penalty for the destination position
        double wallPenalty = calculateWallPenalty(to);

        // Apply wall penalty to the base cost
        return baseCost * (1.0 + wallPenalty);
    }

    // Calculate penalty based on how cramped/surrounded a position is
    private static double calculateWallPenalty(BlockPos pos) {
        int blockedSpaces = 0;
        int totalChecked = 0;

        // Check in a radius around the position for blocked spaces
        for (int x = -FREESPACE_RADIUS; x <= FREESPACE_RADIUS; x++) {
            for (int z = -FREESPACE_RADIUS; z <= FREESPACE_RADIUS; z++) {
                if (x == 0 && z == 0) continue; // Skip center

                BlockPos checkPos = pos.add(x, 0, z);
                totalChecked++;

                if (!canStandHere(checkPos)) {
                    blockedSpaces++;
                }
            }
        }

        // Calculate penalty ratio (0.0 = all free, 1.0 = all blocked)
        double blockedRatio = (double) blockedSpaces / totalChecked;

        // Return penalty multiplier (higher ratio = higher penalty)
        return blockedRatio * WALL_PENALTY_MULTIPLIER;
    }

    // Extended heuristic that includes freespace evaluation
    private static double heuristicWithFreespace(BlockPos pos, BlockPos goal) {
        double baseHeuristic = heuristic(pos, goal);
        double freespaceBonus = calculateFreespaceBonus(pos);

        // Combine normal heuristic with freespace bonus
        return baseHeuristic - (freespaceBonus * FREESPACE_WEIGHT);
    }

    // Calculate bonus based on available freespace
    private static double calculateFreespaceBonus(BlockPos pos) {
        int freeBlocks = 0;
        int totalChecked = 0;

        // Check in a radius around the position
        for (int x = -FREESPACE_RADIUS; x <= FREESPACE_RADIUS; x++) {
            for (int z = -FREESPACE_RADIUS; z <= FREESPACE_RADIUS; z++) {
                if (x == 0 && z == 0) continue; // Skip center

                BlockPos checkPos = pos.add(x, 0, z);
                totalChecked++;

                if (canStandHere(checkPos)) {
                    freeBlocks++;
                }
            }
        }

        // Return number of free blocks as bonus
        return freeBlocks;
    }

    private static List<BlockPos> removeBadNodes(List<BlockPos> path) {
        if (path == null || path.size() < 3) return path;

        List<BlockPos> betterPath = new ArrayList<>();
        betterPath.add(path.get(0)); // start node always stays

        for (int i = 1; i < path.size() - 1; i++) {
            BlockPos prev = path.get(i - 1);
            BlockPos current = path.get(i);
            BlockPos next = path.get(i + 1);

            // Vector from prev->current and current->next
            int dx1 = current.getX() - prev.getX();
            int dz1 = current.getZ() - prev.getZ();
            int dy1 = current.getY() - prev.getY();

            int dx2 = next.getX() - current.getX();
            int dz2 = next.getZ() - current.getZ();
            int dy2 = next.getY() - current.getY();

            // If movement direction is the same -> current is unnecessary
            if (dx1 == dx2 && dz1 == dz2 && dy1 == dy2) {
                continue; // skip current
            }

            betterPath.add(current);
        }

        // Goal node always stays
        betterPath.add(path.get(path.size() - 1));
        return betterPath;
    }

    private static double heuristic(BlockPos a, BlockPos b) {
        double dx = Math.abs(a.getX() - b.getX());
        double dy = Math.abs(a.getY() - b.getY());
        double dz = Math.abs(a.getZ() - b.getZ());
        return dx + dy + dz; // no sqrt, just block steps
    }

    private static List<BlockPos> reconstructPath(Node node) {
        List<BlockPos> path = new ArrayList<>();
        double totalCost = 0;
        while (node != null) {
            path.add(0, node.pos);
            node = node.parent;
            totalCost += node != null ? node.gCost : 0;
        }

        ChatUtil.debugMessage("Total cost: " + totalCost);
        return path;
    }

    private static boolean canStandHere(BlockPos pos) {
        BlockPos below = pos.down();
        Block blockBelow = mc.theWorld.getBlockState(below).getBlock();
        Block blockAt = mc.theWorld.getBlockState(pos).getBlock();
        Block blockAbove = mc.theWorld.getBlockState(pos.up()).getBlock();

        boolean belowSolid = blockBelow.getMaterial().isSolid();

        // The block where the player would stand -> must be air or plant
        boolean spaceFree = blockAt.getMaterial() == Material.air ||
                blockAt.getMaterial() == Material.plants ||
                blockAt == Blocks.tallgrass || isBlockSlabOrStair(blockAt);

        // Head space -> also free
        boolean headFree = blockAbove.getMaterial() == Material.air ||
                blockAbove.getMaterial() == Material.plants ||
                blockAbove == Blocks.tallgrass;

        return belowSolid && spaceFree && headFree;
    }

    public static boolean isBlockSlabOrStair(Block block) {
        return block == Blocks.stone_slab || block == Blocks.stone_slab2 || block == Blocks.wooden_slab ||
                block == Blocks.oak_stairs || block == Blocks.stone_brick_stairs;
    }

    private static List<BlockPos> getNeighbors(BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();

        int[][] directions = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {-1, 1}, {1, -1}, {-1, -1}
        };

        for (int[] dir : directions) {
            BlockPos check = pos.add(dir[0], 0, dir[1]);

            // Diagonal movement only if both orthogonal directions are free
            if (dir[0] != 0 && dir[1] != 0) {
                BlockPos side1 = pos.add(dir[0], 0, 0);
                BlockPos side2 = pos.add(0, 0, dir[1]);
                if (!canStandHere(side1) || !canStandHere(side2)) continue;
            }

            // Check same level, one up and one down
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos candidate = check.add(0, dy, 0);

                Block blockBelow = mc.theWorld.getBlockState(candidate.down()).getBlock();
                if (isBlockSlabOrStair(blockBelow)) {
                    // If there's a slab below the candidate, then stand on the slab (on Y of the slab)
                    BlockPos slabPos = candidate.down();
                    if (canStandHere(slabPos)) {
                        neighbors.add(slabPos);
                    }
                    continue; // Don't check other heights for this neighbor
                }

                if (canStandHere(candidate)) {
                    if (Math.abs(candidate.getY() - pos.getY()) <= 1) {
                        neighbors.add(candidate);
                    }
                }
            }
        }

        return neighbors;
    }
}