package Monopoly.core.service;

import Monopoly.core.event.GameEvent;

/**
 * 实现功能【负责管理玩家回合状态流转的服务接口】
 * <p>
 *
 * <p>
 *
 * @author li.hongyu
 * @date 2025-11-13 16:53:31
 */
public interface TurnService {

    /**
     * 推进一步游戏回合，并返回本次推进产生的事件。
     *
     * @return 本次状态推进产生的领域事件，若无需对外通知可返回 null
     */
    GameEvent advanceTurn();

    /**
     * 判断当前游戏是否已经结束。
     *
     * @return 如果游戏结束返回 true，否则返回 false
     */
    boolean isGameOver();
}

