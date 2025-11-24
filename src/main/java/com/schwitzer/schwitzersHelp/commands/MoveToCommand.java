package com.schwitzer.schwitzersHelp.commands;

import com.schwitzer.schwitzersHelp.features.MovePlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import java.util.Arrays;
import java.util.List;

public class MoveToCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "schwitzer:moveTo";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Moves Player to coordinate";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("sw:moveTo");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        double playerX = Minecraft.getMinecraft().thePlayer.posX;
        double playerY = Minecraft.getMinecraft().thePlayer.posY;
        double playerZ = Minecraft.getMinecraft().thePlayer.posZ;

        BlockPos playerPos = new BlockPos(
                (int) Math.floor(playerX),
                (int) Math.floor(playerY),
                (int) Math.floor(playerZ)
        );

        if (args.length == 1 && args[0].equals("castle")) {
            MovePlayer.movePlayerTo(playerPos, MovePlayer.getCastleWalkingCoords());
            return;
        } else if (args.length == 1 && args[0].equals("zombies")) {
            MovePlayer.movePlayerTo(playerPos, MovePlayer.getCryptWalkingCoords());
            return;
        }

        if (args.length == 3) {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);
            BlockPos[] goal = {new BlockPos(x, y, z)};
            MovePlayer.movePlayerTo(playerPos, goal);
            return;
        }

        throw new CommandException("Usage: /sw:moveTo <x> <y> <z> OR /sw:moveTo castle");
    }


    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
