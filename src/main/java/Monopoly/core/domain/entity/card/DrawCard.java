package Monopoly.core.domain.entity.card;

import lombok.Getter;

/**
 * 实现功能【定义机会/命运等抽牌的通用属性】。
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
@Getter
public abstract class DrawCard {

    /**
     * 卡牌唯一编号。
     */
    private final int id;
    /**
     * 卡牌标题。
     */
    private final String title;
    /**
     * 卡牌效果描述。
     */
    private final String effect;
    /**
     * 卡牌背景故事或补充说明。
     */
    private final String flavorText;

    /**
     * 构造函数。
     *
     * @param id 编号
     * @param title 标题
     * @param effect 效果描述
     * @param flavorText 背景描述
     */
    protected DrawCard(int id, String title, String effect, String flavorText) {
        this.id = id;
        this.title = title;
        this.effect = effect;
        this.flavorText = flavorText;
    }

    /**
     * 获取卡牌类型。
     *
     * @return 卡牌类型枚举
     */
    public abstract CardType getType();
}

