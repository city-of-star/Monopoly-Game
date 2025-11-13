package Monopoly.core.repo.legacy;

import Monopoly.core.domain.entity.player.Player;
import Monopoly.core.repo.PlayerRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 实现功能【以内存 Map 保存玩家数据的简易仓库实现】。
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
public class InMemoryPlayerRepository implements PlayerRepository {

    /**
     * 玩家数据存储容器。
     */
    private final Map<Integer, Player> storage = new HashMap<>();

    @Override
    public void save(Player player) {
        storage.put(player.getId(), player);
    }

    @Override
    public Optional<Player> findById(int id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Collection<Player> findAll() {
        return storage.values();
    }

    @Override
    public void deleteById(int id) {
        storage.remove(id);
    }
}

