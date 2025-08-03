package me.PWRazer.keKWhiteList.api;

import me.PWRazer.keKWhiteList.ConfigManager;
import me.PWRazer.keKWhiteList.WhitelistManager;
import me.PWRazer.keKWhiteList.KeKWhiteList;

import java.util.Set;

/**
 * API для управления постоянным whitelist'ом в плагине KeKWhiteList.
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
     * Проверяет, находится ли игрок в постоянном whitelist'е.
     *
     * @param username Имя игрока.
     * @return true, если игрок в whitelist'е, иначе false.
     */
    public boolean isWhitelisted(String username) {
        if (username == null) return false;
        String lowercaseUsername = username.toLowerCase();
        return whitelistManager.isWhitelisted(lowercaseUsername);
    }

    /**
     * Добавляет игрока в постоянный whitelist.
     *
     * @param username Имя игрока.
     * @return true, если игрок успешно добавлен, false, если игрок уже в whitelist'е или имя некорректно.
     */
    public boolean addPlayer(String username) {
        if (username == null || !isValidUsername(username) || isWhitelisted(username)) {
            return false;
        }
        whitelistManager.addPlayer(username.toLowerCase());
        configManager.saveConfig();
        return true;
    }

    /**
     * Удаляет игрока из постоянного whitelist'а.
     *
     * @param username Имя игрока.
     * @return true, если игрок был удалён, false, если игрока не было в whitelist'е.
     */
    public boolean removePlayer(String username) {
        if (username == null) return false;
        String lowercaseUsername = username.toLowerCase();
        boolean removed = whitelistManager.removePlayer(lowercaseUsername);
        if (removed) {
            configManager.saveConfig();
        }
        return removed;
    }

    /**
     * Получает список игроков в постоянном whitelist'е.
     *
     * @return Неизменяемый набор имён игроков.
     */
    public Set<String> getWhitelistedPlayers() {
        return whitelistManager.getWhitelistedPlayers();
    }

    /**
     * Проверяет, включён ли whitelist.
     *
     * @return true, если whitelist включён, иначе false.
     */
    public boolean isWhitelistEnabled() {
        return configManager.isWhitelistEnabled();
    }

    /**
     * Устанавливает состояние whitelist'а (вкл/выкл).
     *
     * @param enabled true для включения, false для выключения.
     */
    public void setWhitelistEnabled(boolean enabled) {
        configManager.setWhitelistEnabled(enabled);
        configManager.saveConfig();
    }

    /**
     * Перезагружает конфигурацию и языковые файлы плагина.
     */
    public void reload() {
        configManager.loadConfig();
        plugin.getLanguageManager().loadLanguage(configManager.getLanguage());
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