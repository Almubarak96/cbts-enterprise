package com.almubaraksuleiman.cbts.examiner;

import com.almubaraksuleiman.cbts.examiner.model.Examiner;
import com.almubaraksuleiman.cbts.examiner.model.Test;
import com.almubaraksuleiman.cbts.examiner.repository.ExaminerRepository;
import com.almubaraksuleiman.cbts.examiner.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    
    @Autowired
    private ExaminerRepository examinerRepository;

    @Autowired
    private TestRepository testRepository;
    
    public Examiner getCurrentExaminer() {
        String username = getCurrentUsername();
        String role = getCurrentRole();
        
        if ("ROLE_EXAMINER".equalsIgnoreCase(role)) {
            return examinerRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Examiner not found: " + username));
        }
        return null;
    }
    
    public boolean isAdmin() {
        return "ROLE_ADMIN".equalsIgnoreCase(getCurrentRole());
    }
    
    public boolean canAccessTest(Long testId) {
        if (isAdmin()) return true;
        
        Examiner currentExaminer = getCurrentExaminer();
        if (currentExaminer == null) return false;
        
        return testRepository.existsByIdAndCreatedBy(testId, currentExaminer);
    }
    
    public boolean canAccessTest(Test test) {
        if (isAdmin()) return true;
        
        Examiner currentExaminer = getCurrentExaminer();
        return currentExaminer != null && 
               test.getCreatedBy() != null && 
               test.getCreatedBy().getId().equals(currentExaminer.getId());
    }
    
    public void validateTestAccess(Long testId) {
        if (!canAccessTest(testId)) {
            throw new RuntimeException("Access denied: You don't have permission to access this test");
        }
    }
    
    public void validateTestAccess(Test test) {
        if (!canAccessTest(test)) {
            throw new RuntimeException("Access denied: You don't have permission to access this test");
        }
    }
    
    public void validateExaminerOrAdmin() {
        String role = getCurrentRole();
        if (!"ROLE_ADMIN".equalsIgnoreCase(role) && !"ROLE_EXAMINER".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access denied: Requires ADMIN or EXAMINER role");
        }
    }
    
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }
    
    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) return null;
        return auth.getAuthorities().iterator().next().getAuthority();
    }
}