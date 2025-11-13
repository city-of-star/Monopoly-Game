package Monopoly.core.repo.json;

import Monopoly.config.ConfigLoader;
import Monopoly.core.domain.entity.card.CardType;
import Monopoly.core.domain.entity.tile.*;
import Monopoly.core.repo.TileRepository;

import java.io.IOException;
import java.util.*;

/**
 * 实现功能【从 JSON 配置加载地图与各类地块数据】。
 */
public class JsonTileRepository implements TileRepository {

    private final Map<Integer, Tile> tiles = new HashMap<>();

    /**
     * 构造函数。
     *
     * @param loader 配置加载器
     * @throws IOException 读取失败时抛出
     */
    public JsonTileRepository(ConfigLoader loader) throws IOException {
        List<Map<String, Object>> mapItems = loader.loadMap();
        // 先放入特殊/卡牌占位
        for (Map<String, Object> item : mapItems) {
            int position = ((Number) item.get("position")).intValue();
            String name = (String) item.get("name");
            String type = (String) item.get("type");
            switch (type) {
                case "special" -> {
                    String special = Optional.ofNullable(item.get("special")).map(Object::toString).orElse("OTHER");
                    String desc = Optional.ofNullable(item.get("description")).map(Object::toString).orElse(name);
                    tiles.put(position, new SpecialTile(position, name, SpecialTile.SpecialCategory.valueOf(special), desc));
                }
                case "chance" -> tiles.put(position, new CardTile(position, name, CardType.CHANCE));
                case "fate" -> tiles.put(position, new CardTile(position, name, CardType.FATE));
                default -> {
                    // country/trainStation/company 延后根据明细文件补齐
                }
            }
        }
        // 国家
        for (Map<String, Object> item : loader.loadCountries()) {
            int position = ((Number) item.get("position")).intValue();
            int[] houseToll = toIntArray((List<?>) item.get("houseToll"));
            CountryTile tile = new CountryTile(
                    position,
                    (String) item.get("countryName"),
                    (String) item.get("city"),
                    (String) item.get("color"),
                    ((Number) item.get("sellPrice")).intValue(),
                    ((Number) item.get("baseToll")).intValue(),
                    houseToll,
                    ((Number) item.get("hotelToll")).intValue(),
                    ((Number) item.get("buildHouseCost")).intValue(),
                    ((Number) item.get("buildHotelCost")).intValue(),
                    ((Number) item.get("mortgagePrice")).intValue()
            );
            tiles.put(position, tile);
        }
        // 车站
        for (Map<String, Object> item : loader.loadStations()) {
            int position = ((Number) item.get("position")).intValue();
            int[] toll = toIntArray((List<?>) item.get("tollByOwnership"));
            TrainStationTile tile = new TrainStationTile(
                    position,
                    (String) item.get("name"),
                    toll,
                    ((Number) item.get("mortgagePrice")).intValue(),
                    ((Number) item.get("sellPrice")).intValue()
            );
            tiles.put(position, tile);
        }
        // 公司
        for (Map<String, Object> item : loader.loadCompanies()) {
            int position = ((Number) item.get("position")).intValue();
            tiles.put(position, new CompanyTile(position, (String) item.get("name"),
                    ((Number) item.get("mortgagePrice")).intValue()));
        }
    }

    @Override
    public Collection<Tile> findAll() {
        return tiles.values();
    }

    @Override
    public Optional<Tile> findByPosition(int position) {
        return Optional.ofNullable(tiles.get(position));
    }

    private int[] toIntArray(List<?> list) {
        if (list == null) return new int[0];
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = ((Number) list.get(i)).intValue();
        }
        return arr;
    }
}

