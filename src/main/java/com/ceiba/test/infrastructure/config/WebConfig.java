package com.ceiba.test.infrastructure.config;

import com.ceiba.test.application.dto.ApiDateFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final DateTimeFormatter DATE = DateTimeFormatter
            .ofPattern(ApiDateFormat.DATE.replace("yyyy", "uuuu"))
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldType(LocalDate.class, new Formatter<LocalDate>() {
            @Override
            public LocalDate parse(String text, Locale locale) throws ParseException {
                try {
                    return LocalDate.parse(text.trim(), DATE);
                } catch (DateTimeParseException ex) {
                    throw new ParseException("Fecha inválida, formato esperado dd-MM-yyyy: " + text, 0);
                }
            }

            @Override
            public String print(LocalDate date, Locale locale) {
                return DATE.format(date);
            }
        });
    }
}
