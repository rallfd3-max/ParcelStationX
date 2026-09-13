package com.parcelstationx.api.auth;

import com.parcelstationx.model.User;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManager {
  private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
  private final SecureRandom random;
  private final Clock clock;
  private final Duration ttl;

  public SessionManager() {
    this(new SecureRandom(), Clock.systemUTC(), Duration.ofHours(8));
  }

  SessionManager(SecureRandom random, Clock clock, Duration ttl) {
    this.random = random;
    this.clock = clock;
    this.ttl = ttl;
  }

  public String create(User user) {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    Instant now = clock.instant();
    sessions.put(token, new Session(user, now, now.plus(ttl)));
    return token;
  }

  public Optional<User> findUser(String token) {
    if (token == null || token.isBlank()) {
      return Optional.empty();
    }
    Session session = sessions.get(token);
    if (session == null) {
      return Optional.empty();
    }
    if (!clock.instant().isBefore(session.expiresAt())) {
      sessions.remove(token, session);
      return Optional.empty();
    }
    return Optional.of(session.user());
  }

  public void invalidate(String token) {
    if (token != null) {
      sessions.remove(token);
    }
  }

  record Session(User user, Instant createdAt, Instant expiresAt) {}
}
