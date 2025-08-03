package me.PWRazer.keKWhiteList;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

            case "addtemp":
                if (!source.hasPermission("kekwhitelist.addtemp")) {
                    source.sendMessage(languageManager.getMessage("no-permission"));
                    return;
                }
                if (args.length != 3) {
                    source.sendMessage(languageManager.getMessage("addtemp-usage"));
                    return;
                }
                Duration duration;
                try {
                    duration = parseDuration(args[2]);
                } catch (IllegalArgumentException e) {
                    source.sendMessage(languageManager.getMessage("invalid-time-format"));
                    return;
                }
                String playerToAddTemp = args[1].toLowerCase();
                if (!isValidUsername(playerToAddTemp)) {
                    source.sendMessage(languageManager.getMessage("invalid-username"));
                    return;
                }
                if (whitelistManager.isWhitelisted(playerToAddTemp) || whitelistManager.isTempWhitelisted(playerToAddTemp)) {
                    source.sendMessage(languageManager.getMessage("already-whitelisted")
                            .replaceText(builder -> builder.matchLiteral("{player}").replacement(args[1])));
                    return;
                }
                whitelistManager.addTempPlayer(playerToAddTemp, duration);
                source.sendMessage(languageManager.getMessage("added-temp")
                        .replaceText(builder -> builder.matchLiteral("{player}").replacement(args[1]))
                        .replaceText(builder -> builder.matchLiteral("{time}").replacement(formatDuration(duration))));
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
                boolean removedPermanent = whitelistManager.removePlayer(playerToRemove);
                boolean removedTemp = whitelistManager.removeTempPlayer(playerToRemove);
                if (!removedPermanent && !removedTemp) {
                    source.sendMessage(languageManager.getMessage("not-whitelisted")
                            .replaceText(builder -> builder.matchLiteral("{player}").replacement(args[1])));
                    return;
                }
                if (removedPermanent) {
                    configManager.saveConfig();
                }
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
                if (!whitelistManager.getTempWhitelistedPlayers().isEmpty()) {
                    listMessage.append(miniMessage.serialize(languageManager.getMessage("list-temp-header")));
                    whitelistManager.getTempWhitelistedPlayers().forEach((name, expiry) ->
                            listMessage.append(name).append(" (until ").append(expiry.toString()).append("), "));
                    listMessage.setLength(listMessage.length() - 2);
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
            suggestions.addAll(List.of("add", "addtemp", "remove", "on", "off", "list", "reload"));
        } else if (args.length == 1) {
            suggestions.addAll(List.of("add", "addtemp", "remove", "on", "off", "list", "reload")
                    .stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("addtemp") || args[0].equalsIgnoreCase("remove"))) {
            suggestions.addAll(server.getAllPlayers()
                    .stream()
                    .map(player -> player.getUsername())
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("addtemp")) {
            suggestions.addAll(List.of("1h", "30m", "1d", "1h30m"));
        }

        return suggestions;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("kekwhitelist.use");
    }

    private Duration parseDuration(String input) {
        Pattern pattern = Pattern.compile("(\\d+)([smhd])");
        Matcher matcher = pattern.matcher(input);
        Duration duration = Duration.ZERO;

        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            if (value <= 0) {
                throw new IllegalArgumentException("Time value must be positive");
            }
            String unit = matcher.group(2);
            switch (unit) {
                case "s": duration = duration.plusSeconds(value); break;
                case "m": duration = duration.plusMinutes(value); break;
                case "h": duration = duration.plusHours(value); break;
                case "d": duration = duration.plusDays(value); break;
            }
        }
        if (duration.isZero()) {
            throw new IllegalArgumentException("Invalid time format");
        }
        return duration;
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        duration = duration.minusDays(days);
        long hours = duration.toHours();
        duration = duration.minusHours(hours);
        long minutes = duration.toMinutes();
        duration = duration.minusMinutes(minutes);
        long seconds = duration.getSeconds();

        StringBuilder formatted = new StringBuilder();
        if (days > 0) formatted.append(days).append("d ");
        if (hours > 0) formatted.append(hours).append("h ");
        if (minutes > 0) formatted.append(minutes).append("m ");
        if (seconds > 0) formatted.append(seconds).append("s ");
        return formatted.length() > 0 ? formatted.toString().trim() : "0s";
    }

    private boolean isValidUsername(String username) {
        return username != null && username.matches("[a-zA-Z0-9_]{3,16}");
    }
}