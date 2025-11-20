package Monopoly.config;

import Monopoly.adapters.console.ConsoleDecisionAdapter;
import Monopoly.adapters.console.ConsoleOutputAdapter;
import Monopoly.app.GameApp;
import Monopoly.core.domain.entity.player.Player;
import Monopoly.core.ports.OutputPort;
import Monopoly.core.ports.DecisionPort;
import Monopoly.core.repo.CardRepository;
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
     * 注意：此方法会收集玩家输入信息。
     *
     * @return 回合服务实例
     */
    public TurnService provideTurnService() {
        try {
            ConfigLoader loader = new ConfigLoader();
            TileRepository tileRepo = new JsonTileRepository(loader);
            CardRepository cardRepo = new JsonCardRepository(loader); // 触发加载与校验
            PlayerRepository playerRepo = new InMemoryPlayerRepository();

            // 控制台交互：人数、初始资金与玩家名称
            DecisionPort decision = provideDecisionPort();
            
            System.out.println("\n" + "═".repeat(64));
            System.out.println("                    🎲 欢迎来到大富翁游戏 🎲");
            System.out.println("═".repeat(64) + "\n");
            
            System.out.println("📋 请配置游戏参数：\n");
            
            // 读取玩家人数，支持默认值
            int playerCount = 2; // 默认2人
            String playerCountInput = decision.requestLine("👥 请输入参加游戏的人数 (2-6，直接回车默认2人): ");
            if (!playerCountInput.isEmpty()) {
                try {
                    playerCount = Integer.parseInt(playerCountInput.trim());
                    playerCount = Math.max(2, Math.min(6, playerCount));
                } catch (NumberFormatException e) {
                    System.out.println("   输入无效，使用默认值2人");
                }
            }
            
            // 读取初始资金，支持默认值
            int initialMoney = DEFAULT_INITIAL_MONEY;
            String moneyInput = decision.requestLine("💰 请输入每位玩家初始资金 (建议5000~10000，直接回车默认" + DEFAULT_INITIAL_MONEY + "): ");
            if (!moneyInput.isEmpty()) {
                try {
                    initialMoney = Integer.parseInt(moneyInput.trim());
                    if (initialMoney <= 0) {
                        System.out.println("   输入无效，使用默认值" + DEFAULT_INITIAL_MONEY);
                        initialMoney = DEFAULT_INITIAL_MONEY;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("   输入无效，使用默认值" + DEFAULT_INITIAL_MONEY);
                }
            }
            
            System.out.println("\n" + "─".repeat(64));
            System.out.println("👤 请输入玩家信息：\n");
            
            for (int i = 1; i <= playerCount; i++) {
                String name = decision.requestLine("   第 " + i + " 位玩家名称 (直接回车使用默认名称「玩家" + i + "」): ");
                if (name.isEmpty()) name = "玩家" + i;
                playerRepo.save(new Player(i, name, initialMoney));
            }
            
            // 打印启动界面
            printGameStartBanner(playerRepo);
            
            return new SimpleTurnService(playerRepo, tileRepo, cardRepo, decision);
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
    
    /**
     * 打印游戏启动界面。
     *
     * @param playerRepo 玩家仓库
     */
    private void printGameStartBanner(PlayerRepository playerRepo) {
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                      🎲 大富翁游戏开始 🎲                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("玩家配置完成：");
        
        var players = playerRepo.findAll().stream()
                .sorted((p1, p2) -> Integer.compare(p1.getId(), p2.getId()))
                .toList();
        
        for (Player player : players) {
            String name = player.getName();
            int money = player.getMoney();
            // 格式化金额，每三位数加一个逗号
            String formattedMoney = String.format("%,d", money);
            System.out.printf("👤 %-10s - 初始资金: 💰%s元%n", name, formattedMoney);
        }

        System.out.println();
    }
}


