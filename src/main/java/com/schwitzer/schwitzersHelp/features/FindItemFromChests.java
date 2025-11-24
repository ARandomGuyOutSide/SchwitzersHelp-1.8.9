package com.schwitzer.schwitzersHelp.features;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.util.InventoryUtil;
import com.schwitzer.schwitzersHelp.util.RenderUtil;
import com.schwitzer.schwitzersHelp.util.HypixelUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class FindItemFromChests {
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    // File storage constants
    private static final Path CHEST_DATA_FILE = Paths.get("config", "schwitzershelp", "chest_data.json");
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(BlockPos.class, new BlockPosTypeAdapter())
            .create();

    private static List<ChestContents> allScannedChests = new LinkedList<>();
    public static List<BlockPos> blocksToRender = new LinkedList<>();

    // Class fields
    private long lastChestUpdate = 0;
    private static final long UPDATE_COOLDOWN = 500; // 500ms between updates
    private boolean hasStoredHash = false;
    private int storedContainerHash = 0;

    /**
     * Initialize the chest scanner - call this on mod startup
     */
    public static void init() {
        loadChestDataFromFile();

        // Add shutdown hook to save data when game closes
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveChestDataToFile();
        }));
    }

    /**
     * Load chest data from file
     */
    private static void loadChestDataFromFile() {
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(CHEST_DATA_FILE.getParent());

            // Check if file exists
            if (!Files.exists(CHEST_DATA_FILE)) {
                return;
            }

            // Read and parse JSON
            String jsonContent = new String(Files.readAllBytes(CHEST_DATA_FILE));
            Type listType = new TypeToken<List<ChestContents>>(){}.getType();
            List<ChestContents> loadedChests = gson.fromJson(jsonContent, listType);

            if (loadedChests != null) {
                allScannedChests.clear();
                allScannedChests.addAll(loadedChests);
            }

        } catch (Exception e) {
            System.err.println("Failed to load chest data from file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save chest data to file
     */
    private static void saveChestDataToFile() {
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(CHEST_DATA_FILE.getParent());

            // Convert to JSON and save
            String jsonContent = gson.toJson(allScannedChests);
            Files.write(CHEST_DATA_FILE, jsonContent.getBytes());

        } catch (Exception e) {
            System.err.println("Failed to save chest data to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears all stored chest data
     */
    public static void clearAllChests() {
        allScannedChests.clear();
        blocksToRender.clear();
        saveChestDataToFile(); // Save empty list to file
    }

    /**
     * Gets the number of currently stored chests
     * @return number of stored chests
     */
    public static int getStoredChestCount() {
        return allScannedChests.size();
    }

    public static void findItem(String itemName)
    {
        itemName = itemName.toLowerCase().trim();

        // Clear previous results before a new search
        blocksToRender.clear();

        // Only search if on Private Island
        if (!HypixelUtil.isOnPrivateIsland()) {
            return;
        }

        boolean found = false;

        for (ChestContents contents : allScannedChests) {
            boolean matches = false;
            for (String n : contents.itemNames) {
                if (n.contains(itemName)) {
                    matches = true;
                    break;
                }
            }
            if (matches) {
                if (!contents.chestPositions.isEmpty()) {
                    BlockPos posToRender = contents.chestPositions.get(0);
                    if (!blocksToRender.contains(posToRender)) {
                        blocksToRender.add(posToRender);
                    }
                    found = true;
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if(blocksToRender.isEmpty()) return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        double renderPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
        double renderPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
        double renderPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

        for(BlockPos pos : blocksToRender)
        {
            AxisAlignedBB box = new AxisAlignedBB(
                    pos.getX() - renderPosX,
                    pos.getY() - renderPosY,
                    pos.getZ() - renderPosZ,
                    pos.getX() + 1 - renderPosX,
                    pos.getY() + 1 - renderPosY,
                    pos.getZ() + 1 - renderPosZ
            );

            Vec3 endPos = new Vec3(
                    pos.getX() + 0.5 - renderPosX,
                    pos.getY() + 0.5 - renderPosY,
                    pos.getZ() + 0.5 - renderPosZ
            );

            RenderUtil.drawLine(endPos, new OneColor(255, 0, 0, 255), true);

            RenderUtil.drawFilledBox(box, 1.0f, 0.0f, 0.0f, 1f);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        // Clear render list when changing worlds
        blocksToRender.clear();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiChest)) {
            // Reset hash when chest is closed
            hasStoredHash = false;
            storedContainerHash = 0;
            return;
        }

        // Only monitor chests on Private Island
        if (!HypixelUtil.isOnPrivateIsland()) return;

        // Cooldown check - only update every 0.5 seconds
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastChestUpdate < UPDATE_COOLDOWN) return;

        // Check if container contents have changed using hash comparison
        GuiContainer guiContainer = (GuiContainer) mc.currentScreen;
        Container container = guiContainer.inventorySlots;

        int currentHash = calculateContainerHash(container);

        // Only update if hash changed or first scan
        if (!hasStoredHash || storedContainerHash != currentHash) {
            scanAndUpdateChestContents();
            storedContainerHash = currentHash;
            hasStoredHash = true;
            lastChestUpdate = currentTime;
        }
    }

    /**
     * Calculates a fast hash of container contents for change detection
     */
    private int calculateContainerHash(Container container) {
        int hash = 0;
        int playerInventorySize = Minecraft.getMinecraft().thePlayer.inventory.getSizeInventory();
        int containerSize = container.inventorySlots.size() - playerInventorySize;

        for (int i = 0; i < containerSize; i++) {
            net.minecraft.item.ItemStack stack = container.getSlot(i).getStack();
            if (stack != null) {
                hash = hash * 31 + stack.getItem().hashCode();
                hash = hash * 31 + stack.stackSize;
                hash = hash * 31 + stack.getItemDamage();
            } else {
                hash = hash * 31; // Null slot
            }
        }
        return hash;
    }

    /**
     * Scans the currently open chest and updates the stored contents
     * Only saves chests when on Hypixel Skyblock Private Island
     */
    private void scanAndUpdateChestContents() {
        // Only scan and save chests on Private Island
        if (!HypixelUtil.isOnPrivateIsland()) {
            return;
        }

        ChestContents container = new ChestContents();
        container.fillItemNames();

        boolean dataChanged = false;
        if (allScannedChests.contains(container)) {
            allScannedChests.remove(container);
            allScannedChests.add(container);
            dataChanged = true;
        }
        else
        {
            allScannedChests.add(container);
            dataChanged = true;
        }

        // Save to file after any changes
        if (dataChanged) {
            saveChestDataToFile();
        }

        // If the just-opened chest was targeted for rendering, clear the render list
        for (BlockPos chestPos : container.chestPositions) {
            if (blocksToRender.contains(chestPos)) {
                blocksToRender.clear();
                break;
            }
        }
    }

    public static class ChestContents {
        private List<String> itemNames;
        private List<BlockPos> chestPositions = new LinkedList<>();

        // Default constructor for JSON deserialization
        public ChestContents() {}

        // Constructor with parameters
        public ChestContents(List<String> itemNames, List<BlockPos> chestPositions) {
            this.itemNames = itemNames != null ? new ArrayList<>(itemNames) : new ArrayList<>();
            this.chestPositions = chestPositions != null ? new LinkedList<>(chestPositions) : new LinkedList<>();
        }

        // Getters and setters for JSON serialization
        public List<String> getItemNames() {
            return itemNames;
        }

        public void setItemNames(List<String> itemNames) {
            this.itemNames = itemNames;
        }

        public List<BlockPos> getChestPositions() {
            return chestPositions;
        }

        public void setChestPositions(List<BlockPos> chestPositions) {
            this.chestPositions = chestPositions;
        }

        private void fillItemNames() {
            itemNames = InventoryUtil.getItemNamesFromContainer();

            if (itemNames.isEmpty()) return;

            BlockPos primary = InventoryUtil.getChestPos();
            if (primary != null) {
                chestPositions.add(primary);
                // Detect double chest by checking adjacent chest blocks
                World world = Minecraft.getMinecraft().theWorld;
                if (world != null) {
                    Block primaryBlock = world.getBlockState(primary).getBlock();
                    if (primaryBlock instanceof BlockChest) {
                        int[][] offsets = new int[][]{{1,0,0},{-1,0,0},{0,0,1},{0,0,-1}};
                        for (int[] off : offsets) {
                            BlockPos neighbor = primary.add(off[0], off[1], off[2]);
                            Block neighborBlock = world.getBlockState(neighbor).getBlock();
                            if (neighborBlock instanceof BlockChest) {
                                if (!chestPositions.contains(neighbor)) {
                                    chestPositions.add(neighbor);
                                }
                                break; // Only one adjacent chest possible for a double chest
                            }
                        }
                    }
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ChestContents that = (ChestContents) o;
            if (this.chestPositions.size() != that.chestPositions.size()) return false;
            return this.chestPositions.containsAll(that.chestPositions) && that.chestPositions.containsAll(this.chestPositions);
        }

        @Override
        public int hashCode() {
            // Order-independent hash: sum of hashes
            int h = 0;
            for (BlockPos p : chestPositions) {
                h += Objects.hashCode(p);
            }
            return h;
        }

        @Override
        public String toString() {
            return "ChestContents{" +
                    "chestPositions=" + chestPositions +
                    '}';
        }
    }

    /**
     * Custom TypeAdapter for BlockPos serialization/deserialization
     */
    private static class BlockPosTypeAdapter implements JsonSerializer<BlockPos>, JsonDeserializer<BlockPos> {
        @Override
        public JsonElement serialize(BlockPos src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("x", src.getX());
            jsonObject.addProperty("y", src.getY());
            jsonObject.addProperty("z", src.getZ());
            return jsonObject;
        }

        @Override
        public BlockPos deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            int x = jsonObject.get("x").getAsInt();
            int y = jsonObject.get("y").getAsInt();
            int z = jsonObject.get("z").getAsInt();
            return new BlockPos(x, y, z);
        }
    }
}