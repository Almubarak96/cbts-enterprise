// ChartData.java
package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartData {
    private String name;
    private Number value;
    private String color;
    private String description;
}