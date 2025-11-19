// ActivityData.java
package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityData {
    private String type;
    private String description;
    private String time;
    private String icon;
    private Map<String, Object> metadata;
}