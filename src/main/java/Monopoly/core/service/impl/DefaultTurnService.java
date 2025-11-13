package Monopoly.core.service.impl;

import Monopoly.core.domain.entity.player.Player;
import Monopoly.core.event.GameEvent;
import Monopoly.core.event.TurnAdvancedEvent;
import Monopoly.core.service.TurnService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 实现功能【默认的回合管理服务，按照玩家顺序循环推进回合并在达到最大轮次后结束游戏】。
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
public class DefaultTurnService implements TurnService {

    /**
     * 玩家执行顺序列表
     */
    private final List<Player> playerOrder;
    /**
     * 最大轮次数
     */
    private final int maxRounds;
    /**
     * 当前轮次，从 0 开始计数
     */
    private int currentRound = 0;
    /**
     * 当前轮次中玩家索引，从 0 开始计数
     */
    private int currentPlayerIndex = 0;
    /**
     * 游戏是否已结束
     */
    private boolean gameOver = false;

    /**
     * 构造函数。
     *
     * @param players   初始玩家集合，会按照玩家 id 排序后决定执行顺序
     * @param maxRounds 最大轮次数，必须大于 0
     */
    public DefaultTurnService(Collection<Player> players, int maxRounds) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("初始化回合服务需要至少一名玩家。");
        }
        if (maxRounds <= 0) {
            throw new IllegalArgumentException("最大轮次数必须大于 0。");
        }
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort(Comparator.comparingInt(Player::getId));
        this.playerOrder = Collections.unmodifiableList(sortedPlayers);
        this.maxRounds = maxRounds;
    }

    /**
     * 推进一步游戏回合，并返回本次推进产生的事件。
     *
     * @return 回合推进事件；若游戏已结束则返回 null
     */
    @Override
    public synchronized GameEvent advanceTurn() {
        if (gameOver) {
            return null;
        }
        Player currentPlayer = playerOrder.get(currentPlayerIndex);
        currentRound++;
        boolean reachMaxRounds = currentRound >= maxRounds;
        GameEvent event = new TurnAdvancedEvent(
                currentPlayer,
                currentRound,
                currentPlayerIndex + 1,
                reachMaxRounds
        );
        if (reachMaxRounds) {
            gameOver = true;
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % playerOrder.size();
        }
        return event;
    }

    /**
     * 判断当前游戏是否已经结束。
     *
     * @return 如果游戏结束返回 true，否则返回 false
     */
    @Override
    public synchronized boolean isGameOver() {
        return gameOver;
    }

    /**
     * 获取当前参与游戏的玩家快照列表，主要用于调试与对外展示。
     *
     * @return 不可变玩家列表
     */
    public List<Player> getPlayerOrderSnapshot() {
        return playerOrder;
    }

    /**
     * 获取默认构造的最大轮次数。
     *
     * @return 最大轮次数
     */
    public int getMaxRounds() {
        return maxRounds;
    }
}


