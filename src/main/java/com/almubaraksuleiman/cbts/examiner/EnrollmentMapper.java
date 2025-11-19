package com.almubaraksuleiman.cbts.examiner;

import com.almubaraksuleiman.cbts.dto.TestDto;
import com.almubaraksuleiman.cbts.examiner.dto.EnrollmentDto;
import com.almubaraksuleiman.cbts.examiner.model.Enrollment;
import com.almubaraksuleiman.cbts.examiner.model.Test;

public class EnrollmentMapper {


    public EnrollmentDto toDto(Enrollment enrollment) {
        if ( enrollment == null ) {
            return null;
        }

        EnrollmentDto enrollmentDto = new EnrollmentDto();

        enrollmentDto.setId( enrollment.getId() );
        enrollmentDto.setTest( enrollment.getTest() );
        enrollmentDto.setStudent( enrollment.getStudent() );

        return enrollmentDto;
    }


}
