package com.almubaraksuleiman.cbts.examiner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeAnalysis {
    private String timeRange;
    private int count;
}