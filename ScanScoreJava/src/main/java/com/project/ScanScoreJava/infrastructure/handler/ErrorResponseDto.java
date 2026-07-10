package com.project.ScanScoreJava.infrastructure.handler;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponseDto(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path
) {
}
