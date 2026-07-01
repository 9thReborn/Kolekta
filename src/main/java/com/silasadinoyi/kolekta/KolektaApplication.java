package com.silasadinoyi.kolekta;

import com.silasadinoyi.kolekta.config.NombaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(NombaProperties.class)
@EnableScheduling
public class KolektaApplication {

	public static void main(String[] args) {
		SpringApplication.run(KolektaApplication.class, args);
	}

}
