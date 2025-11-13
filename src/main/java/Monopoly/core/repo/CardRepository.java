package Monopoly.core.repo;

import Monopoly.core.domain.entity.card.CardType;
import Monopoly.core.domain.entity.card.DrawCard;

import java.util.Collection;
import java.util.Optional;

/**
 * 实现功能【卡牌仓库接口，提供机会/命运等卡组数据访问】。
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
public interface CardRepository {

    /**
     * 根据卡牌类型获取全量卡牌。
     *
     * @param cardType 卡牌类型
     * @return 卡牌集合
     */
    Collection<DrawCard> findAllByType(CardType cardType);

    /**
     * 根据类型与编号查找卡牌。
     *
     * @param cardType 卡牌类型
     * @param id 卡牌编号
     * @return 卡牌实体，可为空
     */
    Optional<DrawCard> findByTypeAndId(CardType cardType, int id);
}

