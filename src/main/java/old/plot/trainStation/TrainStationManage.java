package old.plot.trainStation;

import old.map.Map;

import java.util.HashMap;

public class TrainStationManage {
    public static final HashMap<Integer, TrainStation> trainStations = new HashMap<>();

    public TrainStationManage() {
        for (int i = 1, k = 1; i < Map.map.length; i++) {
            if (Map.map[i][1] == "trainStation") {
                TrainStation trainStation = new TrainStation(k++, Map.map[i][0]);
                trainStations.put(i, trainStation);
            }
        }
    }
}
