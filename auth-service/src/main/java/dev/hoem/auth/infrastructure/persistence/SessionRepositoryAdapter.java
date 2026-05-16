package dev.hoem.auth.infrastructure.persistence;

import dev.hoem.auth.domain.model.Session;
import dev.hoem.auth.domain.port.SessionRepository;
import org.springframework.stereotype.Component;

@Component
public class SessionRepositoryAdapter implements SessionRepository {

    private final SessionJpaRepository jpaRepository;

    public SessionRepositoryAdapter(SessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Session save(Session session) {
        jpaRepository.save(new SessionJpaEntity(
                session.getId(), session.getUserId(), session.getRefreshToken(),
                session.getExpiresAt(), session.getCreatedAt()));
        return session;
    }
}