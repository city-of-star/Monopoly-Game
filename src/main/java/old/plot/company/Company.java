package old.plot.company;

import lombok.Data;

@Data
public class Company {  // 过路费为所转转盘数的 10 倍，若同一玩家拥有两个公司则为 100 倍
    private int id;
    private String CompanyName;
    private int mortgagePrice = 750;  // 抵押价
    private int owner = 0;  // 拥有者(0->银行; 1、2、3->玩家)


    public Company(int id, String CompanyName) {
        this.id = id;
        this.CompanyName = CompanyName;
    }
}
