package com.example.chatmod;

import net.luckperms.api.model.user.User;
import net.luckperms.api.LuckPerms;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class ChatPacketHandler {

    public static void handleChat(ServerPlayerEntity player, String message) {
        String prefix = getPlayerPrefix(player);
        String formattedMessage = prefix + player.getDisplayName().getString() + ": " + message;

        System.out.println("💬 " + formattedMessage); // For server console logging
        player.getServer().getPlayerList().getPlayers().forEach(p ->
                p.sendMessage(new StringTextComponent(formattedMessage), player.getUUID())); // Reverted to getUUID
    }

    public static void sendGlobalMessage(ServerPlayerEntity sender, String message) {
        String prefix = getPlayerPrefix(sender);
        System.out.println("🔎 Глобальне повідомлення: " + prefix + sender.getName().getString());

        sender.getServer().getPlayerList().getPlayers().forEach(player ->
                player.sendMessage(new StringTextComponent("§9[Глобальний чат] " + prefix + sender.getName().getString() + ": " + message), sender.getUUID()) // Reverted to getUUID
        );
    }

    public static void sendLocalMessage(ServerPlayerEntity sender, String message) {
        String prefix = getPlayerPrefix(sender);
        System.out.println("🔎 Локальне повідомлення: " + prefix + sender.getName().getString());

        sender.getServer().getPlayerList().getPlayers().stream()
                .filter(p -> p.distanceTo(sender) <= 50)
                .forEach(p ->
                        p.sendMessage(new StringTextComponent("§f[Локальний чат] " + prefix + sender.getName().getString() + ": " + message), sender.getUUID()) // Reverted to getUUID
                );
    }

    public static String getPlayerPrefix(ServerPlayerEntity player) {
        LuckPerms luckPerms = LuckPermsInitializer.getLuckPermsSafe().orElse(null);
        if (luckPerms == null) {
            System.err.println("🔴 LuckPerms API недоступний! Prefixes cannot be fetched.");
            return "";
        }

        User user = luckPerms.getUserManager().getUser(player.getUUID()); // Reverted to getUUID()
        if (user == null) {
            // Using getUUID() in the error message as well, for consistency with the call above
            System.err.println("🔴 Не вдалося отримати користувача LuckPerms для " + player.getName().getString() + " (UUID: " + player.getUUID().toString() + "). Можливо, гравець новий або дані ще не завантажені.");
            return "";
        }

        String prefix = user.getCachedData().getMetaData().getPrefix();
        if (prefix == null) {
            return "";
        }
        return prefix + " ";
    }
}