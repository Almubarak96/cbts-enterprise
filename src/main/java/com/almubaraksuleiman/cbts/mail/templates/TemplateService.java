package com.almubaraksuleiman.cbts.mail.templates;

import java.util.Map;

/**
 * Service that loads templates (file system first, DB fallback), renders them with variables,
 * and returns rendered content.
 *
 * Supports render to HTML and render-to-PDF via separate methods.
 */
public interface TemplateService {

    /**
     * Render a named template to HTML.
     *
     * @param templateName logical name, e.g. "reset-password" (extension optional)
     * @param variables    key/value pairs available in template
     * @return rendered HTML
     */
    String renderToHtml(String templateName, Map<String, Object> variables);

    /**
     * Render a named template to PDF bytes.
     * This typically uses HTML template content then converts to PDF.
     *
     * @param templateName logical name, e.g. "certificate"
     * @param variables    key/value pairs available in template
     * @return generated PDF bytes
     */
    byte[] renderToPdf(String templateName, Map<String, Object> variables);

    /**
     * Return raw template content (for admin preview/edit).
     */
    String getRawTemplate(String templateName);

    /**
     * Upsert a template into DB (admin API).
     */
    void upsertTemplate(String name, String subject, String content);

    /**
     * Delete template from DB (does not affect filesystem).
     */
    void deleteTemplate(String name);
}
