package Monopoly.core.ports;

/**
 * 实现功能【向玩家请求简单决策的输入端口，默认控制台实现】。
 */
public interface DecisionPort {

    /**
     * 请求玩家输入一个整数选项。
     *
     * @param prompt 提示文本
     * @return 玩家输入的整数
     */
    int requestInt(String prompt);

    /**
     * 请求玩家输入一行文本。
     *
     * @param prompt 提示文本
     * @return 玩家输入的文本（去除首尾空白）
     */
    String requestLine(String prompt);
}

