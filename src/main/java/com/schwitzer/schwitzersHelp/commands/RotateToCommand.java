package com.schwitzer.schwitzersHelp.commands;

import com.schwitzer.schwitzersHelp.util.ChatUtil;
import com.schwitzer.schwitzersHelp.util.RotatePlayerTo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import java.util.Arrays;
import java.util.List;

public class RotateToCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "rotateTo";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Rotates Player to coordinate";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("sw:rotateTo");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if(args.length <= 4)
        {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);
            int speed = Integer.parseInt(args[3]);

            RotatePlayerTo.lookAtBlock(new BlockPos(x, y, z), speed);

            ChatUtil.formatedChatMessage(x + " " + y + " " + z);
        }

    }

    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
