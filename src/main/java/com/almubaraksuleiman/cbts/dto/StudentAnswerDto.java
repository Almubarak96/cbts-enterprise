package com.almubaraksuleiman.cbts.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentAnswerDto {
    private Long studentExamQuestionId;
    private String answer;


}