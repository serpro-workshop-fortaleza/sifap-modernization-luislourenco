package br.gov.sifap.beneficiary;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA de {@link BeneficiaryQuery}. */
@Service
class JpaBeneficiaryQuery implements BeneficiaryQuery {

    private final BeneficiaryRepository beneficiaryRepository;

    JpaBeneficiaryQuery(BeneficiaryRepository beneficiaryRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiarySnapshot> findActiveOrderedByCpf() {
        return beneficiaryRepository.findByStatusOrderByCpfAsc(BeneficiaryStatus.ACTIVE).stream()
                .map(BeneficiaryEntity::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BeneficiarySnapshot> findByCpf(String cpf) {
        return beneficiaryRepository.findByCpf(cpf).map(BeneficiaryEntity::toSnapshot);
    }
}
