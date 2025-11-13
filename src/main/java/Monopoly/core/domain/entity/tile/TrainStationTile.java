package Monopoly.core.domain.entity.tile;

import lombok.Getter;

/**
 * 实现功能【描述火车站地块的价格与收费规则】
 * <p>
 *
 * <p>
 *
 * @author li.hongyu
 * @date 2025-11-13 17:09:04
 */
@Getter
public class TrainStationTile extends Tile {

    /**
     * 拥有 1~4 个车站时的收费标准。
     */
    private final int[] tollByOwnership;
    /**
     * 抵押价。
     */
    private final int mortgagePrice;
    /**
     * 售价。
     */
    private final int sellPrice;

    /**
     * 构造函数。
     *
     * @param position 地块顺序
     * @param name 火车站名称
     * @param tollByOwnership 1~4 个车站的收费
     * @param mortgagePrice 抵押价
     * @param sellPrice 售价
     */
    public TrainStationTile(int position, String name, int[] tollByOwnership, int mortgagePrice, int sellPrice) {
        super(position, name, TileType.TRAIN_STATION);
        this.tollByOwnership = tollByOwnership;
        this.mortgagePrice = mortgagePrice;
        this.sellPrice = sellPrice;
    }
}

