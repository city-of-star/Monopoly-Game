package Monopoly.plot.fate;

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

    public void fun(int id) {
        switch (id) {
            case 1: fun1();break;
            case 2: fun2();break;
            case 3: fun3();break;
            case 4: fun4();break;
            case 5: fun5();break;
            case 6: fun6();break;
            case 7: fun7();break;
            case 8: fun8();break;
            case 9: fun9();break;
            case 10: fun10();break;
            case 11: fun11();break;
            case 12: fun12();break;
            case 13: fun13();break;
            case 14: fun14();break;
            case 15: fun15();break;
        }
    }

    private void fun1() {}
    private void fun2() {}
    private void fun3() {}
    private void fun4() {}
    private void fun5() {}
    private void fun6() {}
    private void fun7() {}
    private void fun8() {}
    private void fun9() {}
    private void fun10() {}
    private void fun11() {}
    private void fun12() {}
    private void fun13() {}
    private void fun14() {}
    private void fun15() {}
}
