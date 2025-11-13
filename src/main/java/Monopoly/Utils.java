package Monopoly;

import Monopoly.player.Player;
import Monopoly.plot.country.Country;
import Monopoly.plot.country.CountryManage;

public class Utils {
    // 减少当前玩家的资产
    public void reduceCurrentPlayerMoney(int money) {
        Game.players.get(Game.currentPlayerId).setMoney(Game.players.get(Game.currentPlayerId).getMoney() - money);
    }

    // 增加当前玩家的资产
    public void addCurrentPlayerMoney(int money) {
        Game.players.get(Game.currentPlayerId).setMoney(Game.players.get(Game.currentPlayerId).getMoney() + money);
    }

    // 减少指定玩家的资产
    public void reducePlayerMoney(int playerId, int money) {
        Game.players.get(playerId).setMoney(Game.players.get(playerId).getMoney() - money);
    }

    // 增加指定玩家的资产
    public void addPlayerMoney(int playerId, int money) {
        Game.players.get(playerId).setMoney(Game.players.get(playerId).getMoney() + money);
    }

    // 获取当前玩家
    public Player getCurrentPlayer() {
        return Game.players.get(Game.currentPlayerId);
    }

    // 获取指定 id 的玩家
    public Player getPlayer(int playerId) {
        return Game.players.get(playerId);
    }

    // 如果该玩家破产，那么回收这个玩家所拥有的地块
    public void recoverLand(int playerId) {
        for (int countryId : getPlayer(playerId).getOwnedCountries()) {
            CountryManage.countries.get(countryId).setOwner(0);
        }
    }

    // 获取当前玩家所在地块的 id
    public int getCurrentLandId() {
        return Game.players.get(Game.currentPlayerId).getCurrentPosition();
    }

    // 抵押函数(玩家 playerId 需要花费 cost 但是钱不够需要抵押)
    public void mortgage(int playerId, int cost) {

        System.out.println("您名下的房产有: ");
        for (Integer countryId : getCurrentPlayer().getOwnedCountries()) {
            Country country1 = CountryManage.countries.get(countryId);
            System.out.println(countryId + "." + country1.getCountryName() + ": " + country1.getHouseNumber() + "房" + country1.getHotelNumber() + "旅 抵押价: " + country1.getTotalMortgagePrice() + " 元");
        }
        while (true) {
            System.out.print("请选择抵押哪个地块: ");
            int input3 = Game.input();
            Country country1 = CountryManage.countries.get(input3);  // 将要被抵押的国家

            // 抵押国家，更新数据
            Game.players.get(playerId).getOwnedCountries().remove(input3);
            Game.players.get(playerId).getMortgageCountries().add(input3);
            addPlayerMoney(playerId, country1.getTotalMortgagePrice());

//            if (getCurrentPlayer().getMoney() - cost >= 0) {  // 抵押后可以买的起
//                // 买国家，更新数据
//                CountryManage.countries.get(positionId).setOwner(playerId);
//                players.get(playerId).getOwnedCountries().add(positionId);
//                reducePlayerMoney(playerId, CountryManage.countries.get(positionId).getSellPrice());
//
//                System.out.println("已抵押" + country1.getCountryName() + "，获得 " + country1.getTotalMortgagePrice() + " 元" );
//                System.out.println("您向银行支付了 " + country.getSellPrice() + " 元，您的余额为 " + players.get(playerId).getMoney() + " 元");
//                break;
//            } else {  // 抵押后还是买不起
//                System.out.println("已抵押" + country1.getCountryName() + "，获得 " + country1.getTotalMortgagePrice() + " 元，还需 " + (country.getSellPrice() - player.getMoney()));
//            }
        }
    }
}
