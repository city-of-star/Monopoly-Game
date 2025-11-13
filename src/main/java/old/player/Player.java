package old.player;

import lombok.Data;

import java.util.HashSet;

@Data
public class Player {
    private int id;
    private String playerName;  // 玩家的昵称
    private int money = 0;  // 玩家拥有的钱
    private HashSet<Integer> ownedCountries = new HashSet<>();  // 玩家拥有的地块(用id表示)
    private HashSet<Integer> mortgageCountries = new HashSet<>();  // 玩家已抵押的地块(用id表示)
    private int currentPosition = 1;  // 玩家当前位置
    private Boolean isPause = false;  // 是否被暂停一回合
    private Boolean isHavePrisonReleasePermit = false;  // 是否有出狱许可证

    public Player(int id, String playerName, int money) {
        this.id = id;
        this.playerName = playerName;
        this.money = money;
    }
}
