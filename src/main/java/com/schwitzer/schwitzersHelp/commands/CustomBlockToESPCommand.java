package com.schwitzer.schwitzersHelp.commands;

import com.schwitzer.schwitzersHelp.features.CustomBlockEsp;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class CustomBlockToESPCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "schwitzer:customespblock";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sw:customespblock <add block_name|remove block_name|list|clear> - Add, remove, list, or clear blocks for custom ESP";
    }

    @Override
    public java.util.List<String> getCommandAliases() {
        return java.util.Arrays.asList("sw:customespblock");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            ChatUtil.formatedChatMessage("Usage: /sw:customespblock <add block_name|remove block_name|list|clear>");
            ChatUtil.formatedChatMessage("Examples:");
            ChatUtil.formatedChatMessage("  /sw:customespblock add diamond_ore");
            ChatUtil.formatedChatMessage("  /sw:customespblock remove diamond_ore");
            ChatUtil.formatedChatMessage("  /sw:customespblock list");
            ChatUtil.formatedChatMessage("  /sw:customespblock clear");
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "add":
                if (args.length < 2) {
                    ChatUtil.formatedChatMessage("Usage: /sw:customespblock add <block_name>");
                    return;
                }
                addBlock(args[1]);
                break;
            case "list":
                listBlocks();
                break;
            case "clear":
                clearBlocks();
                break;
            case "remove":
                if (args.length < 2) {
                    ChatUtil.formatedChatMessage("Usage: /sw:customespblock remove <block_name>");
                    return;
                }
                removeBlock(args[1]);
                break;
            default:
                break;
        }
    }

    private void addBlock(String blockName) {
        Block block = getBlockByName(blockName);
        
        if (block == null) {
            ChatUtil.formatedChatMessage(EnumChatFormatting.RED + "Error: Block '" + blockName + "' not found!");
            ChatUtil.formatedChatMessage("Try blocks like: diamond_ore, emerald_ore, coal_ore, iron_ore, gold_ore, lapis_ore, redstone_ore");
            return;
        }

        if (CustomBlockEsp.containsBlock(block)) {
            ChatUtil.formatedChatMessage(EnumChatFormatting.YELLOW + "Block '" + blockName + "' is already in the ESP list!");
            return;
        }

        boolean added = CustomBlockEsp.addBlock(block);
        if (added) {
            ChatUtil.formatedChatMessage(EnumChatFormatting.GREEN + "Successfully added '" + blockName + "' to ESP list!");
            ChatUtil.formatedChatMessage("Total blocks in ESP: " + CustomBlockEsp.getBlockCount());
            ChatUtil.formatedChatMessage("Make sure 'Block Esp outline' is enabled in the mod config!");
        } else {
            ChatUtil.formatedChatMessage(EnumChatFormatting.RED + "Failed to add block to ESP list!");
        }
    }

    private void removeBlock(String blockName) {
        Block block = getBlockByName(blockName);
        
        if (block == null) {
            ChatUtil.formatedChatMessage(EnumChatFormatting.RED + "Error: Block '" + blockName + "' not found!");
            return;
        }

        boolean removed = CustomBlockEsp.removeBlock(block);
        if (removed) {
            ChatUtil.formatedChatMessage(EnumChatFormatting.GREEN + "Successfully removed '" + blockName + "' from ESP list!");
            ChatUtil.formatedChatMessage("Total blocks in ESP: " + CustomBlockEsp.getBlockCount());
        } else {
            ChatUtil.formatedChatMessage(EnumChatFormatting.YELLOW + "Block '" + blockName + "' was not in the ESP list!");
        }
    }

    private void listBlocks() {
        if (CustomBlockEsp.getBlockCount() == 0) {
            ChatUtil.formatedChatMessage(EnumChatFormatting.YELLOW + "No blocks in the ESP list!");
            return;
        }

        ChatUtil.formatedChatMessage(EnumChatFormatting.GREEN + "Blocks in ESP list (" + CustomBlockEsp.getBlockCount() + "):");
        for (Block block : CustomBlockEsp.getEspBlocks()) {
            String blockName = Block.blockRegistry.getNameForObject(block).getResourcePath();
            ChatUtil.formatedChatMessage("  - " + blockName);
        }
    }

    private void clearBlocks() {
        int count = CustomBlockEsp.getBlockCount();
        CustomBlockEsp.clearAllBlocks();
        ChatUtil.formatedChatMessage(EnumChatFormatting.GREEN + "Cleared " + count + " blocks from ESP list!");
    }

    /**
     * Get a block by its name (supports both with and without minecraft: prefix)
     * @param blockName The name of the block
     * @return The Block object, or null if not found
     */
    private Block getBlockByName(String blockName) {
        // Try with minecraft: prefix first
        Block block = Block.getBlockFromName("minecraft:" + blockName);
        if (block != null) {
            return block;
        }
        
        // Try without prefix
        block = Block.getBlockFromName(blockName);
        if (block != null) {
            return block;
        }
        
        // Try some common name variations
        String[] variations = {
            blockName.replace("_", ""),
            blockName.replace(" ", "_"),
            blockName.toLowerCase(),
            "minecraft:" + blockName.toLowerCase()
        };
        
        for (String variation : variations) {
            block = Block.getBlockFromName(variation);
            if (block != null) {
                return block;
            }
        }
        
        return null;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Allow all players to use this command
    }
}
