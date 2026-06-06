# Leaderboard API

This project features:
- multiple rate limiting strategies
- leaderboard of user's scores

Built with in memory database Redis

### Rate limiting algorithms

#### Fixed window

One key per client with window size set as expiration. Low memory usage but possible 2x bursts at the boundaries with technique below

1. Fire one request that will set expiration time.
2. Near end of expiration time fire maximum amount of requests.
3. Key expires at boundary.
4. Fire max amount of requests again.


### TODO

- [ ] convert rate limiter algorithms to lua scripts for atomic operations
 