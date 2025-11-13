package Monopoly.legacy.plot.country;

import lombok.Data;

@Data
public class Country {
    private int id;
    private String countryName;  // 国家名
    private String cityName;  // 城市名
    private String LandColor;  // 地块颜色
    private int sellPrice;  // 销售价格
    private int basicToll;  // 基础过路费
    private int l1HouseToll;  // 一幢房屋过路费
    private int l2HouseToll;  // 两幢房屋过路费
    private int l3HouseToll;  // 三幢房屋过路费
    private int l4HouseToll;  // 四幢房屋过路费
    private int hotelToll;  // 一幢旅馆过路费
    private int buildHousePrice;  // 房屋建筑费
    private int buildHotelPrice;  // 旅馆建筑费
    private int mortgagePrice;  // 抵押价

    private int owner = 0;  // 拥有者(0->银行; 1、2、3->玩家)
    private Boolean MortgageStatus = false;  // 抵押状态(false->未抵押; true->已抵押)
    private int houseNumber = 0;  // 房屋数量
    private int hotelNumber = 0;  // 旅馆数量
    private Boolean doubleStatus = false;  // 过路费翻倍状态

    public int getTotalMortgagePrice() {
        return (houseNumber * buildHousePrice + hotelNumber * buildHotelPrice) / 2 + mortgagePrice;
    }

    // 计算过路费
    public int getToll() {
        int toll = basicToll;
        if (houseNumber == 0 && hotelNumber == 0) return toll;
        switch (houseNumber) {
            case 1: toll += l1HouseToll;break;
            case 2: toll += l2HouseToll;break;
            case 3: toll += l3HouseToll;break;
            case 4: toll += l4HouseToll;break;
        }
        toll += hotelNumber * hotelToll;
        return toll;
    }

    public Country(int id, String countryName, String cityName, String LandColor, int sellPrice, int basicToll, int l1HouseToll, int l2HouseToll, int l3HouseToll, int l4HouseToll, int hotelToll, int buildHousePrice, int buildHotelPrice, int mortgagePrice) {
        this.id = id;
        this.countryName = countryName;
        this.cityName = cityName;
        this.LandColor = LandColor;
        this.sellPrice = sellPrice;
        this.basicToll = basicToll;
        this.l1HouseToll = l1HouseToll;
        this.l2HouseToll = l2HouseToll;
        this.l3HouseToll = l3HouseToll;
        this.l4HouseToll = l4HouseToll;
        this.hotelToll = hotelToll;
        this.buildHousePrice = buildHousePrice;
        this.buildHotelPrice = buildHotelPrice;
        this.mortgagePrice = mortgagePrice;
    }
}

