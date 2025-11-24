package com.schwitzer.schwitzersHelp;

import com.schwitzer.schwitzersHelp.bedwars.BedWarsEsp;
import com.schwitzer.schwitzersHelp.bedwars.PlayerLinesMod;
import com.schwitzer.schwitzersHelp.commands.*;
import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.debug.InventorySlots;
import com.schwitzer.schwitzersHelp.dungeon.DungeonEsp;
import com.schwitzer.schwitzersHelp.dungeon.GhostBlock;
import com.schwitzer.schwitzersHelp.dungeon.InformTeammatesInChat;
import com.schwitzer.schwitzersHelp.dungeon.ShutUpDungeons;
import com.schwitzer.schwitzersHelp.failsaves.BazaarFailsaves;
import com.schwitzer.schwitzersHelp.failsaves.Worldchange;
import com.schwitzer.schwitzersHelp.features.*;
import com.schwitzer.schwitzersHelp.guilde.AutoKick;
import com.schwitzer.schwitzersHelp.guilde.WelcomeMessages;
import com.schwitzer.schwitzersHelp.helpStuff.*;
import com.schwitzer.schwitzersHelp.macros.MacroController;
import com.schwitzer.schwitzersHelp.minigames.MinigameEsp;
import com.schwitzer.schwitzersHelp.mining.MiningStuff;
import com.schwitzer.schwitzersHelp.mod.RenderLogoOnDungeonHub;
import com.schwitzer.schwitzersHelp.slayer.SlayerEsp;
import com.schwitzer.schwitzersHelp.util.BossBarUtil;
import com.schwitzer.schwitzersHelp.util.PathFinder.PathFinding;
import com.schwitzer.schwitzersHelp.util.PlayerInputUtil;
import com.schwitzer.schwitzersHelp.util.RotatePlayerTo;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

@Mod(modid = "schwitzershelp", name = "Schwitzers Help", version = "1.08")
public class SchwitzersHelp {
    public static final String VERSION = "%%VERSION%%";
    public static File jarFile = null;
    public static SchwitzerHelpConfig config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        jarFile = event.getSourceFile();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Initialize all modules
        config = SchwitzerHelpConfig.getInstance();
        MinecraftForge.EVENT_BUS.register(this);

        // Commands
        ClientCommandHandler.instance.registerCommand(new TestingCommand());
        ClientCommandHandler.instance.registerCommand(new PlaceBlockCommand());
        ClientCommandHandler.instance.registerCommand(new RotateToCommand());
        ClientCommandHandler.instance.registerCommand(new MoveToCommand());
        ClientCommandHandler.instance.registerCommand(new CustomBlockToESPCommand());
        ClientCommandHandler.instance.registerCommand(new CustomNameToESPCommand());
        ClientCommandHandler.instance.registerCommand(new FindItemFromScannedChestsCommand());
        ClientCommandHandler.instance.registerCommand(new AutoUnlockLvlSevenCommand());

        // Mod
        MinecraftForge.EVENT_BUS.register(new RenderLogoOnDungeonHub());

        // Bedwars
        MinecraftForge.EVENT_BUS.register(new PlayerLinesMod());
        MinecraftForge.EVENT_BUS.register(new BedWarsEsp());

        // Dungeons
        MinecraftForge.EVENT_BUS.register(new GhostBlock());
        MinecraftForge.EVENT_BUS.register(new InformTeammatesInChat());
        MinecraftForge.EVENT_BUS.register(new DungeonEsp());
        MinecraftForge.EVENT_BUS.register(new ShutUpDungeons());

        // Failsaves
        MinecraftForge.EVENT_BUS.register(new Worldchange());
        MinecraftForge.EVENT_BUS.register(new BazaarFailsaves());

        // Features
        MinecraftForge.EVENT_BUS.register(new CustomBlockEsp());
        CustomBlockEsp.init();
        MinecraftForge.EVENT_BUS.register(new CustomNameEsp());
        CustomNameEsp.init();
        MinecraftForge.EVENT_BUS.register(new DisconnectWhenISayIt());
        MinecraftForge.EVENT_BUS.register(new PlayerMentionedInChat());
        MinecraftForge.EVENT_BUS.register(new MovePlayer());
        MinecraftForge.EVENT_BUS.register(new PlaceBlocksOnCommand());
        MinecraftForge.EVENT_BUS.register(new AutoChangeChat());
        MinecraftForge.EVENT_BUS.register(new FindItemFromChests());
        FindItemFromChests.init();
        MinecraftForge.EVENT_BUS.register(new HubWheatFarmer());

        // Guilde
        MinecraftForge.EVENT_BUS.register(new WelcomeMessages());
        MinecraftForge.EVENT_BUS.register(new AutoKick());

        // Help
        MinecraftForge.EVENT_BUS.register(new AimAssist());
        MinecraftForge.EVENT_BUS.register(new ItemCount());
        MinecraftForge.EVENT_BUS.register(new MovementHelp());
        MinecraftForge.EVENT_BUS.register(new Reach());
        MinecraftForge.EVENT_BUS.register(new SafeWalk());

        // Macros
        MinecraftForge.EVENT_BUS.register(new MacroController());

        // Mining
        MinecraftForge.EVENT_BUS.register(new MiningStuff());

        // Debug
        MinecraftForge.EVENT_BUS.register(new InventorySlots());

        // Minigames
        MinecraftForge.EVENT_BUS.register(new MinigameEsp());

        // Slayer
        MinecraftForge.EVENT_BUS.register(new SlayerEsp());

        // Util
        MinecraftForge.EVENT_BUS.register(new RotatePlayerTo());
        MinecraftForge.EVENT_BUS.register(new PathFinding());
        MinecraftForge.EVENT_BUS.register(new BossBarUtil());
        MinecraftForge.EVENT_BUS.register(new PlayerInputUtil());
    }

    public static String getCurrentVersion() {
        return "1.0.8";
    }
}
