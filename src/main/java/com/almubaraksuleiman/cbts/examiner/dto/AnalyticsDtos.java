package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDtos {
    private List<Map<String, Object>> scoreDistribution;
    private List<Map<String, Object>> timeAnalysis;
    private List<Map<String, Object>> questionAnalysis;
    private List<Map<String, Object>> studentPerformance;
}
