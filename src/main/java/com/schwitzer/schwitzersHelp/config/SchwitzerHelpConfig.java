package com.schwitzer.schwitzersHelp.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.migration.VigilanceName;

/**
 * The main Config entrypoint that extends the Config type and inits the config options.
 * See <a href="https://docs.polyfrost.cc/oneconfig/config/adding-options">this link</a> for more config Options
 */
public class SchwitzerHelpConfig extends Config {

    private static SchwitzerHelpConfig instance; // Singleton Instanz

    private static final transient String General = "General";
    private static final transient String WallHacks = "Bedwars";
    private static final transient String Dungeons = "Dungeons";
    private static final transient String Mining = "Mining";
    private static final transient String Help = "Help";
    private static final transient String Minigames = "Minigames";
    private static final transient String Macros = "Macros";
    private static final transient String Slayers = "Slayers";
    private static final transient String Guild = "Guild";
    private static final transient String Discord = "Discord";

    //General

    //General
    @VigilanceName(name = "Rainbow Mode", category = General, subcategory = "Blocks")
    @Switch(name = "Rainbow Mode", category = General, subcategory = "Blocks")
    public boolean rainbow = false;

    @Switch(name = "Block Esp Outline", category = General, subcategory = "Blocks")
    public boolean blockOutline = false;

    @Switch(name = "Debug Mode", category = General, subcategory = "Debuging")
    public boolean debugMode = false;

    @Switch(name = "Fun Mode ;)", category = General, subcategory = "Fun")
    public boolean funMode = true;

    @Switch(name = "Chat Mention Notification", category = General, subcategory = "QOL")
    public boolean chatMention = true;

    @Number(name = "Titel Duration (s)", category = General, min = 1, max = 10, subcategory = "QOL")
    public int chatMentionTitleDuration = 3;

    @Switch(name = "Auto Change Chat On Party Join", category = General, subcategory = "QOL")
    public boolean autoChangeChat = true;

    //Bedwars

    @VigilanceName(name = "Bed Wars", category = WallHacks, subcategory = "Players")

    @Switch(name = "Player Esp", category = WallHacks, subcategory = "Players")
    public boolean playerEsp = true;

    @Color(name = "Color", category = WallHacks, subcategory = "Players")
    public OneColor playerEspColor = new OneColor(0, 100, 100, 255);

    @Switch(name = "Player Lines", category = WallHacks, subcategory = "Players")
    public boolean playerLines = true;

    @Color(name = "Player Lines", category = WallHacks, subcategory = "Players")
    public OneColor playerLinesColor = new OneColor(320, 100, 100, 255);

    @Switch(name = "Bed Esp", category = WallHacks, subcategory = "Bed Esp")
    public boolean bedEsp = true;

    @Color(name = "Bed Color", category = WallHacks, subcategory = "Bed Esp")
    public OneColor bedEspColor = new OneColor(290, 100, 100, 255);

    //Carneval

    //Dungeons

    @VigilanceName(name = "Key Mob Esp", category = Dungeons, subcategory = "Mobs Esp")

    @Switch(name = "Key Mobs Esp", category = Dungeons, subcategory = "Mob Esp")
    public boolean keyMobEsp = true;

    @Color(name = "Color Of The Key Mob Esp", category = Dungeons, subcategory = "Mob Esp")
    public OneColor keyMobEspColor = new OneColor(50, 100, 100, 255);

    @Switch(name = "Bat Esp", category = Dungeons, subcategory = "Mob Esp")
    public boolean batEsp = true;

    @Color(name = "Color Of The Bat Esp", category = Dungeons, subcategory = "Mob Esp")
    public OneColor batEspColor = new OneColor(50, 100, 100, 255);

    @Switch(name = "Ghost Block", category = Dungeons, subcategory = "Ghost Block", size = 2)
    public boolean ghostBlock = false;

    @Slider(name = "Distance", category = Dungeons, subcategory = "Ghost Block", min = 1, max = 100)
    public int ghostBlockDistance = 5;

    @Slider(name = "Delay (ms)", category = Dungeons, subcategory = "Ghost Block", min = 0, max = 50)
    public int ghostBlockDelay = 5;

    @Switch(name = "Make Essence/Readstone Head Ghost Block", category = Dungeons, subcategory = "Ghost Block")
    public boolean ghostBlockEssence = true;

    @Switch(name = "Send Leap Message", category = Dungeons, subcategory = "Chat Stuff")
    public boolean sendLeapMessage = true;

    @Text(name = "Custom Leap Message", placeholder = "Leaped to ", multiline = false, category = Dungeons, subcategory = "Chat Stuff")
    public String customLeapMessage = "";

    @Switch(name = "Send Saved Message", description = "Send messages when you where saved by your bonzo/spirit mask or phonix pet", category = Dungeons, subcategory = "Chat Stuff", size = 2)
    public boolean sendSavedMessage = true;

    @Switch(name = "Shut Up Dungeons!", description = "Hides all messages received in dungeons", category = Dungeons, subcategory = "Chat Stuff")
    public boolean shutUpDungeons;

    @Switch(name = "Even Boss Shut Up!", description = "Hides even boss messages", category = Dungeons, subcategory = "Chat Stuff")
    public boolean evenBossShutUp;



    // Mining

    @VigilanceName(name = "Titanium Esp", category = Mining, subcategory = "Titanium Esp")

    @Switch(name = "Titanium Esp", category = Mining, subcategory = "Titanium Esp")
    public boolean scanForTitanium = true;

    @Switch(name = "Coal Esp", category = Mining, subcategory = "Coal Esp")
    public boolean coalEsp = false;

    @Color(name = "Color Of Coal Esp", category = Mining, subcategory = "Coal Esp")
    public OneColor coalEspColor = new OneColor(50, 100, 100, 255);

    @Switch(name = "Best Coal Vein Line", category = Mining, subcategory = "Coal Esp")
    public boolean coalVeinLine = false;

    @Color(name = "Color Of Line Esp", category = Mining, subcategory = "Coal Esp")
    public OneColor coalEspLineColor = new OneColor(50, 100, 100, 255);

    @Slider(name = "Distance", category = Mining, subcategory = "Coal Esp", min = 1, max = 50)
    public int coalEspRange = 5;

    // Help

    @VigilanceName(name = "Config Movement", category = Help, subcategory = "Config Movement")

    @Switch(name = "Modify Movementspeed", category = Help, subcategory = "Modify Movement", size = 1)
    public boolean modify_movementspeed = false;

    @Number(name = "Set Movementspeed Value", category = Help, subcategory = "Modify Movement", min = 1.0f, max = 25.0f, size = 1)
    public float movementspeed = 1;

    @Switch(name = "Modify Movementspeed Hypixel", category = Help, subcategory = "Modify Movement", size = 2)
    public boolean modify_movementspeed_hypixel = false;

    @Switch(name = "Fly", category = Help, subcategory = "Modify Movement", size = 2)
    public boolean fly = false;

    @Switch(name = "No Fall", category = Help, subcategory = "Modify Movement", size = 2)
    public boolean noFall = false;

    @Switch(name = "Auto Jump", category = Help, description = "Activating it can bypass SOME anit cheat plugins", subcategory = "Modify Movement", size = 2)
    public boolean auto_jump = true;


    @VigilanceName(name = "World Render Help", category = Help, subcategory = "World Render Help")

    @Switch(name = "Display Item Count", category = Help, subcategory = "World Render Help", size = 2)
    public boolean itemCount = false;

    @VigilanceName(name = "Bridging", category = Help, subcategory = "Bridging")

    @Switch(name = "Fast Place", category = Help, subcategory = "Bridging", size = 2)
    public boolean fastPlace = false;

    @Switch(name = "Safe Walk", category = Help, subcategory = "Bridging", size = 2)
    public boolean safeWalk = false;

    @VigilanceName(name = "Combat", category = Help, subcategory = "Combat")

    @Switch(name = "Reach", category = Help, subcategory = "Combat", size = 1)
    public boolean reach = false;

    @Number(name = "Reach Distance", category = Help, subcategory = "Combat", min = 3.0f, max = 6.0f, size = 1)
    public float reach_distance = 1;

    @Switch(name = "Aim Assist", category = Help, subcategory = "Combat", size = 1)
    public boolean aim_assist = false;

    @Switch(name = "Show Circle", category = Help, subcategory = "Combat", size = 1)
    public boolean show_circle = false;

    @Checkbox(name = "Click Aim", category = Help, subcategory = "Combat", size = 1)
    public boolean click_aim;

    @Checkbox(name = "Weapon Only", category = Help, subcategory = "Combat", size = 1)
    public boolean weapon_only;

    @Checkbox(name = "Aim Invis", category = Help, subcategory = "Combat", size = 1)
    public boolean aim_invis;

    @Checkbox(name = "Blatant Mode", category = Help, subcategory = "Combat", size = 1)
    public boolean blatant_mode;

    @Checkbox(name = "Ignore Friends", category = Help, subcategory = "Combat", size = 1)
    public boolean ignore_friends;


    @Slider(name = "Speed", category = Help, subcategory = "Combat", min = 1, max = 10)
    public int aim_assist_speed = 1;

    @Slider(name = "Distance", category = Help, subcategory = "Combat", min = 1, max = 10)
    public int aim_assist_distance = 5;

    @Slider(name = "Fov", category = Help, subcategory = "Combat", min = 0, max = 180)
    public int aim_assist_fov = 90;


    //Minigames

    @VigilanceName(name = "Minigames", category = Minigames, subcategory = "Minigames")

    @Switch(name = "Zombies Game Esp", category = Minigames, subcategory = "Zombies")
    public boolean zombieEsp = false;

    @Color(name = "Zombies Game Esp Color", category = Minigames, subcategory = "Zombies")
    public OneColor zombies_color = new OneColor(50, 100, 100, 255);

    @Switch(name = "Dragon Esp", category = Minigames, subcategory = "Disasters")
    public boolean dragon_Esp = false;

    @Color(name = "Dragon Esp Color", category = Minigames, subcategory = "Disasters")
    public OneColor dragon_Esp_color = new OneColor(50, 100, 100, 255);

    @Switch(name = "Dragon Line", category = Minigames, subcategory = "Disasters")
    public boolean dragonLine = false;

    @Color(name = "Dragon Line Color", category = Minigames, subcategory = "Disasters")
    public OneColor dragonLineColor = new OneColor(50, 100, 100, 255);

    // Slayers

    @Switch(name = "Slayer Esp", category = Slayers, subcategory = "General")
    public boolean slayerEsp;

    @Color(name = "Slayer Esp Color", category = Slayers, subcategory = "General")
    public OneColor slayerEspColor = new OneColor(50, 100, 100, 255);

    @Switch(name = "Slayer Carry Mode", category = Slayers, subcategory = "General")
    public boolean slayerCarryMode = false;

    @Text(name = "Carry Name/s", category = Slayers, subcategory = "General")
    public String slayerCarryNames;

    // Macros

    @VigilanceName(name = "Macros", category = Macros, subcategory = "Macros")

    @KeyBind(name = "Macro Key", category = Macros, subcategory = "Guild Macro")
    public OneKeyBind guildMacroKey = new OneKeyBind();

    @KeyBind(name = "Macro Key", category = Macros, subcategory = "Stash Macro")
    public OneKeyBind stashMacroKey = new OneKeyBind();

    @KeyBind(name = "Macro Key", category = Macros, subcategory = "Bazaar Ordering Macro")
    public OneKeyBind bazaarOrderingMacroKey = new OneKeyBind();

    @Dropdown(name = "Item Place", options = {"Already in bazaar", "Sack"}, category = Macros, subcategory = "Bazaar Ordering Macro")
    public int itemPlaceOption = 0;

    @Switch(name = "Sell Offer", category = Macros, subcategory = "Bazaar Ordering Macro")
    public boolean sellOffer = false;

    @Switch(name = "Custom Price/s", category = Macros, subcategory = "Bazaar Ordering Macro")
    public boolean customPrices = false;

    @Text(name = "Item Name/s", placeholder = "enchanted_coal;redstone", multiline = true, category = Macros, subcategory = "Bazaar Ordering Macro")
    public String itemNames = "";

    @Text(name = "Item Price/s", placeholder = "1.3k;7", description = "Use in right order as item names!", multiline = true, category = Macros, subcategory = "Bazaar Ordering Macro")
    public String itemPrices = "";

    @KeyBind(name = "Rev Slayer Macro", category = Macros, subcategory = "Rev Slayer Macro")
    public OneKeyBind revSlayerMacroKey = new OneKeyBind();


    // Guild

    @VigilanceName(name = "Guild", category = Guild, subcategory = "Guild")
    @Switch(name = "Message On Join", category = Guild, subcategory = "On Join")
    public boolean sendMessageOnGuildJoin = true;



    // Discord

    @VigilanceName(name = "Guild", category = Discord, subcategory = "Discord")
    @Switch(name = "Send Information To Discord Webhook", category = Discord, subcategory = "Webhook")
    public boolean sendDiscordInformation = true;

    @Text(name = "Discord Webhook", category = Discord, subcategory = "Webhook", size = 2, placeholder = "Webhook")
    public String discordWebhook = "";

    @Switch(name = "Auto Kick System", category = Guild, subcategory = "Auto Kick")
    public boolean autoKickEnabled = false;

    @Checkbox(name = "Monday", category = Guild, subcategory = "Auto Kick")
    public boolean kickOnMonday = false;

    @Checkbox(name = "Tuesday", category = Guild, subcategory = "Auto Kick")
    public boolean kickOnTuesday = false;

    @Checkbox(name = "Wednesday", category = Guild, subcategory = "Auto Kick")
    public boolean kickOnWednesday = false;

    @Checkbox(name = "Thursday", category = Guild, subcategory = "Auto Kick")
    public boolean kickOnThursday = false;

    @Checkbox(name = "Friday", category = Guild, subcategory = "Auto Kick")
    public boolean kickOnFriday = false;

    @Checkbox(name = "Saturday", category = Guild, subcategory = "Auto Kick")
    public boolean kickOnSaturday = false;

    @Checkbox(name = "Sunday", category = Guild, subcategory = "Auto Kick")
    public boolean kickOnSunday = false;


    public SchwitzerHelpConfig() {
        super(new Mod("Schwitzers help", ModType.SKYBLOCK), "schwitzers_conf.json");

        // Initialisierung der Konfiguration
        initialize();

        // Dependency's

        // General
        this.addDependency("chatMentionTitleDuration", "chatMention");

        // Dungeons
        this.addDependency("ghost_block_delay", "make_ghost_block");
        this.addDependency("ghost_block_distance", "make_ghost_block");
        this.addDependency("ghost_block_essence", "make_ghost_block");
        this.addDependency("custom_leap_message", "send_leap_message");
        this.addDependency("evenBossShutUp", "shutUpDungeons");

        // Mining
        this.addDependency("coalEspRange", "coalEsp");
        this.addDependency("coalVeinLine", "coalEsp");
        this.addDependency("packetsPerSecond", "pinglessHardstone");

        // Macros
        this.addDependency("itemPrices", "customPrices");
    }

    // Methode, um die einzige Instanz der Klasse zu erhalten
    public static synchronized SchwitzerHelpConfig getInstance() {
        if (instance == null) {
            instance = new SchwitzerHelpConfig();
        }
        return instance;
    }

    public OneKeyBind getBazaarOrderingMacroKey() {
        return bazaarOrderingMacroKey;
    }

    public String getItemPrices() {
        return itemPrices;
    }

    public int getItemPlaceOption() {
        return itemPlaceOption;
    }

    public boolean isSellOffer() {
        return sellOffer;
    }

    public boolean isCoalVeinLine() {
        return coalVeinLine;
    }

    public String getItemNames() {
        return itemNames;
    }

    public OneKeyBind getStashMacroKey() {
        return stashMacroKey;
    }

    public int getChatMentionTitleDuration() {
        return chatMentionTitleDuration;
    }

    public OneColor getCoalEspLineColor() {
        return coalEspLineColor;
    }

    public boolean isFunMode() {
        return funMode;
    }

    public String getDiscordWebhook() {
        return discordWebhook;
    }

    public OneKeyBind getGuildMacroKey() {
        return guildMacroKey;
    }

    public boolean isSendDiscordInformation() {
        return sendDiscordInformation;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isSendMessageOnGuildJoin() {
        return sendMessageOnGuildJoin;
    }

    public boolean isBlockOutline() {
        return blockOutline;
    }

    public boolean isPlayerEsp() {
        return playerEsp;
    }

    public boolean isBedEsp() {
        return bedEsp;
    }

    public OneColor getPlayerEspColor() {
        return playerEspColor;
    }

    public boolean isSendLeapMessage() {
        return sendLeapMessage;
    }

    public String getCustomLeapMessage() {
        return customLeapMessage;
    }

    public OneColor getBedEspColor() {
        return bedEspColor;
    }

    public boolean isKeyMobEsp() {
        return keyMobEsp;
    }

    public OneColor getKeyMobEspColor() {
        return keyMobEspColor;
    }

    public boolean isPlayerLines() {
        return playerLines;
    }

    public OneColor getPlayerLinesColor() {
        return playerLinesColor;
    }

    public boolean isScanForTitanium() {
        return scanForTitanium;
    }

    public boolean isSlayerEsp() {
        return slayerEsp;
    }

    public boolean isModify_movementspeed() {
        return modify_movementspeed;
    }

    public boolean isAuto_jump() {
        return auto_jump;
    }

    public float getMovementspeed() {
        return movementspeed;
    }

    public boolean isFly() {
        return fly;
    }

    public boolean isSlayerCarryMode() {
        return slayerCarryMode;
    }

    public boolean isEvenBossShutUp() {
        return evenBossShutUp;
    }

    public boolean isSendSavedMessage() {
        return sendSavedMessage;
    }

    public String getSlayerCarryNames() {
        return slayerCarryNames;
    }

    public boolean isNoFall() {
        return noFall;
    }

    public boolean isAutoKickEnabled() {
        return autoKickEnabled;
    }

    public boolean isModify_movementspeed_hypixel() {
        return modify_movementspeed_hypixel;
    }

    public boolean isItemCount() {
        return itemCount;
    }

    public boolean isFastPlace() {
        return fastPlace;
    }

    public boolean isReach() {
        return reach;
    }

    public float getReach_distance() {
        return reach_distance;
    }

    public boolean isSafeWalk() {
        return safeWalk;
    }

    public boolean isZombieGameEsp() {
        return zombieEsp;
    }

    public OneColor getSlayerEspColor() {
        return slayerEspColor;
    }

    public OneColor getZombiesGameColor() {
        return zombies_color;
    }

    public boolean isChaosDragonEsp() {
        return dragon_Esp;
    }

    public boolean isShutUpDungeons() {
        return shutUpDungeons;
    }

    public OneColor getChaosDragonEspColor() {
        return dragon_Esp_color;
    }

    public boolean isDragonline() {
        return dragonLine;
    }

    public OneColor getDragonLineColor() {
        return dragonLineColor;
    }

    public boolean isGhost_block() {
        return ghostBlock;
    }

    public int getGhostBlockDistance() {
        return ghostBlockDistance;
    }

    public boolean isAim_Assist() {
        return aim_assist;
    }

    public boolean isCustomPrices() {
        return customPrices;
    }

    public OneKeyBind getRevSlayerMacroKey() {return revSlayerMacroKey;}

    public int getAimAssistSpeed() {
        return aim_assist_speed;
    }

    public int getGhostBlockDelay() {
        return ghostBlockDelay;
    }

    public boolean isGhostBlockEssence() {
        return ghostBlockEssence;
    }

    public int getAimAssistDistance() {
        return aim_assist_distance;
    }

    public int getAimAssistFov() {
        return aim_assist_fov;
    }

    public boolean isChatMention() {
        return chatMention;
    }

    public boolean isShow_circle() {
        return show_circle;
    }

    public boolean isBlatant_mode() {
        return blatant_mode;
    }

    public boolean isCoalEsp() {
        return coalEsp;
    }

    public boolean isBatEsp() {
        return batEsp;
    }

    public OneColor getBatEspColor() {
        return batEspColor;
    }

    public OneColor getCoalEspColor() {
        return coalEspColor;
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public int getCoalEspRange() {
        return coalEspRange;
    }

    public boolean isAim_invis() {
        return aim_invis;
    }

    public boolean isWeapon_only() {
        return weapon_only;
    }

    public boolean isClick_aim() {
        return click_aim;
    }

    public boolean isIgnore_friends() {
        return ignore_friends;
    }

    public boolean isKickOnMonday() {
        return kickOnMonday;
    }

    public boolean isKickOnTuesday() {
        return kickOnTuesday;
    }

    public boolean isKickOnWednesday() {
        return kickOnWednesday;
    }

    public boolean isKickOnThursday() {
        return kickOnThursday;
    }

    public boolean isKickOnFriday() {
        return kickOnFriday;
    }

    public boolean isKickOnSaturday() {
        return kickOnSaturday;
    }

    public boolean isKickOnSunday() {
        return kickOnSunday;
    }

    public boolean isAutoChangeChat() {
        return autoChangeChat;
    }
}

