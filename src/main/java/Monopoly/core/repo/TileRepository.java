package Monopoly.core.repo;

import Monopoly.core.domain.entity.tile.Tile;

import java.util.Collection;
import java.util.Optional;

/**
 * 实现功能【地块仓库接口，负责提供地图数据】。
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
public interface TileRepository {

    /**
     * 获取全部地块。
     *
     * @return 地块集合
     */
    Collection<Tile> findAll();

    /**
     * 根据位置获取地块。
     *
     * @param position 地块顺序位置
     * @return 地块实体，可为空
     */
    Optional<Tile> findByPosition(int position);
}

