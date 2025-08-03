package me.PWRazer.keKWhiteList.api;

import me.PWRazer.keKWhiteList.ConfigManager;
import me.PWRazer.keKWhiteList.WhitelistManager;
import me.PWRazer.keKWhiteList.KeKWhiteList;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

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
     * Проверяет, находится ли игрок в постоянном или временном whitelist'е.
     *
     * @param username Имя игрока.
     * @return true, если игрок в whitelist'е (постоянном или временном), иначе false.
     */
    public boolean isWhitelisted(String username) {
        if (username == null) return false;
        String lowercaseUsername = username.toLowerCase();
        return whitelistManager.isWhitelisted(lowercaseUsername) || whitelistManager.isTempWhitelisted(lowercaseUsername);
    }

    /**
     * Добавляет игрока в постоянный whitelist.
     *
     * @param username Имя игрока.
     * @return true, если игрок успешно добавлен, false, если игрок уже в whitelist'е.
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
     * Добавляет игрока во временный whitelist на указанную длительность.
     *
     * @param username Имя игрока.
     * @param duration Длительность действия временного whitelist'а.
     * @return true, если игрок успешно добавлен, false, если игрок уже в whitelist'е.
     */
    public boolean addTempPlayer(String username, Duration duration) {
        if (username == null || !isValidUsername(username) || duration == null || duration.isZero() || duration.isNegative() || isWhitelisted(username)) {
            return false;
        }
        whitelistManager.addTempPlayer(username.toLowerCase(), duration);
        return true;
    }

    /**
     * Удаляет игрока из постоянного или временного whitelist'а.
     *
     * @param username Имя игрока.
     * @return true, если игрок был удален, false, если игрока не было в whitelist'е.
     */
    public boolean removePlayer(String username) {
        if (username == null) return false;
        String lowercaseUsername = username.toLowerCase();
        boolean removedPermanent = whitelistManager.removePlayer(lowercaseUsername);
        boolean removedTemp = whitelistManager.removeTempPlayer(lowercaseUsername);
        if (removedPermanent) {
            configManager.saveConfig();
        }
        return removedPermanent || removedTemp;
    }

    /**
     * Получает список игроков в постоянном whitelist'е.
     *
     * @return Неизменяемый набор имен игроков.
     */
    public Set<String> getWhitelistedPlayers() {
        return whitelistManager.getWhitelistedPlayers();
    }

    /**
     * Получает список игроков во временном whitelist'е с их сроками действия.
     *
     * @return Неизменяемая карта с именами игроков и сроками действия.
     */
    public Map<String, Instant> getTempWhitelistedPlayers() {
        return whitelistManager.getTempWhitelistedPlayers();
    }

    /**
     * Проверяет, включен ли whitelist.
     *
     * @return true, если whitelist включен, иначе false.
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
     * Изменяет длительность временного whitelist'а для существующего игрока.
     *
     * @param username Имя игрока.
     * @param duration Новая длительность.
     * @return true, если длительность изменена, false, если игрок не в временном whitelist'е.
     */
    public boolean setTempWhitelistDuration(String username, Duration duration) {
        if (username == null || duration == null || duration.isZero() || duration.isNegative()) {
            return false;
        }
        if (!whitelistManager.isTempWhitelisted(username.toLowerCase())) {
            return false;
        }
        whitelistManager.setTempWhitelistDuration(username.toLowerCase(), duration);
        return true;
    }

    /**
     * Перезагружает конфигурацию и языковые файлы плагина.
     */
    public void reload() {
        configManager.loadConfig();
        plugin.getLanguageManager().loadLanguage(configManager.getLanguage());
    }

    private boolean isValidUsername(String username) {
        return username != null && username.matches("[a-zA-Z0-9_]{3,16}");
    }
}