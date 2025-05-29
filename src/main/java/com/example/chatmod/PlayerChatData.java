package com.example.chatmod;

import java.util.UUID;

public class PlayerChatData {
    private final String prefix;
    private final String playerName; // From func_200200_C_ (getName)
    private final String displayName; // From func_145748_c_ (getDisplayName)
    private final UUID playerUUID;    // From func_110124_au (getUUID)

    public PlayerChatData(String prefix, String playerName, String displayName, UUID playerUUID) {
        this.prefix = prefix != null ? prefix : "";
        this.playerName = playerName != null ? playerName : "<unknown_name>";
        this.displayName = displayName != null ? displayName : "<unknown_display_name>";
        this.playerUUID = playerUUID; // Can be null if UUID reflection fails
    }

    public String getPrefix() {
        return prefix;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getFormattedPrefix() {
        return prefix.isEmpty() ? "" : prefix + " ";
    }
}