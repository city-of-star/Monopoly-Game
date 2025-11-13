package Monopoly.adapters.console;

import Monopoly.core.message.GameMessage;
import Monopoly.core.ports.OutputPort;

/**
 * 实现功能【简单的控制台输出适配器，将消息格式化后打印到标准输出】。
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
public class ConsoleOutputPort implements OutputPort {

    /**
     * 发布一条面向玩家的消息。
     *
     * @param message 需要输出的消息对象
     * @return 无返回值，方法执行后消息即被打印到控制台
     */
    @Override
    public void publish(GameMessage message) {
        if (message == null) {
            System.out.println("[WARN] 收到空消息，已忽略。");
            return;
        }
        System.out.printf("[%s] %s%n", message.getType(), message.getContent());
    }
}


