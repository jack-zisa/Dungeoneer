package dev.creoii.dungeoneer.server.database.repository;

import dev.creoii.dungeoneer.definitions.Message;
import org.jdbi.v3.core.Jdbi;

public class ChatMessageRepository {
    private final Jdbi jdbi;

    public ChatMessageRepository(Jdbi jdbi) {
        this.jdbi = jdbi;

        // Initialize schema
        jdbi.useHandle(handle ->
            handle.execute("""
            CREATE TABLE IF NOT EXISTS chat_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                faction_id INTEGER NOT NULL,
                account_id INTEGER NOT NULL,
                text TEXT NOT NULL,
                flagged BOOLEAN NOT NULL
            )
        """)
        );
    }

    public void setFlagged(long messageId) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE chat_messages
            SET flagged = true,
                text = '*****'
            WHERE id = :id
        """)
                .bind("id", messageId)
                .execute()
        );
    }

    public Message create(Message message) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO chat_messages(faction_id, account_id, text, flagged)
                VALUES(:faction_id, :account_id, :text, :flagged)
            """)
            .bind("faction_id", message.factionId())
            .bind("account_id", message.accountId())
            .bind("text", message.text())
            .bind("flagged", message.flagged())
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .one()
        );

        return new Message(id, message.factionId(), message.accountId(), message.text(), message.flagged());
    }
}
