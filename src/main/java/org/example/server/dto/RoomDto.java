package org.example.server.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RoomDto {
    private UUID id;
    private String name;
    private Instant createdAt;
    private boolean isPrivate;
    private List<String> users;
}
