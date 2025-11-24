package com.schwitzer.schwitzersHelp.commands;

import com.schwitzer.schwitzersHelp.macros.AutoUnlockLvlSeven;
import com.schwitzer.schwitzersHelp.util.BossBarUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class AutoUnlockLvlSevenCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "schwitzer:unlocklvlseven";
    }

    @Override
    public java.util.List<String> getCommandAliases() {
        return java.util.Arrays.asList("sw:unlocklvlseven");
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        System.out.println(BossBarUtil.getBossBarInfo());
        AutoUnlockLvlSeven autoUnlockLvlSeven = new AutoUnlockLvlSeven();

        autoUnlockLvlSeven.initAutoUnlockLvlSeven();
    }
}
