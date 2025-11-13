package Monopoly.legacy.map;

import lombok.Data;

@Data
public class Map {
    public static final String[][] map = new String[41][2];

    /* 打印地图 */
    public void printMap() {
        System.out.println("                                       地图");
        System.out.println(map[21][0] + "  " + map[22][0] + "  " + map[23][0] + "  " + map[24][0] + "  " + map[25][0] + "  " + map[26][0] + "  " + map[27][0] + "  " + map[28][0] + "  " + map[29][0] + "  " + map[30][0] + "  " + map[31][0]);System.out.println();
        System.out.println(map[20][0] + "                                                                       " + map[32][0]);System.out.println();
        System.out.println(map[19][0] + "                                                                       " + map[33][0]);System.out.println();
        System.out.println(map[18][0] + "                                                                       " + map[34][0]);System.out.println();
        System.out.println(map[17][0] + "                                                                     " + map[35][0]);System.out.println();
        System.out.println(map[16][0] + "                                                                  " + map[36][0]);System.out.println();
        System.out.println(map[15][0] + "                                                                      " + map[37][0]);System.out.println();
        System.out.println(map[14][0] + "                                                                       " + map[38][0]);System.out.println();
        System.out.println(map[13][0] + "                                                                    " + map[39][0]);System.out.println();
        System.out.println(map[12][0] + "                                                                       " + map[40][0]);System.out.println();
        System.out.println(map[11][0] + "     " + map[10][0] + "  " + map[9][0] + "  " + map[8][0] + "  " + map[7][0] + "  " + map[6][0] + "  " + map[5][0] + "  " + map[4][0] + "  " + map[3][0] + "  " + map[2][0] + "  " + map[1][0]);System.out.println();
    }

    /*
     * special -> 特殊地块
     * country -> 国家
     * trainStation -> 火车站
     * company -> 公司
     * fate -> 命运
     * chance -> 机会
     * */
    public Map() {
        map[1][0] = "起点"; map[1][1] = "special";
        map[2][0] = "美国"; map[2][1] = "country";
        map[3][0] = "命运"; map[3][1] = "fate";
        map[4][0] = "加拿大"; map[4][1] = "country";
        map[5][0] = "所得税-付2000元"; map[5][1] = "special";
        map[6][0] = "纽约火车站"; map[6][1] = "trainStation";
        map[7][0] = "阿根廷"; map[7][1] = "country";
        map[8][0] = "机会"; map[8][1] = "chance";
        map[9][0] = "墨西哥"; map[9][1] = "country";
        map[10][0] = "古巴"; map[10][1] = "country";
        map[11][0] = "坐牢"; map[11][1] = "special";
        map[12][0] = "法国"; map[12][1] = "country";
        map[13][0] = "电力公司"; map[13][1] = "company";
        map[14][0] = "德国"; map[14][1] = "country";
        map[15][0] = "意大利"; map[15][1] = "country";
        map[16][0] = "巴黎火车站"; map[16][1] = "trainStation";
        map[17][0] = "西班牙"; map[17][1] = "country";
        map[18][0] = "命运"; map[18][1] = "fate";
        map[19][0] = "希腊"; map[19][1] = "country";
        map[20][0] = "荷兰"; map[20][1] = "country";
        map[21][0] = "免费停车场"; map[21][1] = "special";
        map[22][0] = "英国"; map[22][1] = "country";
        map[23][0] = "机会"; map[23][1] = "chance";
        map[24][0] = "俄罗斯"; map[24][1] = "country";
        map[25][0] = "泰国"; map[25][1] = "country";
        map[26][0] = "东京火车站"; map[26][1] = "trainStation";
        map[27][0] = "土耳其"; map[27][1] = "country";
        map[28][0] = "澳大利亚"; map[28][1] = "country";
        map[29][0] = "自来水公司"; map[29][1] = "company";
        map[30][0] = "新加坡"; map[30][1] = "country";
        map[31][0] = "进牢"; map[31][1] = "special";
        map[32][0] = "韩国"; map[32][1] = "country";
        map[33][0] = "中华人民共和国"; map[33][1] = "country";
        map[34][0] = "命运"; map[34][1] = "fate";
        map[35][0] = "中国香港"; map[35][1] = "country";
        map[36][0] = "北京火车站"; map[36][1] = "trainStation";
        map[37][0] = "机会"; map[37][1] = "chance";
        map[38][0] = "日本"; map[38][1] = "country";
        map[39][0] = "财产税-交1000元"; map[39][1] = "special";
        map[40][0] = "巴西"; map[40][1] = "country";
    }
}

