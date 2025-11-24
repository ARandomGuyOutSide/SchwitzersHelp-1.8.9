package com.schwitzer.schwitzersHelp.features;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomBlockEsp {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    // Static list to store custom blocks for ESP
    private static final Set<Block> customEspBlocks = new HashSet<>();

    // File path for storing custom ESP blocks
    private static final Path ESP_BLOCKS_FILE = Paths.get("config", "schwitzershelp", "custom_esp_blocks.txt");

    // Scan range for custom blocks
    private static final int SCAN_RANGE = 30;

    // Cache for found blocks to improve performance
    private final List<BlockPos> foundBlocks = new ArrayList<>();
    private long lastScanTime = 0;
    private static final long SCAN_INTERVAL = 500; // Scan every 500ms

    /**
     * Initialize and load blocks from file
     */
    public static void init() {
        loadBlocksFromFile();
    }

    /**
     * Load custom ESP blocks from file
     */
    private static void loadBlocksFromFile() {
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(ESP_BLOCKS_FILE.getParent());

            // Create file if it doesn't exist
            if (!Files.exists(ESP_BLOCKS_FILE)) {
                Files.createFile(ESP_BLOCKS_FILE);
                return;
            }

            // Read blocks from file
            List<String> lines = Files.readAllLines(ESP_BLOCKS_FILE);
            customEspBlocks.clear();

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                try {
                    // Parse block resource location (e.g., "minecraft:diamond_ore")
                    Block block = Block.getBlockFromName(line);
                    if (block != null) {
                        customEspBlocks.add(block);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load block: " + line);
                    e.printStackTrace();
                }
            }

            System.out.println("Loaded " + customEspBlocks.size() + " custom ESP blocks from file");

        } catch (IOException e) {
            System.err.println("Failed to load custom ESP blocks from file");
            e.printStackTrace();
        }
    }

    /**
     * Save custom ESP blocks to file
     */
    private static void saveBlocksToFile() {
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(ESP_BLOCKS_FILE.getParent());

            // Write blocks to file
            try (BufferedWriter writer = Files.newBufferedWriter(ESP_BLOCKS_FILE)) {
                writer.write("# Custom ESP Blocks - One block registry name per line\n");
                writer.write("# Format: minecraft:block_name or modid:block_name\n");
                writer.write("# Lines starting with # are comments\n\n");

                for (Block block : customEspBlocks) {
                    ResourceLocation resourceLocation = Block.blockRegistry.getNameForObject(block);
                    if (resourceLocation != null) {
                        writer.write(resourceLocation.toString());
                        writer.newLine();
                    }
                }
            }

            System.out.println("Saved " + customEspBlocks.size() + " custom ESP blocks to file");

        } catch (IOException e) {
            System.err.println("Failed to save custom ESP blocks to file");
            e.printStackTrace();
        }
    }

    /**
     * Add a block to the custom ESP list
     * @param block The block to add
     * @return true if added successfully, false if already exists
     */
    public static boolean addBlock(Block block) {
        if (block == null) return false;
        boolean added = customEspBlocks.add(block);
        if (added) {
            saveBlocksToFile();
        }
        return added;
    }

    /**
     * Remove a block from the custom ESP list
     * @param block The block to remove
     * @return true if removed successfully, false if not found
     */
    public static boolean removeBlock(Block block) {
        if (block == null) return false;
        boolean removed = customEspBlocks.remove(block);
        if (removed) {
            saveBlocksToFile();
        }
        return removed;
    }

    /**
     * Clear all custom ESP blocks
     */
    public static void clearAllBlocks() {
        customEspBlocks.clear();
        saveBlocksToFile();
    }

    /**
     * Get all blocks currently in the ESP list
     * @return Set of blocks
     */
    public static Set<Block> getEspBlocks() {
        return new HashSet<>(customEspBlocks);
    }

    /**
     * Check if a block is in the ESP list
     * @param block The block to check
     * @return true if the block is in the ESP list
     */
    public static boolean containsBlock(Block block) {
        return customEspBlocks.contains(block);
    }

    /**
     * Get the number of blocks in the ESP list
     * @return Number of blocks
     */
    public static int getBlockCount() {
        return customEspBlocks.size();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        // Only render if there are blocks to show and block outline is enabled
        if (customEspBlocks.isEmpty() || !config.isBlockOutline()) {
            return;
        }

        EntityPlayer player = mc.thePlayer;
        World world = mc.theWorld;

        if (player == null || world == null) return;

        // Update block cache periodically
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime >= SCAN_INTERVAL) {
            scanForCustomBlocks(world, player);
            lastScanTime = currentTime;
        }

        // Render all found blocks
        renderCustomBlocks(event, player);
    }

    private void scanForCustomBlocks(World world, EntityPlayer player) {
        foundBlocks.clear();

        int playerX = (int) player.posX;
        int playerY = (int) player.posY;
        int playerZ = (int) player.posZ;

        // Scan in a cube around the player
        for (int x = playerX - SCAN_RANGE; x <= playerX + SCAN_RANGE; x++) {
            for (int y = Math.max(0, playerY - SCAN_RANGE); y <= Math.min(255, playerY + SCAN_RANGE); y++) {
                for (int z = playerZ - SCAN_RANGE; z <= playerZ + SCAN_RANGE; z++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    // Skip if chunk is not loaded
                    if (!world.isBlockLoaded(pos)) continue;

                    Block block = world.getBlockState(pos).getBlock();

                    // Check if this block is in our custom ESP list
                    if (customEspBlocks.contains(block)) {
                        foundBlocks.add(pos);
                    }
                }
            }
        }
    }

    private void renderCustomBlocks(RenderWorldLastEvent event, EntityPlayer player) {
        // Calculate render position offset
        double renderPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
        double renderPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
        double renderPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

        // Use default ESP color (can add to config later if needed)
        OneColor color = new OneColor(255, 0, 255, 200); // Magenta by default

        // Use RenderUtil to render all blocks at once
        RenderUtil.drawCustomBlocks(foundBlocks, renderPosX, renderPosY, renderPosZ, color);
    }
}