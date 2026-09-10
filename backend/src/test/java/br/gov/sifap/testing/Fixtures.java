package br.gov.sifap.testing;

import br.gov.sifap.beneficiary.BeneficiarySnapshot;
import br.gov.sifap.beneficiary.BeneficiaryStatus;
import br.gov.sifap.shared.Money;
import br.gov.sifap.socialprogram.DiscountType;
import br.gov.sifap.socialprogram.ProgramRules;
import br.gov.sifap.socialprogram.ProgramStatus;
import br.gov.sifap.socialprogram.ProgramType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/** Fixtures compartilhadas pelos testes de dominio. */
public final class Fixtures {

    /** CPF sintetico com digito verificador valido. */
    public static final String VALID_CPF = "52998224725";

    public static final String OTHER_VALID_CPF = "16899535009";

    private Fixtures() {
    }

    public static BeneficiarySnapshotBuilder beneficiary() {
        return new BeneficiarySnapshotBuilder();
    }

    public static ProgramRulesBuilder program() {
        return new ProgramRulesBuilder();
    }

    public static final class BeneficiarySnapshotBuilder {

        private String rawCpf = VALID_CPF;
        private String fullName = "Maria Aparecida da Silva";
        private LocalDate birthDate = LocalDate.of(1980, 5, 12);
        private BeneficiaryStatus status = BeneficiaryStatus.ACTIVE;
        private String programCode = "0001";
        private Money familyIncome = Money.of("250.00");
        private int activeDependents = 0;
        private int regionCode = 1;
        private String federativeUnit = "AC";
        private Long nis = 12345678901L;
        private boolean documentationComplete = true;

        public BeneficiarySnapshotBuilder cpf(String value) {
            this.rawCpf = value;
            return this;
        }

        public BeneficiarySnapshotBuilder birthDate(LocalDate value) {
            this.birthDate = value;
            return this;
        }

        public BeneficiarySnapshotBuilder status(BeneficiaryStatus value) {
            this.status = value;
            return this;
        }

        public BeneficiarySnapshotBuilder programCode(String value) {
            this.programCode = value;
            return this;
        }

        public BeneficiarySnapshotBuilder familyIncome(String value) {
            this.familyIncome = Money.of(value);
            return this;
        }

        public BeneficiarySnapshotBuilder dependents(int value) {
            this.activeDependents = value;
            return this;
        }

        public BeneficiarySnapshotBuilder regionCode(int value) {
            this.regionCode = value;
            return this;
        }

        public BeneficiarySnapshotBuilder nis(Long value) {
            this.nis = value;
            return this;
        }

        public BeneficiarySnapshotBuilder documentationComplete(boolean value) {
            this.documentationComplete = value;
            return this;
        }

        public BeneficiarySnapshot build() {
            return new BeneficiarySnapshot(
                    rawCpf,
                    fullName,
                    birthDate,
                    status,
                    programCode,
                    familyIncome,
                    activeDependents,
                    regionCode,
                    federativeUnit,
                    nis,
                    documentationComplete);
        }
    }

    public static final class ProgramRulesBuilder {

        private String code = "0001";
        private String name = "Programa de Transferencia de Renda";
        private ProgramType type = ProgramType.ASSISTANCE;
        private ProgramStatus status = ProgramStatus.ACTIVE;
        private Money baseAmount = Money.of("600.00");
        private BigDecimal adjustmentFactor = BigDecimal.ZERO;
        private Money maximumIncome = Money.ZERO;
        private int minimumAge = 0;
        private int maximumAge = 0;
        private String eligibilityCode = "  ";
        private Set<DiscountType> discounts = Set.of();

        public ProgramRulesBuilder code(String value) {
            this.code = value;
            return this;
        }

        public ProgramRulesBuilder type(ProgramType value) {
            this.type = value;
            return this;
        }

        public ProgramRulesBuilder status(ProgramStatus value) {
            this.status = value;
            return this;
        }

        public ProgramRulesBuilder baseAmount(String value) {
            this.baseAmount = Money.of(value);
            return this;
        }

        public ProgramRulesBuilder adjustmentFactor(String value) {
            this.adjustmentFactor = new BigDecimal(value);
            return this;
        }

        public ProgramRulesBuilder maximumIncome(String value) {
            this.maximumIncome = Money.of(value);
            return this;
        }

        public ProgramRulesBuilder ageRange(int minimum, int maximum) {
            this.minimumAge = minimum;
            this.maximumAge = maximum;
            return this;
        }

        public ProgramRulesBuilder eligibilityCode(String value) {
            this.eligibilityCode = value;
            return this;
        }

        public ProgramRules build() {
            return new ProgramRules(
                    code,
                    name,
                    type,
                    status,
                    baseAmount,
                    adjustmentFactor,
                    maximumIncome,
                    minimumAge,
                    maximumAge,
                    eligibilityCode,
                    discounts);
        }
    }
}
