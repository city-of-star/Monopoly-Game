package Monopoly.core.event;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * 实现功能【表示在游戏运行过程中发生的领域事件，所有具体事件都应继承该抽象类】
 * <p>
 *
 * <p>
 *
 * @author li.hongyu
 * @date 2025-11-13 16:49:18
 */
@Getter
public abstract class GameEvent {

    /**
     * 事件唯一标识
     */
    private final UUID id;
    /**
     * 事件发生的时间戳
     */
    private final Instant occurredAt;
    /**
     * 玩家标识字符串，若无玩家关联则返回 null
     */
    private final String playerId;

    /**
     * 构造函数。
     *
     * @param playerId 事件关联的玩家标识，若与玩家无关可传入 null
     */
    protected GameEvent(String playerId) {
        this.id = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.playerId = playerId;
    }

    /**
     * 获取事件类型名称，用于日志和输出。
     *
     * @return 事件类型字符串
     */
    public abstract String getType();
}

