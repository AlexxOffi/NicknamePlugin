package de.offi.nickname;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import de.offi.nickname.command.NickCommand;
import de.offi.nickname.component.NicknameComponent;
import de.offi.nickname.event.PlayerEvent;

import javax.annotation.Nonnull;

public class NicknamePlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public NicknamePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        registerComponents();
        registerCommands();
        registerListeners();
    }

    private void registerComponents() {
        ComponentRegistry<EntityStore> registry = EntityStore.REGISTRY;

        ComponentType<EntityStore, NicknameComponent> nicknameType =
            registry.registerComponent(NicknameComponent.class, "nicknamemod:nickname", NicknameComponent.CODEC);

        NicknameComponent.setComponentType(nicknameType);
    }

    private void registerCommands() {
        this.getCommandRegistry().registerCommand(new NickCommand());
    }

    private void registerListeners() {
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerEvent::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, PlayerEvent::onChat);
    }
}