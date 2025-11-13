package Monopoly.core.domain.entity.card;

/**
 * 实现功能【机会卡实体，继承通用抽牌属性】。
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
public class ChanceCard extends DrawCard {

    /**
     * 构造函数。
     *
     * @param id 编号
     * @param title 标题
     * @param effect 效果描述
     * @param flavorText 背景描述
     */
    public ChanceCard(int id, String title, String effect, String flavorText) {
        super(id, title, effect, flavorText);
    }

    @Override
    public CardType getType() {
        return CardType.CHANCE;
    }
}

