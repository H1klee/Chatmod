package com.example.chatmod;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.entity.player.ServerPlayerEntity;

@Mod.EventBusSubscriber
public class ChatEventHandler {
    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        if (event.isCanceled()) {
            System.out.println("⚠️ Подія чату вже скасована, не відправляємо повторно.");
            return;
        }

        ServerPlayerEntity player = event.getPlayer();
        if (player == null) {
            System.err.println("🔴 Гравець не визначений, скасування події.");
            return;
        }

        String message = event.getMessage();
        if (message == null || message.trim().isEmpty()) {
            System.out.println("⚠️ Порожнє повідомлення, нічого не відправляємо.");
            return;
        }

        event.setCanceled(true); // ✅ Запобігання дублюванню стандартного чату
        System.out.println("✅ Скасовано стандартне відправлення чату!");

        String prefix = ChatPacketHandler.getPlayerPrefix(player);
        String formattedMessage = prefix + player.getName().getString() + ": " + message;

        if (message.startsWith("!")) {
            System.out.println("🔎 Визначено глобальне повідомлення");
            ChatPacketHandler.sendGlobalMessage(player, message.substring(1));
        } else {
            System.out.println("🔎 Визначено локальне повідомлення");
            ChatPacketHandler.sendLocalMessage(player, message);
        }
    }
}
