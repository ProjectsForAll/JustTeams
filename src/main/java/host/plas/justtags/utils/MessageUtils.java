package host.plas.justtags.utils;

import host.plas.justtags.JustTags;
import io.streamlined.bukkit.lib.thebase.lib.re2j.Matcher;
import io.streamlined.bukkit.lib.thebase.lib.re2j.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import tv.quaint.utils.MatcherUtils;

import java.awt.*;
import java.util.List;

public class MessageUtils {
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static Component colorizeComp(String message) {
        return getSerializer().deserialize(message);
    }

    public static LegacyComponentSerializer getSerializer() {
        return LegacyComponentSerializer.builder()
                .character('&')
                .extractUrls()
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();
    }

    public static String colorizeHard(String message) {
        // hex format is &#RRGGBB
        // a message can contain more than just hex color codes
        String colored = colorize(message);

        List<String> regexes = List.of(
                "([&][#]([A-Fa-f0-9]{6}))",
                "([{][#]([A-Fa-f0-9]{6})[}])",
                "([#]([A-Fa-f0-9]{6}))",
                "([<][#]([A-Fa-f0-9]{6})[>])"
        );

//        String hexRegexMain = "([&][#]([A-Fa-f0-9]{6}))";
//        String hexRegexCmi = "([{][#]([A-Fa-f0-9]{6})[}])";
//        String hexRegexCmiAlt = "([#]([A-Fa-f0-9]{6}))";
//        String hexRegexOthers = "([<][#]([A-Fa-f0-9]{6})[>])";

        for (String hexRegex : regexes) {
            colored = replaceHex(colored, hexRegex);
        }

        return colored;
    }

    public static String replaceHex(String message, String hexRegex) {
        Matcher matcher = MatcherUtils.matcherBuilder(hexRegex, message);
        List<String[]> groups = MatcherUtils.getGroups(matcher, 2);
        for (String[] group : groups) {
            String hex = group[1];
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            net.md_5.bungee.api.ChatColor color = net.md_5.bungee.api.ChatColor.of(new Color(r, g, b));

            message = message.replace(group[0], color.toString());
        }

        return message;
    }

    public static BaseComponent[] color(String message) {
        message = colorize(message);

        // hex format is &#RRGGBB
        // a message can contain more than just hex color codes
        ComponentBuilder builder = new ComponentBuilder();
        Matcher matcher = Pattern.compile("(&#([A-Fa-f0-9]{6}))").matcher(message);
        int lastEnd = 0;
        // Loop through all found color codes
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Append text before the color code as a new component
            if (start > lastEnd) {
                builder.append(new TextComponent(message.substring(lastEnd, start)));
            }

            // Extract the color and apply it to the next component
            String hex = matcher.group(1);
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            TextComponent coloredText = new TextComponent(message.substring(end));
            coloredText.setColor(net.md_5.bungee.api.ChatColor.of(new java.awt.Color(r, g, b)));

            // Reset lastEnd to the end of the current match
            lastEnd = end;

            // Append the colored text
            builder.append(coloredText);
            break; // Break after applying the first color, assuming one color per message
        }

        // Append any remaining text after the last color code
        if (lastEnd < message.length()) {
            builder.append(new TextComponent(message.substring(lastEnd)));
        }

        return builder.create();
    }

    public static void logMessage(String message) {
        CommandSender sender = Bukkit.getConsoleSender();
        if (sender != null) {
            sender.sendMessage(colorize(message));
        } else {
            System.out.println(colorize(message));
        }
    }

    public static String getInfoLogPrefix() {
        return colorize("&7[&3" + JustTags.getInstance().getName() + "&7] &f");
    }

    public static String getErrorLogPrefix() {
        return colorize("&7[&3" + JustTags.getInstance().getName() + "&7] &c");
    }

    public static String getWarningLogPrefix() {
        return colorize("&7[&3" + JustTags.getInstance().getName() + "&7] &e");
    }

    public static String getDebugLogPrefix() {
        return colorize("&7[&3" + JustTags.getInstance().getName() + "&7] &7[&cDEBUG&7] &b");
    }

    public static void logInfo(String message) {
        logMessage(getInfoLogPrefix() + message);
    }

    public static void logError(String message) {
        logMessage(getErrorLogPrefix() + message);
    }

    public static void logWarning(String message) {
        logMessage(getWarningLogPrefix() + message);
    }

    public static void logDebug(String message) {
        logMessage(getDebugLogPrefix() + message);
    }

    public static void logInfo(String message, Object... args) {
        logInfo(String.format(message, args));
    }

    public static void logError(String message, Object... args) {
        logError(String.format(message, args));
    }

    public static void logWarning(String message, Object... args) {
        logWarning(String.format(message, args));
    }

    public static void logDebug(String message, Object... args) {
        logDebug(String.format(message, args));
    }

    public static void logInfo(Throwable throwable) {
        logInfo(throwable.getMessage());

        for (StackTraceElement element : throwable.getStackTrace()) {
            logInfo(element.toString());
        }

        if (throwable.getCause() != null) {
            logInfo(throwable.getCause());
        }
    }

    public static void logError(Throwable throwable) {
        logError(throwable.getMessage());

        for (StackTraceElement element : throwable.getStackTrace()) {
            logError(element.toString());
        }

        if (throwable.getCause() != null) {
            logError(throwable.getCause());
        }
    }

    public static void logWarning(Throwable throwable) {
        logWarning(throwable.getMessage());

        for (StackTraceElement element : throwable.getStackTrace()) {
            logWarning(element.toString());
        }

        if (throwable.getCause() != null) {
            logWarning(throwable.getCause());
        }
    }

    public static void logDebug(Throwable throwable) {
        logDebug(throwable.getMessage());

        for (StackTraceElement element : throwable.getStackTrace()) {
            logDebug(element.toString());
        }

        if (throwable.getCause() != null) {
            logDebug(throwable.getCause());
        }
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    public void sendMessage(CommandSender sender, String message, Object... args) {
        sender.sendMessage(getInfoLogPrefix() + colorize(String.format(message, args)));
    }
}
