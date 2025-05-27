package com.example.chatmod;

import net.luckperms.api.LuckPerms;
import java.util.Optional;

public class LuckPermsInitializer {
    private static LuckPerms luckPerms = null;

    public static void initialize() {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object pluginManager = bukkitClass.getMethod("getPluginManager").invoke(null);
            if (pluginManager == null) {
                System.err.println("🔴 Bukkit PluginManager не ініціалізований.");
                return;
            }

            Object plugin = pluginManager.getClass().getMethod("getPlugin", String.class).invoke(pluginManager, "LuckPerms");
            if (plugin == null) {
                System.err.println("🔴 LuckPerms плагін не знайдено.");
                return;
            }

            // Ініціалізація LuckPerms API через рефлексію
            try {
                Class<?> lpProvider = Class.forName("net.luckperms.api.LuckPermsProvider");
                luckPerms = (LuckPerms) lpProvider.getMethod("get").invoke(null);
                System.out.println("✅ LuckPerms API підключено через рефлексію.");
            } catch (Exception e) {
                System.err.println("❌ Помилка підключення LuckPerms API: " + e.getMessage());
            }

        } catch (ClassNotFoundException e) {
            System.err.println("🔴 Bukkit або LuckPerms API не знайдено: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Помилка ініціалізації LuckPerms API: " + e);
        }
    }

    public static Optional<LuckPerms> getLuckPermsSafe() {
        return Optional.ofNullable(luckPerms);
    }
}
