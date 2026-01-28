package de.offi.nickname.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import de.offi.nickname.HyNickname;
import de.offi.nickname.cache.NicknameCache;
import de.offi.nickname.component.NicknameComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.platform.PlayerAdapter;

import java.util.UUID;


public class PlayerEvent {

    static String format = "<prefix><username><suffix>: <message>";

    public PlayerEvent(){}

    
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
        if (!HyNickname.getInstance().getConfig().enableChatFormatter) {
            return;
        }
        LuckPerms lp = LuckPermsProvider.get();

        event.setFormatter((playerRef, message) -> {
            
            
            String displayName = NicknameCache.getNickname(playerRef.getUuid());

            PlayerAdapter<PlayerRef> playerAdapter = lp.getPlayerAdapter(PlayerRef.class);
            CachedMetaData metaData = playerAdapter.getMetaData(playerRef);
            String prefix = metaData.getPrefix() == null ? "" : metaData.getPrefix();
            String suffix = metaData.getSuffix() == null ? "" : metaData.getSuffix();
            System.out.println(prefix);
            

            if (displayName == null) {
                displayName = playerRef.getUsername();
            } 
            
        

            Component component = MiniMessage.miniMessage().deserialize(
                    format,
                    parse("prefix", prefix),
                    parse("suffix", suffix),
                    Placeholder.unparsed("username", displayName),
                    Placeholder.unparsed("message", message)
            );
            
           

            return toHytaleMessage(component);
        });
    }


    private static TagResolver parse(@TagPattern String key, String value) {
        boolean containsLegacyFormattingCharacter = value.indexOf(LegacyComponentSerializer.AMPERSAND_CHAR) != -1
                || value.indexOf(LegacyComponentSerializer.SECTION_CHAR) != -1;

        if (containsLegacyFormattingCharacter) {
            TextComponent component = LegacyComponentSerializer.legacyAmpersand().deserialize(value);
            return Placeholder.component(key, component);
        } else {
            return Placeholder.parsed(key, value);
        }
    }


    @SuppressWarnings("null")
    public static Message toHytaleMessage(Component component) {
        if (!(component instanceof TextComponent text)) {
            throw new UnsupportedOperationException("Unsupported component type: " + component.getClass());
        }

        Message message = Message.raw(text.content());

        TextColor color = text.color();
        if (color != null) {
            message.color(color.asHexString());
        }

        TextDecoration.State bold = text.decoration(TextDecoration.BOLD);
        if (bold != TextDecoration.State.NOT_SET) {
            message.bold(bold == TextDecoration.State.TRUE);
        }

        TextDecoration.State italic = text.decoration(TextDecoration.ITALIC);
        if (italic != TextDecoration.State.NOT_SET) {
            message.italic(italic == TextDecoration.State.TRUE);
        }

        ClickEvent clickEvent = text.clickEvent();
        if (clickEvent != null && clickEvent.action() == ClickEvent.Action.OPEN_URL) {
            message.link(clickEvent.value());
        }

        message.insertAll(text.children().stream()
                .map(PlayerEvent::toHytaleMessage)
                .toList()
        );
        return message;
    }
}
