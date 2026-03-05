package edu.robertob.ayd2_p1_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Ayd2P1BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(Ayd2P1BackendApplication.class, args);
	}

}
