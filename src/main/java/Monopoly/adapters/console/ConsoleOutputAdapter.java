package Monopoly.adapters.console;

import Monopoly.core.message.GameMessage;
import Monopoly.core.ports.OutputPort;

/**
 * 实现功能【将 GameMessage 格式化为控制台字符串输出】。
 */
public class ConsoleOutputAdapter implements OutputPort {
    @Override
    public void publish(GameMessage message) {
        System.out.println("[消息] " + message.getType() + " - " + message.getContent());
    }
}

