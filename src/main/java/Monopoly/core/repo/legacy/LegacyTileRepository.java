package Monopoly.core.repo.legacy;

import Monopoly.core.domain.entity.card.CardType;
import Monopoly.core.domain.entity.tile.CardTile;
import Monopoly.core.domain.entity.tile.CompanyTile;
import Monopoly.core.domain.entity.tile.CountryTile;
import Monopoly.core.domain.entity.tile.SpecialTile;
import Monopoly.core.domain.entity.tile.Tile;
import Monopoly.core.domain.entity.tile.TrainStationTile;
import Monopoly.core.repo.TileRepository;
import Monopoly.legacy.map.Map;
import Monopoly.legacy.plot.company.Company;
import Monopoly.legacy.plot.company.CompanyManage;
import Monopoly.legacy.plot.country.Country;
import Monopoly.legacy.plot.country.CountryManage;
import Monopoly.legacy.plot.trainStation.TrainStation;
import Monopoly.legacy.plot.trainStation.TrainStationManage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

/**
 * 实现功能【利用 old 包中的静态数据构建地块仓库】。
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
public class LegacyTileRepository implements TileRepository {

    /**
     * 地块缓存。
     */
    private final java.util.Map<Integer, Tile> tiles = new HashMap<>();

    /**
     * 构造函数。
     */
    public LegacyTileRepository() {
        loadLegacyData();
    }

    @Override
    public Collection<Tile> findAll() {
        return tiles.values();
    }

    @Override
    public Optional<Tile> findByPosition(int position) {
        return Optional.ofNullable(tiles.get(position));
    }

    /**
     * 从 old 包导入地块定义。
     */
    private void loadLegacyData() {
        new Map();
        new CountryManage();
        new CompanyManage();
        new TrainStationManage();

        for (int position = 1; position < Map.map.length; position++) {
            String name = Map.map[position][0];
            String type = Map.map[position][1];
            if (name == null || type == null) {
                continue;
            }
            switch (type) {
                case "country" -> createCountryTile(position, name);
                case "trainStation" -> createStationTile(position, name);
                case "company" -> createCompanyTile(position, name);
                case "chance" -> tiles.put(position, new CardTile(position, name, CardType.CHANCE));
                case "fate" -> tiles.put(position, new CardTile(position, name, CardType.FATE));
                case "special" -> tiles.put(position, createSpecialTile(position, name));
                default -> System.out.println("[WARN] 未识别的地块类型 position=" + position + ", type=" + type);
            }
        }
    }

    private void createCountryTile(int position, String name) {
        Country legacy = CountryManage.countries.get(position);
        if (legacy == null) {
            System.out.println("[WARN] 位置 " + position + " 未找到国家数据 " + name);
            return;
        }
        int[] houseToll = new int[]{
                legacy.getL1HouseToll(),
                legacy.getL2HouseToll(),
                legacy.getL3HouseToll(),
                legacy.getL4HouseToll()
        };
        CountryTile tile = new CountryTile(
                position,
                legacy.getCountryName(),
                legacy.getCityName(),
                legacy.getLandColor(),
                legacy.getSellPrice(),
                legacy.getBasicToll(),
                houseToll,
                legacy.getHotelToll(),
                legacy.getBuildHousePrice(),
                legacy.getBuildHotelPrice(),
                legacy.getMortgagePrice()
        );
        tiles.put(position, tile);
    }

    private void createStationTile(int position, String name) {
        TrainStation station = TrainStationManage.trainStations.get(position);
        if (station == null) {
            System.out.println("[WARN] 位置 " + position + " 未找到火车站数据 " + name);
            return;
        }
        int[] toll = new int[]{
                station.getL1Toll(),
                station.getL2Toll(),
                station.getL3Toll(),
                station.getL4Toll()
        };
        tiles.put(position, new TrainStationTile(position, station.getStationName(), toll, station.getMortgagePrice(), station.getSellPrice()));
    }

    private void createCompanyTile(int position, String name) {
        Company company = CompanyManage.companies.get(position);
        if (company == null) {
            System.out.println("[WARN] 位置 " + position + " 未找到公司数据 " + name);
            return;
        }
        tiles.put(position, new CompanyTile(position, company.getCompanyName(), company.getMortgagePrice()));
    }

    private SpecialTile createSpecialTile(int position, String name) {
        SpecialTile.SpecialCategory category = switch (name) {
            case "起点" -> SpecialTile.SpecialCategory.GO;
            case "坐牢" -> SpecialTile.SpecialCategory.JAIL;
            case "进牢" -> SpecialTile.SpecialCategory.GO_TO_JAIL;
            case "免费停车场" -> SpecialTile.SpecialCategory.FREE_PARKING;
            case "所得税-付2000元", "财产税-交1000元" -> SpecialTile.SpecialCategory.TAX;
            default -> SpecialTile.SpecialCategory.OTHER;
        };
        String description = switch (name) {
            case "所得税-付2000元" -> "需支付 2000 元所得税";
            case "财产税-交1000元" -> "需支付 1000 元财产税";
            case "起点" -> "经过或停留获得 2000 元奖励";
            case "免费停车场" -> "本回合休息";
            case "坐牢" -> "仅访客，下一回合继续";
            case "进牢" -> "直接入狱并暂停";
            default -> name;
        };
        return new SpecialTile(position, name, category, description);
    }
}

