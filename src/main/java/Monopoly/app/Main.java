package Monopoly.app;

import Monopoly.config.GameConfiguration;

/**
 * 实现功能【程序入口。负责装配依赖并启动游戏主循环】
 * <p>
 *
 * <p>
 *
 */
public class Main {

    /**
     * Java 程序入口。负责创建配置对象、装配 GameApp 并启动游戏主循环。
     *
     * @param args 命令行参数，当前版本未使用
     * @return 无返回值，方法执行完毕表示游戏主循环已结束
     */
    public static void main(String[] args) {
        GameConfiguration configuration = new GameConfiguration();
        GameApp app = configuration.provideGameApp();
        app.start();
    }
}