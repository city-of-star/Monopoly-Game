package Monopoly.app;

import Monopoly.core.event.GameEvent;
import Monopoly.core.message.GameMessage;
import Monopoly.core.ports.OutputPort;
import Monopoly.core.service.TurnService;

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
        this.turnService = turnService;
        this.outputPort = outputPort;
    }

    /**
     * 启动游戏主循环。
     */
    public void start() {
        while (!turnService.isGameOver()) {
            GameEvent event = turnService.advanceTurn();
            if (event != null) {
                GameMessage message = new GameMessage(event.getType(), event.toString(), event);
                outputPort.publish(message);
            }
        }
    }
}

