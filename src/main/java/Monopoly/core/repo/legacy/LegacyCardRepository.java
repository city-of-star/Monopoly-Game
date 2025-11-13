package Monopoly.core.repo.legacy;

import Monopoly.core.domain.entity.card.CardType;
import Monopoly.core.domain.entity.card.ChanceCard;
import Monopoly.core.domain.entity.card.DrawCard;
import Monopoly.core.domain.entity.card.FateCard;
import Monopoly.core.repo.CardRepository;
import lombok.extern.slf4j.Slf4j;
import Monopoly.legacy.plot.chance.ChanceManage;
import Monopoly.legacy.plot.fate.FateManage;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * 实现功能【从 old 包迁移机会/命运卡牌数据的仓库实现】。
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
public class LegacyCardRepository implements CardRepository {

    /**
     * 卡牌缓存。
     */
    private final Map<CardType, Map<Integer, DrawCard>> cache = new EnumMap<>(CardType.class);

    /**
     * 构造函数。
     */
    public LegacyCardRepository() {
        cache.put(CardType.CHANCE, new java.util.HashMap<>());
        cache.put(CardType.FATE, new java.util.HashMap<>());
        loadLegacyChanceCards();
        loadLegacyFateCards();
    }

    @Override
    public Collection<DrawCard> findAllByType(CardType cardType) {
        return cache.getOrDefault(cardType, Map.of()).values();
    }

    @Override
    public Optional<DrawCard> findByTypeAndId(CardType cardType, int id) {
        return Optional.ofNullable(cache.getOrDefault(cardType, Map.of()).get(id));
    }

    private void loadLegacyChanceCards() {
        new ChanceManage();
        Map<Integer, Monopoly.legacy.plot.chance.ChanceCard> legacyCards = ChanceManage.chanceCards;
        Map<Integer, DrawCard> target = new java.util.HashMap<>();
        for (var entry : legacyCards.entrySet()) {
            Monopoly.legacy.plot.chance.ChanceCard card = entry.getValue();
            ChanceCard converted = new ChanceCard(card.getId(), card.getCardName(), card.getCardEfficiency(), card.getCardDescription());
            target.put(entry.getKey(), converted);
        }
        cache.put(CardType.CHANCE, target);
    }

    private void loadLegacyFateCards() {
        new FateManage();
        Map<Integer, Monopoly.legacy.plot.fate.FateCard> legacyCards = FateManage.fateCards;
        Map<Integer, DrawCard> target = new java.util.HashMap<>();
        for (var entry : legacyCards.entrySet()) {
            Monopoly.legacy.plot.fate.FateCard card = entry.getValue();
            FateCard converted = new FateCard(card.getId(), card.getCardName(), card.getCardEfficiency(), card.getCardDescription());
            target.put(entry.getKey(), converted);
        }
        cache.put(CardType.FATE, target);
    }
}

