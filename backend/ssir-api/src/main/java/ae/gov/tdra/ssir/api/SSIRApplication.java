package ae.gov.tdra.ssir.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "ae.gov.tdra.ssir")
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "ae.gov.tdra.ssir.core.repository")
@EntityScan(basePackages = "ae.gov.tdra.ssir.core.entity")


public class SSIRApplication {
    public static void main(String[] args) {
        SpringApplication.run(SSIRApplication.class, args);
    }
}