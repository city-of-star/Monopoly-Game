package Monopoly.core.repo;

import Monopoly.core.domain.entity.player.Player;

import java.util.Collection;
import java.util.Optional;

/**
 * 实现功能【玩家仓库接口，封装玩家的持久化访问】。
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
public interface PlayerRepository {

    /**
     * 保存或更新玩家。
     *
     * @param player 玩家实体
     */
    void save(Player player);

    /**
     * 根据编号查找玩家。
     *
     * @param id 玩家编号
     * @return 玩家实体，可为空
     */
    Optional<Player> findById(int id);

    /**
     * 获取所有已注册玩家。
     *
     * @return 玩家集合
     */
    Collection<Player> findAll();

    /**
     * 删除玩家。
     *
     * @param id 玩家编号
     */
    void deleteById(int id);
}

