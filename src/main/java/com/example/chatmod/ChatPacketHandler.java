package com.example.chatmod;

import net.luckperms.api.model.user.User;
import net.luckperms.api.LuckPerms;
import net.minecraft.entity.Entity; // Required for distanceTo reflection
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import org.bukkit.ChatColor; // Using Bukkit ChatColor for translation

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ChatPacketHandler {

    // --- getPlayerDataWithReflection, getMinecraftServerReflectively, getPlayerListReflectively ---
    // --- These three helper methods are IDENTICAL to the version in Turn 184. ---
    // --- Please ensure you copy them from that version if you're building this file manually. ---
    private static PlayerChatData getPlayerDataWithReflection(ServerPlayerEntity player) {
        UUID reflectedUUID = null;
        String reflectedPlayerName = "<unknown_name_init>";
        String reflectedDisplayName = "<unknown_display_init>";
        String initialPlayerNameForLog;
        try {
            initialPlayerNameForLog = player.getGameProfile().getName();
            if (initialPlayerNameForLog == null || initialPlayerNameForLog.isEmpty()) initialPlayerNameForLog = player.toString();
            reflectedPlayerName = initialPlayerNameForLog;
            reflectedDisplayName = initialPlayerNameForLog;
            reflectedUUID = player.getGameProfile().getId();
        } catch (Throwable t) {
            initialPlayerNameForLog = player.toString();
            System.err.println("🔴 Initial attempt to use getGameProfile() for player " + initialPlayerNameForLog + " failed: " + t.getClass().getName() + " - " + t.getMessage());
        }
        try {
            Method getUUIDMethod = player.getClass().getMethod("func_110124_au");
            Object uuidResult = getUUIDMethod.invoke(player);
            if (uuidResult instanceof UUID) reflectedUUID = (UUID) uuidResult;
            else System.err.println("🔴 SRG func_110124_au (getUUID) for " + initialPlayerNameForLog + " did not return UUID. Got: " + (uuidResult == null ? "null" : uuidResult.getClass().getName()) + ". Keeping previous UUID if available.");
            Method getNameMethod = player.getClass().getMethod("func_200200_C_");
            Object nameResult = getNameMethod.invoke(player);
            if (nameResult instanceof ITextComponent) reflectedPlayerName = ((ITextComponent) nameResult).getString();
            else System.err.println("🔴 SRG func_200200_C_ (getName) for " + initialPlayerNameForLog + " did not return ITextComponent. Got: " + (nameResult == null ? "null" : nameResult.getClass().getName()) + ". Keeping previous name if available.");
            Method getDisplayNameMethod = player.getClass().getMethod("func_145748_c_");
            Object displayNameResult = getDisplayNameMethod.invoke(player);
            if (displayNameResult instanceof ITextComponent) reflectedDisplayName = ((ITextComponent) displayNameResult).getString();
            else System.err.println("🔴 SRG func_145748_c_ (getDisplayName) for " + initialPlayerNameForLog + " did not return ITextComponent. Got: " + (displayNameResult == null ? "null" : displayNameResult.getClass().getName()) + ". Keeping previous display name if available.");
        } catch (NoSuchMethodException e) {
            System.err.println("🔴 SRG Reflection Error (NoSuchMethodException) on " + initialPlayerNameForLog + " for core player data: " + e.getMessage());
        } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            System.err.println("🔴 SRG Reflection Error (Access/Invocation) on " + initialPlayerNameForLog + " for core player data: " + e.getMessage());
            if (e instanceof java.lang.reflect.InvocationTargetException && e.getCause() != null) e.getCause().printStackTrace(System.err);
            else e.printStackTrace(System.err);
        } catch (Exception e) {
            System.err.println("🔴 General SRG Reflection Error on " + initialPlayerNameForLog + " for core player data: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        if (reflectedUUID == null) {
            System.err.println("🔴 CRITICAL: ALL attempts to get UUID for player " + initialPlayerNameForLog + " failed. Prefixes cannot be fetched.");
            return new PlayerChatData("", (reflectedPlayerName.startsWith("<unknown") || reflectedPlayerName.isEmpty() ? initialPlayerNameForLog : reflectedPlayerName), (reflectedDisplayName.startsWith("<unknown") || reflectedDisplayName.isEmpty() ? initialPlayerNameForLog : reflectedDisplayName), null);
        }
        if (reflectedPlayerName.startsWith("<unknown") || reflectedPlayerName.isEmpty()) reflectedPlayerName = initialPlayerNameForLog;
        if (reflectedDisplayName.startsWith("<unknown") || reflectedDisplayName.isEmpty()) reflectedDisplayName = reflectedPlayerName;
        LuckPerms luckPerms = LuckPermsInitializer.getLuckPermsSafe().orElse(null);
        String prefixStr = "";
        if (luckPerms != null) {
            User lpUser = luckPerms.getUserManager().getUser(reflectedUUID);
            if (lpUser != null) { prefixStr = lpUser.getCachedData().getMetaData().getPrefix(); if (prefixStr == null) prefixStr = "";}
            else System.err.println("🔴 Не вдалося отримати користувача LuckPerms для " + reflectedDisplayName + " (UUID: " + reflectedUUID.toString() + ").");
        } else { System.err.println("🔴 LuckPerms API недоступний! Cannot fetch prefix for " + reflectedDisplayName); }
        return new PlayerChatData(prefixStr, reflectedPlayerName, reflectedDisplayName, reflectedUUID);
    }

    private static MinecraftServer getMinecraftServerReflectively(ServerPlayerEntity playerEntity, String playerNameForContextLog) {
        MinecraftServer server = null;
        try {
            Method getServerMethod = playerEntity.getClass().getMethod("func_184102_h");
            Object serverResult = getServerMethod.invoke(playerEntity);
            if (serverResult instanceof MinecraftServer) server = (MinecraftServer) serverResult;
            else System.err.println("🔴 SRG func_184102_h (getServer) on " + playerNameForContextLog + " did not return MinecraftServer. Got: " + (serverResult == null ? "null" : serverResult.getClass().getName()));
        } catch (Exception e) {
            System.err.println("🔴 Reflection failed for func_184102_h (getServer) on player " + playerNameForContextLog + ": " + e.getMessage());
            e.printStackTrace(System.err);
        }
        return server;
    }

    private static PlayerList getPlayerListReflectively(MinecraftServer server, String forPlayerNameContext) {
        PlayerList playerList = null;
        if (server == null) { System.err.println("🔴 Cannot get PlayerList: MC Server null for " + forPlayerNameContext); return null; }
        try {
            Method getPlayerListMethod = server.getClass().getMethod("func_184103_al");
            Object playerListResult = getPlayerListMethod.invoke(server);
            if (playerListResult instanceof PlayerList) playerList = (PlayerList) playerListResult;
            else System.err.println("🔴 SRG func_184103_al (getPlayerList) for " + forPlayerNameContext + " no PlayerList. Got: " + (playerListResult == null ? "null" : playerListResult.getClass().getName()));
        } catch (Exception e) {
            System.err.println("🔴 Reflection fail func_184103_al (getPlayerList) for " + forPlayerNameContext + ": " + e.getMessage());
            e.printStackTrace(System.err);
        }
        return playerList;
    }

    private static List<ServerPlayerEntity> getPlayersFromPlayerListReflectively(PlayerList playerList, String forPlayerNameContext) {
        if (playerList == null) { System.err.println("🔴 No players: PlayerList null for " + forPlayerNameContext); return Collections.emptyList();}
        try {
            Method getPlayersMethod = net.minecraft.server.management.PlayerList.class.getMethod("func_181057_v");
            Object playersResult = getPlayersMethod.invoke(playerList);
            if (playersResult instanceof List) {
                @SuppressWarnings("unchecked") List<ServerPlayerEntity> players = (List<ServerPlayerEntity>) playersResult;
                return players;
            } else { System.err.println("🔴 SRG func_181057_v (getPlayers) for " + forPlayerNameContext + " no List. Got: " + (playersResult == null ? "null" : playersResult.getClass().getName())); }
        } catch (Exception e) {
            System.err.println("🔴 Reflection fail func_181057_v (getPlayers) for " + forPlayerNameContext + ": " + e.getMessage());
            e.printStackTrace(System.err);
        }
        return Collections.emptyList();
    }

    private static void sendMessageReflectively(ServerPlayerEntity targetPlayer, ITextComponent messageComponent, UUID senderUUID, String sendingPlayerNameForContext) {
        try {
            Method sendMessageMethod = targetPlayer.getClass().getMethod("sendMessage", UUID.class, ITextComponent[].class);
            sendMessageMethod.invoke(targetPlayer, senderUUID, new ITextComponent[]{messageComponent});
        } catch (Exception e) {
            String targetNameLog = "<unknown_target>";
            try {
                Object nameObj = targetPlayer.getClass().getMethod("func_200200_C_").invoke(targetPlayer);
                if (nameObj instanceof ITextComponent) targetNameLog = ((ITextComponent)nameObj).getString();
                else {
                    try { targetNameLog = targetPlayer.getGameProfile().getName(); } catch (Throwable ignored) {}
                }
                if (targetNameLog == null || targetNameLog.isEmpty() || targetNameLog.startsWith("<unknown")) targetNameLog = targetPlayer.toString();
            } catch (Throwable ignored) {
                targetNameLog = targetPlayer.toString();
            }
            System.err.println("🔴 Reflection failed for sendMessage(UUID, ITextComponent[]) to player " + targetNameLog + " (msg from " + sendingPlayerNameForContext + "): " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    public static void sendLocalMessage(ServerPlayerEntity sender, String messageContent) {
        PlayerChatData senderData = getPlayerDataWithReflection(sender);
        String nameToDisplay = senderData.getDisplayName();
        String rawFormattedMessage = "§a[Локальний чат] " + senderData.getFormattedPrefix() + nameToDisplay + "§f: §a" + messageContent;
        String finalMessageToSend = ChatColor.translateAlternateColorCodes('&', rawFormattedMessage);

        System.out.println("💬 LOCAL: " + finalMessageToSend);

        final UUID finalSenderUUID = (senderData.getPlayerUUID() == null) ? new UUID(0L,0L) : senderData.getPlayerUUID();
        if (new UUID(0L,0L).equals(finalSenderUUID)) System.err.println("WARN: Sender UUID is default zero in sendLocalMessage for " + nameToDisplay);

        MinecraftServer server = getMinecraftServerReflectively(sender, senderData.getPlayerName());
        PlayerList playerListObject = getPlayerListReflectively(server, senderData.getPlayerName());
        List<ServerPlayerEntity> allPlayers = getPlayersFromPlayerListReflectively(playerListObject, senderData.getPlayerName());

        if (allPlayers != null && !allPlayers.isEmpty()) {
            ITextComponent textComponent = new StringTextComponent(finalMessageToSend);
            allPlayers.stream()
                    .filter(p -> {
                        if (p == null) return false;
                        try {
                            // SRG for Entity.distanceTo(Entity) is func_70032_d(Lnet/minecraft/entity/Entity;)F
                            Method distanceToMethod = p.getClass().getMethod("func_70032_d", net.minecraft.entity.Entity.class);
                            Object distObj = distanceToMethod.invoke(p, sender);
                            if (distObj instanceof Float) {
                                return ((Float)distObj <= 50.0f);
                            }
                            return false; // distanceTo didn't return a float
                        } catch (Exception e) {
                            System.err.println("Error reflecting/calculating distance in sendLocalMessage for target " + p.toString() + ": " + e.getMessage());
                            return false;
                        }
                    })
                    .forEach(p -> sendMessageReflectively(p, textComponent, finalSenderUUID, senderData.getPlayerName()));
        } else {
            System.err.println("🔴 No players to send local chat message to for " + senderData.getPlayerName() + ". Message not sent.");
        }
    }

    public static void sendGlobalMessage(ServerPlayerEntity sender, String messageContent) {
        PlayerChatData senderData = getPlayerDataWithReflection(sender);
        String rawFormattedMessage = "§9[Глобальний чат] " + senderData.getFormattedPrefix() + senderData.getDisplayName() + "§f: §e" + messageContent;
        String finalMessageToSend = ChatColor.translateAlternateColorCodes('&', rawFormattedMessage);
        System.out.println("💬 GLOBAL: " + finalMessageToSend);

        final UUID finalSenderUUID = (senderData.getPlayerUUID() == null) ? new UUID(0L,0L) : senderData.getPlayerUUID();
        if (new UUID(0L,0L).equals(finalSenderUUID)) System.err.println("WARN: Sender UUID is default zero in sendGlobalMessage for " + senderData.getDisplayName());

        MinecraftServer mcServer = getMinecraftServerReflectively(sender, senderData.getPlayerName());
        PlayerList playerListObject = getPlayerListReflectively(mcServer, senderData.getPlayerName());
        List<ServerPlayerEntity> playersToSend = getPlayersFromPlayerListReflectively(playerListObject, senderData.getPlayerName());

        if (playersToSend != null && !playersToSend.isEmpty()) {
            ITextComponent textComponent = new StringTextComponent(finalMessageToSend);
            for (ServerPlayerEntity p : playersToSend) {
                sendMessageReflectively(p, textComponent, finalSenderUUID, senderData.getPlayerName());
            }
        } else {
            System.err.println("🔴 No players to send global message to for " + senderData.getPlayerName() + ". Message not sent.");
        }
    }

    // This handleChat method is called by ChatEventHandler for non-global messages.
    public static void handleChat(ServerPlayerEntity player, String messageContent) {
        sendLocalMessage(player, messageContent);
    }
}