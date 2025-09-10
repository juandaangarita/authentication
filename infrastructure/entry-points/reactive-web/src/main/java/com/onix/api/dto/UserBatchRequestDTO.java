package com.onix.api.dto;

import java.util.Set;

public record UserBatchRequestDTO(
        Set<String> emails
) {
}
