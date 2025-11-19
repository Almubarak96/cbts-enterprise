package com.almubaraksuleiman.cbts.api;

import com.almubaraksuleiman.cbts.dto.TestInstructions;
import com.almubaraksuleiman.cbts.examiner.service.TestInstructionsService;
import com.almubaraksuleiman.cbts.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/api/exam/instructions")
@RequiredArgsConstructor

public class TestInstructionsController {

    private final TestInstructionsService instructionsService;
    private final StudentRepository studentRepository;

    @GetMapping("/{examId}")
    public ResponseEntity<TestInstructions> getInstructions(@PathVariable String examId) {
        TestInstructions instructions = instructionsService.getInstructions(examId);
        return ResponseEntity.ok(instructions);
    }

/**
 * Save custom exam instructions
 *
 * @param instructions The custom instructions to save
 * @return ResponseEntity with success message
 */
    @PostMapping
    public ResponseEntity<Void> saveInstructions(@RequestBody TestInstructions instructions) {
        instructionsService.saveInstructions(instructions);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/{examId}")
    public ResponseEntity<Void> updateInstructions( @PathVariable Long examId, @RequestBody TestInstructions instructions) {
        instructionsService.updateInstructions(examId, instructions);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{examId}/status")
    public ResponseEntity<Map<String, Boolean>> hasUserReadInstructions(
            @PathVariable Long examId, Authentication authentication) {
        String username = authentication.getName();

        Long studentId = studentRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Student not found for username: " + username))
                .getId();
        boolean hasRead = instructionsService.hasUserReadInstructions(studentId, examId);
        return ResponseEntity.ok(Map.of("hasRead", hasRead));
    }

    @PostMapping("/{examId}/acknowledge")
    public ResponseEntity<Void> acknowledgeInstructions(
            @PathVariable Long examId, Authentication authentication) {
        String username = authentication.getName();

        Long studentId = studentRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Student not found for username: " + username))
                .getId();

        instructionsService.markInstructionsAsRead(studentId, examId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<TestInstructions>> getAllInstructions() {
        List<TestInstructions> allInstructions = instructionsService.getAllInstructions();
        return ResponseEntity.ok(allInstructions);
    }

    @DeleteMapping("/{examId}")
    public ResponseEntity<Void> deleteInstructions(@PathVariable Long examId) {
        instructionsService.deleteInstructions(examId);
        return ResponseEntity.ok().build();
    }
}