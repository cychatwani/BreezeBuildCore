package dev.chirag45.breeze_core.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectUpsertRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 2000) String description
) {
}
