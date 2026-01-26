package de.offi.nickname.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class NicknameComponent implements Component<EntityStore> {

    public static final BuilderCodec<NicknameComponent> CODEC = BuilderCodec
            .builder(NicknameComponent.class, NicknameComponent::new)
            .append(new KeyedCodec<String>("Nickname", Codec.STRING),
                    (component, nickname) -> component.nickname = nickname,
                    component -> component.nickname)
            .add()
            .build();

    private static ComponentType<EntityStore, NicknameComponent> COMPONENT_TYPE;


    private String nickname;


    public NicknameComponent() {
        this.nickname = null;
    }


    public NicknameComponent(@Nonnull String nickname) {
        this.nickname = nickname;
    }

    @Nullable
    public String getNickname() {
        return nickname;
    }

    public void setNickname(@Nullable String nickname) {
        this.nickname = nickname;
    }

    public boolean hasNickname() {
        return nickname != null && !nickname.isEmpty();
    }

    public void clearNickname() {
        this.nickname = null;
    }

    public static void setComponentType(@Nonnull ComponentType<EntityStore, NicknameComponent> componentType) {
        COMPONENT_TYPE = componentType;
    }

    @Nonnull
    public static ComponentType<EntityStore, NicknameComponent> getComponentType() {
        if (COMPONENT_TYPE == null) {
            throw new IllegalStateException("NicknameComponent not registered yet!");
        }
        return COMPONENT_TYPE;
    }

    @Override
    @Nonnull
    public Component<EntityStore> clone() {
        return new NicknameComponent(this.nickname);
    }
    
    @Override
    public String toString() {
        return "NicknameComponent{nickname='" + nickname + "'}";
    }
}
