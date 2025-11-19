package com.almubaraksuleiman.cbts.api;


import com.almubaraksuleiman.cbts.mail.dto.TemplateRequest;
import com.almubaraksuleiman.cbts.mail.templates.TemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin endpoints: create/update/delete/fetch raw template contents.
 * Filesystem templates are edited directly on server; this controller handles DB-backed templates.
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/api/admin/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping("/{name}")
    public ResponseEntity<String> getRaw(@PathVariable String name) {
        return ResponseEntity.ok(templateService.getRawTemplate(name));
    }

    @PostMapping
    public ResponseEntity<String> upsert(@RequestBody TemplateRequest req) {
        templateService.upsertTemplate(req.getName(), req.getSubject(), req.getContent());
        return ResponseEntity.ok("Saved");
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<String> delete(@PathVariable String name) {
        templateService.deleteTemplate(name);
        return ResponseEntity.ok("Deleted");
    }

}
