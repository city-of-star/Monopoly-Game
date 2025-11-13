package Monopoly.core.domain.entity.tile;

import Monopoly.core.domain.entity.card.CardType;
import lombok.Getter;

/**
 * 实现功能【描述机会/命运等抽牌地块】。
 * <p>
 *
 * <p>
 *
 * @author li.hongyu
 * @date 2025-11-13 17:09:45
 */
@Getter
public class CardTile extends Tile {

    /**
     * 对应的卡牌类型。
     */
    private final CardType cardType;

    /**
     * 构造函数。
     *
     * @param position 地块顺序
     * @param name 地块名称
     * @param cardType 卡牌类型
     */
    public CardTile(int position, String name, CardType cardType) {
        super(position, name, cardType == CardType.CHANCE ? TileType.CHANCE : TileType.FATE);
        this.cardType = cardType;
    }
}

