package Monopoly.config;

import Monopoly.adapters.console.ConsoleDecisionAdapter;
import Monopoly.adapters.console.ConsoleOutputAdapter;
import Monopoly.app.GameApp;
import Monopoly.core.domain.entity.player.Player;
import Monopoly.core.ports.OutputPort;
import Monopoly.core.ports.DecisionPort;
import Monopoly.core.repo.PlayerRepository;
import Monopoly.core.repo.TileRepository;
import Monopoly.core.repo.json.JsonCardRepository;
import Monopoly.core.repo.json.JsonTileRepository;
import Monopoly.core.repo.legacy.InMemoryPlayerRepository;
import Monopoly.core.service.TurnService;
import Monopoly.core.service.impl.SimpleTurnService;

/**
 * 实现功能【集中管理游戏启动所需的依赖装配与初始化数据】。
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
public class GameConfiguration {

    /**
     * 默认初始资金
     */
    private static final int DEFAULT_INITIAL_MONEY = 8000;

    /**
     * 构造函数。
     */
    public GameConfiguration() {
    }

    /**
     * 提供一个默认的决策适配器（控制台输入）。
     *
     * @return 决策端口实例
     */
    public DecisionPort provideDecisionPort() {
        return new ConsoleDecisionAdapter();
    }

    /**
     * 提供一个默认的输出适配器。
     *
     * @return 输出端口实例
     */
    public OutputPort provideOutputPort() {
        return new ConsoleOutputAdapter();
    }

    /**
     * 提供一个默认的回合管理服务（基于 JSON 配置与内存玩家仓库）。
     *
     * @return 回合服务实例
     */
    public TurnService provideTurnService() {
        try {
            ConfigLoader loader = new ConfigLoader();
            TileRepository tileRepo = new JsonTileRepository(loader);
            new JsonCardRepository(loader); // 触发加载与校验
            PlayerRepository playerRepo = new InMemoryPlayerRepository();

            // 控制台交互：人数、初始资金与玩家名称
            DecisionPort decision = provideDecisionPort();
            int playerCount = Math.max(2, Math.min(6, decision.requestInt("请输入参加游戏的人数(2-6): ")));
            int initialMoney = decision.requestInt("请输入每位玩家初始资金(建议5000~10000，默认" + DEFAULT_INITIAL_MONEY + "): ");
            if (initialMoney <= 0) initialMoney = DEFAULT_INITIAL_MONEY;
            for (int i = 1; i <= playerCount; i++) {
                String name = decision.requestLine("请输入第 " + i + " 位玩家的名称(默认: 玩家" + i + "): ");
                if (name.isEmpty()) name = "玩家" + i;
                playerRepo.save(new Player(i, name, initialMoney));
            }
            return new SimpleTurnService(playerRepo, tileRepo, decision);
        } catch (Exception e) {
            throw new RuntimeException("初始化回合服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 装配并提供 GameApp。
     *
     * @return 已装配的 GameApp 实例
     */
    public GameApp provideGameApp() {
        TurnService turnService = provideTurnService();
        OutputPort outputPort = provideOutputPort();
        return new GameApp(turnService, outputPort);
    }
}


