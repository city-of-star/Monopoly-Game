package Monopoly.adapters.console;

import Monopoly.core.ports.DecisionPort;

import java.util.Scanner;

/**
 * 实现功能【从控制台读取玩家决策（整数选项）】。
 */
public class ConsoleDecisionAdapter implements DecisionPort {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public int requestInt(String prompt) {
        System.out.print(prompt);
        while (true) {
            String s = scanner.nextLine();
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                System.out.print("输入无效，请输入整数: ");
            }
        }
    }

    @Override
    public String requestLine(String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine();
        return line == null ? "" : line.trim();
    }
}

