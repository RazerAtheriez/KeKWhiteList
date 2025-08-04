package me.PWRazer.keKWhiteList;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WhitelistCommand implements SimpleCommand {
    private final KeKWhiteList plugin;
    private final ProxyServer server;
    private final ConfigManager configManager;
    private final WhitelistManager whitelistManager;
    private final LanguageManager languageManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public WhitelistCommand(KeKWhiteList plugin, ProxyServer server, ConfigManager configManager,
                            WhitelistManager whitelistManager, LanguageManager languageManager) {
        this.plugin = plugin;
        this.server = server;
        this.configManager = configManager;
        this.whitelistManager = whitelistManager;
        this.languageManager = languageManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            source.sendMessage(languageManager.getMessage("usage"));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                if (!source.hasPermission("kekwhitelist.add")) {
                    source.sendMessage(languageManager.getMessage("no-permission"));
                    return;
                }
                if (args.length != 2) {
                    source.sendMessage(languageManager.getMessage("add-usage"));
                    return;
                }
                String playerToAdd = args[1].toLowerCase();
                if (!isValidUsername(playerToAdd)) {
                    source.sendMessage(languageManager.getMessage("invalid-username"));
                    return;
                }
                if (whitelistManager.isWhitelisted(playerToAdd)) {
                    source.sendMessage(languageManager.getMessage("already-whitelisted")
                            .replaceText(builder -> builder.matchLiteral("{player}").replacement(args[1])));
                    return;
                }
                whitelistManager.addPlayer(playerToAdd);
                configManager.saveConfig();
                source.sendMessage(languageManager.getMessage("added")
                        .replaceText(builder -> builder.matchLiteral("{player}").replacement(args[1])));
                break;

            case "remove":
                if (!source.hasPermission("kekwhitelist.remove")) {
                    source.sendMessage(languageManager.getMessage("no-permission"));
                    return;
                }
                if (args.length != 2) {
                    source.sendMessage(languageManager.getMessage("remove-usage"));
                    return;
                }
                String playerToRemove = args[1].toLowerCase();
                boolean removed = whitelistManager.removePlayer(playerToRemove);
                if (!removed) {
                    source.sendMessage(languageManager.getMessage("not-whitelisted")
                            .replaceText(builder -> builder.matchLiteral("{player}").replacement(args[1])));
                    return;
                }
                configManager.saveConfig();
                source.sendMessage(languageManager.getMessage("removed")
                        .replaceText(builder -> builder.matchLiteral("{player}").replacement(args[1])));
                break;

            case "on":
                if (!source.hasPermission("kekwhitelist.on")) {
                    source.sendMessage(languageManager.getMessage("no-permission"));
                    return;
                }
                if (configManager.isWhitelistEnabled()) {
                    source.sendMessage(languageManager.getMessage("already-enabled"));
                    return;
                }
                configManager.setWhitelistEnabled(true);
                configManager.saveConfig();
                source.sendMessage(languageManager.getMessage("enabled"));
                break;

            case "off":
                if (!source.hasPermission("kekwhitelist.off")) {
                    source.sendMessage(languageManager.getMessage("no-permission"));
                    return;
                }
                if (!configManager.isWhitelistEnabled()) {
                    source.sendMessage(languageManager.getMessage("already-disabled"));
                    return;
                }
                configManager.setWhitelistEnabled(false);
                configManager.saveConfig();
                source.sendMessage(languageManager.getMessage("disabled"));
                break;

            case "list":
                if (!source.hasPermission("kekwhitelist.list")) {
                    source.sendMessage(languageManager.getMessage("no-permission"));
                    return;
                }
                StringBuilder listMessage = new StringBuilder();
                listMessage.append(miniMessage.serialize(languageManager.getMessage("list-header")));
                if (!whitelistManager.getWhitelistedPlayers().isEmpty()) {
                    listMessage.append(String.join(", ", whitelistManager.getWhitelistedPlayers()));
                } else {
                    listMessage.append("None");
                }
                source.sendMessage(miniMessage.deserialize(listMessage.toString()));
                break;

            case "reload":
                if (!source.hasPermission("kekwhitelist.reload")) {
                    source.sendMessage(languageManager.getMessage("no-permission"));
                    return;
                }
                configManager.loadConfig();
                languageManager.loadLanguage(configManager.getLanguage());
                source.sendMessage(languageManager.getMessage("reloaded"));
                break;

            default:
                source.sendMessage(languageManager.getMessage("unknown-subcommand"));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> suggestions = new ArrayList<>();

        if (args.length == 0) {
            suggestions.addAll(List.of("add", "remove", "on", "off", "list", "reload"));
        } else if (args.length == 1) {
            suggestions.addAll(List.of("add", "remove", "on", "off", "list", "reload")
                    .stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            suggestions.addAll(server.getAllPlayers()
                    .stream()
                    .map(player -> player.getUsername())
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList()));
        }

        return suggestions;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("kekwhitelist.use");
    }

    private boolean isValidUsername(String username) {
        return username != null && username.matches("[a-zA-Z0-9_]{3,16}");
    }
}