package de.offi.nickname.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import de.offi.nickname.cache.NicknameCache;
import de.offi.nickname.component.NicknameComponent;

import java.util.UUID;


public class PlayerEvent {

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();

        assert player.getWorld() != null;

        player.getWorld().execute(() -> {
            Ref<EntityStore> playerRef = player.getReference();
            Store<EntityStore> store = player.getWorld().getEntityStore().getStore();

            assert playerRef != null;
            UUIDComponent uuidComponent = store.getComponent(playerRef, UUIDComponent.getComponentType());

            assert uuidComponent != null;
            UUID playerUUID = uuidComponent.getUuid();

            NicknameComponent nicknameComponent = store.getComponent(
                playerRef,
                NicknameComponent.getComponentType()
            );

            if (nicknameComponent != null && nicknameComponent.hasNickname()) {
                NicknameCache.setNickname(playerUUID, nicknameComponent.getNickname());
            }
        });
    }

    public static void onChat(PlayerChatEvent event) {
        event.setFormatter((playerRef, message) -> {
            String displayName = NicknameCache.getNickname(playerRef.getUuid());

            if (displayName == null) {
                displayName = playerRef.getUsername();
                return Message.raw(displayName + ": " + message);
            } else {
                return Message.raw(displayName + ": " + message);
            }
        });
    }
}
