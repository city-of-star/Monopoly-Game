package old.plot.chance;

import old.Game;
import old.Utils;
import old.map.Map;
import old.plot.country.CountryManage;

import java.util.HashMap;

public class ChanceManage {
    private static final Utils utils = new Utils();
    public static final HashMap<Integer, ChanceCard> chanceCards = new HashMap<>();  // 机会卡

    public ChanceManage() {
        ChanceCard chanceCard1 = new ChanceCard(1, "到印尼品尝麝香猫咖啡", "花费 1200 元", "原产于印度尼西亚的麝香猫咖啡,曾经是世界上最昂贵的咖啡豆。麝香猫吃了树上的咖啡果后，经过消化道完整的排泄出来，又称猫屎咖啡。");
        ChanceCard chanceCard2 = new ChanceCard(2, "在加拿大遇到龙卷风车子被风吹走了", "损失 800 元", "龙卷风是一种相当猛烈的天气现象，因快速旋转而造成直立中空管状的强力气流，破坏力惊人，常造成严重的生命财产损失。");
        ChanceCard chanceCard3 = new ChanceCard(3, "在牙买加学习雷鬼音乐", "房子最少的玩家免费盖一栋", "雷鬼乐是牙买加盛行的曲风，融合了非洲传统乐，美国抒情蓝调及拉丁热情曲风，对西方音乐有重大的影响。");
        ChanceCard chanceCard4 = new ChanceCard(4, "到西班牙参加“斗牛”受伤", "玩家下回合暂停一次", "“斗牛”是一项人与牛斗的运动。参与斗牛的人称为斗牛士，是西班牙的国技。这项运动非常危险，常有斗牛士因此受伤或死亡。");
        ChanceCard chanceCard5 = new ChanceCard(5, "在撒哈拉沙漠因海市蜃楼而迷路", "现金最多的玩家罚 1000 元", "海市蜃楼是一种光学幻景，是地球上物体反射的光经大气折射而形成的虚像，在沙漠常常出现幻景，让旅行者迷路。");
        ChanceCard chanceCard6 = new ChanceCard(6, "搭乘日本新干线旅游", "马上回到起点并领取 2000 元", "新干线是日本的高速铁路客运线系统,其他地区又称新干线为子弹列车。因其顶尖的铁路技术、安全性跟控制系统,有不少国家引进日本新干线的系统。");
        ChanceCard chanceCard7 = new ChanceCard(7, "在阿拉斯加挖到石油", "获得出狱许可证(可以保留或出售)", "阿拉斯加早期被当成是冰封的世界，随着大量的金矿与石油被发现，成为美国的能源宝库，主要产原油、天然气、贵金属。");
        ChanceCard chanceCard8 = new ChanceCard(8, "拯救了迷路的北极熊宝宝", "玩家得 800 元", "北极熊是生长在北极的熊，也是陆地上最庞大的肉食动物，它拥有极厚的脂肪及毛发保暖，所以能在北极严酷的气候里生存。");
        ChanceCard chanceCard9 = new ChanceCard(9, "在巴西参加狂欢节", "大家转转盘，点数最大的人拿取点数 x10 的金额", "巴西狂欢节被称为世界上最大的狂欢节，有“地球上最伟大的表演”之称。每年二月的中旬或下旬举行三天。");
        ChanceCard chanceCard10 = new ChanceCard(10, "在澳洲被无尾熊抓伤，因无尾熊指甲细菌多而发炎", "花费 600 元看医生", "无尾熊只要吃尤加利树的树叶，就可以供给它们所需要的养分及水分，所以英语名称Koala是澳洲方言“不喝水”的意思。");
        ChanceCard chanceCard11 = new ChanceCard(11, "与爱斯基摩人一起盖冰屋时因错算角度导致冰屋倒塌", "房子最多的人拆一栋房子", "爱斯基摩人又称为因纽特人，他们擅长搭盖由雪块做成的房屋，作为他们在漫长的严冬中狩猎期间的临时住所。");
        ChanceCard chanceCard12 = new ChanceCard(12, "在印度吃牛肉干被罚", "拘票-立刻坐牢", "牛在印度教中被视为神圣的动物，因此不得宰杀牛，不得使用牛革制品，更忌食牛肉。");
        ChanceCard chanceCard13 = new ChanceCard(13, "到英国享受下午茶", "最靠近英国的玩家付 500 元", "最早有下午喝茶习惯的是以茶文化著称的中国，然而将下午茶发展为一种特定习俗的则是英国人");
        ChanceCard chanceCard14 = new ChanceCard(14, "到苏格兰试穿苏格兰裙", "捐 200 元再转转盘行动一次", "苏格兰短裙，是一种男性穿着的民族礼服裙子。始创于苏格兰高地，以羊毛布料制成，有格子花纹。");
        ChanceCard chanceCard15 = new ChanceCard(15, "登机时携带洗发精被美国海关没收", "最靠近美国的玩家罚 700 元", "为了防止恐怖事件，美国禁止旅客把胶状、膏状和液状的物品放置在随身行李中，但可以放在行李箱里托运。");
        chanceCards.put(1, chanceCard1);
        chanceCards.put(2, chanceCard2);
        chanceCards.put(3, chanceCard3);
        chanceCards.put(4, chanceCard4);
        chanceCards.put(5, chanceCard5);
        chanceCards.put(6, chanceCard6);
        chanceCards.put(7, chanceCard7);
        chanceCards.put(8, chanceCard8);
        chanceCards.put(9, chanceCard9);
        chanceCards.put(10, chanceCard10);
        chanceCards.put(11, chanceCard11);
        chanceCards.put(12, chanceCard12);
        chanceCards.put(13, chanceCard13);
        chanceCards.put(14, chanceCard14);
        chanceCards.put(15, chanceCard15);
    }

    // 使用机会卡
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

    private void fun1() {
        utils.reduceCurrentPlayerMoney(1200);
        System.out.println("您花费了 1200 元，您的余额为 " + utils.getCurrentPlayer().getMoney() + " 元");
    }
    private void fun2() { utils.reduceCurrentPlayerMoney(800);
        System.out.println("您损失了 800 元，您的余额为 " + utils.getCurrentPlayer().getMoney() + " 元");}
    private void fun3() {
        int number = Integer.MAX_VALUE;  // 房子数量最少的玩家的房子数量
        for (int i = 1; i <= Game.players.size(); i++) {
            if (Game.players.get(i) != null) {  // 如果该玩家没有出局
                int houseNumber = 0;
                for (int countryId : Game.players.get(i).getOwnedCountries()) {
                    houseNumber += CountryManage.countries.get(countryId).getHouseNumber();
                }
                if (houseNumber < number) number = houseNumber;
            }
        }
        for (int i = 1; i <= Game.players.size(); i++) {  // 如果有多名玩家的房屋数量相同，就都盖一栋房屋
            if (Game.players.get(i) != null) {  // 如果该玩家没有出局
                int houseNumber = 0;
                for (int countryId : Game.players.get(i).getOwnedCountries()) {
                    houseNumber += CountryManage.countries.get(countryId).getHouseNumber();
                }
                if (houseNumber == number) {
                    // 检索该玩家的国家，选取房价最贵的地块盖一栋房屋
                    int price = 0;
                    int countryId1 = 0;
                    for (int countryId : Game.players.get(i).getOwnedCountries()) {
                        if (CountryManage.countries.get(countryId).getBuildHousePrice() > price) {
                            price = CountryManage.countries.get(countryId).getBuildHousePrice();
                            countryId1 = countryId;
                        }
                    }
                    if (CountryManage.countries.get(countryId1).getHouseNumber() == 4) {  // 如果有 4 栋房子就盖一栋旅馆
                        CountryManage.countries.get(countryId1).setHouseNumber(0);
                        CountryManage.countries.get(countryId1).setHotelNumber(CountryManage.countries.get(countryId1).getHotelNumber() + 1);
                        System.out.println(utils.getPlayer(i).getPlayerName() + " 的 (" + CountryManage.countries.get(countryId1).getCountryName() + ") 将四栋房屋打造合成了一栋旅馆");
                    } else {  // 否则盖一栋房屋
                        CountryManage.countries.get(countryId1).setHouseNumber(CountryManage.countries.get(countryId1).getHouseNumber() + 1);
                        System.out.println(utils.getPlayer(i).getPlayerName() + " 的 (" + CountryManage.countries.get(countryId1).getCountryName() + ") 盖了一栋房屋");
                    }
                }
            }
        }
    }
    private void fun4() {
        Game.players.get(Game.currentPlayerId).setIsPause(true);
        System.out.println("您下一回合将被暂停移动");
    }
    private void fun5() {
        int money = 0;
        int playerId = 0;
        for (int i = 1; i <= Game.players.size(); i++) {
            if (Game.players.get(i) != null && Game.players.get(i).getMoney() >= money) {
                money = Game.players.get(i).getMoney();
                playerId = i;
            }
        }
        utils.reducePlayerMoney(playerId, 1000);
        System.out.println(utils.getPlayer(playerId).getPlayerName() + " 被罚 1000 元，他的余额为 " + utils.getPlayer(playerId).getMoney() + " 元");
    }
    private void fun6() {
        Game.players.get(Game.currentPlayerId).setCurrentPosition(1);
        utils.addCurrentPlayerMoney(2000);
        System.out.println("您回到了起点并领取了 2000 元，您的余额为 " + utils.getCurrentPlayer().getMoney() + " 元");
    }
    private void fun7() {
        Game.players.get(Game.currentPlayerId).setIsHavePrisonReleasePermit(true);
        System.out.println("您获得了一张出狱许可证");
    }
    private void fun8() {
        utils.addCurrentPlayerMoney(800);
        System.out.println("您获得了 800 元，您的余额为 " + utils.getCurrentPlayer().getMoney() + " 元");
    }
    private void fun9() {
        int point = 0;  // 赢家点数
        int playerId = 0;  // 赢家 id
        for (int i = 1; i <= Game.players.size(); i++) {
            if (Game.players.get(i) != null) {
                int point1 = Game.random.nextInt(12) + 1;  // 随机生成1~12的数
                System.out.println(utils.getPlayer(i).getPlayerName() + " 的转盘数为 " + point1);
                if (point1 > point) {
                    point = point1;
                    playerId = i;
                }
            }
        }
        utils.addPlayerMoney(playerId, point * 10);
        System.out.println(utils.getPlayer(playerId).getPlayerName() + " 取得了胜利，奖励 " + (point * 10) + " 元，他的余额为 " + utils.getPlayer(playerId).getMoney() + " 元");
    }
    private void fun10() {
        utils.reduceCurrentPlayerMoney(600);
        System.out.println("您花费了 600 元，您的余额为 " + utils.getCurrentPlayer().getMoney() + " 元");
    }
    private void fun11() {
        int number = 0;  // 房子数量最多的玩家的房子数量
        for (int i = 1; i <= Game.players.size(); i++) {
            if (Game.players.get(i) != null) {  // 如果该玩家没有出局
                int houseNumber = 0;
                for (int countryId : Game.players.get(i).getOwnedCountries()) {
                    houseNumber += CountryManage.countries.get(countryId).getHouseNumber();
                }
                if (houseNumber > number) number = houseNumber;
            }
        }
        if (number == 0) {  // 每个玩家都没有房屋
            System.out.println("因为没有玩家有房屋，所以无事发生");
            return;
        }
        for (int i = 1; i <= Game.players.size(); i++) {  // 如果有多名玩家的房屋数量相同，就都拆一栋房屋
            if (Game.players.get(i) != null) {  // 如果该玩家没有出局
                int houseNumber = 0;
                for (int countryId : Game.players.get(i).getOwnedCountries()) {
                    houseNumber += CountryManage.countries.get(countryId).getHouseNumber();
                }
                if (houseNumber == number) {
                    // 检索该玩家的国家，选取房价最低的地块拆一栋房屋
                    int price = Integer.MAX_VALUE;
                    int countryId1 = 0;
                    for (int countryId : Game.players.get(i).getOwnedCountries()) {
                        if (CountryManage.countries.get(countryId).getBuildHousePrice() < price) {
                            price = CountryManage.countries.get(countryId).getBuildHousePrice();
                            countryId1 = countryId;
                        }
                    }
                    CountryManage.countries.get(countryId1).setHouseNumber(CountryManage.countries.get(countryId1).getHouseNumber() - 1);
                    System.out.println(utils.getPlayer(i).getPlayerName() + " 的 (" + CountryManage.countries.get(countryId1).getCountryName() + ") 被拆了一栋房屋");
                }
            }
        }
    }
    private void fun12() {
        for (int i = 1; i < Map.map.length; i++) {
            if (Map.map[i][0] == "坐牢") {
                if (Game.players.get(Game.currentPlayerId).getIsHavePrisonReleasePermit()) {
                    System.out.println("经银行检测您拥有尊贵的出狱许可证，所以您被即刻释放，您的出狱许可证已被消耗");
                    Game.players.get(Game.currentPlayerId).setIsHavePrisonReleasePermit(false);
                } else {
                    Game.players.get(Game.currentPlayerId).setIsPause(true);
                    Game.players.get(Game.currentPlayerId).setCurrentPosition(i);  // 移动到坐牢地块
                    System.out.println("您进监狱了，下一回合将被暂停");
                }
            }
        }
    }
    private void fun13() {
        int EnglandPosition = 0;
        for (int i = 1; i < Map.map.length; i++) {
            if (Map.map[i][0] == "英国") {
                EnglandPosition = i;
            }
        }
        int differPosition = Integer.MAX_VALUE;
        for (int i = 1; i <= Game.players.size(); i++) {
            if (Game.players.get(i) != null && Math.abs(Game.players.get(i).getCurrentPosition() - EnglandPosition) < differPosition) {
                differPosition = Math.abs(Game.players.get(i).getCurrentPosition() - EnglandPosition);
            }
        }
        // 如果有多人离英国的距离相同，那么他们将每人都减去 500 元
        for (int i = 1; i <= Game.players.size(); i++) {
            if (Game.players.get(i) != null && Math.abs(Game.players.get(i).getCurrentPosition() - EnglandPosition) == differPosition) {
                utils.reducePlayerMoney(i, 500);
                System.out.println(utils.getPlayer(i).getPlayerName() + " 花费了 500 元，他的余额为 " + utils.getPlayer(i).getMoney() + " 元");
            }
        }
    }
    private void fun14() {
        utils.reduceCurrentPlayerMoney(200);
        int move = Game.random.nextInt(12) + 1;  // 玩家的移动步数(1~12的随机数)
        System.out.println("您的移动步数为: " + move);
        int playerCurrentPosition = Game.players.get(Game.currentPlayerId).getCurrentPosition();  // 获取玩家当前位置
        int playerAfterPosition = playerCurrentPosition + move;  // 计算玩家移动后的位置
        if (playerAfterPosition >= Map.map.length) {  // 走完一圈
            playerAfterPosition = playerAfterPosition - (Map.map.length - 1);
            Game.players.get(Game.currentPlayerId).setMoney(Game.players.get(Game.currentPlayerId).getMoney() + 2000);  // 奖励 2000 块
        }
        System.out.println("您从 (" + Map.map[playerCurrentPosition][0] + ") 移动到 (" + Map.map[playerAfterPosition][0] + ")");
        Game.players.get(Game.currentPlayerId).setCurrentPosition(playerAfterPosition);  // 更新玩家的当前位置

        /* 分析地块种类并操作 */
        Game.analyzeAndOperate(Game.currentPlayerId, playerAfterPosition);
    }
    private void fun15() {
        int AmericaPosition = 0;
        for (int i = 1; i < Map.map.length; i++) {
            if (Map.map[i][0] == "美国") {
                AmericaPosition = i;
            }
        }
        int differPosition = Integer.MAX_VALUE;
        for (int i = 1; i <= Game.players.size(); i++) {
            if (Game.players.get(i) != null && Math.abs(Game.players.get(i).getCurrentPosition() - AmericaPosition) < differPosition) {
                differPosition = Math.abs(Game.players.get(i).getCurrentPosition() - AmericaPosition);
            }
        }
        // 如果有多人离美国的距离相同，那么他们将每人都减去 700 元
        for (int i = 1; i < Game.players.size(); i++) {
            if (Game.players.get(i) != null && Math.abs(Game.players.get(i).getCurrentPosition() - AmericaPosition) == differPosition) {
                utils.reducePlayerMoney(i, 700);
                System.out.println(utils.getPlayer(i).getPlayerName() + " 被罚了 700 元，他的余额为 " + utils.getPlayer(i).getMoney() + " 元");
            }
        }
    }
}


