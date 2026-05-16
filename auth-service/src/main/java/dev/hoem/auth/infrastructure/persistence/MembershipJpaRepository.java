package dev.hoem.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MembershipJpaRepository extends JpaRepository<MembershipJpaEntity, UUID> {

    @Query("SELECT m.householdId FROM MembershipJpaEntity m "
            + "WHERE m.userId = :userId ORDER BY m.joinedAt ASC")
    List<UUID> findHouseholdIdsByUserId(@Param("userId") UUID userId);
}