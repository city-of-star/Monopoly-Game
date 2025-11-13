package Monopoly.core.domain.entity.tile;

import lombok.Getter;

/**
 * 实现功能【描述除地产以外的特殊事件地块，如起点、税费、监狱等】。
 * <p>
 *
 * <p>
 *
 * @author li.hongyu
 * @date 2025-11-13 17:09:58
 */
@Getter
public class SpecialTile extends Tile {

    /**
     * 特殊地块的分类。
     */
    public enum SpecialCategory {
        /**
         * 起点。
         */
        GO,
        /**
         * 监狱（访客）。
         */
        JAIL,
        /**
         * 进监狱。
         */
        GO_TO_JAIL,
        /**
         * 收税。
         */
        TAX,
        /**
         * 免费停车。
         */
        FREE_PARKING,
        /**
         * 其他类别。
         */
        OTHER
    }

    /**
     * 特殊地块类别。
     */
    private final SpecialCategory category;
    /**
     * 地块附加描述，例如税费金额。
     */
    private final String description;

    /**
     * 构造函数。
     *
     * @param position 地块顺序
     * @param name 地块名称
     * @param category 特殊地块分类
     * @param description 额外描述
     */
    public SpecialTile(int position, String name, SpecialCategory category, String description) {
        super(position, name, TileType.SPECIAL);
        this.category = category;
        this.description = description;
    }
}

