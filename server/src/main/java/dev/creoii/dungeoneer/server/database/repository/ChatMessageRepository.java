package dev.creoii.dungeoneer.server.database.repository;

import dev.creoii.dungeoneer.definitions.Message;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

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
                text TEXT NOT NULL
            )
        """)
        );
    }

    @Nullable
    public Message getById(long id) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM chat_messages
                WHERE id = :id
            """)
                .bind("id", id)
                .map((rs, _) -> new Message(
                    rs.getInt("id"),
                    rs.getInt("faction_id"),
                    rs.getInt("account_id"),
                    rs.getString("text")
                ))
                .findOne()
                .orElse(null)
        );
    }

    public Message create(Message message) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO chat_messages(faction_id, account_id, text)
                VALUES(:faction_id, :account_id, :text)
            """)
            .bind("faction_id", message.factionId())
            .bind("account_id", message.accountId())
            .bind("text", message.text())
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .one()
        );

        return new Message(id, message.factionId(), message.accountId(), message.text());
    }
}
