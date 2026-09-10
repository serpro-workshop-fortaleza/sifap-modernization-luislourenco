package br.gov.sifap.socialprogram;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

interface SocialProgramRepository extends JpaRepository<SocialProgramEntity, String> {}

/** Adaptador JPA de {@link SocialProgramQuery}. */
@Service
class JpaSocialProgramQuery implements SocialProgramQuery {

    private final SocialProgramRepository socialProgramRepository;

    JpaSocialProgramQuery(SocialProgramRepository socialProgramRepository) {
        this.socialProgramRepository = socialProgramRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProgramRules> findByCode(String code) {
        return socialProgramRepository.findById(code).map(SocialProgramEntity::toRules);
    }
}
