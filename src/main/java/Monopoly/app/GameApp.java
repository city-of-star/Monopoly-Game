package Monopoly.app;

import Monopoly.adapters.console.ConsoleDecisionAdapter;
import Monopoly.adapters.console.ConsoleOutputAdapter;
import Monopoly.config.ConfigLoader;
import Monopoly.core.domain.entity.player.Player;
import Monopoly.core.event.GameEvent;
import Monopoly.core.event.InteractiveEvent;
import Monopoly.core.message.GameMessage;
import Monopoly.core.ports.OutputPort;
import Monopoly.core.repo.PlayerRepository;
import Monopoly.core.repo.TileRepository;
import Monopoly.core.repo.json.JsonCardRepository;
import Monopoly.core.repo.json.JsonTileRepository;
import Monopoly.core.repo.legacy.InMemoryPlayerRepository;
import Monopoly.core.service.TurnService;
import Monopoly.core.service.impl.SimpleTurnService;

import java.util.Objects;

/**
 * 实现功能【应用层入口，负责组装依赖并驱动游戏核心】
 * <p>
 *
 * <p>
 *
 * @author li.hongyu
 * @date 2025-11-13 16:45:23
 */
public class GameApp {

    private final TurnService turnService;
    private final OutputPort outputPort;

    /**
     * 构造函数。
     *
     * @param turnService 回合管理服务，实现核心状态推进
     * @param outputPort 输出端口，用于将事件转换为字符串提示
     */
    public GameApp(TurnService turnService, OutputPort outputPort) {
        this.turnService = Objects.requireNonNull(turnService, "回合服务不能为空。");
        this.outputPort = Objects.requireNonNull(outputPort, "输出端口不能为空。");
    }

    /**
     * 启动游戏主循环：持续从 {@link TurnService} 拉取事件并输出，直到游戏结束。
     *
     * @return 无返回值，循环结束即表示游戏完结
     */
    public void start() {
        while (!turnService.isGameOver()) {
            GameEvent event = turnService.advanceTurn();
            GameEvent current = event;
            while (current != null) {
                GameMessage message = new GameMessage(current.getType(), current.toString(), current);
                outputPort.publish(message);
                if (current instanceof InteractiveEvent interactive) {
                    current = interactive.interact();
                } else {
                    current = null;
                }
            }
        }
    }

    /**
     * 构建默认依赖并返回可运行的 GameApp。
     */
    public static GameApp defaultApp() {
        try {
            ConfigLoader loader = new ConfigLoader();
            TileRepository tileRepo = new JsonTileRepository(loader);
            new JsonCardRepository(loader); // 校验卡牌配置加载
            PlayerRepository playerRepo = new InMemoryPlayerRepository();
            playerRepo.save(new Player(1, "玩家A", 8000));
            playerRepo.save(new Player(2, "玩家B", 8000));
            TurnService turnService = new SimpleTurnService(playerRepo, tileRepo, new ConsoleDecisionAdapter());
            OutputPort output = new ConsoleOutputAdapter();
            return new GameApp(turnService, output);
        } catch (Exception e) {
            throw new RuntimeException("初始化默认应用失败: " + e.getMessage(), e);
        }
    }
}

