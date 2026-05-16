package dev.hoem.auth.domain.port;

import dev.hoem.auth.domain.model.Session;

public interface SessionRepository {

    Session save(Session session);
}