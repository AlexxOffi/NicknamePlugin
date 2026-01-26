package de.offi.nickname.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import de.offi.nickname.cache.NicknameCache;
import de.offi.nickname.command.argument.StringArgumentType;
import de.offi.nickname.component.NicknameComponent;
import de.offi.nickname.util.MessageUtil;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.Color;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NickCommand extends AbstractAsyncCommand {
    
    private final RequiredArg<String> nicknameArg;
    
    public NickCommand() {
        super("nick", "Change your nickname");

        this.nicknameArg = this.withRequiredArg("nickname", "New nickname (or 'reset' to clear)",
                StringArgumentType.INSTANCE
        );
    }
    
    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        CommandSender sender = commandContext.sender();

        if (!(sender instanceof Player player)) {
            MessageUtil.error(sender, "Players only.");
            return CompletableFuture.completedFuture(null);
        }

        String newNickname = commandContext.get(nicknameArg);
        
        World world = player.getWorld();
        if (world == null) {
            MessageUtil.error(player, "Internal error.");
            return CompletableFuture.completedFuture(null);
        }

        world.execute(() -> {
            Store<EntityStore> store = world.getEntityStore().getStore();
            Ref<EntityStore> playerRef = player.getReference();

            assert playerRef != null;
            UUIDComponent uuidComponent = store.getComponent(playerRef, UUIDComponent.getComponentType());

            assert uuidComponent != null;
            UUID playerUUID = uuidComponent.getUuid();

            NicknameComponent nicknameComponent = store.getComponent(
                playerRef,
                NicknameComponent.getComponentType()
            );

            if (newNickname.equalsIgnoreCase("reset")) {
                if (nicknameComponent != null && nicknameComponent.hasNickname()) {
                    nicknameComponent.clearNickname();
                    NicknameCache.removeNickname(playerUUID);
                    MessageUtil.success(player, "Nickname reset.");
                } else {
                    MessageUtil.warning(player, "No nickname set.");
                }
                return;
            }

            if (newNickname.length() > 16) {
                MessageUtil.error(player, "Nickname too long (max 16 characters).");
                return;
            }

            if (!newNickname.matches("[a-zA-Z0-9_]+")) {
                MessageUtil.error(player, "Invalid characters (letters, numbers, _ only).");
                return;
            }

            if (nicknameComponent == null) {
                nicknameComponent = new NicknameComponent(newNickname);
                store.addComponent(
                    playerRef,
                    NicknameComponent.getComponentType(),
                    nicknameComponent
                );
                NicknameCache.setNickname(playerUUID, newNickname);
                MessageUtil.success(player, "Nickname set: " + newNickname);
            } else {
                nicknameComponent.setNickname(newNickname);
                NicknameCache.setNickname(playerUUID, newNickname);
                MessageUtil.sendCombined(player, MessageUtil.combineMessages(player, Message.raw("Nickname changed to: ").color(Color.GREEN), Message.raw(newNickname).color(Color.CYAN)));
      
            }
        });
        
        return CompletableFuture.completedFuture(null);
    }
}
