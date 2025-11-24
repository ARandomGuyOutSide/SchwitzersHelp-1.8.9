package com.schwitzer.schwitzersHelp.macros;

import com.schwitzer.schwitzersHelp.config.SchwitzerHelpConfig;
import com.schwitzer.schwitzersHelp.features.MovePlayer;
import com.schwitzer.schwitzersHelp.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RevSlayerMacro implements Macro {
    private final SchwitzerHelpConfig config = SchwitzerHelpConfig.getInstance();
    private final Minecraft mc = Minecraft.getMinecraft();
    private MacroController.MacroState macroControllerState = MacroController.MacroState.DISABLED;

    private TimerUtil timerUtil;

    private boolean isInCave = false;
    private Entity nearestEntity = null;

    private MacroStates currentState;

    private enum MacroStates {
        CHECK_CURRENT_SLAYER_QUEST, WAIT_FOR_PHONE_MENU, WALKING_TO_CRYPT_GHOULS, FIND_NEXT_ZOMBIE, MOVE_TO_ZOMBIE, KILL_ZOMBIE,
        KILL_SLAYER
    }

    private Set<String> mobNames = new HashSet<>(Arrays.asList(
            "Crypt Ghoul"
    ));

    private Set<String> slayerName;

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        timerUtil = new TimerUtil();
        macroControllerState = MacroController.MacroState.ENABLED;
        currentState = MacroStates.CHECK_CURRENT_SLAYER_QUEST;

        if (mc.thePlayer != null) {
            slayerName = new HashSet<>(Arrays.asList(
                    "Spawned by: " + mc.thePlayer.getName()
            ));
        } else {
            slayerName = new HashSet<>();
        }

        if (checkIfPlayerIsInCave()) isInCave = true;
    }

    @Override
    public void onDisable() {
        macroControllerState = MacroController.MacroState.DISABLED;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public String getName() {
        return "Rev Slayer Macro";
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
    public void onTick(TickEvent.ClientTickEvent tickEvent) {
        if (mc.thePlayer == null) return;

        switch (currentState) {
            case CHECK_CURRENT_SLAYER_QUEST: {
                ChatUtil.formatedChatMessage("check if maaddox batphone is in inv");
                InventoryUtil.setHotbarSlotToItemName("maddox batphone");

                if (timerUtil.hasPassed(1000)) {
                    InventoryUtil.useItem();
                    timerUtil.reset();
                    currentState = MacroStates.WAIT_FOR_PHONE_MENU;
                }
                break;
            }

            case WAIT_FOR_PHONE_MENU: {
                if (timerUtil.hasPassed(5000)) {
                    try {
                        ItemStack stack = InventoryUtil.findItemAtIndex(34);
                        ChatUtil.formatedChatMessage("Item at 34 " + stack + " with lore " + InventoryUtil.getItemLore(stack));
                        if (stack.getItem() == Items.rotten_flesh) {
                            mc.thePlayer.closeScreen();
                            currentState = MacroStates.WALKING_TO_CRYPT_GHOULS;
                        } else {
                            onDisable();
                        }
                    } catch (NullPointerException ignore) {
                        // If no item found, still proceed to walking state
                        currentState = MacroStates.WALKING_TO_CRYPT_GHOULS;
                    }
                    timerUtil.reset();
                }
                break;
            }

            case WALKING_TO_CRYPT_GHOULS: {
                if (!isInCave) {
                    if (!MovePlayer.isMoving()) {
                        MovePlayer.movePlayerTo(mc.thePlayer.getPosition(), MovePlayer.getCryptWalkingCoords());
                    } else {
                        if (checkIfPlayerIsInCave()) {
                            isInCave = true;
                            ChatUtil.formatedChatMessage("Reached cave");
                            MovePlayer.stopMoving();
                            currentState = MacroStates.FIND_NEXT_ZOMBIE;
                        }
                    }
                } else {
                    ChatUtil.formatedChatMessage("Is in cave");
                    currentState = MacroStates.FIND_NEXT_ZOMBIE;
                }
                break;
            }

            case FIND_NEXT_ZOMBIE: {
                InventoryUtil.setHotbarSlotToItemName("sharp pooch sword");
                findClosestZombie();
                if (nearestEntity != null) {
                    ChatUtil.formatedChatMessage("Found zombie, switching to MOVE_TO_ZOMBIE state");
                    currentState = MacroStates.MOVE_TO_ZOMBIE;
                } else {
                    ChatUtil.formatedChatMessage("No zombie found, continuing search...");
                    // Stay in FIND_NEXT_ZOMBIE state to keep searching
                }
                break;
            }

            case MOVE_TO_ZOMBIE: {
                if (nearestEntity != null) {
                    // Check if the entity is still valid (not dead, still exists)
                    if (nearestEntity.isDead || !mc.theWorld.loadedEntityList.contains(nearestEntity)) {
                        ChatUtil.formatedChatMessage("Zombie died or disappeared, searching for new one");
                        nearestEntity = null;
                        currentState = MacroStates.FIND_NEXT_ZOMBIE;
                        break;
                    }

                    double distanceToZombie = EntityUtil.getDistanceToTarget(nearestEntity);

                    if (distanceToZombie > 3.5) {
                        ChatUtil.formatedChatMessage("Zombie is " + String.format("%.1f", distanceToZombie) + " blocks away, moving to entity");

                        // Only start moving if not already moving to avoid repeated calls
                        if (!MovePlayer.isMoving()) {
                            MovePlayer.movePlayerToEntity(nearestEntity);
                        }
                        // Stay in MOVE_TO_ZOMBIE state while moving
                    } else {
                        ChatUtil.formatedChatMessage("Zombie is close (" + String.format("%.1f", distanceToZombie) + " blocks), rotating to zombie");
                        MovePlayer.stopMoving(); // Stop any movement
                        RotatePlayerTo.lookAtEntity(nearestEntity, MovePlayer.NORMAL_ROTATION_SPEED, 0);
                        currentState = MacroStates.KILL_ZOMBIE;
                    }
                } else {
                    ChatUtil.formatedChatMessage("Lost target zombie, searching for new one");
                    currentState = MacroStates.FIND_NEXT_ZOMBIE;
                }
                break; // This was missing!
            }

            case KILL_ZOMBIE: {
                if (nearestEntity != null) {
                    // Check if zombie is still alive and close
                    if (nearestEntity.isDead || !mc.theWorld.loadedEntityList.contains(nearestEntity)) {
                        if(getSlayerEntity() != null)
                        {
                            currentState = MacroStates.KILL_SLAYER;
                        }
                        else
                        {
                            ChatUtil.formatedChatMessage("Zombie killed or disappeared, searching for new one");
                            nearestEntity = null;
                            currentState = MacroStates.FIND_NEXT_ZOMBIE;
                            break;
                        }
                    }

                    double distanceToZombie = EntityUtil.getDistanceToTarget(nearestEntity);

                    if (distanceToZombie > 4.5) {
                        // Zombie moved away, go back to moving
                        ChatUtil.formatedChatMessage("Zombie moved away, returning to movement state");
                        currentState = MacroStates.MOVE_TO_ZOMBIE;
                        break;
                    }

                    // Continue aiming and attacking
                    RotatePlayerTo.lookAtEntity(nearestEntity, MovePlayer.NORMAL_ROTATION_SPEED, 0);
                    moveAndAimAtZombie();
                } else {
                    ChatUtil.formatedChatMessage("No zombie to kill, searching for new one");
                    currentState = MacroStates.FIND_NEXT_ZOMBIE;
                }
                break;
            }
            case KILL_SLAYER:
            {
                nearestEntity = getSlayerEntity();

                if(nearestEntity == null) currentState = MacroStates.FIND_NEXT_ZOMBIE;

                RotatePlayerTo.lookAtEntity(nearestEntity, MovePlayer.NORMAL_ROTATION_SPEED, 0);
                moveAndAimAtZombie();
            }
        }
    }

    private Entity getSlayerEntity() {
        for(EntityLivingBase entity : EntityUtil.getEntities(slayerName))
        {
            ChatUtil.formatedChatMessage("Found slayer");
            return entity;
        }
        return null;
    }

    private void moveAndAimAtZombie() {
        InventoryUtil.setHotbarSlotToItemName("sharp pooch sword");

        // Attack every 500ms with a 50ms hold duration
        if(timerUtil.hasPassed(230)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
            timerUtil.reset();
        }

        // Release attack key after 50ms
        if(timerUtil.hasPassed(50)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
        }
    }

    private void findClosestZombie() {
        try {
            ChatUtil.formatedChatMessage("Finding closest zombie");
            List<EntityLivingBase> zombies = EntityUtil.getEntities(mobNames);

            if (!zombies.isEmpty()) {
                nearestEntity = zombies.get(0); // Assuming getEntities returns sorted by distance
            } else {
                nearestEntity = null;
                ChatUtil.formatedChatMessage("No zombies found in area");
            }
        } catch (Exception e) {
            ChatUtil.formatedChatMessage("Error finding zombie: " + e.getMessage());
            nearestEntity = null;
        }
    }

    private boolean checkIfPlayerIsInCave() {
        if (mc.thePlayer.posY < 62 && mc.thePlayer.posX > -162) {
            ChatUtil.formatedChatMessage("In Cave!");
            return true;
        }
        return false;
    }
}
