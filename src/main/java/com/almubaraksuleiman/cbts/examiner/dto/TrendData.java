// TrendData.java
package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendData {
    private String date;
    private Number value;
    private String label;
}