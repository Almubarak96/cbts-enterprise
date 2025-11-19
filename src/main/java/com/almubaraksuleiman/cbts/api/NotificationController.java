//package com.almubaraksuleiman.cbts.api;
//
//
//import com.almubaraksuleiman.cbts.mail.service.EmailService;
//import com.almubaraksuleiman.cbts.mail.templates.TemplateService;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
///**
// * Endpoints used by Angular:
// *  - /preview -> render HTML string for preview
// *  - /send    -> send an email rendered from template
// *  - /download/pdf/{name} -> render template to PDF and download
// *
// * @author Almubarak Suleiman
// * @version 1.0
// * @since 2025
// */
//@CrossOrigin("http://localhost:4200")
//@RestController
//@RequestMapping("/api/admin/notifications")
//public class NotificationController {
//
//    private final EmailService emailService;
//    private final TemplateService templateService;
//
//    public NotificationController(EmailService emailService, TemplateService templateService) {
//        this.emailService = emailService;
//        this.templateService = templateService;
//    }
//
//    /**
//     * Preview rendered HTML (Angular can open in modal/iframe).
//     */
//    @PostMapping(value = "/preview", produces = MediaType.TEXT_HTML_VALUE)
//    public ResponseEntity<String> preview(@RequestBody SendEmailRequest req) {
//        String html = templateService.renderToHtml(req.getTemplateName(), req.getVariables());
//        return ResponseEntity.ok(html);
//    }
//
//    /**
//     * Send an email using a named template.
//     */
//    @PostMapping("/send")
//    public ResponseEntity<String> send(@RequestBody SendEmailRequest req) {
//        emailService.sendTemplate(req.getTo(), req.getSubject(), req.getTemplateName(), req.getVariables());
//        return ResponseEntity.ok("Email queued/sent");
//    }
//
//    /**
//     * Download PDF generated from a template (certificate/result).
//     */
//    @PostMapping(value = "/download/pdf/{name}", produces = MediaType.APPLICATION_PDF_VALUE)
//    public ResponseEntity<byte[]> downloadPdf(@PathVariable String name, @RequestBody(required = false) java.util.Map<String, Object> variables) {
//        byte[] pdf = templateService.renderToPdf(name, variables == null ? java.util.Map.of() : variables);
//        return ResponseEntity.ok()
//                .header("Content-Disposition", "attachment; filename=" + name + ".pdf")
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(pdf);
//    }
//}
