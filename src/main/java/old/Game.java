package old;

import old.plot.chance.ChanceManage;
import old.plot.company.Company;
import old.plot.company.CompanyManage;
import old.plot.country.Country;
import old.plot.fate.FateManage;
import old.map.Map;
import old.plot.country.CountryManage;
import old.player.Player;
import old.plot.trainStation.TrainStation;
import old.plot.trainStation.TrainStationManage;
import lombok.Data;

import java.util.*;

@Data
public class Game {
    private static final Scanner sc = new Scanner(System.in);
    public static final Random random = new Random();

    public static final Utils utils = new Utils();  // 工具类
    private static final Map map = new Map();  // 地图
    public static final HashMap<Integer, Player> players = new HashMap<>();  // 玩家
    private static final ChanceManage chanceManage = new ChanceManage();  // 机会卡管理类
    private static final FateManage fateManage = new FateManage();  // 命运卡管理类
    private static final CountryManage countryManage = new CountryManage();  // 国家管理类
    private static final TrainStationManage trainStationManage = new TrainStationManage();  // 火车站管理类
    private static final CompanyManage companyManage = new CompanyManage();  // 公司管理类

    public static int currentPlayerId = 1;  // 当前玩家的 id

    public Game() {

    }

    // 减少玩家的钱
    private static void reducePlayerMoney(int playerId, int money) {
        players.get(playerId).setMoney(players.get(playerId).getMoney() - money);
    }

    // 增加玩家的钱
    private static void addPlayerMoney(int playerId, int money) {
        players.get(playerId).setMoney(players.get(playerId).getMoney() + money);
    }


    // 展示玩家信息
    private static void displayPlayerInformation(Player player) {
        System.out.println("您的资产为 " + player.getMoney() + " 元");
        if (!player.getOwnedCountries().isEmpty()) {  // 如果名下有国家
            System.out.println("您名下的国家有: ");
            for (Integer countryId : player.getOwnedCountries()) {
                Country ownerCountry = CountryManage.countries.get(countryId);
                System.out.println(ownerCountry.getCountryName() + " " + ownerCountry.getHouseNumber() + "房" + ownerCountry.getHotelNumber() + "旅 抵押价: " + ownerCountry.getTotalMortgagePrice() + " 元");
            }
        } else {  // 如果名下无国家
            System.out.println("您的名下暂无国家");
        }
    }

    // 检测输入的合法性
    static int input() {
        int validInput = 0;
        boolean isValid = false;

        while (!isValid) {
            String input = sc.next();
            try {
                // 尝试将输入的字符串转换为整数
                validInput = Integer.parseInt(input);
                isValid = true;
            } catch (NumberFormatException e) {
                System.out.print("输入无效，请输入一个有效的整数: ");
            }
        }
        return validInput;
    }

    // 分析地块种类并操作
    public static void analyzeAndOperate(int playerId, int positionId) {
        String location = Map.map[positionId][0];  // 当前玩家移动后所在的格子
        Player player = players.get(playerId);  // 当前玩家

        switch (location) {
            case "起点":
                // 玩家到达起点，可获得双倍奖励(4000元)
                System.out.println("您到达起点，额外奖励2000元!");
                player.setMoney(player.getMoney() + 2000);  // 因为到达起点算走完一圈会自动奖励2000，所以这里只+2000
                break;
            case "命运":
                // 触发命运事件
                int randomFateId = random.nextInt(FateManage.fateCards.size()) + 1;  // 随机(1~15)抽取命运卡
                System.out.println("您抽取的命运卡为");
                System.out.println(FateManage.fateCards.get(randomFateId).getCardName());
                System.out.println(FateManage.fateCards.get(randomFateId).getCardDescription());
                System.out.println("此卡的效果为: " + FateManage.fateCards.get(randomFateId).getCardEfficiency());
                System.out.println();
                fateManage.fun(randomFateId);  // 生效
                break;
            case "机会":
                // 触发机会事件
                int randomChanceId = random.nextInt(ChanceManage.chanceCards.size()) + 1;  // 随机(1~15)抽取机会卡
                System.out.println("您抽取的机会卡为");
                System.out.println(ChanceManage.chanceCards.get(randomChanceId).getCardName());
                System.out.println(ChanceManage.chanceCards.get(randomChanceId).getCardDescription());
                System.out.println("此卡的效果为: " + ChanceManage.chanceCards.get(randomChanceId).getCardEfficiency());
                chanceManage.fun(randomChanceId);  // 生效
                break;
            case "所得税-付2000元":
                // 玩家需要支付所得税
                reducePlayerMoney(playerId, 2000);
                System.out.println("您向银行支付了 2000 元，您的余额为 " + players.get(playerId).getMoney() + " 元");
                if (players.get(playerId).getMoney() < 0) {
                    System.out.println("您破产了");
                    utils.recoverLand(playerId);
                    players.remove(playerId);
                    isGameOver();  // 检测游戏是否结束
                }
                break;
            case "财产税-交1000元":
                // 玩家需要支付财产税
                reducePlayerMoney(playerId, 1000);
                System.out.println("您向银行支付了 1000 元，您的余额为 " + players.get(playerId).getMoney() + " 元");
                if (players.get(playerId).getMoney() < 0) {
                    System.out.println("您破产了");
                    utils.recoverLand(playerId);
                    players.remove(playerId);
                    isGameOver();  // 检测游戏是否结束
                }
                break;
            case "免费停车场":
                // 玩家到达免费停车场，本回合休息，下一回合正常移动
                System.out.println("本回合您将在停车场休息，下一回合可正常移动");
                break;
            case "坐牢": break;
            case "进牢":
                // 玩家进监狱，下一回合停止移动
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
                break;
            case "自来水公司":
            case "电力公司":
                // 玩家到达公司
                Company company = CompanyManage.companies.get(positionId);
                System.out.println(company.getCompanyName());
                break;
            case "纽约火车站":
            case "巴黎火车站":
            case "东京火车站":
            case "北京火车站":
                // 玩家到达火车站
                TrainStation trainStation = TrainStationManage.trainStations.get(positionId);
                if (trainStation.getOwner() == 0) {  // 如果该火车站属于银行

                } else if (trainStation.getOwner() != playerId) {  // 如果该火车属于其他玩家

                }
                break;
            default:
                // 玩家到达国家
                Country country = CountryManage.countries.get(positionId);
                if (country.getOwner() == 0) {  // 如果该国家属于银行
                    System.out.print("该国家属于银行，售价 " + country.getSellPrice() + " 元，您的资产为 " + player.getMoney() + " 元，是否选择购买？(1 表示确定，0 表示取消): ");
                    int input = input();
                    if (input == 1) {  // 购买
                        if (player.getMoney() >= country.getSellPrice()) {  // 玩家有足够的钱来买下国家
                            reducePlayerMoney(playerId, country.getSellPrice());  // 付钱
                            CountryManage.countries.get(positionId).setOwner(playerId);  // 更新
                            players.get(playerId).getOwnedCountries().add(positionId);  // 更新
                            System.out.println("购买成功!");
                            System.out.println("您的余额为 " + players.get(playerId).getMoney() + " 元");
                        } else {  // 玩家没有足够的钱购买
                            int totalMortgagePrice = 0;
                            for (Integer countryId : player.getOwnedCountries()) {
                                totalMortgagePrice += CountryManage.countries.get(countryId).getTotalMortgagePrice();
                            }
                            if (totalMortgagePrice + player.getMoney() < country.getSellPrice()) {  // 玩家即使抵押所有的地块加上现有的资金也还是买不起此国家
                                System.out.println("很遗憾，经银行检测，即使抵押您名下所有的地块加上您现有的资金也买不起此国家");
                            } else if (players.get(playerId).getOwnedCountries().isEmpty()) {  // 玩家名下没有地块可以抵押
                                System.out.println("很抱歉，经银行检测，您名下没有地块，您也没有足够的资金进行购买");
                            } else {  // 玩家名下有地块可以抵押，且抵押后能买得起
                                System.out.print("您没有足够的钱来购买此国家，是否选择抵押房屋(1 表示确定，0表示取消): ");
                                int input2 = input();
                                if (input2 == 1) {  // 抵押
                                    System.out.println("您名下的房产有: ");
                                    for (Integer countryId : player.getOwnedCountries()) {
                                        Country country1 = CountryManage.countries.get(countryId);
                                        System.out.println(countryId + "." + country1.getCountryName() + ": " + country1.getHouseNumber() + "房" + country1.getHotelNumber() + "旅 抵押价: " + country1.getTotalMortgagePrice() + " 元");
                                    }
                                    while (true) {
                                        System.out.print("请选择抵押哪个地块: ");
                                        int input3 = input();
                                        Country country1 = CountryManage.countries.get(input3);  // 将要被抵押的国家

                                        // 抵押国家，更新数据
                                        players.get(playerId).getOwnedCountries().remove(input3);
                                        players.get(playerId).getMortgageCountries().add(input3);
                                        addPlayerMoney(playerId, country1.getTotalMortgagePrice());

                                        if (player.getMoney() - country.getSellPrice() >= 0) {  // 抵押后可以买的起
                                            // 买国家，更新数据
                                            CountryManage.countries.get(positionId).setOwner(playerId);
                                            players.get(playerId).getOwnedCountries().add(positionId);
                                            reducePlayerMoney(playerId, CountryManage.countries.get(positionId).getSellPrice());

                                            System.out.println("已抵押" + country1.getCountryName() + "，获得 " + country1.getTotalMortgagePrice() + " 元" );
                                            System.out.println("您向银行支付了 " + country.getSellPrice() + " 元，您的余额为 " + players.get(playerId).getMoney() + " 元");
                                            break;
                                        } else {  // 抵押后还是买不起
                                            System.out.println("已抵押" + country1.getCountryName() + "，获得 " + country1.getTotalMortgagePrice() + " 元，还需 " + (country.getSellPrice() - player.getMoney()));
                                        }
                                    }
                                } else {  // 不抵押
                                    System.out.println("取消抵押");
                                }
                            }
                        }
                    } else if (input == 0){  // 不买
                        System.out.println("取消购买");
                    }
                } else if (country.getOwner() == playerId) {  // 如果该国家属于该玩家
                    System.out.println("该国家您已购买，目前有 " + country.getHouseNumber() + "房" + country.getHotelNumber() + "旅，您的资产为 " + player.getMoney() + " 元");
                    if (country.getHouseNumber() == 4) {  // 购买旅馆，可以将 4 栋房屋打造合成一栋旅馆
                        System.out.print("您在该国家已有 4 栋房屋，是否花费 " + country.getBuildHotelPrice() + " 元来将 4 栋房屋打造合成为一栋旅馆？(1 表示确定，0 表示取消): ");
                        int input = input();
                        if (input == 1) {  // 购买
                            if (player.getMoney() >= country.getBuildHotelPrice()) {  // 买得起
                                // 买旅馆，更新数据
                                players.get(playerId).setMoney(players.get(playerId).getMoney() - country.getBuildHotelPrice());
                                CountryManage.countries.get(positionId).setHotelNumber(CountryManage.countries.get(positionId).getHotelNumber() + 1);
                                CountryManage.countries.get(positionId).setHouseNumber(0);

                                System.out.println("购买成功!");
                            } else {  // 买不起
                                System.out.println("很抱歉，您的资金不够，无法买得起房屋");
                            }
                        } else if (input == 0) {  // 不买
                            System.out.println("取消购买");
                        }
                    } else {  // 购买房屋
                        System.out.print("您在该国家已有 " + country.getHouseNumber() + " 栋房屋，是否花费 " + country.getBuildHousePrice() + " 元来再盖一栋房屋？(1 表示确定，0 表示取消): ");
                        int input = input();
                        if (input == 1) {  // 购买
                            if (player.getMoney() >= country.getBuildHousePrice()) {  // 买得起
                                CountryManage.countries.get(positionId).setHouseNumber(CountryManage.countries.get(positionId).getHouseNumber() + 1);
                                System.out.println("购买成功!");
                            } else {  // 买不起
                                System.out.println("很抱歉，您的资金不够，无法买得起房屋");
                            }
                        } else {  // 不买
                            System.out.println("取消购买");
                        }
                    }
                } else {  // 如果该国家属于其他玩家
                    System.out.println("此国家属于 " + players.get(country.getOwner()).getPlayerName() + " ，您需要支付 " + country.getToll() + " 元，您目前有 " + player.getMoney() + " 元");
                    if (player.getMoney() >= country.getToll()) {  // 玩家有足够的钱支付
                        // 一个付钱，另一个收钱，更新数据
                        reducePlayerMoney(playerId, country.getToll());
                        addPlayerMoney(country.getOwner(), country.getToll());

                        System.out.println("经银行检测，您有足够的资金");
                        System.out.println("您已支付 " + country.getToll() + " 元给 " + players.get(country.getOwner()).getPlayerName()) ;
                        System.out.println("您的账户余额为 " + players.get(playerId).getMoney() + " 元");
                    } else {
                        int totalMortgagePrice = 0;
                        for (Integer countryId : player.getOwnedCountries()) {
                            totalMortgagePrice += CountryManage.countries.get(countryId).getTotalMortgagePrice();
                        }
                        if (player.getOwnedCountries().isEmpty()) {  // 玩家名下没有其他地块了-破产
                            addPlayerMoney(country.getOwner(), country.getToll());  // 破产-银行付过路费
                            utils.recoverLand(playerId);
                            players.remove(playerId);
                            System.out.println("很遗憾，您的名下没有其他能抵押的地块了，您破产了");
                            System.out.println("玩家 " + playerId + " 已出局");
                            isGameOver();  // 检测游戏是否结束
                        } else if (totalMortgagePrice + player.getMoney() < country.getToll()) {  // 玩家即使抵押所有的地块加上现有的资金都不够过路费-破产
                            addPlayerMoney(country.getOwner(), country.getToll());  // 破产-银行付过路费
                            utils.recoverLand(playerId);
                            players.remove(playerId);
                            System.out.println("很遗憾，经银行检测，即使抵押您名下所有的地块加上您现有的资金也不能付得起过路费，您破产了");
                            System.out.println("玩家 " + playerId + " 已出局");
                            isGameOver();  // 检测游戏是否结束
                        } else {  // 玩家名下的其他地块的总抵押价和现有的资金足够支付过路费
                            System.out.println("您没有足够的资金去支付过路费，您名下的房产有: ");
                            for (Integer countryId : player.getOwnedCountries()) {
                                Country country1 = CountryManage.countries.get(countryId);
                                System.out.println(countryId + "." + country1.getCountryName() + ": " + country1.getHouseNumber() + "房" + country1.getHotelNumber() + "旅 抵押价: " + country1.getTotalMortgagePrice() + " 元");
                            }
                            while (true) {
                                System.out.print("请选择抵押哪个地块: ");
                                int input2 = input();

                                // 抵押地块，更新数据
                                players.get(playerId).getOwnedCountries().remove(input2);
                                players.get(playerId).getMortgageCountries().add(input2);
                                players.get(playerId).setMoney(players.get(playerId).getMoney() + CountryManage.countries.get(input2).getTotalMortgagePrice());

                                if (player.getMoney() - country.getToll() >= 0) {  // 抵押后可以付得起过路费
                                    // 付过路费，更新数据
                                    players.get(playerId).setMoney(players.get(playerId).getMoney() - country.getToll());  // 该玩家付过路费
                                    players.get(country.getOwner()).setMoney(players.get(country.getOwner()).getMoney() + country.getToll());  // 其他玩家获得过路费

                                    System.out.println("已抵押" + CountryManage.countries.get(input2).getCountryName() + "，获得 " + CountryManage.countries.get(input2).getTotalMortgagePrice() + " 元" );
                                    System.out.println("您向玩家 " + country.getOwner() + " 支付了 " + country.getToll() + " 元，您的余额为 " + players.get(playerId).getMoney());
                                    break;
                                } else {  // 抵押后付不起过路费
                                    System.out.println("已抵押" + CountryManage.countries.get(input2).getCountryName() + "，获得 " + CountryManage.countries.get(input2).getTotalMortgagePrice() + " 元，还需 " + (country.getToll() - player.getMoney()) + " 元");
                                }
                            }
                        }
                    }
                }
        }
    }

    // 有人破产后判断游戏是否结束
    private static void isGameOver() {
        if (players.size() == 1) {
            System.out.println();
            System.out.println("游戏结束!");
            System.out.println("恭喜玩家 " + players.get(players.keySet().iterator().next()).getPlayerName() + " 获得胜利!!!");
            displayPlayerInformation(players.get(players.keySet().iterator().next()));
            System.exit(0);  // 终止程序
        }
    }

    // 赎回地块
    private void redeemLand(int playerId) {
        Player player = players.get(playerId);
        if (!player.getMortgageCountries().isEmpty()) {  // 此玩家有待赎回的地块
            for (Integer countryId : player.getMortgageCountries()) {
                if (player.getMoney() >= CountryManage.countries.get(countryId).getTotalMortgagePrice()) {  // 只要玩家可以赎回其中某一个地块
                    // 正文
                    System.out.print("温馨提醒: 您的资金为" + player.getMoney() + " 元，您有地块待赎回，是否选择赎回地块?(1表示确定，0表示取消): ");
                    int input = input();
                    if (input == 1) {  // 确定
                        for (Integer countryId1 : player.getMortgageCountries()) {
                            Country country1 = CountryManage.countries.get(countryId1);
                            System.out.println(countryId1 + "." + country1.getCountryName() + ": 赎回价: " + country1.getTotalMortgagePrice() + " 元");
                        }
                        while (true) {
                            System.out.print("请选择赎回哪一个国家: ");
                            int input2 = sc.nextInt();
                            if (player.getMoney() >= CountryManage.countries.get(input2).getTotalMortgagePrice()) {  // 有足够的资金赎回
                                players.get(playerId).getOwnedCountries().add(input2);
                                players.get(playerId).getMortgageCountries().remove(input2);
                                players.get(playerId).setMoney(players.get(playerId).getMoney() - CountryManage.countries.get(input2).getTotalMortgagePrice());
                                System.out.println("赎回成功，您的余额为" + players.get(playerId).getMoney() + " 元");
                            } else {  // 资金不够
                                System.out.println("很抱歉，您的资金不足以赎回您想赎回的地块");
                            }
                            if (!players.get(playerId).getMortgageCountries().isEmpty()) {  // 如果还有待赎回的地块
                                System.out.print("是否还要赎回其他的地块？(1 表示确定，0 表示取消): ");
                                int input3 = input();
                                if (input3 == 0) break;
                            } else return;  // 没有待赎回的地块就退出
                        }
                    } else if (input == 2) {  // 取消
                        System.out.println("取消赎回");
                        return;
                    }
                }
            }
        }
    }

    // 创建玩家
    private void createPlayer(int playerNumber, int money) {
        System.out.println();
        System.out.println("请各位玩家输入自己的名称");
        for (int i = 1; i <= playerNumber; i++) {
            System.out.print("第 " + i + " 位玩家的名称是: ");
            String playerName = sc.next();
            Player player = new Player(i, playerName, money);
            players.put(i, player);
            System.out.println("创建成功!");
            System.out.println();
        }
        System.out.println("加载中...");
    }

    // 开始游戏
    public void startGame() {
        System.out.println("欢迎来到大富翁!!!");
        System.out.print("请输入参加游戏的人数: ");
        int number = input();
        System.out.print("请输入玩家的初始资金(建议5000~10000): ");
        int money = input();
        createPlayer(number, money);  // 创建玩家

        System.out.println("地图加载成功，游戏开始!");
        System.out.println();

        for (int k = 1; k < Integer.MAX_VALUE; k++) {
            isGameOver();  // 玩家只剩最后一人，游戏结束
//            map.printMap();  // 打印地图
            System.out.println("------------------------------------- 回合 " + k + " -------------------------------------");
            for (int i = 1; i <= players.size(); i++) {
                Player player = players.get(i);  // 当前玩家
                if (player == null) continue;  // 此玩家已破产跳过他的回合
                System.out.println(player.getPlayerName() + " 的回合开始: ");
                currentPlayerId = i;  // 更新当前玩家
                displayPlayerInformation(players.get(i));  // 展示该玩家的详细信息
                if (player.getIsPause()) {  // 检测玩家是否要暂停一回合
                    players.get(i).setIsPause(false);
                    System.out.println("很抱歉，该回合您无法行动");
                    System.out.println("回合结束");
                    System.out.println();
                    continue;
                }

                /* 赎回地块 */
                redeemLand(i);

                /* 玩家移动 */
                int move = random.nextInt(12) + 1;  // 玩家的移动步数(1~12的随机数)
                System.out.println("您的移动步数为: " + move);
                int playerCurrentPosition = players.get(i).getCurrentPosition();  // 获取玩家当前位置
                int playerAfterPosition = playerCurrentPosition + move;  // 计算玩家移动后的位置
                if (playerAfterPosition >= Map.map.length) {  // 走完一圈
                    System.out.println("您已走完一圈奖励2000元!");
                    playerAfterPosition = playerAfterPosition - (Map.map.length - 1);  //
                    players.get(i).setMoney(players.get(i).getMoney() + 2000);  // 奖励 2000 块
                }
                System.out.println("您从 (" + Map.map[playerCurrentPosition][0] + ") 移动到 (" + Map.map[playerAfterPosition][0] + ")");
                players.get(i).setCurrentPosition(playerAfterPosition);  // 更新玩家的当前位置

                /* 分析地块种类并操作 */
                analyzeAndOperate(i, playerAfterPosition);
                System.out.print("请按任意键结束您的回合: ");
                sc.nextLine();
                System.out.println("回合结束");
                System.out.println();
            }
        }
    }
}
