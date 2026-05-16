package dev.hoem.auth.domain.port;

import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository {

    Optional<UUID> findFirstHouseholdId(UUID userId);
}