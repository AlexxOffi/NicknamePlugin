package de.offi.nickname.cache;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class NicknameCache {
    private static final ConcurrentHashMap<UUID, String> nicknames = new ConcurrentHashMap<>();

    public static void setNickname(UUID playerId, String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            nicknames.remove(playerId);
        } else {
            nicknames.put(playerId, nickname);
        }
    }

    public static String getNickname(UUID playerId) {
        return nicknames.get(playerId);
    }

    public static boolean hasNickname(UUID playerId) {
        return nicknames.containsKey(playerId);
    }

    public static void removeNickname(UUID playerId) {
        nicknames.remove(playerId);
    }

    public static void clear() {
        nicknames.clear();
    }
}
