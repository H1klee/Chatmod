package com.example.chatmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent; // Added
import net.minecraftforge.eventbus.api.SubscribeEvent; // Added

@Mod("chatmod") // Assuming 'chatmod' is your mod ID
public class ChatMod {

    public ChatMod() {
        System.out.println("✅ ChatMod Loaded!"); // Your original log message

        // Register ChatEventHandler (your original line)
        MinecraftForge.EVENT_BUS.register(new ChatEventHandler());

        // Register this class (ChatMod instance) to receive Forge lifecycle events
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("ChatMod: Registered itself for FML lifecycle events."); // Log to confirm registration
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        // This event is fired after the server has fully started.
        // It's a good place to initialize things that depend on other mods/plugins being ready.
        System.out.println("ChatMod: FMLServerStartedEvent received. Attempting to initialize LuckPerms connection...");
        LuckPermsInitializer.initialize();
    }
}
