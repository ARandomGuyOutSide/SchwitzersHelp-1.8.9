package com.schwitzer.schwitzersHelp.commands;

import com.schwitzer.schwitzersHelp.features.FindItemFromChests;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FindItemFromScannedChestsCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "schwitzer:finditem";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sw:finditem <item-name>";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("sw:finditem");
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) return;

        String itemToFind = String.join(" ", args).trim();
        FindItemFromChests.blocksToRender = new LinkedList<>();

        FindItemFromChests.findItem(itemToFind);
    }
}
