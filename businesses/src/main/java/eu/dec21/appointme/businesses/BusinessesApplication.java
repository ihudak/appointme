package eu.dec21.appointme.businesses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
@EnableMethodSecurity
@ComponentScan(basePackages = {
		"eu.dec21.appointme.businesses",
		"eu.dec21.appointme.exceptions",
		"eu.dec21.appointme.common"
})
public class BusinessesApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusinessesApplication.class, args);
	}

}
