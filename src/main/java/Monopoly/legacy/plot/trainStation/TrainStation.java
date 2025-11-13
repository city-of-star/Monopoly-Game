package Monopoly.legacy.plot.trainStation;

import lombok.Data;

@Data
public class TrainStation {
    private int id;
    private String stationName;
    private int l1Toll = 250;  // 若同一玩家购得一个车站
    private int l2Toll = 500;  // 若同一玩家购得两个车站
    private int l3Toll = 1000;  // 若同一玩家购得三个车站
    private int l4Toll = 2000;  // 若同一玩家购得四个车站
    private int mortgagePrice = 1000;  // 抵押价
    private int sellPrice = 2000;  // 售价
    private int owner = 0;  // 拥有者(0->银行; 1、2、3->玩家)

    public TrainStation(int id, String stationName) {
        this.id = id;
        this.stationName = stationName;
    }
}

