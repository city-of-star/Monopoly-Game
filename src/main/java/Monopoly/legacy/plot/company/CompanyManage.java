package Monopoly.legacy.plot.company;

import Monopoly.legacy.map.Map;

import java.util.HashMap;

public class CompanyManage {
    public static final HashMap<Integer, Company> companies = new HashMap<>();

    public CompanyManage() {
        for (int i = 1, k = 1; i < Map.map.length; i++) {
            if (Map.map[i][1] == "company") {
                Company company = new Company(k, Map.map[i][0]);
                companies.put(i, company);
            }
        }
    }
}

