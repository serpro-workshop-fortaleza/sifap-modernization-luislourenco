package br.gov.sifap.beneficiary;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface BeneficiaryRepository extends JpaRepository<BeneficiaryEntity, Long> {

    List<BeneficiaryEntity> findByStatusOrderByCpfAsc(BeneficiaryStatus status);

    Optional<BeneficiaryEntity> findByCpf(String cpf);
}
