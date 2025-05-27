package com.example.chatmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("chatmod")
public class ChatMod {
    public ChatMod() {
        System.out.println("✅ ChatMod Loaded!");

        // ✅ Реєстрація обробників
        MinecraftForge.EVENT_BUS.register(new ChatEventHandler());

        // ✅ Ініціалізація LuckPerms вручну
        LuckPermsInitializer.initialize();
    }
}
