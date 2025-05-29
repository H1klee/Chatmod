package com.example.chatmod;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ChatEventHandler {
    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        // System.out.println("ChatEventHandler: onPlayerChat EVENT RECEIVED!"); // Basic log, can be re-enabled for debugging

        if (event.isCanceled()) {
            // System.out.println("⚠️ ChatEvent was already cancelled. Ignoring.");
            return;
        }

        ServerPlayerEntity player = event.getPlayer();
        if (player == null) {
            System.err.println("🔴 Player in ServerChatEvent is null. Cannot process chat.");
            return;
        }

        String message = event.getMessage();
        if (message == null || message.trim().isEmpty()) {
            // System.out.println("⚠️ Empty message. Ignoring.");
            return;
        }

        event.setCanceled(true);
        // System.out.println("✅ Vanilla chat message cancelled by ChatEventHandler.");

        if (message.startsWith("!")) {
            // System.out.println("🔎 Global message detected by ChatEventHandler.");
            ChatPacketHandler.sendGlobalMessage(player, message.substring(1));
        } else {
            // System.out.println("🔎 Local message detected by ChatEventHandler, routing to sendLocalMessage.");
            ChatPacketHandler.sendLocalMessage(player, message);
        }
    }
}