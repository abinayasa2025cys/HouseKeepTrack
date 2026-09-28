package com.example.housekeeptrack;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI housekeepTrackOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HouseKeepTrack API")
                        .description("Hotel Housekeeping Task Assignment by Room Status — REST API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HouseKeepTrack")
                                .email("admin@housekeeptrack.com")));
    }
}
