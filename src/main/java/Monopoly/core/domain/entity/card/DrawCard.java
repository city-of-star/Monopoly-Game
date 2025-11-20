package Monopoly.core.domain.entity.card;

import lombok.Getter;

/**
 * 实现功能【定义机会/命运等抽牌的通用属性】。
 * <p>
 *
 * <p>
 *
 * @author li.hongyu
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
     * 卡牌效果描述（用于展示）。
     */
    private final String effect;
    /**
     * 卡牌效果指令（内部逻辑使用）。
     */
    @Getter
    private final String effectCommand;
    /**
     * 卡牌背景故事或补充说明。
     */
    private final String flavorText;

    /**
     * 构造函数。
     *
     * @param id 编号
     * @param title 标题
     * @param effect 效果描述（展示用）
     * @param flavorText 背景描述
     */
    protected DrawCard(int id, String title, String effect, String flavorText) {
        this(id, title, effect, null, flavorText);
    }

    /**
     * 构造函数。
     *
     * @param id 编号
     * @param title 标题
     * @param effect 效果描述（展示用）
     * @param effectCommand 效果指令（逻辑用）
     * @param flavorText 背景描述
     */
    protected DrawCard(int id, String title, String effect, String effectCommand, String flavorText) {
        this.id = id;
        this.title = title;
        this.effect = effect;
        this.effectCommand = (effectCommand == null || effectCommand.isBlank()) ? effect : effectCommand;
        this.flavorText = flavorText;
    }

    /**
     * 获取卡牌类型。
     *
     * @return 卡牌类型枚举
     */
    public abstract CardType getType();
}

