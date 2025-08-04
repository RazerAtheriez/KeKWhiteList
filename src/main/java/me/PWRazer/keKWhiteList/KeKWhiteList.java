package me.PWRazer.keKWhiteList;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "kekwhitelist",
        name = "KeKWhiteList",
        version = "1.3.6",
        authors = {"PWRazer"},
        dependencies = {}
)
public class KeKWhiteList {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final ConfigManager configManager;
    private final WhitelistManager whitelistManager;
    private final LanguageManager languageManager;

    @Inject
    public KeKWhiteList(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.configManager = new ConfigManager(dataDirectory, logger);
        this.whitelistManager = new WhitelistManager();
        this.languageManager = new LanguageManager(dataDirectory, logger);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        configManager.loadConfig();
        languageManager.loadLanguage(configManager.getLanguage());
        server.getCommandManager().register("kekwhitelist", new WhitelistCommand(this, server, configManager, whitelistManager, languageManager), "kwl");
        logger.info("KeKWhiteList loaded successfully!");
    }

    @Subscribe
    public void onPlayerLogin(LoginEvent event) {
        if (configManager.isWhitelistEnabled() && !event.getPlayer().hasPermission("kekwhitelist.bypass")) {
            String username = event.getPlayer().getUsername().toLowerCase();
            if (!whitelistManager.isWhitelisted(username)) {
                event.getPlayer().disconnect(languageManager.getMessage("no-whitelisted"));
                logger.info("Player {} was denied access (not whitelisted).", username);
            }
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public WhitelistManager getWhitelistManager() {
        return whitelistManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public Logger getLogger() {
        return logger;
    }
}