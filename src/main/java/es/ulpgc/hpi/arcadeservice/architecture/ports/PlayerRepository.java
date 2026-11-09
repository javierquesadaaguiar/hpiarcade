package es.ulpgc.hpi.arcadeservice.architecture.ports;

import es.ulpgc.hpi.arcadeservice.architecture.model.Player;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository {
    List<Player> findAll();

    Optional<Player> findById(long id);

    boolean existsByNickname(String nickname);

    void save(Player player);
}
