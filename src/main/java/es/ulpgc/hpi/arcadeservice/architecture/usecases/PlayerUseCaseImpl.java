package es.ulpgc.hpi.arcadeservice.architecture.usecases;

import es.ulpgc.hpi.arcadeservice.architecture.domain.PlayerFactory;
import es.ulpgc.hpi.arcadeservice.architecture.ports.PlayerRepository;
import es.ulpgc.hpi.arcadeservice.architecture.model.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class PlayerUseCaseImpl implements PlayerUseCase {
    private final PlayerRepository repository;
    private final AtomicLong nextId = new AtomicLong(1);

    public PlayerUseCaseImpl(PlayerRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public List<Player> list() {
        return repository.findAll();
    }

    @Override
    public Optional<Player> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public synchronized Player create(String nickname) {
        if (nickname != null && !nickname.isBlank() && repository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("Nickname already in use");
        }
        var player = PlayerFactory.create(nickname, nextId::getAndIncrement);
        repository.save(player);
        return player;
    }

    @Override
    public synchronized Optional<Player> addPoints(long id, int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }
        return repository.findById(id).map(player -> {
            int total;
            try {
                total = Math.addExact(player.score(), points);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("Score is too large");
            }
            var updated = new Player(player.id(), player.nickname(), total);
            repository.save(updated);
            return updated;
        });
    }

    @Override
    public List<Player> leaderboard(Integer limit) {
        if (limit != null && limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        var ranking = repository.findAll().stream()
            .sorted(Comparator.comparingInt(Player::score).reversed()
                .thenComparing(Player::nickname, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Player::nickname)
                .thenComparingLong(Player::id));
        return (limit == null ? ranking : ranking.limit(limit)).toList();
    }
}
