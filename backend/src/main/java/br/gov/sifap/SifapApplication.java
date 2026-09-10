package br.gov.sifap;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Monolito modular do SIFAP 2.0.
 *
 * <p>Uma unica unidade implantavel com fronteiras internas entre os contextos
 * {@code beneficiary}, {@code socialprogram}, {@code benefitcalculation}, {@code payment}
 * e {@code audit}. Ver {@code 02-modern-spec/bounded-contexts.md}.
 */
@SpringBootApplication
public class SifapApplication {

    public static void main(String[] args) {
        SpringApplication.run(SifapApplication.class, args);
    }

    /** Injetavel para tornar a data de processamento controlavel em teste. */
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
