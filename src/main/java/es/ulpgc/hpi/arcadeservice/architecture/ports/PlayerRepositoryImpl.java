package es.ulpgc.hpi.arcadeservice.architecture.ports;

import es.ulpgc.hpi.arcadeservice.architecture.model.Player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerRepositoryImpl implements PlayerRepository {
    private final ConcurrentHashMap<Long, Player> players = new ConcurrentHashMap<>();

    @Override
    public List<Player> findAll() {
        return List.copyOf(players.values());
    }

    @Override
    public Optional<Player> findById(long id) {
        return Optional.ofNullable(players.get(id));
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return players.values().stream()
            .anyMatch(player -> player.nickname().equalsIgnoreCase(nickname.trim()));
    }

    @Override
    public void save(Player player) {
        players.put(player.id(), player);
    }
}
