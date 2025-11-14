package Monopoly.core.domain.state;

import lombok.Getter;
import lombok.Setter;

/**
 * 实现功能【管理地块的状态：房屋数、旅馆数、抵押状态】
 */
@Getter
@Setter
public class PropertyState {
    /**
     * 房屋数量（0-4）
     */
    private int houseCount = 0;
    
    /**
     * 旅馆数量（0或1）
     */
    private int hotelCount = 0;
    
    /**
     * 是否已抵押
     */
    private boolean mortgaged = false;

    /**
     * 是否可以建造房屋
     */
    public boolean canBuildHouse() {
        return !mortgaged && houseCount < 4 && hotelCount == 0;
    }

    /**
     * 是否可以建造旅馆
     */
    public boolean canBuildHotel() {
        return !mortgaged && houseCount == 4 && hotelCount == 0;
    }

    /**
     * 是否可以建造（房屋或旅馆）
     */
    public boolean canBuild() {
        return canBuildHouse() || canBuildHotel();
    }

    /**
     * 建造房屋
     */
    public void buildHouse() {
        if (canBuildHouse()) {
            houseCount++;
        } else {
            throw new IllegalStateException("无法建造房屋：房屋数=" + houseCount + ", 旅馆数=" + hotelCount + ", 抵押状态=" + mortgaged);
        }
    }

    /**
     * 建造旅馆（会移除4幢房屋）
     */
    public void buildHotel() {
        if (canBuildHotel()) {
            houseCount = 0;
            hotelCount = 1;
        } else {
            throw new IllegalStateException("无法建造旅馆：房屋数=" + houseCount + ", 旅馆数=" + hotelCount + ", 抵押状态=" + mortgaged);
        }
    }

    /**
     * 是否有建筑物（房屋或旅馆）
     */
    public boolean hasBuildings() {
        return houseCount > 0 || hotelCount > 0;
    }
}

