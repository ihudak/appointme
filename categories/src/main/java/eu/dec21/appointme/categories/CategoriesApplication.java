package eu.dec21.appointme.categories;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableAsync
@ComponentScan(basePackages = {
		"eu.dec21.appointme.categories",
		"eu.dec21.appointme.exceptions",
		"eu.dec21.appointme.common"
})

public class CategoriesApplication {

	public static void main(String[] args) {
		SpringApplication.run(CategoriesApplication.class, args);
	}

}
