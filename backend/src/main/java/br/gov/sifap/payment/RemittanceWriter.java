package br.gov.sifap.payment;

import br.gov.sifap.shared.ReferencePeriod;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Grava o arquivo de remessa bancaria do ciclo.
 *
 * <p>REQ-029. O arquivo e o artefato comparado na execucao em sombra descrita no ADR-004:
 * enquanto a equivalencia com o legado nao estiver demonstrada, ele e gerado e
 * <strong>nao transmitido</strong>.
 */
@Component
class RemittanceWriter {

    private static final Logger log = LoggerFactory.getLogger(RemittanceWriter.class);

    private final Path outputDirectory;

    RemittanceWriter(@Value("${sifap.remittance.output-directory:./remittance}") Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    void write(ReferencePeriod period, List<RemittanceRecord> records) {
        if (records.isEmpty()) {
            return;
        }
        Path file = outputDirectory.resolve("REMESSA-%s.txt".formatted(period));
        try {
            Files.createDirectories(outputDirectory);
            Files.write(
                    file,
                    records.stream().map(RemittanceRecord::toFixedWidthLine).toList(),
                    StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException("Falha ao gravar o arquivo de remessa " + file, ex);
        }
        log.info("remessa gravada periodo={} registros={}", period, records.size());
    }
}
