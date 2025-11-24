package com.schwitzer.schwitzersHelp.mining;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.RenderUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

public class MiningStuff {

    private SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    private boolean scanForTitanium;
    private boolean isCoalESP;
    private OneColor coalESPColor;
    private boolean coalVeinLine;
    private OneColor coalESPLineColor;
    private int coalESPRange;

    // Für Coal ESP Optimierung
    private List<BlockPos> coalBlocks = new ArrayList<>();
    private long lastCoalScan = 0;
    private static final long COAL_SCAN_INTERVAL = 100;

    // Für Coal Vein Detection
    private List<CoalVein> coalVeins = new ArrayList<>();
    private CoalVein bestCoalVein = null;

    // Mindestgröße für Coal Veins
    private static final int MIN_VEIN_SIZE = 7;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        World world = Minecraft.getMinecraft().theWorld;

        if (player == null || world == null) return;

        scanForTitanium = config.isScanForTitanium();
        isCoalESP = config.isCoalEsp();
        coalVeinLine = config.isCoalVeinLine();
        coalESPRange = config.getCoalEspRange();
        coalESPColor = config.getCoalEspColor();
        coalESPLineColor = config.getCoalEspLineColor();

        // Kamera-Position für Rendering-Offset
        double renderPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
        double renderPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
        double renderPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

        if (scanForTitanium) {
            int playerX = (int) player.posX;
            int playerY = (int) player.posY;
            int playerZ = (int) player.posZ;

            // Optimiert: Äußere Schleifen für bessere Cache-Performance
            for (int y = playerY - 20; y <= playerY + 20; y++) {
                for (int x = playerX - 25; x <= playerX + 25; x++) {
                    for (int z = playerZ - 25; z <= playerZ + 25; z++) {
                        BlockPos pos = new BlockPos(x, y, z);

                        // Nur geladene Chunks prüfen
                        if (!world.isBlockLoaded(pos)) continue;

                        IBlockState blockState = world.getBlockState(pos);

                        // Polished Diorite Erkennung
                        if (blockState.getBlock() == Blocks.stone &&
                                blockState.getBlock().getMetaFromState(blockState) == 4) {

                            AxisAlignedBB box = new AxisAlignedBB(
                                    pos.getX() - renderPosX,
                                    pos.getY() - renderPosY,
                                    pos.getZ() - renderPosZ,
                                    pos.getX() + 1 - renderPosX,
                                    pos.getY() + 1 - renderPosY,
                                    pos.getZ() + 1 - renderPosZ
                            );

                            RenderUtil.drawFilledBox(box, 1.0f, 0.0f, 0.0f, 0.3f);
                        }
                    }
                }
            }
        }

        if (isCoalESP) {
            long currentTime = System.currentTimeMillis();

            // Nur alle 100ms nach Coal-Blöcken scannen
            if (currentTime - lastCoalScan >= COAL_SCAN_INTERVAL) {
                scanForCoalBlocks(world, player);
                if(coalVeinLine)
                {
                    findCoalVeins(player);
                }
                lastCoalScan = currentTime;
            }

            // Alle gefundenen Coal-Blöcke rendern
            renderCoalBlocks(renderPosX, renderPosY, renderPosZ);

            // Linie zum besten Coal Vein zeichnen
            if (bestCoalVein != null && coalVeinLine) {
                renderLineToCoalVein(player, bestCoalVein, renderPosX, renderPosY, renderPosZ);
            }
        }
    }

    private void scanForCoalBlocks(World world, EntityPlayer player) {
        coalBlocks.clear(); // Alte Blöcke löschen

        int playerX = (int) player.posX;
        int playerY = (int) player.posY;
        int playerZ = (int) player.posZ;

        // Real-time scanning in definiertem Bereich
        for (int y = playerY - coalESPRange; y <= playerY + coalESPRange; y++) {
            for (int x = playerX - coalESPRange; x <= playerX + coalESPRange; x++) {
                for (int z = playerZ - coalESPRange; z <= playerZ + coalESPRange; z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);

                    // Nur geladene Chunks prüfen für Performance
                    if (!world.isBlockLoaded(blockPos)) continue;

                    if (world.getBlockState(blockPos).getBlock() == Blocks.coal_ore) {
                        coalBlocks.add(blockPos);
                    }
                }
            }
        }
    }

    private void findCoalVeins(EntityPlayer player) {
        coalVeins.clear();
        bestCoalVein = null;

        Set<BlockPos> processedBlocks = new HashSet<>();
        Vec3 playerPos = new Vec3(player.posX, player.posY, player.posZ);

        // Für jeden Coal Block eine Vein durch Flood-Fill finden
        for (BlockPos coalBlock : coalBlocks) {
            if (processedBlocks.contains(coalBlock)) continue;

            CoalVein vein = new CoalVein();
            findConnectedCoalBlocks(coalBlock, processedBlocks, vein.blocks);

            // Nur Veins mit mindestens 7 Blöcken berücksichtigen
            if (vein.blocks.size() >= MIN_VEIN_SIZE) {
                // Zentrum der Vein berechnen
                vein.calculateCenter();
                vein.calculateDistance(playerPos);
                coalVeins.add(vein);
            }
        }

        // Beste Vein finden basierend auf der gewünschten Logik
        for (CoalVein vein : coalVeins) {
            if (bestCoalVein == null || isBetterVein(vein, bestCoalVein)) {
                bestCoalVein = vein;
            }
        }
    }

    private void findConnectedCoalBlocks(BlockPos start, Set<BlockPos> processed, List<BlockPos> veinBlocks) {
        Stack<BlockPos> toProcess = new Stack<>();
        toProcess.push(start);

        while (!toProcess.isEmpty()) {
            BlockPos current = toProcess.pop();

            if (processed.contains(current) || !coalBlocks.contains(current)) {
                continue;
            }

            processed.add(current);
            veinBlocks.add(current);

            // Alle 6 angrenzenden Blöcke prüfen
            BlockPos[] neighbors = {
                    current.add(1, 0, 0), current.add(-1, 0, 0),
                    current.add(0, 1, 0), current.add(0, -1, 0),
                    current.add(0, 0, 1), current.add(0, 0, -1)
            };

            for (BlockPos neighbor : neighbors) {
                if (!processed.contains(neighbor) && coalBlocks.contains(neighbor)) {
                    toProcess.push(neighbor);
                }
            }
        }
    }

    private boolean isBetterVein(CoalVein candidate, CoalVein current) {
        // Regel: Für jeden Block weiter entfernt muss die Vein 3 Blöcke mehr haben
        double distanceDiff = candidate.distanceToPlayer - current.distanceToPlayer;
        int sizeDiff = candidate.blocks.size() - current.blocks.size();

        // Wenn die candidate Vein näher ist, ist sie besser
        if (candidate.distanceToPlayer < current.distanceToPlayer) {
            return true;
        }

        // Wenn sie weiter ist, muss sie proportional mehr Blöcke haben (3:1 Verhältnis)
        if (distanceDiff > 0) {
            return sizeDiff >= (distanceDiff * 3);
        }

        // Bei gleicher Entfernung: mehr Blöcke = besser
        return sizeDiff > 0;
    }

    private void renderCoalBlocks(double renderPosX, double renderPosY, double renderPosZ) {

        for (BlockPos blockPos : coalBlocks) {
            AxisAlignedBB box = new AxisAlignedBB(
                    blockPos.getX() - renderPosX,
                    blockPos.getY() - renderPosY,
                    blockPos.getZ() - renderPosZ,
                    blockPos.getX() + 1 - renderPosX,
                    blockPos.getY() + 1 - renderPosY,
                    blockPos.getZ() + 1 - renderPosZ
            );

            RenderUtil.drawFilledBox(box, coalESPColor);
        }
    }

    private void renderLineToCoalVein(EntityPlayer player, CoalVein vein, double renderPosX, double renderPosY, double renderPosZ) {
        // Start: Immer vom Ursprung (0,0,0) da renderPos bereits abgezogen wurde
        Vec3 startPos = new Vec3(0, player.getEyeHeight(), 0);

        // Ende: Zentrum der Vein (relativ zum Render-Ursprung)
        Vec3 endPos = new Vec3(
                vein.centerX - renderPosX,
                vein.centerY - renderPosY,
                vein.centerZ - renderPosZ
        );

        RenderUtil.drawLine(endPos, coalESPLineColor, true);
    }

    // Helper-Klasse für Coal Veins
    private static class CoalVein {
        List<BlockPos> blocks = new ArrayList<>();
        double centerX, centerY, centerZ;
        double distanceToPlayer;

        void calculateCenter() {
            if (blocks.isEmpty()) return;

            double sumX = 0, sumY = 0, sumZ = 0;
            for (BlockPos block : blocks) {
                sumX += block.getX() + 0.5; // +0.5 für Block-Zentrum
                sumY += block.getY() + 0.5;
                sumZ += block.getZ() + 0.5;
            }

            centerX = sumX / blocks.size();
            centerY = sumY / blocks.size();
            centerZ = sumZ / blocks.size();
        }

        void calculateDistance(Vec3 playerPos) {
            double dx = centerX - playerPos.xCoord;
            double dy = centerY - playerPos.yCoord;
            double dz = centerZ - playerPos.zCoord;
            distanceToPlayer = Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }
}