package edu.robertob.ayd2_p1_backend.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration

public class AppProperties {

    @Value("${app.frontendHost}")
    private String frontendHost;

    @Value("${backend.host}")
    private String backendHost;

//    @Value("${spring.mail.username}")
//    private String mailFrom;
}
