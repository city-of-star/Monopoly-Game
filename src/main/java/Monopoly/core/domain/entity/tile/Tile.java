package Monopoly.core.domain.entity.tile;

import lombok.Getter;

/**
 * 实现功能【描述地图上任意地块的通用属性，具体类型需要继承该抽象类】
 * <p>
 *
 * <p>
 *
 * @author li.hongyu
 * @date 2025-11-13 17:08:34
 */
@Getter
public abstract class Tile {

    /**
     * 地块在地图中的顺序位置，从 1 开始。
     */
    private final int position;
    /**
     * 地块显示名称。
     */
    private final String name;
    /**
     * 地块类型，决定落地后的处理策略。
     */
    private final TileType tileType;

    /**
     * 构造函数。
     *
     * @param position 地块顺序位置
     * @param name 地块名称
     * @param tileType 地块类型
     */
    protected Tile(int position, String name, TileType tileType) {
        this.position = position;
        this.name = name;
        this.tileType = tileType;
    }
}

