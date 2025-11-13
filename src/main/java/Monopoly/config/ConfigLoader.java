package Monopoly.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 实现功能【从 resources/config 下加载地图与卡牌配置的工具类】
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
public class ConfigLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 加载 map.json。
     *
     * @return 地图条目列表
     * @throws IOException 读取失败时抛出
     */
    public List<Map<String, Object>> loadMap() throws IOException {
        return readList("/config/map.json");
    }

    /**
     * 加载 countries.json。
     *
     * @return 国家条目列表
     * @throws IOException 读取失败时抛出
     */
    public List<Map<String, Object>> loadCountries() throws IOException {
        return readList("/config/countries.json");
    }

    /**
     * 加载 stations.json。
     */
    public List<Map<String, Object>> loadStations() throws IOException {
        return readList("/config/stations.json");
    }

    /**
     * 加载 companies.json。
     */
    public List<Map<String, Object>> loadCompanies() throws IOException {
        return readList("/config/companies.json");
    }

    /**
     * 加载 chance.json。
     */
    public List<Map<String, Object>> loadChance() throws IOException {
        return readList("/config/chance.json");
    }

    /**
     * 加载 fate.json。
     */
    public List<Map<String, Object>> loadFate() throws IOException {
        return readList("/config/fate.json");
    }

    private List<Map<String, Object>> readList(String path) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("配置文件未找到: " + path);
            }
            return objectMapper.readValue(in, new TypeReference<List<Map<String, Object>>>() {});
        }
    }
}

