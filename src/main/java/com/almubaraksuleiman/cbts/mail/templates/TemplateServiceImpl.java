package com.almubaraksuleiman.cbts.mail.templates;


import com.almubaraksuleiman.cbts.mail.exception.NotFoundException;
import com.almubaraksuleiman.cbts.mail.model.EmailTemplate;
import com.almubaraksuleiman.cbts.mail.repository.EmailTemplateRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * TemplateService implementation:
 * - DB stores only relative paths to template files
 * - Templates are read from filesystem
 * - Fallback to default template in DB if file not found
 */
@Service
public class TemplateServiceImpl implements TemplateService {

    private final TemplateEngine templateEngine;
    private final EmailTemplateRepository repo;
    private final String templatesDir;

    public TemplateServiceImpl(TemplateEngine templateEngine,
                               EmailTemplateRepository repo,
                               @Value("${cbt.templates.dir}") String templatesDir) {
        this.templateEngine = templateEngine;
        this.repo = repo;
        this.templatesDir = normalize(templatesDir);
    }

    @Override
    public String renderToHtml(String templateName, Map<String, Object> variables) {
        Context ctx = new Context();
        ctx.setVariables(variables != null ? variables : Map.of());

        // Let Thymeleaf resolve the template from the filesystem
        return templateEngine.process(stripExtension(templateName), ctx);
    }

    @Override
    public byte[] renderToPdf(String templateName, Map<String, Object> variables) {
        String html = renderToHtml(templateName, variables);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to render PDF for template: " + templateName, e);
        }
    }

    @Override
    public String getRawTemplate(String templateName) {
        // Lookup DB record to get relative path
        EmailTemplate tpl = repo.findByNameAndActiveTrue(stripExtension(templateName))
                .orElseThrow(() -> new NotFoundException("Template not found in DB: " + templateName));

        Path path = Path.of(templatesDir, tpl.getPath());
        if (Files.exists(path)) {
            try {
                return Files.readString(path, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read template file: " + path, e);
            }
        }
        throw new NotFoundException("Template file not found: " + path);
    }

    @Override
    public void upsertTemplate(String name, String subject, String relativePath) {
        EmailTemplate tpl = repo.findByNameAndActiveTrue(stripExtension(name))
                .orElseGet(EmailTemplate::new);
        tpl.setName(stripExtension(name));
        tpl.setSubject(subject);
        tpl.setPath(relativePath); // <-- store only path
        tpl.setActive(true);
        repo.save(tpl);
    }

    @Override
    public void deleteTemplate(String name) {
        repo.findByNameAndActiveTrue(stripExtension(name))
                .ifPresent(repo::delete);
    }

    // Helpers
    private static String stripExtension(String name) {
        if (name == null) return "";
        return name.toLowerCase().endsWith(".html") ? name.substring(0, name.length() - 5) : name;
    }

    private static String normalize(String dir) {
        if (dir == null) return "";
        if (!dir.endsWith("/") && !dir.endsWith("\\")) return dir + "/";
        return dir;
    }
}
