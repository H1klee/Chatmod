package com.example.chatmod;

import net.luckperms.api.LuckPerms;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsInitializer {
    private static LuckPerms luckPerms = null;

    public static void initialize() {
        System.out.println("⚙️ Початок ініціалізації LuckPerms API через Bukkit Services Manager...");
        try {
            if (Bukkit.getServer() == null) {
                System.err.println("🔴 Bukkit.getServer() повернув null! Bukkit API ще не доступне.");
                luckPerms = null;
            } else {
                ServicesManager servicesManager = Bukkit.getServicesManager();
                if (servicesManager == null) {
                    System.err.println("🔴 Bukkit ServicesManager не знайдено! Можливо, Bukkit API ще не повністю готове.");
                    luckPerms = null;
                } else {
                    System.out.println("✅ Bukkit ServicesManager успішно отримано.");
                    RegisteredServiceProvider<LuckPerms> registration = servicesManager.getRegistration(LuckPerms.class);
                    if (registration == null) {
                        System.err.println("🔴 Реєстрацію сервісу LuckPerms не знайдено в Bukkit ServicesManager.");
                        System.err.println("ℹ️ Переконайтеся, що плагін LuckPerms (версія для Bukkit) встановлено, увімкнено та правильно зареєстровано свої сервіси.");
                        luckPerms = null;
                    } else {
                        System.out.println("✅ Реєстрацію сервісу LuckPerms знайдено.");
                        luckPerms = registration.getProvider();
                        if (luckPerms != null) {
                            System.out.println("✅ API LuckPerms успішно отримано від Bukkit plugin!");
                            System.out.println("Використовується реалізація: " + luckPerms.getClass().getName());
                        } else {
                            System.err.println("🔴 Постачальник LuckPerms API (provider) є null, незважаючи на наявність реєстрації.");
                        }
                    }
                }
            }
            // Corrected catch block below:
        } catch (NoClassDefFoundError e) { // Removed ClassNotFoundException from this specific multi-catch
            System.err.println("🔴 NoClassDefFoundError під час ініціалізації LuckPerms через Bukkit: " + e.getMessage());
            System.err.println("ℹ️ Це може вказувати на проблеми з доступом до класів Bukkit або LuckPerms API.");
            System.err.println("ℹ️ Перевірте, чи CatServer правильно надає доступ до API Bukkit для вашого Forge моду, та чи LuckPerms плагін активний.");
            e.printStackTrace(System.err);
            luckPerms = null;
        } catch (IllegalStateException e) {
            System.err.println("🔴 IllegalStateException під час доступу до сервісів Bukkit: " + e.getMessage());
            System.err.println("ℹ️ Можливо, Bukkit API викликається на неналежному етапі завантаження сервера (хоча FMLServerStartedEvent має бути безпечним).");
            e.printStackTrace(System.err);
            luckPerms = null;
        } catch (Exception e) {
            System.err.println("❌ Загальна помилка під час ініціалізації LuckPerms API через Bukkit: " + e.getMessage());
            e.printStackTrace(System.err);
            luckPerms = null;
        }

        if (luckPerms == null) {
            System.out.println("⚠️ LuckPerms API НЕ ініціалізовано через Bukkit Services Manager. Префікси не працюватимуть.");
        } else {
            System.out.println("✅ LuckPerms API схоже що успішно ініціалізовано через Bukkit Services Manager.");
        }
    }

    public static Optional<LuckPerms> getLuckPermsSafe() {
        return Optional.ofNullable(luckPerms);
    }
}