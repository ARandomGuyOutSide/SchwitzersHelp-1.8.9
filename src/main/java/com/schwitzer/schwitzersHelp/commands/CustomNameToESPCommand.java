package com.schwitzer.schwitzersHelp.commands;

import com.schwitzer.schwitzersHelp.features.CustomNameEsp;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import java.util.Arrays;
import java.util.List;

public class CustomNameToESPCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "schwitzer:customespname";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sw:customespname <add name|remove name|list|clear> - Add, remove, list, or clear entity names for custom ESP";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("sw:customespname");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            ChatUtil.formatedChatMessage("Usage: /sw:customespname <add name|remove name|list|clear>");
            ChatUtil.formatedChatMessage("Examples:");
            ChatUtil.formatedChatMessage("  /sw:customespname add Zombie Villager");
            ChatUtil.formatedChatMessage("  /sw:customespname remove Zombie Villager");
            ChatUtil.formatedChatMessage("  /sw:customespname list");
            ChatUtil.formatedChatMessage("  /sw:customespname clear");
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "add":
                if (args.length < 2) {
                    ChatUtil.formatedChatMessage("Usage: /sw:customespname add <name>");
                    return;
                }
                addName(args[1]);
                break;
            case "list":
                listNames();
                break;
            case "clear":
                clearNames();
                break;
            case "remove":
                if (args.length < 2) {
                    ChatUtil.formatedChatMessage("Usage: /sw:customespname remove <name>");
                    return;
                }
                // Join all arguments after "remove" to handle names with spaces
                String nameToRemove = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                removeName(nameToRemove);
                break;
            default:
                // Default action is to add a name (join all arguments to handle names with spaces)
                String nameToAdd = String.join(" ", args);
                addName(nameToAdd);
                break;
        }
    }

    private void addName(String entityName) {
        if (entityName == null || entityName.trim().isEmpty()) {
            ChatUtil.formatedChatMessage(EnumChatFormatting.RED + "Error: Entity name cannot be empty!");
            return;
        }

        String trimmedName = entityName.trim();
        
        if (CustomNameEsp.containsName(trimmedName)) {
            ChatUtil.formatedChatMessage(EnumChatFormatting.YELLOW + "Name '" + trimmedName + "' is already in the ESP list!");
            return;
        }

        boolean added = CustomNameEsp.addName(trimmedName);
        if (added) {
            ChatUtil.formatedChatMessage(EnumChatFormatting.GREEN + "Successfully added '" + trimmedName + "' to name ESP list!");
            ChatUtil.formatedChatMessage("Total names in ESP: " + CustomNameEsp.getNameCount());
            ChatUtil.formatedChatMessage("The system will look for entities containing this name in their custom name tag.");
            ChatUtil.formatedChatMessage(EnumChatFormatting.GRAY + "Debug: Enable debug mode in config to see entity search details.");
        } else {
            ChatUtil.formatedChatMessage(EnumChatFormatting.RED + "Failed to add name to ESP list!");
        }
    }

    private void removeName(String entityName) {
        if (entityName == null || entityName.trim().isEmpty()) {
            ChatUtil.formatedChatMessage(EnumChatFormatting.RED + "Error: Entity name cannot be empty!");
            return;
        }

        String trimmedName = entityName.trim();
        
        boolean removed = CustomNameEsp.removeName(trimmedName);
        if (removed) {
            ChatUtil.formatedChatMessage(EnumChatFormatting.GREEN + "Successfully removed '" + trimmedName + "' from name ESP list!");
            ChatUtil.formatedChatMessage("Total names in ESP: " + CustomNameEsp.getNameCount());
        } else {
            ChatUtil.formatedChatMessage(EnumChatFormatting.YELLOW + "Name '" + trimmedName + "' was not in the ESP list!");
        }
    }

    private void listNames() {
        if (CustomNameEsp.getNameCount() == 0) {
            ChatUtil.formatedChatMessage(EnumChatFormatting.YELLOW + "No names in the ESP list!");
            return;
        }

        ChatUtil.formatedChatMessage(EnumChatFormatting.GREEN + "Names in ESP list (" + CustomNameEsp.getNameCount() + "):");
        for (String name : CustomNameEsp.getEspNames()) {
            ChatUtil.formatedChatMessage("  - \"" + name + "\"");
        }
    }

    private void clearNames() {
        int count = CustomNameEsp.getNameCount();
        CustomNameEsp.clearAllNames();
        ChatUtil.formatedChatMessage(EnumChatFormatting.GREEN + "Cleared " + count + " names from ESP list!");
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
