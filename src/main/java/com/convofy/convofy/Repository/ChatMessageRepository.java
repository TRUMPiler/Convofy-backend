package com.convofy.convofy.Repository;

import com.convofy.convofy.Entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    List<ChatMessage> findByChatroomIdOrderByTimestampDesc(UUID chatroomId, Pageable pageable);
}
