package de.offi.nickname.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import de.offi.nickname.HyNickname;
import de.offi.nickname.cache.NicknameCache;
import de.offi.nickname.command.argument.StringArgumentType;
import de.offi.nickname.component.NicknameComponent;
import de.offi.nickname.util.MessageUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.PlayerSaveResult;
import net.luckperms.api.model.user.User;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.Color;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

public class NickCommand extends AbstractAsyncCommand {
    
   // private final RequiredArg<String> nicknameArg;
    private final LuckPerms api;
   // private final OptionalArg<String> resetArg;

    private final RequiredArg<String> firstArg;
    private final OptionalArg<String> secondArg;
    
    @SuppressWarnings("null")
    public NickCommand(LuckPerms api) {
        super("nick", "Change your nickname");

        this.requirePermission("nickname.use");
   
        this.api = api;


        firstArg = this.withRequiredArg("nickname", "Nickname", ArgTypes.STRING);
        secondArg = this.withOptionalArg("target", "nickname.arg.target", ArgTypes.STRING);

    }
    
  
    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext) {
        CommandSender sender = commandContext.sender();

        if (!(sender instanceof Player player)) {
            MessageUtil.error(sender, "Players only.");
            return CompletableFuture.completedFuture(null);
        }

        //String newNickname = commandContext.get(nicknameArg);
        String newNickname = firstArg.get(commandContext);
        String second = secondArg.provided(commandContext) ? secondArg.get(commandContext) : "";

        World world = player.getWorld();
        if (world == null) {
            MessageUtil.error(player, "Internal error.");
            return CompletableFuture.completedFuture(null);
        }

        world.execute(() -> {
            Store<EntityStore> store = world.getEntityStore().getStore();
            Ref<EntityStore> playerRef = player.getReference();

            if (!HyNickname.getInstance().getConfig().enableChatFormatter) {
                player.sendMessage(Message.raw("Nickname feature is disabled.").color(Color.RED));
                return;
            }

            assert playerRef != null;
            UUIDComponent uuidComponent = store.getComponent(playerRef, UUIDComponent.getComponentType());

            assert uuidComponent != null;
            UUID playerUUID = uuidComponent.getUuid();

            NicknameComponent nicknameComponent = store.getComponent(
                playerRef,
                NicknameComponent.getComponentType()
            );

            if (newNickname.equalsIgnoreCase("reset")) {
                if (second == ""){
                    if (nicknameComponent != null && nicknameComponent.hasNickname()) {
                        nicknameComponent.clearNickname();
                        api.getUserManager().savePlayerData(playerUUID, sender.getDisplayName());
                        NicknameCache.removeNickname(playerUUID);
                        MessageUtil.success(player, "Nickname reset.");
                    } else {
                        MessageUtil.warning(player, "No nickname set.");
                    }
                }else{
                    if (!player.hasPermission("nickname.admin.reset")) {
                        MessageUtil.error(player, "You don't have permission to reset nicknames for others.");
                        return;
                    }else{
                        Universe universe = Universe.get();
                        PlayerRef tPlayerRef = universe.getPlayerByUsername(second, NameMatching.STARTS_WITH_IGNORE_CASE);
                        Ref<EntityStore> tRef = tPlayerRef.getReference();
                        assert tPlayerRef != null;
                        UUIDComponent tUuidComponent = store.getComponent(tRef, UUIDComponent.getComponentType());

                        assert tUuidComponent != null;
                        UUID tPlayerUUID = tUuidComponent.getUuid();

                        NicknameComponent tNicknameComponent = store.getComponent(
                            tRef,
                            NicknameComponent.getComponentType()
                        );
                        if (tNicknameComponent != null && tNicknameComponent.hasNickname()){
                            tNicknameComponent.clearNickname();
                            api.getUserManager().savePlayerData(tPlayerUUID, tPlayerRef.getUsername());
                            NicknameCache.removeNickname(tPlayerUUID);
                            MessageUtil.success(player, "Nickname for " + second + " has been reset.");

                        }else{
                            MessageUtil.warning(player, "No nickname set for " + second + ".");
                        }

                    
                    }
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

            if (HyNickname.getInstance().getForbiddenNames().isForbidden(newNickname)) {
                MessageUtil.error(player, "This nickname is not allowed.");
                return;
            }


            User user = api.getUserManager().getUser(playerUUID);


            if (nicknameComponent == null) {
                nicknameComponent = new NicknameComponent(newNickname);
                store.addComponent(
                    playerRef,
                    NicknameComponent.getComponentType(),
                    nicknameComponent
                );
                NicknameCache.setNickname(playerUUID, newNickname);
                api.getUserManager().savePlayerData(playerUUID, newNickname)
                .thenAccept(result -> {
                    if (result.includes(PlayerSaveResult.Outcome.USERNAME_UPDATED)){

                    }
                });
                //api.getUserManager().savePlayerData(playerUUID, newNickname);
         
                MessageUtil.success(player, "Nickname set: " + newNickname);
            } else {
                nicknameComponent.setNickname(newNickname);
                NicknameCache.setNickname(playerUUID, newNickname);
                //api.getUserManager().savePlayerData(playerUUID, newNickname);
                api.getUserManager().savePlayerData(playerUUID, newNickname)
                .thenAccept(result -> {
                    if (result.includes(PlayerSaveResult.Outcome.USERNAME_UPDATED)){
                    
                    }
                });
               
                MessageUtil.sendCombined(player, MessageUtil.combineMessages(player, Message.raw("Nickname changed to: ").color(Color.GREEN), Message.raw(newNickname).color(Color.CYAN)));
      
            }
        });
        
        return CompletableFuture.completedFuture(null);
    }
}
