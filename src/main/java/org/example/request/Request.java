package org.example.request;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record Request(
        String username,
        String password
) {
}
