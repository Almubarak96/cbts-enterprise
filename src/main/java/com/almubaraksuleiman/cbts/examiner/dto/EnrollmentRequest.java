package com.almubaraksuleiman.cbts.examiner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {
    private List<Long> studentIds;
    private Long testId;
    private Boolean sendNotification = false;
    private String notificationMessage;
}

