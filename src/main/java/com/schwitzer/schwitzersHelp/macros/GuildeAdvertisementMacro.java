package com.schwitzer.schwitzersHelp.macros;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.discord.DiscordNotifications;
import com.schwitzer.schwitzersHelp.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class GuildeAdvertisementMacro implements Macro {
    private final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();

    private final String discordWebhook = config.getDiscordWebhook();
    private MacroController.MacroState macroControllerState = MacroController.MacroState.DISABLED;
    private MacroState currentState = MacroState.WAITING_AT_HUB;
    private MacroState previousState = MacroState.DISABLED;

    // KORREKTUR: Separate Strings statt einem einzigen String mit Kommas
    private String[] locations = {"Hub", "End", "Forge", "Dungeon Hub"};
    private String[] messages = {
            "Du Suchst eine Aktive, anfängerfreundliche Gilde mit Discord & keinen Anforderungen? /g join GooDz4",
            "Tritt jetzt einer der größten Deutschen Gilden bei! -> /g join GooDz4",
            "Eine der neusten Gilden des freundlichen GooDz Clans sucht neue aktive Mitglieder /g join GooDz4",
            "Sehr aktive und anfängerfreundliche Gilde sucht neue Mitglieder! /g join GooDz4",
            "Schließe dich jetzt der aktiven und netten Gilde GooDz4 an! → /g join GooDz4",
            "Du willst Spaß, Aktivität & Discord? Dann ab zu uns! → /g join GooDz4",
            "Aktive deutsche Gilde mit freundlicher Community sucht dich! /g join GooDz4",
            "Noch auf Gildensuche? Werde Teil von GooDz4 – ganz ohne Anforderungen!",
            "Deutschsprachige, aktive Gilde mit Discord nimmt neue Leute auf! /g join GooDz4",
            "Jetzt beitreten und Teil einer großen, aktiven Gilde werden → /g join GooDz4"
    };
    private List<String> usedLocations = new LinkedList<>();
    private List<String> usedMessages = new LinkedList<>();
    private Random random = new Random();
    private int tickcounter;
    private String currentLocation;
    private String currentMessage;

    private enum MacroState {
        WAITING_AT_HUB, WARPING, WAITING_AFTER_WARP, SENDING_CHAT_MESSAGE, SENDING_WEBHOOK, WAITING_10_MINUTES, DISABLED
    }

    private void setState(MacroState newState) {
        previousState = currentState;
        currentState = newState;
    }

    private boolean isGuiOpen() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.currentScreen != null;
    }

    @Override
    public void onEnable() {
        currentState = MacroState.WARPING;
        previousState = MacroState.DISABLED;
        macroControllerState = MacroController.MacroState.ENABLED;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        currentState = MacroState.DISABLED;
        previousState = MacroState.DISABLED;
        macroControllerState = MacroController.MacroState.DISABLED;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public String getName() {
        return "Guilde Advertisement Warp Bot";
    }

    @Override
    public MacroController.MacroState getState() {
        return macroControllerState;
    }

    @Override
    public void setState(MacroController.MacroState state) {
        this.macroControllerState = state;
        if (state == MacroController.MacroState.DISABLED) {
            onDisable();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) throws IOException {
        if (event.phase != TickEvent.Phase.END) return;
        if (macroControllerState != MacroController.MacroState.ENABLED) return;
        if (currentState == MacroState.DISABLED) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (tickcounter > 0) {
            tickcounter--;
            return;
        }

        switch (currentState) {
            case WARPING:
                // GUI-Check: Wenn eine GUI offen ist, warten bis sie geschlossen wird
                if (isGuiOpen()) {
                    // Warte bis GUI geschlossen ist, dann fortfahren
                    tickcounter = 10; // Kurze Pause und erneut prüfen
                    break;
                }

                // Überprüfe ob alle Locations verwendet wurden und leere die Liste
                if (usedLocations.size() >= locations.length) {
                    usedLocations.clear();
                }

                String selectedLocation;

                // Wähle eine Location die noch nicht verwendet wurde
                do {
                    selectedLocation = locations[random.nextInt(locations.length)];
                } while (usedLocations.contains(selectedLocation));

                // Füge die ausgewählte Location zu usedLocations hinzu
                usedLocations.add(selectedLocation);
                currentLocation = selectedLocation;

                switch (selectedLocation) {
                    case "Hub":
                        ChatUtil.sendMessage("/warp hub");
                        break;
                    case "End":
                        ChatUtil.sendMessage("/warp end");
                        break;
                    case "Forge":
                        ChatUtil.sendMessage("/warp forge");
                        break;
                    case "Dungeon Hub":
                        ChatUtil.sendMessage("/warp dungeon_hub");
                        break;
                }

                // Warte 3 Sekunden nach dem Warp-Befehl
                tickcounter = 3 * 20; // 3 Sekunden = 60 Ticks
                setState(MacroState.WAITING_AFTER_WARP);
                break;

            case WAITING_AFTER_WARP:
                // GUI-Check: Wenn eine GUI offen ist, warten bis sie geschlossen wird
                if (isGuiOpen()) {
                    // Warte bis GUI geschlossen ist, dann fortfahren
                    tickcounter = 10; // Kurze Pause und erneut prüfen
                    break;
                }

                setState(MacroState.SENDING_CHAT_MESSAGE);
                break;

            case SENDING_CHAT_MESSAGE:
                // GUI-Check: Wenn eine GUI offen ist, warten bis sie geschlossen wird
                if (isGuiOpen()) {
                    // Warte bis GUI geschlossen ist, dann fortfahren
                    tickcounter = 10; // Kurze Pause und erneut prüfen
                    break;
                }

                // Überprüfe ob alle Messages verwendet wurden und leere die Liste
                if (usedMessages.size() >= messages.length) {
                    usedMessages.clear();
                }

                String selectedMessage;

                // Wähle eine Message die noch nicht verwendet wurde
                do {
                    selectedMessage = messages[random.nextInt(messages.length)];
                } while (usedMessages.contains(selectedMessage));

                // Füge die ausgewählte Message zu usedMessages hinzu
                usedMessages.add(selectedMessage);
                currentMessage = selectedMessage;

                // Sende die Message in den Chat
                ChatUtil.sendMessage(selectedMessage);

                // Kurze Pause vor dem Webhook
                tickcounter = 2 * 20; // 2 Sekunden = 40 Ticks
                setState(MacroState.SENDING_WEBHOOK);
                break;

            case SENDING_WEBHOOK:
                // Sende Discord Webhook Nachricht
                DiscordNotifications.sendEmbedToWebhook("Guild Advertisement",
                        "Sent \"" + currentMessage + "\" in " + currentLocation,
                        65280, discordWebhook);

                // Warte 10 Minuten (600 Sekunden)
                tickcounter = 600 * 20; // 10 Minuten = 12000 Ticks
                setState(MacroState.WAITING_10_MINUTES);
                break;

            case WAITING_10_MINUTES:
                // Nach 10 Minuten wieder von vorne beginnen
                setState(MacroState.WARPING);
                break;

            case DISABLED:
                break;
        }
    }
}