package de.offi.nickname.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;

import java.awt.*;

public class MessageUtil {

    private static final String PREFIX = "[Nickname] ";
    private static final Color PREFIX_COLOR = Color.MAGENTA;

    public static void send(CommandSender sender, String text, Color color) {
        sender.sendMessage(Message.join(
            Message.raw(PREFIX).color(PREFIX_COLOR),
            Message.raw(text).color(color)
        ));
    }

    public static void success(CommandSender sender, String text) {
        send(sender, text, Color.GREEN);
    }

    public static void error(CommandSender sender, String text) {
        send(sender, text, Color.RED);
    }

    public static void warning(CommandSender sender, String text) {
        send(sender, text, Color.ORANGE);
    }

    public static void info(CommandSender sender, String text) {
        send(sender, text, Color.WHITE);
    }

    public static void raw(CommandSender sender, String text, Color color) {
        sender.sendMessage(Message.raw(text).color(color));
    }

    public static Message combineMessages(CommandSender sender, Message... messages) {
        Message combined = Message.raw("");
        for (Message msg : messages) {
            combined = Message.join(combined, msg);
        }
        return combined;
    }

    public static void sendCombined(CommandSender sender, Message combined) {
        sender.sendMessage(Message.join(
           Message.raw(PREFIX).color(PREFIX_COLOR),
           combined
        ));
    }
}
