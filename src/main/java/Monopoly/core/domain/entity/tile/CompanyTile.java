package Monopoly.core.domain.entity.tile;

import lombok.Getter;

/**
 * 实现功能【描述公用事业公司地块的收费与抵押信息】
 * <p>
 *
 * <p>
 *
 * @author li.hongyu
 * @date 2025-11-13 17:08:18
 */
@Getter
public class CompanyTile extends Tile {

    /**
     * 售价。
     */
    private final int sellPrice;
    /**
     * 抵押价。
     */
    private final int mortgagePrice;

    /**
     * 构造函数。
     *
     * @param position 地块顺序
     * @param name 公司名称
     * @param sellPrice 售价
     * @param mortgagePrice 抵押价
     */
    public CompanyTile(int position, String name, int sellPrice, int mortgagePrice) {
        super(position, name, TileType.COMPANY);
        this.sellPrice = sellPrice;
        this.mortgagePrice = mortgagePrice;
    }
}

