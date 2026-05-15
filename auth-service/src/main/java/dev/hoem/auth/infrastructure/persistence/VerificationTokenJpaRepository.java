package dev.hoem.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface VerificationTokenJpaRepository extends JpaRepository<VerificationTokenJpaEntity, UUID> {
}