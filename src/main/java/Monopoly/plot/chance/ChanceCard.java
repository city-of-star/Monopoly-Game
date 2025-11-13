package Monopoly.plot.chance;

import lombok.Data;

@Data
public class ChanceCard {
    private int id;
    private String cardName;  // 卡牌名
    private String cardEfficiency;  // 卡牌效果
    private String cardDescription;  // 卡牌描述

    public ChanceCard(int id, String cardName, String cardEfficiency, String cardDescription) {
        this.id = id;
        this.cardName = cardName;
        this.cardEfficiency = cardEfficiency;
        this.cardDescription = cardDescription;
    }
}

