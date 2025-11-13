package Monopoly.core.event;

import Monopoly.core.domain.entity.player.Player;

/**
 * 实现功能【表示一次成功推进的回合，包括本轮玩家与回合信息】。
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
public class TurnAdvancedEvent extends GameEvent {

    /**
     * 当前执行回合的玩家
     */
    private final Player currentPlayer;
    /**
     * 当前是第几轮，从 1 开始
     */
    private final int roundNumber;
    /**
     * 当前玩家在轮次中的顺序，从 1 开始
     */
    private final int playerOrder;
    /**
     * 本次回合推进后游戏是否进入结束状态
     */
    private final boolean gameOver;

    /**
     * 构造函数。
     *
     * @param currentPlayer 当前执行回合的玩家
     * @param roundNumber   当前第几轮
     * @param playerOrder   玩家在轮次中的顺序
     * @param gameOver      推进后游戏是否结束
     */
    public TurnAdvancedEvent(Player currentPlayer, int roundNumber, int playerOrder, boolean gameOver) {
        super(currentPlayer == null ? null : String.valueOf(currentPlayer.getId()));
        this.currentPlayer = currentPlayer;
        this.roundNumber = roundNumber;
        this.playerOrder = playerOrder;
        this.gameOver = gameOver;
    }

    /**
     * 获取当前执行回合的玩家。
     *
     * @return 玩家实体，若玩家信息缺失则返回 null
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * 获取当前是第几轮。
     *
     * @return 轮次序号，从 1 开始
     */
    public int getRoundNumber() {
        return roundNumber;
    }

    /**
     * 获取当前玩家在轮次中的执行顺序。
     *
     * @return 玩家顺序序号，从 1 开始
     */
    public int getPlayerOrder() {
        return playerOrder;
    }

    /**
     * 判断推进后游戏是否已结束。
     *
     * @return 如果游戏结束返回 true，否则返回 false
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * 获取事件的类型标识。
     *
     * @return 类型字符串，当回合推进后游戏结束时返回 "GAME_OVER"，否则返回 "TURN_ADVANCED"
     */
    @Override
    public String getType() {
        return gameOver ? "GAME_OVER" : "TURN_ADVANCED";
    }

    /**
     * 将事件转换为可读字符串。
     *
     * @return 格式化描述字符串
     */
    @Override
    public String toString() {
        if (currentPlayer == null) {
            return String.format("第 %d 轮：无玩家信息可供显示。", roundNumber);
        }
        return String.format("第 %d 轮，第 %d 位玩家【%s】执行回合%s。",
                roundNumber,
                playerOrder,
                currentPlayer.getName(),
                gameOver ? "，游戏进入结束状态" : "");
    }
}


