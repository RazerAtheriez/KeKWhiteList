package me.PWRazer.keKWhiteList;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ConfigManager {
    private static final String CONFIG_FILE_NAME = "config.yml";
    private static final String WHITELIST_KEY = "whitelist";
    private static final String LANGUAGE_KEY = "language";
    private static final String WHITELISTED_PLAYERS_KEY = "whitelisted";

    private final Path dataDirectory;
    private final Logger logger;
    private boolean whitelistEnabled;
    private String language;
    private final Set<String> whitelistedPlayers;

    public ConfigManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.whitelistEnabled = true;
        this.language = "en";
        this.whitelistedPlayers = new HashSet<>();
    }

    public void loadConfig() {
        File configFile = new File(dataDirectory.toFile(), CONFIG_FILE_NAME);
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        try (FileReader reader = new FileReader(configFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(reader);
            if (config != null) {
                whitelistEnabled = (Boolean) config.getOrDefault(WHITELIST_KEY, true);
                language = (String) config.getOrDefault(LANGUAGE_KEY, "en");
                Object whitelisted = config.getOrDefault(WHITELISTED_PLAYERS_KEY, new ArrayList<>());
                whitelistedPlayers.clear();
                if (whitelisted instanceof List) {
                    ((List<?>) whitelisted).stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .map(String::toLowerCase)
                            .forEach(whitelistedPlayers::add);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load config.yml", e);
            whitelistedPlayers.clear();
        }
    }

    private void saveDefaultConfig() {
        File configFile = new File(dataDirectory.toFile(), CONFIG_FILE_NAME);
        configFile.getParentFile().mkdirs();

        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put(WHITELIST_KEY, true);
        defaultConfig.put(LANGUAGE_KEY, "en");
        defaultConfig.put(WHITELISTED_PLAYERS_KEY, new ArrayList<>(List.of("PWRazer")));

        try (FileWriter writer = new FileWriter(configFile)) {
            new Yaml().dump(defaultConfig, writer);
        } catch (IOException e) {
            logger.error("Failed to save default config.yml", e);
        }
    }

    public void saveConfig() {
        File configFile = new File(dataDirectory.toFile(), CONFIG_FILE_NAME);
        Map<String, Object> config = new HashMap<>();
        config.put(WHITELIST_KEY, whitelistEnabled);
        config.put(LANGUAGE_KEY, language);
        config.put(WHITELISTED_PLAYERS_KEY, new ArrayList<>(whitelistedPlayers));

        try (FileWriter writer = new FileWriter(configFile)) {
            new Yaml().dump(config, writer);
        } catch (IOException e) {
            logger.error("Failed to save config.yml", e);
        }
    }

    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    public void setWhitelistEnabled(boolean enabled) {
        this.whitelistEnabled = enabled;
    }

    public String getLanguage() {
        return language;
    }

    public Set<String> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }
}