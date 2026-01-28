package de.offi.nickname;

import com.hypixel.hytale.Main;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;

import de.offi.nickname.command.NickCommand;
import de.offi.nickname.component.NicknameComponent;
import de.offi.nickname.config.ConfigManager;
import de.offi.nickname.config.ForbiddenNamesConfig;
import de.offi.nickname.config.MainConfig;
import de.offi.nickname.event.PlayerEvent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import javax.annotation.Nonnull;

public class HyNickname extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    protected LuckPerms luckPerms;
    private static HyNickname instance;

    private ConfigManager configManager;
    private MainConfig config;
    private ForbiddenNamesConfig forbiddenNames;


    public HyNickname(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        
    }

    @Override
    protected void setup() {
        ComponentType<EntityStore, NicknameComponent> nicknameType = 
            getEntityStoreRegistry().registerComponent(
                NicknameComponent.class, 
                "nicknamemod:nickname", 
                NicknameComponent.CODEC
            );
        NicknameComponent.setComponentType(nicknameType);
        registerListeners();

        this.configManager = new ConfigManager(getDataDirectory());

        // Configs laden
        this.config = configManager.load("config.yml", MainConfig.class);
        this.forbiddenNames = configManager.load("forbiddennames.yml", ForbiddenNamesConfig.class);

        LOGGER.atInfo().log("Config loaded: Formatter enabled=%s",
            config.enableChatFormatter);
        LOGGER.atInfo().log("Loaded %d forbidden names, %d patterns",
            forbiddenNames.getNames().size(), forbiddenNames.getPatterns().size());
    }

    public void reloadConfigs() {
        this.config = configManager.load("config.yml", MainConfig.class);
        this.forbiddenNames = configManager.load("forbiddennames.yml", ForbiddenNamesConfig.class);
        LOGGER.atInfo().log("Configs reloaded!");
    }

    public MainConfig getConfig() { return config; }

    public ForbiddenNamesConfig getForbiddenNames() { 
        this.forbiddenNames = configManager.load("forbiddennames.yml", ForbiddenNamesConfig.class);
        return forbiddenNames; 
    }

    @Override
    protected void start() {
        this.luckPerms = LuckPermsProvider.get();
        registerCommands();
    }
    
    private void registerCommands() {
    
        this.getCommandRegistry().registerCommand(new NickCommand(luckPerms));
    }

    private void registerListeners() {
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerEvent::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, PlayerEvent::onChat);
    }

    public static HyNickname getInstance() { return instance; }

}