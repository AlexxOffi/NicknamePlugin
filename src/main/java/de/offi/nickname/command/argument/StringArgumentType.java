package de.offi.nickname.command.argument;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;

import javax.annotation.Nonnull;


public class StringArgumentType extends ArgumentType<String> {

    public static final StringArgumentType INSTANCE = new StringArgumentType();

    private StringArgumentType() {
        super("string", Message.raw("<text>"), 1, "SuperPseudo");
    }


    @Override
    public String parse(@Nonnull String[] input, @Nonnull ParseResult result) {
        return input[0];
    }
}
