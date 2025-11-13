package Monopoly.core.repo.json;

import Monopoly.config.ConfigLoader;
import Monopoly.core.domain.entity.card.*;
import Monopoly.core.repo.CardRepository;

import java.io.IOException;
import java.util.*;

/**
 * 实现功能【从 JSON 加载机会/命运卡牌】。
 */
public class JsonCardRepository implements CardRepository {

    private final Map<CardType, Map<Integer, DrawCard>> cache = new EnumMap<>(CardType.class);

    /**
     * 构造函数。
     *
     * @param loader 配置加载器
     * @throws IOException 读取失败时抛出
     */
    public JsonCardRepository(ConfigLoader loader) throws IOException {
        cache.put(CardType.CHANCE, new HashMap<>());
        cache.put(CardType.FATE, new HashMap<>());
        loadChance(loader);
        loadFate(loader);
    }

    @Override
    public Collection<DrawCard> findAllByType(CardType cardType) {
        return cache.getOrDefault(cardType, Map.of()).values();
    }

    @Override
    public Optional<DrawCard> findByTypeAndId(CardType cardType, int id) {
        return Optional.ofNullable(cache.getOrDefault(cardType, Map.of()).get(id));
    }

    private void loadChance(ConfigLoader loader) throws IOException {
        Map<Integer, DrawCard> map = cache.get(CardType.CHANCE);
        for (Map<String, Object> item : loader.loadChance()) {
            int id = ((Number) item.get("id")).intValue();
            map.put(id, new ChanceCard(id, (String) item.get("title"),
                    (String) item.get("effect"),
                    Optional.ofNullable(item.get("flavor")).map(Object::toString).orElse("")));
        }
    }

    private void loadFate(ConfigLoader loader) throws IOException {
        Map<Integer, DrawCard> map = cache.get(CardType.FATE);
        for (Map<String, Object> item : loader.loadFate()) {
            int id = ((Number) item.get("id")).intValue();
            map.put(id, new FateCard(id, (String) item.get("title"),
                    (String) item.get("effect"),
                    Optional.ofNullable(item.get("flavor")).map(Object::toString).orElse("")));
        }
    }
}

