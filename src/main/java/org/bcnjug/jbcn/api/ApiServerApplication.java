package org.bcnjug.jbcn.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@SpringBootApplication
public class ApiServerApplication {

//    @Bean
//    public Jackson2ObjectMapperBuilderCustomizer cosa(Jackson2ObjectMapperBuilder builder) {
//        return jacksonObjectMapperBuilder -> builder.serializationInclusion(JsonInclude.Include.NON_EMPTY);
//    }

    public static void main(String[] args) {
        SpringApplication.run(ApiServerApplication.class, args);
    }

}
