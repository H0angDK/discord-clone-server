package org.example.server.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class MessageDto {
    private UUID id;
    private String content;
    private UUID senderId;
    private UUID roomId;
    private Instant createdAt;
}
