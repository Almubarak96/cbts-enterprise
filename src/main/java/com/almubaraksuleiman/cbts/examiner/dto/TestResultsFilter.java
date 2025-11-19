// TestResultsFilter.java
package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultsFilter {
    private String searchTerm;
    private String status; // "published", "draft"
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
