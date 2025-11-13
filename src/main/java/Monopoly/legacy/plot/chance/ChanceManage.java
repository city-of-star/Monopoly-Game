package Monopoly.legacy.plot.chance;

import Monopoly.legacy.map.Map;
import Monopoly.legacy.plot.country.CountryManage;

import java.util.HashMap;

public class ChanceManage {
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
}

