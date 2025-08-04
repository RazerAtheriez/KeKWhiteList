package me.PWRazer.keKWhiteList.api;

import me.PWRazer.keKWhiteList.ConfigManager;
import me.PWRazer.keKWhiteList.WhitelistManager;
import me.PWRazer.keKWhiteList.KeKWhiteList;

import java.util.Set;

/**
 * API для управления постоянным белым списком в плагине KeKWhiteList.
 * Все изменения белого списка сохраняются только в config.yml плагина KeKWhiteList.
 */
public class KeKWhiteListAPI {
    private final KeKWhiteList plugin;
    private final ConfigManager configManager;
    private final WhitelistManager whitelistManager;

    public KeKWhiteListAPI(KeKWhiteList plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.whitelistManager = plugin.getWhitelistManager();
    }

    /**
     * Проверяет, находится ли игрок в постоянном белом списке.
     *
     * @param username Имя игрока.
     * @return true, если игрок в белом списке, иначе false.
     */
    public boolean isWhitelisted(String username) {
        if (username == null) return false;
        String lowercaseUsername = username.toLowerCase();
        return whitelistManager.isWhitelisted(lowercaseUsername);
    }

    /**
     * Добавляет игрока в постоянный белый список.
     * Сохраняет изменения только в config.yml плагина KeKWhiteList.
     *
     * @param username Имя игрока.
     * @return true, если игрок успешно добавлен, false, если игрок уже в белом списке или имя некорректно.
     */
    public boolean addPlayer(String username) {
        if (username == null || !isValidUsername(username) || isWhitelisted(username)) {
            return false;
        }
        whitelistManager.addPlayer(username.toLowerCase());
        configManager.saveConfig(); // Сохраняет только в config.yml плагина KeKWhiteList
        plugin.getLogger().info("Добавлен игрок {} в whitelist через API.", username);
        return true;
    }

    /**
     * Удаляет игрока из постоянного белого списка.
     * Сохраняет изменения только в config.yml плагина KeKWhiteList.
     *
     * @param username Имя игрока.
     * @return true, если игрок был удалён, false, если игрока не было в белом списке.
     */
    public boolean removePlayer(String username) {
        if (username == null) return false;
        String lowercaseUsername = username.toLowerCase();
        boolean removed = whitelistManager.removePlayer(lowercaseUsername);
        if (removed) {
            configManager.saveConfig(); // Сохраняет только в config.yml плагина KeKWhiteList
            plugin.getLogger().info("Удалён игрок {} из whitelist через API.", username);
        }
        return removed;
    }

    /**
     * Получает список игроков в постоянном белом списке.
     *
     * @return Неизменяемый набор имён игроков.
     */
    public Set<String> getWhitelistedPlayers() {
        return whitelistManager.getWhitelistedPlayers();
    }

    /**
     * Проверяет, включён ли белый список.
     *
     * @return true, если белый список включён, иначе false.
     */
    public boolean isWhitelistEnabled() {
        return configManager.isWhitelistEnabled();
    }

    /**
     * Устанавливает состояние белого списка (вкл/выкл).
     * Сохраняет изменения только в config.yml плагина KeKWhiteList.
     *
     * @param enabled true для включения, false для выключения.
     */
    public void setWhitelistEnabled(boolean enabled) {
        configManager.setWhitelistEnabled(enabled);
        configManager.saveConfig();
        plugin.getLogger().info("Состояние whitelist изменено на {} через API.", enabled);
    }

    /**
     * Перезагружает конфигурацию и языковые файлы плагина KeKWhiteList.
     */
    public void reload() {
        configManager.loadConfig();
        plugin.getLanguageManager().loadLanguage(configManager.getLanguage());
        plugin.getLogger().info("Конфигурация KeKWhiteList перезагружена через API.");
    }

    /**
     * Проверяет, является ли имя игрока валидным (3-16 символов, только буквы, цифры и подчёркивания).
     *
     * @param username Имя игрока.
     * @return true, если имя валидно, иначе false.
     */
    private boolean isValidUsername(String username) {
        return username != null && username.matches("[a-zA-Z0-9_]{3,16}");
    }
}