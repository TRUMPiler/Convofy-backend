package com.convofy.convofy.Repository;

import com.convofy.convofy.Entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
}
