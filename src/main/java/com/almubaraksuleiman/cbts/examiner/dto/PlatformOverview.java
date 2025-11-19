package com.almubaraksuleiman.cbts.examiner.dto;

import com.almubaraksuleiman.cbts.examiner.model.StudentPerformance;
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
public class PlatformOverview {
    //private List<Map<String, Object>> testsByStatus;
    private List<ChartData> testsByStatus;

    private List<ChartData> studentPerformance;
    private List<ChartData> questionDistribution;
    private List<TrendData> enrollmentTrends;
    private List<ActivityData> recentActivity;
    private List<StudentPerformance> topPerformers;
    private List<ChartData> testPerformance;
    private List<ChartData> scoreDistribution;
}
