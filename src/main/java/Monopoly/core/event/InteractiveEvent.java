package Monopoly.core.event;

/**
 * 实现功能【表示带有后续交互步骤的领域事件】。
 */
public interface InteractiveEvent {

    /**
     * 执行交互步骤（例如提示玩家输入决策），并返回交互产生的新事件。
     *
     * @return 后续事件；若无需追加输出则返回 null
     */
    GameEvent interact();
}

