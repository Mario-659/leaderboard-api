# Leaderboard API

This project features:

- multiple rate limiting strategies
- leaderboard of user's scores

Built with in memory database Redis

## Rate limiting algorithms

Selectable at startup via `rate-limiter.strategy`

Two algorithms are implemented **fixed window** and **sliding window log**. Each with a non-atomic and an atomic (Lua script-based) version, to compare a naive multi-command approach against one that guarantees atomicity via Redis scripting.

### Fixed window

One key per client with window size set as expiration. Low memory usage but possible 2x bursts at the boundaries with technique below

1. Fire one request that will set expiration time.
2. Near end of expiration time fire maximum amount of requests.
3. Key expires at boundary.
4. Fire max amount of requests again.

### Sliding window log

Each request timestamp is stored, and expired entries are pruned from the log on every request. More memory and compute intensive than fixed window, but provides exact rate limiting with no boundary bursts. Well suited for sensitive APIs and audit-critical endpoints.

## Resources

- https://redis.io/tutorials/rate-limiting-in-java-spring-with-redis/
- https://redis.io/tutorials/howtos/ratelimiting/
 