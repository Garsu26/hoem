package dev.hoem.auth.infrastructure.persistence;

import dev.hoem.auth.domain.port.MembershipRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MembershipRepositoryAdapter implements MembershipRepository {

    private final MembershipJpaRepository jpaRepository;

    public MembershipRepositoryAdapter(MembershipJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<UUID> findFirstHouseholdId(UUID userId) {
        List<UUID> ids = jpaRepository.findHouseholdIdsByUserId(userId);
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }
}