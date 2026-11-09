package es.ulpgc.hpi.arcadeservice.architecture;

import es.ulpgc.hpi.arcadeservice.architecture.ports.PlayerRepositoryImpl;
import es.ulpgc.hpi.arcadeservice.architecture.usecases.PlayerUseCaseImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreAndLeaderboardTest {
    private final PlayerUseCaseImpl useCase = new PlayerUseCaseImpl(new PlayerRepositoryImpl());

    private List<String> nicknames(Integer limit) {
        return useCase.leaderboard(limit).stream().map(p -> p.nickname()).toList();
    }

    @Test
    void addsPointsToTotal() {
        var ada = useCase.create("Ada");
        assertEquals(10, useCase.addPoints(ada.id(), 10).orElseThrow().score());
        assertEquals(15, useCase.addPoints(ada.id(), 5).orElseThrow().score());
        assertEquals(15, useCase.findById(ada.id()).orElseThrow().score());
    }

    @Test
    void unknownPlayerIsEmpty() {
        assertTrue(useCase.addPoints(99, 10).isEmpty());
    }

    @Test
    void rejectsZeroAndNegativePoints() {
        var ada = useCase.create("Ada");
        assertThrows(IllegalArgumentException.class, () -> useCase.addPoints(ada.id(), 0));
        assertThrows(IllegalArgumentException.class, () -> useCase.addPoints(ada.id(), -3));
        assertEquals(0, useCase.findById(ada.id()).orElseThrow().score());
    }

    @Test
    void leaderboardOrdersByScoreThenNickname() {
        var carl = useCase.create("Carl");
        var bea = useCase.create("Bea");
        var ada = useCase.create("Ada");
        var dan = useCase.create("Dan");
        useCase.addPoints(carl.id(), 50);
        useCase.addPoints(bea.id(), 50);
        useCase.addPoints(dan.id(), 70);
        assertEquals(List.of("Dan", "Bea", "Carl", "Ada"), nicknames(null));
    }

    @Test
    void leaderboardAppliesLimit() {
        useCase.addPoints(useCase.create("Ada").id(), 1);
        useCase.addPoints(useCase.create("Bea").id(), 2);
        useCase.create("Carl");
        assertEquals(List.of("Bea", "Ada"), nicknames(2));
        assertEquals(3, nicknames(10).size());
    }

    @Test
    void leaderboardRejectsInvalidLimit() {
        assertThrows(IllegalArgumentException.class, () -> useCase.leaderboard(0));
        assertThrows(IllegalArgumentException.class, () -> useCase.leaderboard(-1));
    }

    @Test
    void leaderboardOfNoPlayersIsEmpty() {
        assertTrue(useCase.leaderboard(null).isEmpty());
    }
}
