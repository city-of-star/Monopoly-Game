package Monopoly.core.domain.entity.tile;

/**
 * 实现功能【标记地图格子的类型枚举，帮助区分不同的地块逻辑】
 * <p>
 *
 * <p>
 *
 * @author li.hongyu
 * @date 2025-11-13 17:08:56
 */
public enum TileType {
    /**
     * 国家地块。
     */
    COUNTRY,
    /**
     * 火车站地块。
     */
    TRAIN_STATION,
    /**
     * 公司地块。
     */
    COMPANY,
    /**
     * 机会卡地块。
     */
    CHANCE,
    /**
     * 命运卡地块。
     */
    FATE,
    /**
     * 特殊地块，例如起点、税收、监狱等。
     */
    SPECIAL
}

