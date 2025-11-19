package com.almubaraksuleiman.cbts.examiner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsFilters {
    private String dateRange; // "today", "week", "month", "all"
    private String startDate;
    private String endDate;
}