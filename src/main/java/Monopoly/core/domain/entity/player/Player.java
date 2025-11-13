package Monopoly.core.domain.entity.player;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * 实现功能【表示参与游戏的玩家及其资产状态】。
 * <p>
 *
 * <p>
 *
 * @author
 * @date 2025-11-13
 */
@Getter
@Setter
public class Player {

    /**
     * 玩家唯一编号。
     */
    private final int id;
    /**
     * 玩家昵称。
     */
    private final String name;
    /**
     * 玩家当前资金。
     */
    private int money;
    /**
     * 玩家已购置的地块位置集合。
     */
    private final Set<Integer> ownedTilePositions = new HashSet<>();
    /**
     * 玩家已抵押的地块位置集合。
     */
    private final Set<Integer> mortgagedTilePositions = new HashSet<>();
    /**
     * 玩家当前所在位置。
     */
    private int position = 1;
    /**
     * 是否需要暂停回合。
     */
    private boolean paused;
    /**
     * 是否持有出狱卡。
     */
    private boolean hasJailReleasePermit;

    /**
     * 构造函数。
     *
     * @param id 编号
     * @param name 名称
     * @param money 初始资金
     */
    public Player(int id, String name, int money) {
        this.id = id;
        this.name = name;
        this.money = money;
    }

    /**
     * 新增一块已拥有地块。
     *
     * @param tilePosition 地块位置
     * @return 是否新增成功（避免重复）
     */
    public boolean addOwnedTile(int tilePosition) {
        return ownedTilePositions.add(tilePosition);
    }

    /**
     * 新增一块已抵押地块。
     *
     * @param tilePosition 地块位置
     * @return 是否新增成功（避免重复）
     */
    public boolean addMortgagedTile(int tilePosition) {
        return mortgagedTilePositions.add(tilePosition);
    }
}

