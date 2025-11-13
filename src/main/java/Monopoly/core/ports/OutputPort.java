package Monopoly.core.ports;

import Monopoly.core.message.GameMessage;

/**
 * 实现功能【输出端口，用于将领域事件转换成对玩家可读的字符串并发送到指定媒介】
 * <p>
 *
 * <p>
 *
 * @author li.hongyu
 * @date 2025-11-13 16:53:11
 */
public interface OutputPort {

    /**
     * 发布一条面向玩家的消息。
     *
     * @param message 需要输出的消息对象
     */
    void publish(GameMessage message);
}

