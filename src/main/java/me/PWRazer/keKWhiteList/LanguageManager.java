package me.PWRazer.keKWhiteList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private static final String LANG_DIR = "lang";
    private static final String DEFAULT_LANGUAGE = "en";

    private final Path dataDirectory;
    private final Logger logger;
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, Component> cachedMessages = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public LanguageManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public void loadLanguage(String language) {
        File langDir = new File(dataDirectory.toFile(), LANG_DIR);
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        File langFile = new File(langDir, language + ".yml");
        if (!langFile.exists()) {
            saveDefaultLanguageFile(language);
        }

        try (FileReader reader = new FileReader(langFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> loaded = yaml.load(reader);
            messages.clear();
            cachedMessages.clear();
            if (loaded instanceof Map) {
                loaded.forEach((key, value) -> {
                    if (value != null) {
                        messages.put(key, value.toString());
                        cachedMessages.put(key, miniMessage.deserialize(value.toString()));
                    }
                });
            }
        } catch (IOException e) {
            logger.error("Failed to load language file {}.yml, falling back to default", language, e);
            if (!language.equals(DEFAULT_LANGUAGE)) {
                loadLanguage(DEFAULT_LANGUAGE);
            }
        }
    }

    private void saveDefaultLanguageFile(String lang) {
        File langFile = new File(dataDirectory.toFile(), LANG_DIR + "/" + lang + ".yml");
        Map<String, String> defaultMessages = new HashMap<>();

        if (lang.equals("ru")) {
            defaultMessages.put("no-whitelisted", "<white>Вас нет в Whitelist сервера. Подайте заявку на вступление в дискорде!\n<aqua>wiki.mkek.fun");
            defaultMessages.put("usage", "<red>Использование: /kekwhitelist <add|remove|on|off|list|reload> [игрок]");
            defaultMessages.put("no-permission", "<red>Нет прав!");
            defaultMessages.put("add-usage", "<red>Использование: /kekwhitelist add <игрок>");
            defaultMessages.put("invalid-username", "<red>Некорректное имя игрока! Используйте 3-16 символов (буквы, цифры, подчёркивания).");
            defaultMessages.put("already-whitelisted", "<yellow>{player} уже в whitelist!");
            defaultMessages.put("added", "<green>{player} добавлен в whitelist!");
            defaultMessages.put("remove-usage", "<red>Использование: /kekwhitelist remove <игрок>");
            defaultMessages.put("not-whitelisted", "<yellow>{player} не в whitelist!");
            defaultMessages.put("removed", "<green>{player} удалён из whitelist!");
            defaultMessages.put("already-enabled", "<yellow>Whitelist уже включён!");
            defaultMessages.put("enabled", "<green>Whitelist включён!");
            defaultMessages.put("already-disabled", "<yellow>Whitelist уже выключен!");
            defaultMessages.put("disabled", "<green>Whitelist выключен!");
            defaultMessages.put("list-header", "<green>Игроки в whitelist: ");
            defaultMessages.put("reloaded", "<green>Конфиг перезагружен!");
            defaultMessages.put("unknown-subcommand", "<red>Неизвестная подкоманда! Используйте: add, remove, on, off, list, reload");
        } else {
            defaultMessages.put("no-whitelisted", "<white>You are not on the server's whitelist. Apply to join on our Discord!\n<aqua>wiki.mkek.fun");
            defaultMessages.put("usage", "<red>Usage: /kekwhitelist <add|remove|on|off|list|reload> [player]");
            defaultMessages.put("no-permission", "<red>No permission!");
            defaultMessages.put("add-usage", "<red>Usage: /kekwhitelist add <player>");
            defaultMessages.put("invalid-username", "<red>Invalid username! Use 3-16 characters (letters, numbers, underscores).");
            defaultMessages.put("already-whitelisted", "<yellow>{player} is already whitelisted!");
            defaultMessages.put("added", "<green>{player} added to whitelist!");
            defaultMessages.put("remove-usage", "<red>Usage: /kekwhitelist remove <player>");
            defaultMessages.put("not-whitelisted", "<yellow>{player} is not whitelisted!");
            defaultMessages.put("removed", "<green>{player} removed from whitelist!");
            defaultMessages.put("already-enabled", "<yellow>Whitelist is already enabled!");
            defaultMessages.put("enabled", "<green>Whitelist enabled!");
            defaultMessages.put("already-disabled", "<yellow>Whitelist is already disabled!");
            defaultMessages.put("disabled", "<green>Whitelist disabled!");
            defaultMessages.put("list-header", "<green>Whitelisted players: ");
            defaultMessages.put("reloaded", "<green>Config reloaded!");
            defaultMessages.put("unknown-subcommand", "<red>Unknown subcommand! Use: add, remove, on, off, list, reload");
        }

        try (FileWriter writer = new FileWriter(langFile)) {
            new Yaml().dump(defaultMessages, writer);
        } catch (IOException e) {
            logger.error("Failed to save default language file {}.yml", lang, e);
        }
    }

    public Component getMessage(String key) {
        return cachedMessages.computeIfAbsent(key, k ->
                miniMessage.deserialize(messages.getOrDefault(k, "Message not found: " + k)));
    }
}