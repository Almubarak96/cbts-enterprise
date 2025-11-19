package com.almubaraksuleiman.cbts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/
@Configuration
public class ThymeleafConfig {

    @Value("${cbt.templates.dir}")
    private String templatesDir;

    @Bean
    @Primary
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("file:" + normalize(templatesDir));
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCacheable(false); // true in production
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine( SpringResourceTemplateResolver resolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        // this ensures Spring EL is used instead of OGNL
        engine.setEnableSpringELCompiler(true);
        return engine;
    }

    private String normalize(String dir) {
        if (dir == null) return "";
        if (!dir.endsWith("/") && !dir.endsWith("\\")) return dir + "/";
        return dir;
    }
}
