package edu.robertob.ayd2_p1_backend.core.models.entities.response;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * @param <T> the type of items in the page
 */
public record PagedResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}

