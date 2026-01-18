package eu.dec21.appointme.users;

import eu.dec21.appointme.users.roles.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableAsync
@ComponentScan(basePackages = {
		"eu.dec21.appointme.users",
		"eu.dec21.appointme.exceptions",
		"eu.dec21.appointme.common"
})
public class UsersApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsersApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(RoleRepository roleRepository) {
		return args -> {
			// Initialize roles if they do not exist
			if (roleRepository.count() == 0 || roleRepository.findByName("User").isEmpty()) {
				roleRepository.saveAll(RoleRepository.defaultRoles());
			}
		};
	}
}
