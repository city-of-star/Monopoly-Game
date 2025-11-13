package Monopoly.legacy.plot.fate;

import java.util.HashMap;

public class FateManage {
    public static final HashMap<Integer, FateCard> fateCards = new HashMap<>();  // 命运卡

    public FateManage() {
        FateCard fateCard1 = new FateCard(1, "", "", "");
        FateCard fateCard2 = new FateCard(2, "", "", "");
        FateCard fateCard3 = new FateCard(3, "", "", "");
        FateCard fateCard4 = new FateCard(4, "", "", "");
        FateCard fateCard5 = new FateCard(5, "", "", "");
        FateCard fateCard6 = new FateCard(6, "", "", "");
        FateCard fateCard7 = new FateCard(7, "", "", "");
        FateCard fateCard8 = new FateCard(8, "", "", "");
        FateCard fateCard9 = new FateCard(9, "", "", "");
        FateCard fateCard10 = new FateCard(10, "", "", "");
        FateCard fateCard11 = new FateCard(11, "", "", "");
        FateCard fateCard12 = new FateCard(12, "", "", "");
        FateCard fateCard13 = new FateCard(13, "", "", "");
        FateCard fateCard14 = new FateCard(14, "", "", "");
        FateCard fateCard15 = new FateCard(15, "搭乘”欧洲之星“从巴黎直达伦敦", "", "");
        fateCards.put(1, fateCard1);
        fateCards.put(2, fateCard2);
        fateCards.put(3, fateCard3);
        fateCards.put(4, fateCard4);
        fateCards.put(5, fateCard5);
        fateCards.put(6, fateCard6);
        fateCards.put(7, fateCard7);
        fateCards.put(8, fateCard8);
        fateCards.put(9, fateCard9);
        fateCards.put(10, fateCard10);
        fateCards.put(11, fateCard11);
        fateCards.put(12, fateCard12);
        fateCards.put(13, fateCard13);
        fateCards.put(14, fateCard14);
        fateCards.put(15, fateCard15);
    }
}

