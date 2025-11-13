package Monopoly.core.domain.entity.tile;

import lombok.Getter;

/**
 * 实现功能【承载国家地块的价格、费用与建筑信息】
 * <p>
 *
 * <p>
 *
 * @author li.hongyu
 * @date 2025-11-13 17:08:26
 */
@Getter
public class CountryTile extends Tile {

    /**
     * 售价。
     */
    private final int sellPrice;
    /**
     * 基础过路费。
     */
    private final int baseToll;
    /**
     * 一到四栋房屋的过路费。
     */
    private final int[] houseToll;
    /**
     * 旅馆过路费。
     */
    private final int hotelToll;
    /**
     * 盖房成本。
     */
    private final int buildHouseCost;
    /**
     * 建造旅馆成本。
     */
    private final int buildHotelCost;
    /**
     * 抵押价。
     */
    private final int mortgagePrice;
    /**
     * 地块颜色，用于垄断判定。
     */
    private final String color;
    /**
     * 城市名称，辅助输出。
     */
    private final String city;

    /**
     * 构造函数。
     *
     * @param position 地块顺序
     * @param name 国家名称
     * @param city 城市名称
     * @param color 地块颜色
     * @param sellPrice 售价
     * @param baseToll 基础过路费
     * @param houseToll 一到四栋房屋的过路费
     * @param hotelToll 旅馆过路费
     * @param buildHouseCost 建房成本
     * @param buildHotelCost 建旅馆成本
     * @param mortgagePrice 抵押价
     */
    public CountryTile(int position, String name, String city, String color,
                       int sellPrice, int baseToll, int[] houseToll, int hotelToll,
                       int buildHouseCost, int buildHotelCost, int mortgagePrice) {
        super(position, name, TileType.COUNTRY);
        this.city = city;
        this.color = color;
        this.sellPrice = sellPrice;
        this.baseToll = baseToll;
        this.houseToll = houseToll;
        this.hotelToll = hotelToll;
        this.buildHouseCost = buildHouseCost;
        this.buildHotelCost = buildHotelCost;
        this.mortgagePrice = mortgagePrice;
    }
}

