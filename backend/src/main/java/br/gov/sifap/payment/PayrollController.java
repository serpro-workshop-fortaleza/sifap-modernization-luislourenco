package br.gov.sifap.payment;

import br.gov.sifap.shared.ReferencePeriod;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Superficie HTTP da geracao da folha.
 *
 * <p>REQ-001 — o periodo e sempre explicito no corpo da solicitacao. Nao existe endpoint
 * que derive o periodo da data corrente.
 */
@RestController
@RequestMapping("/api/v1/payrolls")
@Tag(name = "Folha de pagamento")
class PayrollController {

    private final PayrollGenerationService payrollGenerationService;

    PayrollController(PayrollGenerationService payrollGenerationService) {
        this.payrollGenerationService = payrollGenerationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gerar a folha de pagamento de um periodo de referencia")
    @ApiResponse(responseCode = "201", description = "Ciclo executado")
    @ApiResponse(responseCode = "400", description = "Periodo invalido")
    PayrollResponse generate(@Valid @RequestBody GeneratePayrollRequest request) {
        PayrollOutcome outcome = payrollGenerationService.generate(
                ReferencePeriod.parse(request.referencePeriod()));
        return PayrollResponse.from(outcome);
    }

    /** REQ-001 — formato {@code YYYYMM}, obrigatorio. */
    record GeneratePayrollRequest(
            @NotBlank @Pattern(regexp = "\\d{6}", message = "Periodo deve ter o formato YYYYMM")
                    String referencePeriod) {}

    record PayrollResponse(
            String referencePeriod,
            String outcome,
            int processed,
            int generated,
            int skipped,
            List<RejectionResponse> rejected) {

        static PayrollResponse from(PayrollOutcome outcome) {
            return new PayrollResponse(
                    outcome.period().toString(),
                    outcome.outcome().name(),
                    outcome.processed(),
                    outcome.generated(),
                    outcome.skipped(),
                    outcome.rejected().stream().map(RejectionResponse::from).toList());
        }
    }

    /** REQ-031 — a resposta expoe somente o documento mascarado. */
    record RejectionResponse(String maskedCpf, String reason, String detail) {

        static RejectionResponse from(PayrollOutcome.RejectedBeneficiary rejected) {
            return new RejectionResponse(
                    rejected.maskedCpf(), rejected.reason().name(), rejected.detail());
        }
    }
}
