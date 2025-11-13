package Monopoly.core.message;

import Monopoly.core.event.GameEvent;
import lombok.Getter;

/**
 * 实现功能【描述一次需要输出给玩家的消息。该消息通常由领域事件转换而来，用于统一字符串打印逻辑】
 * <p>
 *
 * <p>
 *
 * @author li.hongyu
 * @date 2025-11-13 16:50:48
 */
@Getter
public class GameMessage {

    /**
     *  消息类型
     */
    private final String type;
    /**
     *  已格式化的消息正文
     */
    private final String content;
    /**
     * 关联的领域事件，可能为 null
     */
    private final GameEvent sourceEvent;

    /**
     * 构造函数。
     *
     * @param type 消息类型，通常与事件类型或业务场景对应
     * @param content 已格式化的消息正文
     * @param sourceEvent 触发该消息的事件原始对象，可为空
     */
    public GameMessage(String type, String content, GameEvent sourceEvent) {
        this.type = type;
        this.content = content;
        this.sourceEvent = sourceEvent;
    }

}

