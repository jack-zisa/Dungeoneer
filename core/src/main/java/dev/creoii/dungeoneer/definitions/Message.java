package dev.creoii.dungeoneer.definitions;

public record Message(long messageId, long factionId, long accountId, String text, boolean flagged) {
}
