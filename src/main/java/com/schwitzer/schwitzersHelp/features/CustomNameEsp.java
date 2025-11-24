package com.schwitzer.schwitzersHelp.features;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import com.schwitzer.schwitzersHelp.util.EntityUtil;
import com.schwitzer.schwitzersHelp.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomNameEsp {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    // Static set to store custom entity names for ESP
    private static final Set<String> customEspNames = new HashSet<>();

    // File path for storing custom ESP names
    private static final Path ESP_NAMES_FILE = Paths.get("config", "schwitzershelp", "custom_esp_names.txt");

    // Cache for found entities to improve performance
    private List<EntityLivingBase> foundEntities;
    private long lastScanTime = 0;
    private static final long SCAN_INTERVAL = 250; // Scan every 250ms for better responsiveness

    /**
     * Initialize and load names from file
     * Call this from your main mod's init method
     */
    public static void init() {
        loadNamesFromFile();
    }

    /**
     * Load custom ESP names from file
     */
    private static void loadNamesFromFile() {
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(ESP_NAMES_FILE.getParent());

            // Create file if it doesn't exist
            if (!Files.exists(ESP_NAMES_FILE)) {
                Files.createFile(ESP_NAMES_FILE);
                return;
            }

            // Read names from file
            List<String> lines = Files.readAllLines(ESP_NAMES_FILE);
            customEspNames.clear();

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                customEspNames.add(line.toLowerCase());
            }

            System.out.println("Loaded " + customEspNames.size() + " custom ESP names from file");

        } catch (IOException e) {
            System.err.println("Failed to load custom ESP names from file");
            e.printStackTrace();
        }
    }

    /**
     * Save custom ESP names to file
     */
    private static void saveNamesToFile() {
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(ESP_NAMES_FILE.getParent());

            // Write names to file
            try (BufferedWriter writer = Files.newBufferedWriter(ESP_NAMES_FILE)) {
                writer.write("# Custom ESP Names - One entity name per line\n");
                writer.write("# Format: Entity name exactly as it appears in-game\n");
                writer.write("# Lines starting with # are comments\n\n");

                for (String name : customEspNames) {
                    writer.write(name);
                    writer.newLine();
                }
            }

            System.out.println("Saved " + customEspNames.size() + " custom ESP names to file");

        } catch (IOException e) {
            System.err.println("Failed to save custom ESP names to file");
            e.printStackTrace();
        }
    }

    /**
     * Add an entity name to the custom ESP list
     * @param name The entity name to add
     * @return true if added successfully, false if already exists
     */
    public static boolean addName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String trimmedName = name.trim().toLowerCase();
        boolean added = customEspNames.add(trimmedName);
        if (added) {
            saveNamesToFile();
            if (SchwitzerHelpConfig.getInstance().isDebugMode()) {
                ChatUtil.formatedChatMessage("CustomNameEsp: Added '" + trimmedName + "' to list. Total names: " + customEspNames.size());
            }
        }
        return added;
    }

    /**
     * Remove an entity name from the custom ESP list
     * @param name The entity name to remove
     * @return true if removed successfully, false if not found
     */
    public static boolean removeName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        boolean removed = customEspNames.remove(name.trim().toLowerCase());
        if (removed) {
            saveNamesToFile();
        }
        return removed;
    }

    /**
     * Clear all custom ESP names
     */
    public static void clearAllNames() {
        customEspNames.clear();
        saveNamesToFile();
    }

    /**
     * Get all names currently in the ESP list
     * @return Set of entity names
     */
    public static Set<String> getEspNames() {
        return new HashSet<>(customEspNames);
    }

    /**
     * Check if a name is in the ESP list
     * @param name The name to check
     * @return true if the name is in the ESP list
     */
    public static boolean containsName(String name) {
        if (name == null) return false;
        return customEspNames.contains(name.trim().toLowerCase());
    }

    /**
     * Get the number of names in the ESP list
     * @return Number of names
     */
    public static int getNameCount() {
        return customEspNames.size();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        // Only render if there are names to search for
        if (customEspNames.isEmpty()) {
            return;
        }

        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Update entity cache periodically
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime >= SCAN_INTERVAL) {
            scanForCustomNamedEntities();
            lastScanTime = currentTime;
        }

        // Render all found entities
        renderCustomNamedEntities(event);
    }

    private void scanForCustomNamedEntities() {
        try {
            // Debug: Show what names we're searching for
            if (config.isDebugMode()) {
                ChatUtil.formatedChatMessage("CustomNameEsp: Searching for names: " + customEspNames.toString());
            }

            // Use EntityUtil to find entities with matching names
            foundEntities = EntityUtil.getEntities(customEspNames);

            // Debug: Show how many entities we found
            if (config.isDebugMode() && foundEntities != null) {
                ChatUtil.formatedChatMessage("CustomNameEsp: Found " + foundEntities.size() + " entities");
                for (EntityLivingBase entity : foundEntities) {
                    if (entity.hasCustomName()) {
                        ChatUtil.formatedChatMessage("  - Entity: " + entity.getCustomNameTag());
                    }
                }
            }
        } catch (Exception e) {
            // Handle any potential exceptions from EntityUtil
            foundEntities = null;
            if (config.isDebugMode()) {
                ChatUtil.formatedChatMessage("CustomNameEsp: Error - " + e.getMessage());
            }
        }
    }

    private void renderCustomNamedEntities(RenderWorldLastEvent event) {
        if (foundEntities == null || foundEntities.isEmpty()) return;

        // Calculate render position offset
        double camX = mc.getRenderManager().viewerPosX;
        double camY = mc.getRenderManager().viewerPosY;
        double camZ = mc.getRenderManager().viewerPosZ;

        for(EntityLivingBase entity : foundEntities)
        {
            double relativeX = entity.posX - camX;
            double relativeY = entity.posY - camY;
            double relativeZ = entity.posZ - camZ;

            RenderUtil.drawEntityBox(relativeX, relativeY, relativeZ, entity.width, entity.height, new OneColor(5, 5, 5, 255), 0);
        }
    }

    /**
     * Get information about currently tracked entities (for debugging)
     * @return Number of entities currently being tracked
     */
    public int getTrackedEntityCount() {
        return foundEntities != null ? foundEntities.size() : 0;
    }

    /**
     * Check if the ESP system is currently active
     * @return true if there are names to search for and entities are being tracked
     */
    public static boolean isActive() {
        return !customEspNames.isEmpty();
    }
}