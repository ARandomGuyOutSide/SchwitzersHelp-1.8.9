package com.schwitzer.schwitzersHelp.macros;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.features.HubWheatFarmer;
import com.schwitzer.schwitzersHelp.features.MovePlayer;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import com.schwitzer.schwitzersHelp.util.PathFinder.PathFinding;
import com.schwitzer.schwitzersHelp.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.io.input.TeeInputStream;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AutoUnlockLvlSeven {
    private static final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private final Minecraft mc = Minecraft.getMinecraft();

    private final EntityPlayerSP player = mc.thePlayer;


    private TimerUtil timerUtil;

    private MacroStates currentState;

    private MacroController.MacroState macroControllerState = MacroController.MacroState.DISABLED;

    private int currentSkyblockXp;

    private BlockPos[] villagersPosition =
            {
                    new BlockPos(-8, 70, -74), // Leo
                    new BlockPos(-17, 70, -80), // Vex
                    new BlockPos(-12, 70, -94), // Duke
                    new BlockPos(-25, 68, -105), // Felix
                    new BlockPos(-22, 68, -123), //Lynn
                    new BlockPos(26, 70, -117), // Ryu
                    new BlockPos(19, 70, -99), // Stella
                    new BlockPos(10, 70, -44), // Liam
                    new BlockPos(28, 69, -56), // Tom
                    new BlockPos(38, 68, -49), // Andrew
                    new BlockPos(-1, 70, -53), // Jack
                    new BlockPos(-35, 68, -39) // Jamie
            };

    private Set<String> villagerNames = new HashSet<>(Arrays.asList(
            "Leo",
            "Vex",
            "Duke",
            "Felix",
            "Lynn",
            "Ryu",
            "Stella",
            "Liam",
            "Tom",
            "Andrew",
            "Jack",
            "Jamie"
    ));

    private enum MacroStates {
        CHECK_LVL, TALK_TO_ALL_VILLAGERS_IN_HUB, TALK_TO_THE_FARMER_IN_HUB, FARM_WHEAT_IN_HUB
    }

    public void initAutoUnlockLvlSeven() {
        MinecraftForge.EVENT_BUS.register(this);
        timerUtil = new TimerUtil();
        macroControllerState = MacroController.MacroState.ENABLED;
        currentState = MacroStates.FARM_WHEAT_IN_HUB;

        /// TODO: close screen if open while talking to villagers

        /// TODO: check if player is over lvl 6
        /// TODO: explore the hubs (visit all areas in the hub 13 areas)
        /// TODO: talk to farmhand on the barn
        /// TODO: farming until lvl 10
        /// TODO: give 25 wheat to sam
        /// TODO: craft bee pet
        /// TODO: unlock enchanting, lvl, gold shovel with effi 5
        /// TODO: sand mining till mining 15
        /// TODO: give pick to Lazy Miner (Gold mine)
        /// TODO: foraging in hub till lvl 8
        /// TODO: do lumber jack quest
        /// TODO: alchemy
        /// TODO: unlock redstone collection 2
        /// TODO: buy talis

        /// TODO: donate items to museum


        /// TODO: always do stuff on private hub

        System.out.println("Initialized Auto Unlock Lvl Seven Macro");
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        switch (currentState) {
            case TALK_TO_ALL_VILLAGERS_IN_HUB:
                MovePlayer.movePlayerTo(mc.thePlayer.getPosition(), villagersPosition, villagerNames);
                currentState = MacroStates.TALK_TO_THE_FARMER_IN_HUB;
                currentSkyblockXp += 5;
                System.out.println("Changed state to CheckLvl");
                break;
            case TALK_TO_THE_FARMER_IN_HUB:
                ChatUtil.sendMessage("/hub");
                Set<String> farmerName = new HashSet<>(Arrays.asList("Farmer"));
                MovePlayer.movePlayerTo(mc.thePlayer.getPosition(), new BlockPos[] {new BlockPos(44, 72, -161)},  farmerName);
                currentSkyblockXp += 5;
                currentState = MacroStates.FARM_WHEAT_IN_HUB;
                break;
            case FARM_WHEAT_IN_HUB:
                //MovePlayer.movePlayerTo(player.getPosition(), new BlockPos[] {new BlockPos(37, 70, -120)});
                HubWheatFarmer hubWheatFarmer = new HubWheatFarmer();

                hubWheatFarmer.scanForWheat();
        }
    }
}
