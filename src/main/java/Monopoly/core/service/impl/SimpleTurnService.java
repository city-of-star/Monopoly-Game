package Monopoly.core.service.impl;

import Monopoly.core.domain.entity.player.Player;
import Monopoly.core.domain.entity.tile.*;
import Monopoly.core.domain.state.PropertyState;
import Monopoly.core.event.GameEvent;
import Monopoly.core.event.InteractiveEvent;
import Monopoly.core.ports.DecisionPort;
import Monopoly.core.repo.PlayerRepository;
import Monopoly.core.repo.TileRepository;
import Monopoly.core.service.TurnService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 实现功能【最小可玩的回合推进：掷骰、移动、落地触发购买/过路费等基础逻辑】。
 */
public class SimpleTurnService implements TurnService {

    private final PlayerRepository playerRepository;
    private final TileRepository tileRepository;
    private final DecisionPort decisionPort;
    private final Random random = new Random();
    private int currentPlayerIndex = 0;
    private boolean gameOver = false;
    private int turnCounter = 0;
    /**
     * 记录地块所有者（仅针对可购买地块）。
     */
    private final Map<Integer, Integer> tileOwners = new HashMap<>();
    
    /**
     * 记录地块状态（房屋数、旅馆数、抵押状态）。
     */
    private final Map<Integer, PropertyState> propertyStates = new HashMap<>();

    /**
     * 构造函数。
     *
     * @param playerRepository 玩家仓库
     * @param tileRepository 地块仓库
     * @param decisionPort 决策端口
     */
    public SimpleTurnService(PlayerRepository playerRepository, TileRepository tileRepository, DecisionPort decisionPort) {
        this.playerRepository = playerRepository;
        this.tileRepository = tileRepository;
        this.decisionPort = decisionPort;
    }

    @Override
    public GameEvent advanceTurn() {
        var players = playerRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Player::getId))
                .toList();
        if (players.isEmpty()) {
            gameOver = true;
            return new TextEvent(null, "暂无玩家，游戏结束");
        }
        // 如果当前索引超出（玩家可能被淘汰）
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
        Player player = players.get(currentPlayerIndex);
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        turnCounter++;
        
        String fromName = getTileName(player.getPosition());

        // 玩家回合开始时，检查是否有可赎回的地块（包括国家、公司、火车站）
        List<Tile> mortgagedTiles = getMortgagedTilesForPlayer(player);
        if (!mortgagedTiles.isEmpty()) {
            return new RedeemChoiceEvent(player, mortgagedTiles, turnCounter, fromName);
        }

        // 执行回合的游戏流程
        return executeTurn(player, turnCounter, fromName);
    }

    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    private int rollDice() {
        return random.nextInt(6) + 1 + random.nextInt(6) + 1;
    }

    private int movePlayer(Player player, int dice) {
        int newPos = player.getPosition() + dice;
        if (newPos > 40) {
            newPos -= 40;
            player.setMoney(player.getMoney() + 2000);
        }
        player.setPosition(newPos);
        playerRepository.save(player);
        return newPos;
    }

    private GameEvent handleSpecialTile(Player player, SpecialTile tile, boolean passedGo, boolean isOnGo,
                                       String header, String locationLine) {
        return switch (tile.getCategory()) {
            case GO -> {
                String detail;
                if (passedGo && isOnGo) {
                    // 经过并停留在起点，已奖励2000（经过）+ 2000（停留）= 4000
                    detail = "经过并停留在起点，奖励 4000元（经过2000 + 停留2000），现金：" + formatMoney(player.getMoney());
                } else if (passedGo) {
                    // 只经过起点，已在movePlayer中奖励2000
                    detail = "经过起点，已奖励 2000元，现金：" + formatMoney(player.getMoney());
                } else if (isOnGo) {
                    // 停留在起点（没有经过），奖励4000元
                    player.setMoney(player.getMoney() + 4000);
                    playerRepository.save(player);
                    detail = "停留在起点，奖励 4000元，现金：" + formatMoney(player.getMoney());
                } else {
                    detail = "起点，无奖励，现金：" + formatMoney(player.getMoney());
                }
                yield new TurnSummaryEvent(player, buildSummary(header, locationLine, detail, player));
            }
            case TAX -> {
                int tax = tile.getName().contains("2000") ? 2000 : 1000;
                if (player.getMoney() >= tax) {
                    // 钱够，直接支付
                    payMoney(player, tax);
                    playerRepository.save(player);
                    String detail = "支付税费 " + formatMoney(tax) + "，现金：" + formatMoney(player.getMoney());
                    yield new TurnSummaryEvent(player, buildSummary(header, locationLine, detail, player));
                } else {
                    // 钱不够，需要抵押或破产
                    String body = "需支付税费 " + formatMoney(tax) + "，但您的现金不足(" + formatMoney(player.getMoney()) + ")。";
                    yield new TaxPaymentPromptEvent(player, tile, tax, buildSummary(header, locationLine, body, player));
                }
            }
            case JAIL -> {
                // 经过或停在坐牢都没有影响
                String detail = "经过或停留在坐牢，无影响。";
                yield new TurnSummaryEvent(player, buildSummary(header, locationLine, detail, player));
            }
            case GO_TO_JAIL -> {
                // 停在进牢时，立即移动到坐牢位置（11），不算经过起点
                int jailPosition = 11; // 坐牢位置
                player.setPosition(jailPosition);
                player.setJailTurnsRemaining(2); // 此回合和下回合无法行动
                playerRepository.save(player);
                String detail = "停在进牢，立即被送入坐牢。此回合和下回合无法行动，只能展示基本信息。";
                yield new TurnSummaryEvent(player, buildSummary(header, locationLine, detail, player));
            }
            case FREE_PARKING -> {
                String detail = "免费停车休息，无额外事件。";
                yield new TurnSummaryEvent(player, buildSummary(header, locationLine, detail, player));
            }
            default -> {
                String detail = "暂无额外事件。";
                yield new TurnSummaryEvent(player, buildSummary(header, locationLine, detail, player));
            }
        };
    }

    private GameEvent createCountryEvent(List<Player> players, Player player, CountryTile tile,
                                         String header, String locationLine) {
        int position = tile.getPosition();
        Integer ownerId = tileOwners.get(position);
        if (ownerId == null) {
            int price = tile.getSellPrice();
            String body = "该国家无人持有，售价 " + formatMoney(price)
                    + "，基础过路费 " + formatMoney(tile.getBaseToll())
                    + "。\n当前现金：" + formatMoney(player.getMoney())
                    + "，稍后将询问是否购买。";
            String preMessage = buildSummary(header, locationLine, body, player);
            return new PurchasePromptEvent(player, tile, preMessage);
        }
        if (ownerId.equals(player.getId())) {
            // 玩家停留在自己的国家上，可以建造房屋/旅馆
            PropertyState state = getPropertyState(position);
            String statusInfo = getPropertyStatusInfo(tile, state);
            String body = "这是您自己的国家。" + statusInfo;
            // 如果未抵押且可以建造，可以建造
            if (state.canBuild()) {
                return new BuildPromptEvent(player, tile, state, buildSummary(header, locationLine, body, player));
            } else {
                return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
            }
        }
        Optional<Player> ownerOpt = players.stream().filter(p -> p.getId() == ownerId).findFirst();
        if (ownerOpt.isEmpty()) {
            String body = "该国家原有者不存在，暂不收费。";
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        }
        Player owner = ownerOpt.get();
        PropertyState state = getPropertyState(position);
        
        // 如果已抵押，不需要支付过路费
        if (state.isMortgaged()) {
            String body = "该地块已抵押给银行，无需支付过路费。";
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        }
        
        // 计算过路费（包括房屋/旅馆和垄断翻倍）
        int toll = calculateToll(tile, state, ownerId);
        if (player.getMoney() >= toll) {
            payMoney(player, toll);
            owner.setMoney(owner.getMoney() + toll);
            playerRepository.save(owner);
            String body = "需向 " + owner.getName() + " 支付过路费 " + formatMoney(toll)
                    + "。\n您的现金：" + formatMoney(player.getMoney())
                    + "；" + owner.getName() + " 现金：" + formatMoney(owner.getMoney());
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        } else {
            // 钱不够，需要抵押或破产
            String body = "需向 " + owner.getName() + " 支付过路费 " + formatMoney(toll)
                    + "，但您的现金不足(" + formatMoney(player.getMoney()) + ")。";
            return new TollPaymentPromptEvent(player, owner, tile, toll, buildSummary(header, locationLine, body, player));
        }
    }

    /**
     * 创建火车站地块事件。
     */
    private GameEvent createTrainStationEvent(List<Player> players, Player player, TrainStationTile tile,
                                               String header, String locationLine) {
        int position = tile.getPosition();
        Integer ownerId = tileOwners.get(position);
        if (ownerId == null) {
            // 无人持有，可以购买
            int price = tile.getSellPrice();
            String body = "该火车站无人持有，售价 " + formatMoney(price)
                    + "，抵押价格 " + formatMoney(tile.getMortgagePrice())
                    + "。\n当前现金：" + formatMoney(player.getMoney())
                    + "，稍后将询问是否购买。";
            String preMessage = buildSummary(header, locationLine, body, player);
            return new TrainStationPurchasePromptEvent(player, tile, preMessage);
        }
        if (ownerId.equals(player.getId())) {
            // 玩家停留在自己的火车站上
            PropertyState state = getPropertyState(position);
            String statusInfo = state.isMortgaged() ? "【已抵押】" : "这是您自己的火车站。";
            String body = statusInfo;
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        }
        Optional<Player> ownerOpt = players.stream().filter(p -> p.getId() == ownerId).findFirst();
        if (ownerOpt.isEmpty()) {
            String body = "该火车站原有者不存在，暂不收费。";
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        }
        Player owner = ownerOpt.get();
        PropertyState state = getPropertyState(position);
        
        // 如果已抵押，不需要支付过路费
        if (state.isMortgaged()) {
            String body = "该火车站已抵押给银行，无需支付过路费。";
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        }
        
        // 计算过路费（根据拥有车站数量）
        // 注意：只统计未抵押的车站
        int stationCount = countOwnedTrainStations(ownerId); // 只统计未抵押的车站数量
        int toll = calculateTrainStationToll(tile, stationCount);
        
        String tollDescription = "拥有" + stationCount + "个未抵押的火车站，过路费：" + formatMoney(toll);
        
        if (player.getMoney() >= toll) {
            payMoney(player, toll);
            owner.setMoney(owner.getMoney() + toll);
            playerRepository.save(owner);
            String body = tollDescription + "\n需向 " + owner.getName() + " 支付过路费 " + formatMoney(toll)
                    + "。\n您的现金：" + formatMoney(player.getMoney())
                    + "；" + owner.getName() + " 现金：" + formatMoney(owner.getMoney());
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        } else {
            // 钱不够，需要抵押或破产
            String body = tollDescription + "\n需向 " + owner.getName() + " 支付过路费 " + formatMoney(toll)
                    + "，但您的现金不足(" + formatMoney(player.getMoney()) + ")。";
            return new TrainStationTollPaymentPromptEvent(player, owner, tile, toll, buildSummary(header, locationLine, body, player));
        }
    }

    /**
     * 计算火车站过路费。
     * 规则：根据拥有未抵押的车站数量，1个=250，2个=500，3个=1000，4个=2000。
     */
    private int calculateTrainStationToll(TrainStationTile tile, int stationCount) {
        if (stationCount <= 0 || stationCount > 4) {
            return 0;
        }
        // tollByOwnership数组索引从0开始，所以stationCount-1
        return tile.getTollByOwnership()[stationCount - 1];
    }

    /**
     * 统计玩家拥有的未抵押火车站数量。
     * 注意：只统计未抵押的车站。如果玩家拥有4个车站但其中1个被抵押，则只返回3。
     */
    private int countOwnedTrainStations(int playerId) {
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : tileOwners.entrySet()) {
            if (entry.getValue().equals(playerId)) {
                Tile tile = tileRepository.findByPosition(entry.getKey()).orElse(null);
                if (tile instanceof TrainStationTile) {
                    PropertyState state = getPropertyState(entry.getKey());
                    // 只计算未抵押的车站
                    if (!state.isMortgaged()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * 创建公司地块事件。
     */
    private GameEvent createCompanyEvent(List<Player> players, Player player, CompanyTile tile,
                                         String header, String locationLine) {
        int position = tile.getPosition();
        Integer ownerId = tileOwners.get(position);
        if (ownerId == null) {
            // 无人持有，可以购买
            int price = tile.getSellPrice();
            String body = "该公司无人持有，售价 " + formatMoney(price)
                    + "，抵押价格 " + formatMoney(tile.getMortgagePrice())
                    + "。\n当前现金：" + formatMoney(player.getMoney())
                    + "，稍后将询问是否购买。";
            String preMessage = buildSummary(header, locationLine, body, player);
            return new CompanyPurchasePromptEvent(player, tile, preMessage);
        }
        if (ownerId.equals(player.getId())) {
            // 玩家停留在自己的公司上
            PropertyState state = getPropertyState(position);
            String statusInfo = state.isMortgaged() ? "【已抵押】" : "这是您自己的公司。";
            String body = statusInfo;
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        }
        Optional<Player> ownerOpt = players.stream().filter(p -> p.getId() == ownerId).findFirst();
        if (ownerOpt.isEmpty()) {
            String body = "该公司原有者不存在，暂不收费。";
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        }
        Player owner = ownerOpt.get();
        PropertyState state = getPropertyState(position);
        
        // 如果已抵押，不需要支付过路费
        if (state.isMortgaged()) {
            String body = "该公司已抵押给银行，无需支付过路费。";
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        }
        
        // 计算过路费（转盘1-16，根据拥有公司数量计算）
        // 注意：只统计未抵押的公司，如果有一个公司被抵押，则只按一个公司计算
        int diceRoll = random.nextInt(16) + 1; // 转动转盘（1-16）
        int companyCount = countOwnedCompanies(ownerId); // 只统计未抵押的公司数量
        int multiplier = companyCount == 2 ? 100 : 10;
        int toll = diceRoll * multiplier;
        
        String tollDescription = "转盘数字：" + diceRoll + "，";
        if (companyCount == 2) {
            tollDescription += "拥有两个未抵押的公司，费用 = " + diceRoll + " × 100 = " + formatMoney(toll);
        } else {
            tollDescription += "拥有" + companyCount + "个未抵押的公司，费用 = " + diceRoll + " × 10 = " + formatMoney(toll);
        }
        
        if (player.getMoney() >= toll) {
            payMoney(player, toll);
            owner.setMoney(owner.getMoney() + toll);
            playerRepository.save(owner);
            String body = tollDescription + "\n需向 " + owner.getName() + " 支付过路费 " + formatMoney(toll)
                    + "。\n您的现金：" + formatMoney(player.getMoney())
                    + "；" + owner.getName() + " 现金：" + formatMoney(owner.getMoney());
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        } else {
            // 钱不够，需要抵押或破产
            String body = tollDescription + "\n需向 " + owner.getName() + " 支付过路费 " + formatMoney(toll)
                    + "，但您的现金不足(" + formatMoney(player.getMoney()) + ")。";
            return new CompanyTollPaymentPromptEvent(player, owner, tile, toll, buildSummary(header, locationLine, body, player));
        }
    }


    /**
     * 统计玩家拥有的未抵押公司数量。
     * 注意：只统计未抵押的公司。如果玩家拥有两个公司但其中一个被抵押，
     * 则只返回1，过路费按×10计算；只有两个公司都未抵押时，才返回2，过路费按×100计算。
     */
    private int countOwnedCompanies(int playerId) {
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : tileOwners.entrySet()) {
            if (entry.getValue().equals(playerId)) {
                Tile tile = tileRepository.findByPosition(entry.getKey()).orElse(null);
                if (tile instanceof CompanyTile) {
                    PropertyState state = getPropertyState(entry.getKey());
                    // 只计算未抵押的公司
                    if (!state.isMortgaged()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * 文本事件：最小可玩版本用于输出描述性文本。
     */
    public static class TextEvent extends GameEvent {
        private final String text;
        public TextEvent(String playerId, String text) {
            super(playerId);
            this.text = text;
        }
        @Override
        public String getType() { return "Text"; }
        @Override
        public String toString() { return text; }
    }

    /**
     * 购地交互事件：先输出位置描述，再触发购地决策。
     */
    private class PurchasePromptEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final CountryTile tile;

        PurchasePromptEvent(Player player, CountryTile tile, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.tile = tile;
        }

        @Override
        public GameEvent interact() {
            int price = tile.getSellPrice();
            String prompt = "\n[购地选择] " + player.getName() + " 当前现金 " + formatMoney(player.getMoney())
                    + "，是否以 " + formatMoney(price) + " 购买 [" + tile.getName() + "]？"
                    + "（基础过路费 " + formatMoney(tile.getBaseToll()) + "，1=购买，0=放弃）: ";
            int choice = decisionPort.requestInt(prompt);
            String message;
            if (choice == 1) {
                if (player.getMoney() >= price) {
                    payMoney(player, price);
                    tileOwners.put(tile.getPosition(), player.getId());
                    player.addOwnedTile(tile.getPosition());
                    message = "玩家 " + player.getName() + " 以 " + formatMoney(price)
                            + " 购入 [" + tile.getName() + "]。";
                    playerRepository.save(player);
                    String summary = buildResultSummary("购地结果", message, player);
                    return new TurnSummaryEvent(player, summary);
                } else {
                    // 钱不够，需要抵押
                    int shortage = price - player.getMoney();
                    String body = "现金不足，需要 " + formatMoney(price) + "，当前只有 " + formatMoney(player.getMoney())
                            + "，还差 " + formatMoney(shortage) + "。";
                    return new MortgageForPurchaseEvent(player, tile, price, buildResultSummary("购地", body, player));
                }
            } else {
                message = "玩家 " + player.getName() + " 放弃购买 [" + tile.getName() + "]。";
                playerRepository.save(player);
                String summary = buildResultSummary("购地结果", message, player);
                return new TurnSummaryEvent(player, summary);
            }
        }
    }

    /**
     * 购买公司交互事件：先输出位置描述，再触发购买决策。
     */
    private class CompanyPurchasePromptEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final CompanyTile tile;

        CompanyPurchasePromptEvent(Player player, CompanyTile tile, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.tile = tile;
        }

        @Override
        public GameEvent interact() {
            int price = tile.getSellPrice();
            String prompt = "\n[购买公司] " + player.getName() + " 当前现金 " + formatMoney(player.getMoney())
                    + "，是否以 " + formatMoney(price) + " 购买 [" + tile.getName() + "]？"
                    + "（抵押价格 " + formatMoney(tile.getMortgagePrice()) + "，1=购买，0=放弃）: ";
            int choice = decisionPort.requestInt(prompt);
            String message;
            if (choice == 1) {
                if (player.getMoney() >= price) {
                    payMoney(player, price);
                    tileOwners.put(tile.getPosition(), player.getId());
                    player.addOwnedTile(tile.getPosition());
                    message = "玩家 " + player.getName() + " 以 " + formatMoney(price)
                            + " 购入 [" + tile.getName() + "]。";
                    playerRepository.save(player);
                    String summary = buildResultSummary("购买公司结果", message, player);
                    return new TurnSummaryEvent(player, summary);
                } else {
                    // 钱不够，需要抵押
                    int shortage = price - player.getMoney();
                    String body = "现金不足，需要 " + formatMoney(price) + "，当前只有 " + formatMoney(player.getMoney())
                            + "，还差 " + formatMoney(shortage) + "。";
                    return new CompanyMortgageForPurchaseEvent(player, tile, price, buildResultSummary("购买公司", body, player));
                }
            } else {
                message = "玩家 " + player.getName() + " 放弃购买 [" + tile.getName() + "]。";
                playerRepository.save(player);
                String summary = buildResultSummary("购买公司结果", message, player);
                return new TurnSummaryEvent(player, summary);
            }
        }
    }

    private class TurnSummaryEvent extends TextEvent implements InteractiveEvent {
        TurnSummaryEvent(Player player, String message) {
            super(String.valueOf(player.getId()), message);
        }

        @Override
        public GameEvent interact() {
            decisionPort.requestLine("\n(按回车继续下一位玩家)\n");
            return null;
        }
    }

    private void payMoney(Player player, int amount) {
        player.setMoney(player.getMoney() - amount);
    }

    /**
     * 通用的支付处理方法。
     * 如果玩家现金足够，直接支付；如果不够，触发抵押流程。
     * 
     * @param player 需要支付的玩家
     * @param amount 需要支付的金额
     * @param recipient 收款人（如果是null，表示支付给银行）
     * @param description 支付描述（用于显示消息）
     * @param header 回合头部信息
     * @param locationLine 位置信息
     * @return 游戏事件
     */
    private GameEvent handlePayment(Player player, int amount, Player recipient, String description,
                                   String header, String locationLine) {
        if (player.getMoney() >= amount) {
            // 现金足够，直接支付
            payMoney(player, amount);
            if (recipient != null) {
                recipient.setMoney(recipient.getMoney() + amount);
                playerRepository.save(recipient);
            }
            playerRepository.save(player);
            String body = description + " " + formatMoney(amount);
            if (recipient != null) {
                body += "。\n您的现金：" + formatMoney(player.getMoney())
                        + "；" + recipient.getName() + " 现金：" + formatMoney(recipient.getMoney());
            } else {
                body += "，现金：" + formatMoney(player.getMoney());
            }
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        } else {
            // 现金不足，需要抵押或破产
            String body = description + " " + formatMoney(amount)
                    + "，但您的现金不足(" + formatMoney(player.getMoney()) + ")。";
            if (recipient != null) {
                // 支付给其他玩家（过路费）
                if (description.contains("过路费")) {
                    // 需要知道是哪个地块，这里简化处理，使用通用支付事件
                    return new GenericPaymentPromptEvent(player, recipient, amount, description, 
                            buildSummary(header, locationLine, body, player));
                }
            }
            // 支付给银行（税费、卡牌费用等）
            return new GenericPaymentPromptEvent(player, null, amount, description, 
                    buildSummary(header, locationLine, body, player));
        }
    }

    private String describeAssets(Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append("- 现金：").append(formatMoney(player.getMoney())).append("\n");
        sb.append("- 地产：");
        if (player.getOwnedTilePositions().isEmpty()) {
            sb.append("无");
        } else {
            String owned = player.getOwnedTilePositions().stream()
                    .sorted()
                    .map(pos -> {
                        Tile tile = tileRepository.findByPosition(pos).orElse(null);
                        if (tile == null) {
                            return "格" + pos;
                        }
                        String tileName = tile.getName();
                        // 如果是国家地块，显示房屋或旅馆数量
                        if (tile instanceof CountryTile) {
                            PropertyState state = getPropertyState(pos);
                            if (state.getHotelCount() > 0) {
                                tileName += "（" + state.getHotelCount() + "旅馆）";
                            } else if (state.getHouseCount() > 0) {
                                tileName += "（" + state.getHouseCount() + "房屋）";
                            }
                        }
                        return tileName;
                    })
                    .reduce((a, b) -> a + "，" + b)
                    .orElse("无");
            sb.append(owned);
        }
        return sb.toString();
    }

    private String formatMoney(int amount) {
        return amount + "元";
    }

    private String buildTurnHeader(int turn, Player player, int dice,
                                   int oldPos, String fromName, int newPos, String toName) {
        return "==================== 回合 " + turn + " ====================\n"
                + "玩家：" + player.getName() + "\n"
                + "掷骰：" + dice + "\n"
                + "移动：" + fromName + " -> " + toName + "\n\n";
    }

    private String buildPausedSummary(int turn, Player player, String tileName, int position) {
        StringBuilder sb = new StringBuilder();
        sb.append("==================== 回合 ").append(turn).append(" ====================\n");
        sb.append("玩家：").append(player.getName()).append("\n");
        sb.append("状态：暂停一回合\n");
        sb.append("当前位置：").append(tileName).append("\n\n");
        sb.append("事件：玩家被暂停，跳过本次行动。\n\n");
        sb.append("资产概览：\n").append(describeAssets(player));
        return sb.toString();
    }

    private String buildJailSummary(int turn, Player player, String tileName, int position, int remainingTurns) {
        StringBuilder sb = new StringBuilder();
        sb.append("==================== 回合 ").append(turn).append(" ====================\n");
        sb.append("玩家：").append(player.getName()).append("\n");
        sb.append("状态：在监狱中（剩余 ").append(remainingTurns).append(" 回合）\n");
        sb.append("当前位置：").append(tileName).append("\n\n");
        if (remainingTurns > 1) {
            sb.append("事件：玩家在监狱中，无法行动，只能展示基本信息。\n\n");
        } else {
            sb.append("事件：玩家在监狱中，这是最后一回合，下回合可以正常行动。\n\n");
        }
        sb.append("资产概览：\n").append(describeAssets(player));
        return sb.toString();
    }

    private String buildSummary(String header, String locationLine, String body, Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append(header);
        sb.append(locationLine);
        if (body != null && !body.isBlank()) {
            sb.append("事件：").append(body).append("\n");
        }
        sb.append("\n资产概览：\n").append(describeAssets(player));
        return sb.toString();
    }

    private String buildResultSummary(String title, String body, Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append(">>>> ").append(title).append(" <<<<\n");
        if (body != null && !body.isBlank()) {
            sb.append(body).append("\n");
        }
        sb.append("\n资产概览：\n").append(describeAssets(player));
        return sb.toString();
    }

    private String getTileName(int position) {
        return tileRepository.findByPosition(position)
                .map(Tile::getName)
                .orElse(position == 1 ? "起点" : ("格" + position));
    }

    /**
     * 获取地块状态，如果不存在则创建新的。
     */
    private PropertyState getPropertyState(int position) {
        return propertyStates.computeIfAbsent(position, k -> new PropertyState());
    }

    /**
     * 计算过路费。
     * 注意：根据用户说明，houseToll 数组中的值已经包含了 baseToll，所以不需要再加 baseToll。
     */
    private int calculateToll(CountryTile tile, PropertyState state, int ownerId) {
        int toll;
        
        if (state.getHotelCount() > 0) {
            // 有旅馆，使用旅馆过路费（已包含 baseToll）
            toll = tile.getHotelToll();
        } else if (state.getHouseCount() > 0) {
            // 有房屋，使用对应房屋数的过路费（已包含 baseToll）
            int houseIndex = state.getHouseCount() - 1;
            if (houseIndex < tile.getHouseToll().length) {
                toll = tile.getHouseToll()[houseIndex];
            } else {
                toll = tile.getBaseToll();
            }
        } else {
            // 空地，使用基础过路费
            toll = tile.getBaseToll();
        }
        
        // 检查是否垄断（拥有同颜色的所有地块）
        if (hasMonopoly(ownerId, tile.getColor())) {
            toll *= 2;
        }
        
        return toll;
    }

    /**
     * 检查玩家是否拥有同颜色的所有地块（垄断）。
     */
    private boolean hasMonopoly(int playerId, String color) {
        // 获取所有同颜色的地块
        List<CountryTile> sameColorTiles = tileRepository.findAll().stream()
                .filter(t -> t instanceof CountryTile)
                .map(t -> (CountryTile) t)
                .filter(t -> color.equals(t.getColor()))
                .collect(Collectors.toList());
        
        // 检查玩家是否拥有所有这些地块
        return sameColorTiles.stream()
                .allMatch(t -> {
                    Integer owner = tileOwners.get(t.getPosition());
                    return owner != null && owner.equals(playerId);
                });
    }

    /**
     * 获取地块状态信息字符串。
     */
    private String getPropertyStatusInfo(CountryTile tile, PropertyState state) {
        StringBuilder sb = new StringBuilder();
        if (state.isMortgaged()) {
            sb.append("【已抵押】");
        }
        if (state.getHouseCount() > 0) {
            sb.append("房屋数：").append(state.getHouseCount()).append("，");
        }
        if (state.getHotelCount() > 0) {
            sb.append("旅馆数：").append(state.getHotelCount()).append("，");
        }
        if (!state.isMortgaged() && state.getHouseCount() == 0 && state.getHotelCount() == 0) {
            sb.append("空地，");
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '，') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 建造提示事件：玩家停留在自己的地块上时可以建造。
     */
    private class BuildPromptEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final CountryTile tile;
        private final PropertyState state;

        BuildPromptEvent(Player player, CountryTile tile, PropertyState state, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.tile = tile;
            this.state = state;
        }

        @Override
        public GameEvent interact() {
            StringBuilder options = new StringBuilder();
            options.append("\n[建造选择] ").append(player.getName()).append(" 当前现金 ").append(formatMoney(player.getMoney()));
            options.append("，地块：").append(tile.getName());
            
            if (state.canBuildHouse()) {
                options.append("\n  1 = 建造房屋（费用：").append(formatMoney(tile.getBuildHouseCost())).append("）");
            }
            if (state.canBuildHotel()) {
                options.append("\n  2 = 建造旅馆（费用：").append(formatMoney(tile.getBuildHotelCost())).append("，将移除4幢房屋）");
            }
            options.append("\n  0 = 不建造\n请选择: ");
            
            int choice = decisionPort.requestInt(options.toString());
            String message;
            
            if (choice == 1 && state.canBuildHouse()) {
                int cost = tile.getBuildHouseCost();
                if (player.getMoney() >= cost) {
                    payMoney(player, cost);
                    state.buildHouse();
                    message = "玩家 " + player.getName() + " 在 [" + tile.getName() + "] 建造了1幢房屋，花费 " + formatMoney(cost)
                            + "。当前房屋数：" + state.getHouseCount();
                } else {
                    message = "现金不足，无法建造房屋（需要 " + formatMoney(cost) + "，当前 " + formatMoney(player.getMoney()) + "）。";
                }
            } else if (choice == 2 && state.canBuildHotel()) {
                int cost = tile.getBuildHotelCost();
                if (player.getMoney() >= cost) {
                    payMoney(player, cost);
                    state.buildHotel();
                    message = "玩家 " + player.getName() + " 在 [" + tile.getName() + "] 建造了1幢旅馆，花费 " + formatMoney(cost)
                            + "（已移除4幢房屋）。当前旅馆数：" + state.getHotelCount();
                } else {
                    message = "现金不足，无法建造旅馆（需要 " + formatMoney(cost) + "，当前 " + formatMoney(player.getMoney()) + "）。";
                }
            } else {
                message = "玩家 " + player.getName() + " 选择不建造。";
            }
            
            playerRepository.save(player);
            String summary = buildResultSummary("建造结果", message, player);
            return new TurnSummaryEvent(player, summary);
        }
    }

    /**
     * 抵押提示事件：玩家可以抵押自己的地块。
     */
    private class MortgagePromptEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final CountryTile tile;
        private final PropertyState state;

        MortgagePromptEvent(Player player, CountryTile tile, PropertyState state, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.tile = tile;
            this.state = state;
        }

        @Override
        public GameEvent interact() {
            int mortgageValue = calculateMortgageValue(tile, state);
            String prompt = "\n[抵押选择] " + player.getName() + " 当前现金 " + formatMoney(player.getMoney())
                    + "，地块：" + tile.getName()
                    + "\n  抵押价值：" + formatMoney(mortgageValue)
                    + "\n  1 = 抵押，0 = 取消\n请选择: ";
            int choice = decisionPort.requestInt(prompt);
            String message;
            
            if (choice == 1) {
                if (!state.isMortgaged()) {
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(tile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + tile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                } else {
                    message = "该地块已经抵押。";
                }
            } else {
                message = "玩家 " + player.getName() + " 取消抵押。";
            }
            
            playerRepository.save(player);
            String summary = buildResultSummary("抵押结果", message, player);
            return new TurnSummaryEvent(player, summary);
        }
    }

    /**
     * 计算抵押价值。
     * 规则：每建一栋房子或者旅馆便能以一半的价格将他们抵押出去，再加上空地抵押费。
     */
    private int calculateMortgageValue(CountryTile tile, PropertyState state) {
        int buildingValue = (state.getHouseCount() * tile.getBuildHouseCost() 
                + state.getHotelCount() * tile.getBuildHotelCost()) / 2;
        return buildingValue + tile.getMortgagePrice();
    }

    /**
     * 赎回提示事件：玩家停留在已抵押的地块上时可以赎回。
     */
    private class RedeemPromptEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final CountryTile tile;
        private final PropertyState state;

        RedeemPromptEvent(Player player, CountryTile tile, PropertyState state, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.tile = tile;
            this.state = state;
        }

        @Override
        public GameEvent interact() {
            int redeemCost = calculateMortgageValue(tile, state);
            String prompt = "\n[赎回选择] " + player.getName() + " 当前现金 " + formatMoney(player.getMoney())
                    + "，地块：" + tile.getName()
                    + "\n  赎回费用：" + formatMoney(redeemCost)
                    + "\n  1 = 赎回，0 = 取消\n请选择: ";
            int choice = decisionPort.requestInt(prompt);
            String message;
            
            if (choice == 1) {
                if (state.isMortgaged()) {
                    if (player.getMoney() >= redeemCost) {
                        payMoney(player, redeemCost);
                        state.setMortgaged(false);
                        player.getMortgagedTilePositions().remove(tile.getPosition());
                        message = "玩家 " + player.getName() + " 以 " + formatMoney(redeemCost) + " 赎回了 [" + tile.getName() + "]";
                    } else {
                        message = "现金不足，无法赎回（需要 " + formatMoney(redeemCost) + "，当前 " + formatMoney(player.getMoney()) + "）。";
                    }
                } else {
                    message = "该地块未抵押。";
                }
            } else {
                message = "玩家 " + player.getName() + " 取消赎回。";
            }
            
            playerRepository.save(player);
            String summary = buildResultSummary("赎回结果", message, player);
            return new TurnSummaryEvent(player, summary);
        }
    }

    /**
     * 赎回选择事件：玩家回合开始时，可以选择赎回已抵押的地块（包括国家、公司、火车站）。
     */
    private class RedeemChoiceEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final List<Tile> mortgagedTiles;
        private final int turn;
        private final String fromName;

        RedeemChoiceEvent(Player player, List<Tile> mortgagedTiles, int turn, String fromName) {
            super(String.valueOf(player.getId()), 
                    "==================== 回合 " + turn + " ====================\n"
                    + "玩家：" + player.getName() + "\n"
                    + "您有 " + mortgagedTiles.size() + " 块已抵押的地块可以赎回。\n");
            this.player = player;
            this.mortgagedTiles = mortgagedTiles;
            this.turn = turn;
            this.fromName = fromName;
        }

        @Override
        public GameEvent interact() {
            StringBuilder prompt = new StringBuilder();
            prompt.append("\n[赎回选择] ").append(player.getName()).append(" 当前现金 ").append(formatMoney(player.getMoney()));
            prompt.append("\n已抵押的地块：\n");
            for (int i = 0; i < mortgagedTiles.size(); i++) {
                Tile tile = mortgagedTiles.get(i);
                PropertyState state = getPropertyState(tile.getPosition());
                int redeemCost = calculateRedeemCost(tile, state);
                prompt.append("  ").append(i + 1).append(" = ").append(tile.getName())
                        .append("（赎回费用：").append(formatMoney(redeemCost)).append("）\n");
            }
            prompt.append("  0 = 不赎回，继续游戏\n请选择: ");
            
            int choice = decisionPort.requestInt(prompt.toString());
            String message;
            
            if (choice > 0 && choice <= mortgagedTiles.size()) {
                Tile tile = mortgagedTiles.get(choice - 1);
                PropertyState state = getPropertyState(tile.getPosition());
                int redeemCost = calculateRedeemCost(tile, state);
                
                if (state.isMortgaged()) {
                    if (player.getMoney() >= redeemCost) {
                        payMoney(player, redeemCost);
                        state.setMortgaged(false);
                        player.getMortgagedTilePositions().remove(tile.getPosition());
                        message = "玩家 " + player.getName() + " 以 " + formatMoney(redeemCost) + " 赎回了 [" + tile.getName() + "]";
                    } else {
                        message = "现金不足，无法赎回（需要 " + formatMoney(redeemCost) + "，当前 " + formatMoney(player.getMoney()) + "）。";
                    }
                } else {
                    message = "该地块未抵押。";
                }
                playerRepository.save(player);
                String summary = buildResultSummary("赎回结果", message, player);
                // 继续游戏流程
                return new ContinueTurnEvent(player, summary, turn, fromName);
            } else {
                // 不赎回，继续游戏
                return new ContinueTurnEvent(player, "玩家 " + player.getName() + " 选择不赎回，继续游戏。", turn, fromName);
            }
        }
    }

    /**
     * 计算赎回费用（根据地块类型）。
     */
    private int calculateRedeemCost(Tile tile, PropertyState state) {
        if (tile instanceof CountryTile) {
            return calculateMortgageValue((CountryTile) tile, state);
        } else if (tile instanceof CompanyTile) {
            return calculateCompanyMortgageValue((CompanyTile) tile);
        } else if (tile instanceof TrainStationTile) {
            return calculateTrainStationMortgageValue((TrainStationTile) tile);
        }
        return 0;
    }

    /**
     * 继续回合事件：赎回选择后继续正常的游戏流程。
     */
    private class ContinueTurnEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final int turn;
        private final String fromName;
        private final String redeemMessage;

        ContinueTurnEvent(Player player, String redeemMessage, int turn, String fromName) {
            super(String.valueOf(player.getId()), "");
            this.player = player;
            this.turn = turn;
            this.fromName = fromName;
            this.redeemMessage = redeemMessage;
        }

        @Override
        public String toString() {
            // 不显示任何内容，让 WrappedEvent 来显示赎回结果
            return "";
        }

        @Override
        public GameEvent interact() {
            // 继续正常的游戏流程（不重复显示回合号，因为回合号已经在 RedeemChoiceEvent 中显示过了）
            return executeTurn(player, turn, fromName, true, redeemMessage);
        }
    }

    /**
     * 执行回合的游戏流程（掷骰、移动、处理落地事件）。
     * 
     * @param player 当前玩家
     * @param turn 回合号
     * @param fromName 起始位置名称
     * @param skipHeader 是否跳过回合号显示（用于赎回后继续游戏的情况）
     * @param prefixMessage 前缀消息（用于在游戏流程前显示赎回结果等信息）
     * @return 游戏事件
     */
    private GameEvent executeTurn(Player player, int turn, String fromName, boolean skipHeader, String prefixMessage) {
        // 检查玩家是否在监狱中
        if (player.getJailTurnsRemaining() > 0) {
            // 在监狱中，只展示基本信息，不掷骰子
            int remaining = player.getJailTurnsRemaining();
            player.setJailTurnsRemaining(remaining - 1); // 减少剩余回合数
            playerRepository.save(player);
            String jailSummary = buildJailSummary(turn, player, fromName, player.getPosition(), remaining);
            return new TurnSummaryEvent(player, jailSummary);
        }
        
        if (player.isPaused()) {
            player.setPaused(false);
            playerRepository.save(player);
            String pausedSummary = buildPausedSummary(turn, player, fromName, player.getPosition());
            return new TurnSummaryEvent(player, pausedSummary);
        }

        int dice = rollDice();
        int oldPos = player.getPosition();
        // 检查是否经过起点（在移动前检查）
        boolean passedGo = (oldPos + dice) > 40;
        int newPos = movePlayer(player, dice);

        Tile tile = tileRepository.findByPosition(newPos).orElse(null);
        String toName = tile != null ? tile.getName() : getTileName(newPos);
        String header = skipHeader ? "" : buildTurnHeader(turn, player, dice, oldPos, fromName, newPos, toName);
        
        // 如果跳过了回合号，需要构建一个不包含回合号的头部
        if (skipHeader) {
            header = "掷骰：" + dice + "\n"
                    + "移动：" + fromName + " -> " + toName + "\n\n";
        }

        if (tile == null) {
            String summary = buildSummary(header, "落点：未知地块\n", "无额外事件。", player);
            if (skipHeader && prefixMessage != null && !prefixMessage.isEmpty()) {
                summary = prefixMessage + "\n\n" + summary;
            }
            return new TurnSummaryEvent(player, summary);
        }

        String locationLine = "落点：" + toName + "\n";

        GameEvent event;
        var players = playerRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Player::getId))
                .toList();
        switch (tile.getTileType()) {
            case COUNTRY -> event = createCountryEvent(players, player, (CountryTile) tile, header, locationLine);
            case SPECIAL -> {
                // 如果是起点，检查是经过还是停留
                boolean isGo = ((SpecialTile) tile).getCategory() == SpecialTile.SpecialCategory.GO;
                if (isGo) {
                    // 如果经过起点且最终停留在起点，需要额外奖励2000（总共4000）
                    if (passedGo && newPos == 1) {
                        // 已经在movePlayer中奖励了2000，停留再奖励2000
                        player.setMoney(player.getMoney() + 2000);
                        playerRepository.save(player);
                    }
                }
                event = handleSpecialTile(player, (SpecialTile) tile, passedGo, newPos == 1, header, locationLine);
            }
            case TRAIN_STATION -> event = createTrainStationEvent(players, player, (TrainStationTile) tile, header, locationLine);
            case COMPANY -> event = createCompanyEvent(players, player, (CompanyTile) tile, header, locationLine);
            case CHANCE, FATE -> {
                // TODO: 实现机会卡和命运卡逻辑
                // 当卡牌需要玩家支付费用时，使用以下方式：
                // 1. 支付给银行：handlePayment(player, amount, null, "支付卡牌费用", header, locationLine)
                // 2. 支付给其他玩家：handlePayment(player, amount, recipient, "支付卡牌费用", header, locationLine)
                // 3. 或者直接使用：new GenericPaymentPromptEvent(player, recipient, amount, "支付卡牌费用", preMessage)
                // 这样可以确保钱不够时自动触发抵押流程，抵押完还不够就破产
                String body = "抽牌逻辑待实现，暂时无效果。";
                event = new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
            }
            default -> {
                String body = "暂无额外事件。";
                event = new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
            }
        }
        
        // 如果跳过了回合号，需要在事件消息前添加赎回结果
        if (skipHeader && prefixMessage != null && !prefixMessage.isEmpty()) {
            // 创建一个包装事件，将赎回结果和游戏流程合并显示
            event = new WrappedEvent(event, prefixMessage);
        }
        
        playerRepository.save(player);
        return event;
    }
    
    /**
     * 执行回合的游戏流程（掷骰、移动、处理落地事件）。
     * 
     * @param player 当前玩家
     * @param turn 回合号
     * @param fromName 起始位置名称
     * @return 游戏事件
     */
    private GameEvent executeTurn(Player player, int turn, String fromName) {
        return executeTurn(player, turn, fromName, false, null);
    }

    /**
     * 包装事件：用于在游戏流程前添加前缀消息（如赎回结果）。
     */
    private class WrappedEvent extends TextEvent implements InteractiveEvent {
        private final GameEvent originalEvent;
        private final String prefixMessage;

        WrappedEvent(GameEvent originalEvent, String prefixMessage) {
            super(originalEvent.getPlayerId(), prefixMessage);
            this.originalEvent = originalEvent;
            this.prefixMessage = prefixMessage;
        }

        @Override
        public String getType() {
            return originalEvent.getType();
        }

        @Override
        public String toString() {
            return prefixMessage + "\n\n" + originalEvent.toString();
        }

        @Override
        public GameEvent interact() {
            if (originalEvent instanceof InteractiveEvent) {
                return ((InteractiveEvent) originalEvent).interact();
            }
            return null;
        }
    }

    /**
     * 为购买地块而抵押的事件。
     */
    private class MortgageForPurchaseEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final CountryTile tile;
        private final int requiredAmount;

        MortgageForPurchaseEvent(Player player, CountryTile tile, int requiredAmount, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.tile = tile;
            this.requiredAmount = requiredAmount;
        }

        @Override
        public GameEvent interact() {
            List<CountryTile> availableTiles = getAvailableTilesForMortgage(player);
            if (availableTiles.isEmpty()) {
                String message = "没有可抵押的地块，无法购买。";
                playerRepository.save(player);
                String summary = buildResultSummary("购地结果", message, player);
                return new TurnSummaryEvent(player, summary);
            }
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("\n[抵押选择] 需要 ").append(formatMoney(requiredAmount))
                    .append("，当前现金 ").append(formatMoney(player.getMoney()))
                    .append("，还差 ").append(formatMoney(requiredAmount - player.getMoney()))
                    .append("\n可抵押的地块：\n");
            for (int i = 0; i < availableTiles.size(); i++) {
                CountryTile t = availableTiles.get(i);
                PropertyState state = getPropertyState(t.getPosition());
                int mortgageValue = calculateMortgageValue(t, state);
                prompt.append("  ").append(i + 1).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            prompt.append("  0 = 放弃购买\n请选择: ");
            
            int choice = decisionPort.requestInt(prompt.toString());
            String message;
            
            if (choice > 0 && choice <= availableTiles.size()) {
                CountryTile selectedTile = availableTiles.get(choice - 1);
                PropertyState state = getPropertyState(selectedTile.getPosition());
                int mortgageValue = calculateMortgageValue(selectedTile, state);
                
                state.setMortgaged(true);
                player.setMoney(player.getMoney() + mortgageValue);
                player.addMortgagedTile(selectedTile.getPosition());
                message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                playerRepository.save(player);
                
                // 检查现在是否有足够的钱购买
                if (player.getMoney() >= requiredAmount) {
                    // 继续购买流程
                    return new PurchasePromptEvent(player, tile, 
                            buildResultSummary("抵押结果", message + "\n现在可以购买地块了。", player));
                } else {
                    // 还需要继续抵押
                    return new MortgageForPurchaseEvent(player, tile, requiredAmount,
                            buildResultSummary("抵押结果", message + "\n还需要继续抵押。", player));
                }
            } else {
                message = "玩家 " + player.getName() + " 放弃购买。";
                playerRepository.save(player);
                String summary = buildResultSummary("购地结果", message, player);
                return new TurnSummaryEvent(player, summary);
            }
        }
    }

    /**
     * 支付过路费时钱不够的事件。
     */
    private class TollPaymentPromptEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final Player owner;
        private final CountryTile tile;
        private final int toll;

        TollPaymentPromptEvent(Player player, Player owner, CountryTile tile, int toll, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.owner = owner;
            this.tile = tile;
            this.toll = toll;
        }

        @Override
        public GameEvent interact() {
            List<CountryTile> availableTiles = getAvailableTilesForMortgage(player);
            if (availableTiles.isEmpty()) {
                // 没有可抵押的地块，破产
                int paid = player.getMoney();
                player.setMoney(0);
                owner.setMoney(owner.getMoney() + paid);
                playerRepository.save(owner);
                
                // 回收玩家的所有资产
                for (Integer pos : new HashSet<>(player.getOwnedTilePositions())) {
                    tileOwners.remove(pos);
                    propertyStates.remove(pos);
                }
                player.getOwnedTilePositions().clear();
                player.getMortgagedTilePositions().clear();
                
                var players = playerRepository.findAll().stream()
                        .filter(p -> p.getMoney() > 0 || !p.getOwnedTilePositions().isEmpty())
                        .toList();
                gameOver = players.size() <= 1;
                
                String message = "资金不足且无可抵押资产，玩家 " + player.getName() + " 破产出局。"
                        + (paid > 0 ? "已支付全部现金 " + formatMoney(paid) + "。" : "");
                playerRepository.save(player);
                String summary = buildResultSummary("破产", message, player);
                return new TurnSummaryEvent(player, summary);
            }
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("\n[抵押选择] 需要支付过路费 ").append(formatMoney(toll))
                    .append("，当前现金 ").append(formatMoney(player.getMoney()))
                    .append("，还差 ").append(formatMoney(toll - player.getMoney()))
                    .append("\n可抵押的地块：\n");
            for (int i = 0; i < availableTiles.size(); i++) {
                CountryTile t = availableTiles.get(i);
                PropertyState state = getPropertyState(t.getPosition());
                int mortgageValue = calculateMortgageValue(t, state);
                prompt.append("  ").append(i + 1).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            prompt.append("  0 = 破产（如果无法支付）\n请选择: ");
            
            int choice = decisionPort.requestInt(prompt.toString());
            String message;
            
            if (choice > 0 && choice <= availableTiles.size()) {
                CountryTile selectedTile = availableTiles.get(choice - 1);
                PropertyState state = getPropertyState(selectedTile.getPosition());
                int mortgageValue = calculateMortgageValue(selectedTile, state);
                
                state.setMortgaged(true);
                player.setMoney(player.getMoney() + mortgageValue);
                player.addMortgagedTile(selectedTile.getPosition());
                message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                playerRepository.save(player);
                
                // 检查现在是否有足够的钱支付过路费
                if (player.getMoney() >= toll) {
                    payMoney(player, toll);
                    owner.setMoney(owner.getMoney() + toll);
                    playerRepository.save(owner);
                    message += "\n已向 " + owner.getName() + " 支付过路费 " + formatMoney(toll)
                            + "。\n您的现金：" + formatMoney(player.getMoney())
                            + "；" + owner.getName() + " 现金：" + formatMoney(owner.getMoney());
                    String summary = buildResultSummary("支付过路费", message, player);
                    return new TurnSummaryEvent(player, summary);
                } else {
                    // 还需要继续抵押
                    return new TollPaymentPromptEvent(player, owner, tile, toll,
                            buildResultSummary("抵押结果", message + "\n还需要继续抵押。", player));
                }
            } else {
                // 选择破产
                int paid = player.getMoney();
                player.setMoney(0);
                owner.setMoney(owner.getMoney() + paid);
                playerRepository.save(owner);
                
                // 回收玩家的所有资产
                for (Integer pos : new HashSet<>(player.getOwnedTilePositions())) {
                    tileOwners.remove(pos);
                    propertyStates.remove(pos);
                }
                player.getOwnedTilePositions().clear();
                player.getMortgagedTilePositions().clear();
                
                var players = playerRepository.findAll().stream()
                        .filter(p -> p.getMoney() > 0 || !p.getOwnedTilePositions().isEmpty())
                        .toList();
                gameOver = players.size() <= 1;
                
                message = "玩家 " + player.getName() + " 选择破产出局。"
                        + (paid > 0 ? "已支付全部现金 " + formatMoney(paid) + "。" : "");
                playerRepository.save(player);
                String summary = buildResultSummary("破产", message, player);
                return new TurnSummaryEvent(player, summary);
            }
        }
    }

    /**
     * 支付税费时钱不够的事件。
     */
    private class TaxPaymentPromptEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final SpecialTile tile;
        private final int tax;

        TaxPaymentPromptEvent(Player player, SpecialTile tile, int tax, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.tile = tile;
            this.tax = tax;
        }

        @Override
        public GameEvent interact() {
            // 获取可抵押的地块（包括国家和公司）
            List<CountryTile> availableCountryTiles = getAvailableTilesForMortgage(player);
            List<CompanyTile> availableCompanyTiles = getAvailableCompanyTilesForMortgage(player);
            
            if (availableCountryTiles.isEmpty() && availableCompanyTiles.isEmpty()) {
                // 没有可抵押的地块，破产
                int paid = player.getMoney();
                player.setMoney(0);
                playerRepository.save(player);
                
                // 回收玩家的所有资产
                for (Integer pos : new HashSet<>(player.getOwnedTilePositions())) {
                    tileOwners.remove(pos);
                    propertyStates.remove(pos);
                }
                player.getOwnedTilePositions().clear();
                player.getMortgagedTilePositions().clear();
                
                var players = playerRepository.findAll().stream()
                        .filter(p -> p.getMoney() > 0 || !p.getOwnedTilePositions().isEmpty())
                        .toList();
                gameOver = players.size() <= 1;
                
                String message = "资金不足且无可抵押资产，玩家 " + player.getName() + " 破产出局。"
                        + (paid > 0 ? "已支付全部现金 " + formatMoney(paid) + "。" : "");
                playerRepository.save(player);
                String summary = buildResultSummary("破产", message, player);
                return new TurnSummaryEvent(player, summary);
            }
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("\n[抵押选择] 需要支付税费 ").append(formatMoney(tax))
                    .append("，当前现金 ").append(formatMoney(player.getMoney()))
                    .append("，还差 ").append(formatMoney(tax - player.getMoney()))
                    .append("\n可抵押的地块：\n");
            int index = 1;
            for (CountryTile t : availableCountryTiles) {
                PropertyState state = getPropertyState(t.getPosition());
                int mortgageValue = calculateMortgageValue(t, state);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            for (CompanyTile t : availableCompanyTiles) {
                int mortgageValue = calculateCompanyMortgageValue(t);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            prompt.append("  0 = 破产（如果无法支付）\n请选择: ");
            
            int choice = decisionPort.requestInt(prompt.toString());
            String message;
            
            int totalOptions = availableCountryTiles.size() + availableCompanyTiles.size();
            if (choice > 0 && choice <= totalOptions) {
                if (choice <= availableCountryTiles.size()) {
                    // 选择了国家地块
                    CountryTile selectedTile = availableCountryTiles.get(choice - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateMortgageValue(selectedTile, state);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                } else {
                    // 选择了公司地块
                    CompanyTile selectedTile = availableCompanyTiles.get(choice - availableCountryTiles.size() - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateCompanyMortgageValue(selectedTile);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                }
                playerRepository.save(player);
                
                // 检查现在是否有足够的钱支付税费
                if (player.getMoney() >= tax) {
                    payMoney(player, tax);
                    playerRepository.save(player);
                    message += "\n已支付税费 " + formatMoney(tax)
                            + "。\n您的现金：" + formatMoney(player.getMoney());
                    String summary = buildResultSummary("支付税费", message, player);
                    return new TurnSummaryEvent(player, summary);
                } else {
                    // 还需要继续抵押
                    return new TaxPaymentPromptEvent(player, tile, tax,
                            buildResultSummary("抵押结果", message + "\n还需要继续抵押。", player));
                }
            } else {
                // 选择破产
                int paid = player.getMoney();
                player.setMoney(0);
                playerRepository.save(player);
                
                // 回收玩家的所有资产
                for (Integer pos : new HashSet<>(player.getOwnedTilePositions())) {
                    tileOwners.remove(pos);
                    propertyStates.remove(pos);
                }
                player.getOwnedTilePositions().clear();
                player.getMortgagedTilePositions().clear();
                
                var players = playerRepository.findAll().stream()
                        .filter(p -> p.getMoney() > 0 || !p.getOwnedTilePositions().isEmpty())
                        .toList();
                gameOver = players.size() <= 1;
                
                message = "玩家 " + player.getName() + " 选择破产出局。"
                        + (paid > 0 ? "已支付全部现金 " + formatMoney(paid) + "。" : "");
                playerRepository.save(player);
                String summary = buildResultSummary("破产", message, player);
                return new TurnSummaryEvent(player, summary);
            }
        }
    }

    /**
     * 通用支付提示事件：用于处理所有需要支付的场景（过路费、税费、卡牌费用等）。
     */
    private class GenericPaymentPromptEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final Player recipient; // null表示支付给银行
        private final int amount;
        private final String description;

        GenericPaymentPromptEvent(Player player, Player recipient, int amount, String description, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.recipient = recipient;
            this.amount = amount;
            this.description = description;
        }

        @Override
        public GameEvent interact() {
            // 获取可抵押的地块（包括国家和公司）
            List<CountryTile> availableCountryTiles = getAvailableTilesForMortgage(player);
            List<CompanyTile> availableCompanyTiles = getAvailableCompanyTilesForMortgage(player);
            
            if (availableCountryTiles.isEmpty() && availableCompanyTiles.isEmpty()) {
                // 没有可抵押的地块，破产
                int paid = player.getMoney();
                player.setMoney(0);
                if (recipient != null) {
                    recipient.setMoney(recipient.getMoney() + paid);
                    playerRepository.save(recipient);
                }
                playerRepository.save(player);
                
                // 回收玩家的所有资产
                for (Integer pos : new HashSet<>(player.getOwnedTilePositions())) {
                    tileOwners.remove(pos);
                    propertyStates.remove(pos);
                }
                player.getOwnedTilePositions().clear();
                player.getMortgagedTilePositions().clear();
                
                var players = playerRepository.findAll().stream()
                        .filter(p -> p.getMoney() > 0 || !p.getOwnedTilePositions().isEmpty())
                        .toList();
                gameOver = players.size() <= 1;
                
                String message = "资金不足且无可抵押资产，玩家 " + player.getName() + " 破产出局。"
                        + (paid > 0 ? "已支付全部现金 " + formatMoney(paid) + "。" : "");
                playerRepository.save(player);
                String summary = buildResultSummary("破产", message, player);
                return new TurnSummaryEvent(player, summary);
            }
            
            StringBuilder prompt = new StringBuilder();
            String paymentType = recipient != null ? "支付给 " + recipient.getName() : "支付";
            prompt.append("\n[抵押选择] 需要").append(paymentType).append(" ").append(formatMoney(amount))
                    .append("，当前现金 ").append(formatMoney(player.getMoney()))
                    .append("，还差 ").append(formatMoney(amount - player.getMoney()))
                    .append("\n可抵押的地块：\n");
            int index = 1;
            for (CountryTile t : availableCountryTiles) {
                PropertyState state = getPropertyState(t.getPosition());
                int mortgageValue = calculateMortgageValue(t, state);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            for (CompanyTile t : availableCompanyTiles) {
                int mortgageValue = calculateCompanyMortgageValue(t);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            prompt.append("  0 = 破产（如果无法支付）\n请选择: ");
            
            int choice = decisionPort.requestInt(prompt.toString());
            String message;
            
            int totalOptions = availableCountryTiles.size() + availableCompanyTiles.size();
            if (choice > 0 && choice <= totalOptions) {
                if (choice <= availableCountryTiles.size()) {
                    // 选择了国家地块
                    CountryTile selectedTile = availableCountryTiles.get(choice - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateMortgageValue(selectedTile, state);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                } else {
                    // 选择了公司地块
                    CompanyTile selectedTile = availableCompanyTiles.get(choice - availableCountryTiles.size() - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateCompanyMortgageValue(selectedTile);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                }
                playerRepository.save(player);
                
                // 检查现在是否有足够的钱支付
                if (player.getMoney() >= amount) {
                    payMoney(player, amount);
                    if (recipient != null) {
                        recipient.setMoney(recipient.getMoney() + amount);
                        playerRepository.save(recipient);
                    }
                    playerRepository.save(player);
                    String paymentMsg = recipient != null 
                            ? "已向 " + recipient.getName() + " " + description + " " + formatMoney(amount)
                            : "已" + description + " " + formatMoney(amount);
                    message += "\n" + paymentMsg
                            + "。\n您的现金：" + formatMoney(player.getMoney());
                    if (recipient != null) {
                        message += "；" + recipient.getName() + " 现金：" + formatMoney(recipient.getMoney());
                    }
                    String summary = buildResultSummary("支付结果", message, player);
                    return new TurnSummaryEvent(player, summary);
                } else {
                    // 还需要继续抵押
                    return new GenericPaymentPromptEvent(player, recipient, amount, description,
                            buildResultSummary("抵押结果", message + "\n还需要继续抵押。", player));
                }
            } else {
                // 选择破产
                int paid = player.getMoney();
                player.setMoney(0);
                if (recipient != null) {
                    recipient.setMoney(recipient.getMoney() + paid);
                    playerRepository.save(recipient);
                }
                playerRepository.save(player);
                
                // 回收玩家的所有资产
                for (Integer pos : new HashSet<>(player.getOwnedTilePositions())) {
                    tileOwners.remove(pos);
                    propertyStates.remove(pos);
                }
                player.getOwnedTilePositions().clear();
                player.getMortgagedTilePositions().clear();
                
                var players = playerRepository.findAll().stream()
                        .filter(p -> p.getMoney() > 0 || !p.getOwnedTilePositions().isEmpty())
                        .toList();
                gameOver = players.size() <= 1;
                
                message = "玩家 " + player.getName() + " 选择破产出局。"
                        + (paid > 0 ? "已支付全部现金 " + formatMoney(paid) + "。" : "");
                playerRepository.save(player);
                String summary = buildResultSummary("破产", message, player);
                return new TurnSummaryEvent(player, summary);
            }
        }
    }

    /**
     * 为购买火车站而抵押的事件。
     */
    private class TrainStationMortgageForPurchaseEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final TrainStationTile tile;
        private final int requiredAmount;

        TrainStationMortgageForPurchaseEvent(Player player, TrainStationTile tile, int requiredAmount, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.tile = tile;
            this.requiredAmount = requiredAmount;
        }

        @Override
        public GameEvent interact() {
            // 获取可抵押的地块（包括国家、公司和火车站）
            List<CountryTile> availableCountryTiles = getAvailableTilesForMortgage(player);
            List<CompanyTile> availableCompanyTiles = getAvailableCompanyTilesForMortgage(player);
            List<TrainStationTile> availableTrainStations = getAvailableTrainStationsForMortgage(player);
            
            if (availableCountryTiles.isEmpty() && availableCompanyTiles.isEmpty() && availableTrainStations.isEmpty()) {
                String message = "没有可抵押的地块，无法购买。";
                playerRepository.save(player);
                String summary = buildResultSummary("购买火车站结果", message, player);
                return new TurnSummaryEvent(player, summary);
            }
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("\n[抵押选择] 需要 ").append(formatMoney(requiredAmount))
                    .append("，当前现金 ").append(formatMoney(player.getMoney()))
                    .append("，还差 ").append(formatMoney(requiredAmount - player.getMoney()))
                    .append("\n可抵押的地块：\n");
            int index = 1;
            for (CountryTile t : availableCountryTiles) {
                PropertyState state = getPropertyState(t.getPosition());
                int mortgageValue = calculateMortgageValue(t, state);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            for (CompanyTile t : availableCompanyTiles) {
                int mortgageValue = calculateCompanyMortgageValue(t);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            for (TrainStationTile t : availableTrainStations) {
                int mortgageValue = calculateTrainStationMortgageValue(t);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            prompt.append("  0 = 放弃购买\n请选择: ");
            
            int choice = decisionPort.requestInt(prompt.toString());
            String message;
            
            int totalOptions = availableCountryTiles.size() + availableCompanyTiles.size() + availableTrainStations.size();
            if (choice > 0 && choice <= totalOptions) {
                if (choice <= availableCountryTiles.size()) {
                    // 选择了国家地块
                    CountryTile selectedTile = availableCountryTiles.get(choice - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateMortgageValue(selectedTile, state);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                } else if (choice <= availableCountryTiles.size() + availableCompanyTiles.size()) {
                    // 选择了公司地块
                    CompanyTile selectedTile = availableCompanyTiles.get(choice - availableCountryTiles.size() - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateCompanyMortgageValue(selectedTile);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                } else {
                    // 选择了火车站地块
                    TrainStationTile selectedTile = availableTrainStations.get(choice - availableCountryTiles.size() - availableCompanyTiles.size() - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateTrainStationMortgageValue(selectedTile);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                }
                playerRepository.save(player);
                
                // 检查现在是否有足够的钱购买
                if (player.getMoney() >= requiredAmount) {
                    // 继续购买流程
                    return new TrainStationPurchasePromptEvent(player, tile, 
                            buildResultSummary("抵押结果", message + "\n现在可以购买火车站了。", player));
                } else {
                    // 还需要继续抵押
                    return new TrainStationMortgageForPurchaseEvent(player, tile, requiredAmount,
                            buildResultSummary("抵押结果", message + "\n还需要继续抵押。", player));
                }
            } else {
                message = "玩家 " + player.getName() + " 放弃购买。";
                playerRepository.save(player);
                String summary = buildResultSummary("购买火车站结果", message, player);
                return new TurnSummaryEvent(player, summary);
            }
        }
    }

    /**
     * 支付火车站过路费时钱不够的事件。
     */
    private class TrainStationTollPaymentPromptEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final Player owner;
        private final TrainStationTile tile;
        private final int toll;

        TrainStationTollPaymentPromptEvent(Player player, Player owner, TrainStationTile tile, int toll, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.owner = owner;
            this.tile = tile;
            this.toll = toll;
        }

        @Override
        public GameEvent interact() {
            // 获取可抵押的地块（包括国家、公司和火车站）
            List<CountryTile> availableCountryTiles = getAvailableTilesForMortgage(player);
            List<CompanyTile> availableCompanyTiles = getAvailableCompanyTilesForMortgage(player);
            List<TrainStationTile> availableTrainStations = getAvailableTrainStationsForMortgage(player);
            
            if (availableCountryTiles.isEmpty() && availableCompanyTiles.isEmpty() && availableTrainStations.isEmpty()) {
                // 没有可抵押的地块，破产
                int paid = player.getMoney();
                player.setMoney(0);
                owner.setMoney(owner.getMoney() + paid);
                playerRepository.save(owner);
                
                // 回收玩家的所有资产
                for (Integer pos : new HashSet<>(player.getOwnedTilePositions())) {
                    tileOwners.remove(pos);
                    propertyStates.remove(pos);
                }
                player.getOwnedTilePositions().clear();
                player.getMortgagedTilePositions().clear();
                
                var players = playerRepository.findAll().stream()
                        .filter(p -> p.getMoney() > 0 || !p.getOwnedTilePositions().isEmpty())
                        .toList();
                gameOver = players.size() <= 1;
                
                String message = "资金不足且无可抵押资产，玩家 " + player.getName() + " 破产出局。"
                        + (paid > 0 ? "已支付全部现金 " + formatMoney(paid) + "。" : "");
                playerRepository.save(player);
                String summary = buildResultSummary("破产", message, player);
                return new TurnSummaryEvent(player, summary);
            }
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("\n[抵押选择] 需要支付过路费 ").append(formatMoney(toll))
                    .append("，当前现金 ").append(formatMoney(player.getMoney()))
                    .append("，还差 ").append(formatMoney(toll - player.getMoney()))
                    .append("\n可抵押的地块：\n");
            int index = 1;
            for (CountryTile t : availableCountryTiles) {
                PropertyState state = getPropertyState(t.getPosition());
                int mortgageValue = calculateMortgageValue(t, state);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            for (CompanyTile t : availableCompanyTiles) {
                int mortgageValue = calculateCompanyMortgageValue(t);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            for (TrainStationTile t : availableTrainStations) {
                int mortgageValue = calculateTrainStationMortgageValue(t);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            prompt.append("  0 = 破产（如果无法支付）\n请选择: ");
            
            int choice = decisionPort.requestInt(prompt.toString());
            String message;
            
            int totalOptions = availableCountryTiles.size() + availableCompanyTiles.size() + availableTrainStations.size();
            if (choice > 0 && choice <= totalOptions) {
                if (choice <= availableCountryTiles.size()) {
                    // 选择了国家地块
                    CountryTile selectedTile = availableCountryTiles.get(choice - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateMortgageValue(selectedTile, state);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                } else if (choice <= availableCountryTiles.size() + availableCompanyTiles.size()) {
                    // 选择了公司地块
                    CompanyTile selectedTile = availableCompanyTiles.get(choice - availableCountryTiles.size() - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateCompanyMortgageValue(selectedTile);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                } else {
                    // 选择了火车站地块
                    TrainStationTile selectedTile = availableTrainStations.get(choice - availableCountryTiles.size() - availableCompanyTiles.size() - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateTrainStationMortgageValue(selectedTile);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                }
                playerRepository.save(player);
                
                // 检查现在是否有足够的钱支付过路费
                if (player.getMoney() >= toll) {
                    payMoney(player, toll);
                    owner.setMoney(owner.getMoney() + toll);
                    playerRepository.save(owner);
                    message += "\n已向 " + owner.getName() + " 支付过路费 " + formatMoney(toll)
                            + "。\n您的现金：" + formatMoney(player.getMoney())
                            + "；" + owner.getName() + " 现金：" + formatMoney(owner.getMoney());
                    String summary = buildResultSummary("支付过路费", message, player);
                    return new TurnSummaryEvent(player, summary);
                } else {
                    // 还需要继续抵押
                    return new TrainStationTollPaymentPromptEvent(player, owner, tile, toll,
                            buildResultSummary("抵押结果", message + "\n还需要继续抵押。", player));
                }
            } else {
                // 选择破产
                int paid = player.getMoney();
                player.setMoney(0);
                owner.setMoney(owner.getMoney() + paid);
                playerRepository.save(owner);
                
                // 回收玩家的所有资产
                for (Integer pos : new HashSet<>(player.getOwnedTilePositions())) {
                    tileOwners.remove(pos);
                    propertyStates.remove(pos);
                }
                player.getOwnedTilePositions().clear();
                player.getMortgagedTilePositions().clear();
                
                var players = playerRepository.findAll().stream()
                        .filter(p -> p.getMoney() > 0 || !p.getOwnedTilePositions().isEmpty())
                        .toList();
                gameOver = players.size() <= 1;
                
                message = "玩家 " + player.getName() + " 选择破产出局。"
                        + (paid > 0 ? "已支付全部现金 " + formatMoney(paid) + "。" : "");
                playerRepository.save(player);
                String summary = buildResultSummary("破产", message, player);
                return new TurnSummaryEvent(player, summary);
            }
        }
    }

    /**
     * 获取玩家可抵押的地块列表（未抵押的自己拥有的地块）。
     */
    private List<CountryTile> getAvailableTilesForMortgage(Player player) {
        return player.getOwnedTilePositions().stream()
                .filter(pos -> !player.getMortgagedTilePositions().contains(pos))
                .map(pos -> tileRepository.findByPosition(pos).orElse(null))
                .filter(t -> t instanceof CountryTile)
                .map(t -> (CountryTile) t)
                .filter(t -> {
                    PropertyState state = getPropertyState(t.getPosition());
                    return !state.isMortgaged();
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取玩家已抵押的地块列表（包括国家、公司、火车站）。
     */
    private List<Tile> getMortgagedTilesForPlayer(Player player) {
        return player.getMortgagedTilePositions().stream()
                .map(pos -> tileRepository.findByPosition(pos).orElse(null))
                .filter(t -> t != null && (t instanceof CountryTile || t instanceof CompanyTile || t instanceof TrainStationTile))
                .filter(t -> {
                    PropertyState state = getPropertyState(t.getPosition());
                    return state.isMortgaged();
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取玩家可抵押的公司地块列表（未抵押的自己拥有的公司）。
     */
    private List<CompanyTile> getAvailableCompanyTilesForMortgage(Player player) {
        return player.getOwnedTilePositions().stream()
                .filter(pos -> !player.getMortgagedTilePositions().contains(pos))
                .map(pos -> tileRepository.findByPosition(pos).orElse(null))
                .filter(t -> t instanceof CompanyTile)
                .map(t -> (CompanyTile) t)
                .filter(t -> {
                    PropertyState state = getPropertyState(t.getPosition());
                    return !state.isMortgaged();
                })
                .collect(Collectors.toList());
    }

    /**
     * 计算公司抵押价值（公司没有房屋/旅馆，只有抵押价格）。
     */
    private int calculateCompanyMortgageValue(CompanyTile tile) {
        return tile.getMortgagePrice();
    }

    /**
     * 公司赎回提示事件：玩家停留在已抵押的公司上时可以赎回。
     */
    private class CompanyRedeemPromptEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final CompanyTile tile;
        private final PropertyState state;

        CompanyRedeemPromptEvent(Player player, CompanyTile tile, PropertyState state, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.tile = tile;
            this.state = state;
        }

        @Override
        public GameEvent interact() {
            int redeemCost = calculateCompanyMortgageValue(tile);
            String prompt = "\n[赎回选择] " + player.getName() + " 当前现金 " + formatMoney(player.getMoney())
                    + "，公司：" + tile.getName()
                    + "\n  赎回费用：" + formatMoney(redeemCost)
                    + "\n  1 = 赎回，0 = 取消\n请选择: ";
            int choice = decisionPort.requestInt(prompt);
            String message;
            
            if (choice == 1) {
                if (state.isMortgaged()) {
                    if (player.getMoney() >= redeemCost) {
                        payMoney(player, redeemCost);
                        state.setMortgaged(false);
                        player.getMortgagedTilePositions().remove(tile.getPosition());
                        message = "玩家 " + player.getName() + " 以 " + formatMoney(redeemCost) + " 赎回了 [" + tile.getName() + "]";
                    } else {
                        message = "现金不足，无法赎回（需要 " + formatMoney(redeemCost) + "，当前 " + formatMoney(player.getMoney()) + "）。";
                    }
                } else {
                    message = "该公司未抵押。";
                }
            } else {
                message = "玩家 " + player.getName() + " 取消赎回。";
            }
            
            playerRepository.save(player);
            String summary = buildResultSummary("赎回结果", message, player);
            return new TurnSummaryEvent(player, summary);
        }
    }

    /**
     * 购买火车站交互事件：先输出位置描述，再触发购买决策。
     */
    private class TrainStationPurchasePromptEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final TrainStationTile tile;

        TrainStationPurchasePromptEvent(Player player, TrainStationTile tile, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.tile = tile;
        }

        @Override
        public GameEvent interact() {
            int price = tile.getSellPrice();
            String prompt = "\n[购买火车站] " + player.getName() + " 当前现金 " + formatMoney(player.getMoney())
                    + "，是否以 " + formatMoney(price) + " 购买 [" + tile.getName() + "]？"
                    + "（抵押价格 " + formatMoney(tile.getMortgagePrice()) + "，1=购买，0=放弃）: ";
            int choice = decisionPort.requestInt(prompt);
            String message;
            if (choice == 1) {
                if (player.getMoney() >= price) {
                    payMoney(player, price);
                    tileOwners.put(tile.getPosition(), player.getId());
                    player.addOwnedTile(tile.getPosition());
                    message = "玩家 " + player.getName() + " 以 " + formatMoney(price)
                            + " 购入 [" + tile.getName() + "]。";
                    playerRepository.save(player);
                    String summary = buildResultSummary("购买火车站结果", message, player);
                    return new TurnSummaryEvent(player, summary);
                } else {
                    // 钱不够，需要抵押
                    int shortage = price - player.getMoney();
                    String body = "现金不足，需要 " + formatMoney(price) + "，当前只有 " + formatMoney(player.getMoney())
                            + "，还差 " + formatMoney(shortage) + "。";
                    return new TrainStationMortgageForPurchaseEvent(player, tile, price, buildResultSummary("购买火车站", body, player));
                }
            } else {
                message = "玩家 " + player.getName() + " 放弃购买 [" + tile.getName() + "]。";
                playerRepository.save(player);
                String summary = buildResultSummary("购买火车站结果", message, player);
                return new TurnSummaryEvent(player, summary);
            }
        }
    }

    /**
     * 火车站赎回提示事件：玩家停留在已抵押的火车站上时可以赎回。
     */
    private class TrainStationRedeemPromptEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final TrainStationTile tile;
        private final PropertyState state;

        TrainStationRedeemPromptEvent(Player player, TrainStationTile tile, PropertyState state, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.tile = tile;
            this.state = state;
        }

        @Override
        public GameEvent interact() {
            int redeemCost = calculateTrainStationMortgageValue(tile);
            String prompt = "\n[赎回选择] " + player.getName() + " 当前现金 " + formatMoney(player.getMoney())
                    + "，火车站：" + tile.getName()
                    + "\n  赎回费用：" + formatMoney(redeemCost)
                    + "\n  1 = 赎回，0 = 取消\n请选择: ";
            int choice = decisionPort.requestInt(prompt);
            String message;
            
            if (choice == 1) {
                if (state.isMortgaged()) {
                    if (player.getMoney() >= redeemCost) {
                        payMoney(player, redeemCost);
                        state.setMortgaged(false);
                        player.getMortgagedTilePositions().remove(tile.getPosition());
                        message = "玩家 " + player.getName() + " 以 " + formatMoney(redeemCost) + " 赎回了 [" + tile.getName() + "]";
                    } else {
                        message = "现金不足，无法赎回（需要 " + formatMoney(redeemCost) + "，当前 " + formatMoney(player.getMoney()) + "）。";
                    }
                } else {
                    message = "该火车站未抵押。";
                }
            } else {
                message = "玩家 " + player.getName() + " 取消赎回。";
            }
            
            playerRepository.save(player);
            String summary = buildResultSummary("赎回结果", message, player);
            return new TurnSummaryEvent(player, summary);
        }
    }

    /**
     * 计算火车站抵押价值（火车站没有房屋/旅馆，只有抵押价格）。
     */
    private int calculateTrainStationMortgageValue(TrainStationTile tile) {
        return tile.getMortgagePrice();
    }

    /**
     * 获取玩家可抵押的火车站列表（未抵押的自己拥有的火车站）。
     */
    private List<TrainStationTile> getAvailableTrainStationsForMortgage(Player player) {
        return player.getOwnedTilePositions().stream()
                .filter(pos -> !player.getMortgagedTilePositions().contains(pos))
                .map(pos -> tileRepository.findByPosition(pos).orElse(null))
                .filter(t -> t instanceof TrainStationTile)
                .map(t -> (TrainStationTile) t)
                .filter(t -> {
                    PropertyState state = getPropertyState(t.getPosition());
                    return !state.isMortgaged();
                })
                .collect(Collectors.toList());
    }

    /**
     * 为购买公司而抵押的事件。
     */
    private class CompanyMortgageForPurchaseEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final CompanyTile tile;
        private final int requiredAmount;

        CompanyMortgageForPurchaseEvent(Player player, CompanyTile tile, int requiredAmount, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.tile = tile;
            this.requiredAmount = requiredAmount;
        }

        @Override
        public GameEvent interact() {
            // 获取可抵押的地块（包括国家和公司）
            List<CountryTile> availableCountryTiles = getAvailableTilesForMortgage(player);
            List<CompanyTile> availableCompanyTiles = getAvailableCompanyTilesForMortgage(player);
            
            if (availableCountryTiles.isEmpty() && availableCompanyTiles.isEmpty()) {
                String message = "没有可抵押的地块，无法购买。";
                playerRepository.save(player);
                String summary = buildResultSummary("购买公司结果", message, player);
                return new TurnSummaryEvent(player, summary);
            }
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("\n[抵押选择] 需要 ").append(formatMoney(requiredAmount))
                    .append("，当前现金 ").append(formatMoney(player.getMoney()))
                    .append("，还差 ").append(formatMoney(requiredAmount - player.getMoney()))
                    .append("\n可抵押的地块：\n");
            int index = 1;
            for (CountryTile t : availableCountryTiles) {
                PropertyState state = getPropertyState(t.getPosition());
                int mortgageValue = calculateMortgageValue(t, state);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            for (CompanyTile t : availableCompanyTiles) {
                int mortgageValue = calculateCompanyMortgageValue(t);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            prompt.append("  0 = 放弃购买\n请选择: ");
            
            int choice = decisionPort.requestInt(prompt.toString());
            String message;
            
            int totalOptions = availableCountryTiles.size() + availableCompanyTiles.size();
            if (choice > 0 && choice <= totalOptions) {
                if (choice <= availableCountryTiles.size()) {
                    // 选择了国家地块
                    CountryTile selectedTile = availableCountryTiles.get(choice - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateMortgageValue(selectedTile, state);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                } else {
                    // 选择了公司地块
                    CompanyTile selectedTile = availableCompanyTiles.get(choice - availableCountryTiles.size() - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateCompanyMortgageValue(selectedTile);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                }
                playerRepository.save(player);
                
                // 检查现在是否有足够的钱购买
                if (player.getMoney() >= requiredAmount) {
                    // 继续购买流程
                    return new CompanyPurchasePromptEvent(player, tile, 
                            buildResultSummary("抵押结果", message + "\n现在可以购买公司了。", player));
                } else {
                    // 还需要继续抵押
                    return new CompanyMortgageForPurchaseEvent(player, tile, requiredAmount,
                            buildResultSummary("抵押结果", message + "\n还需要继续抵押。", player));
                }
            } else {
                message = "玩家 " + player.getName() + " 放弃购买。";
                playerRepository.save(player);
                String summary = buildResultSummary("购买公司结果", message, player);
                return new TurnSummaryEvent(player, summary);
            }
        }
    }

    /**
     * 支付公司过路费时钱不够的事件。
     */
    private class CompanyTollPaymentPromptEvent extends TextEvent implements InteractiveEvent {
        private final Player player;
        private final Player owner;
        private final CompanyTile tile;
        private final int toll;

        CompanyTollPaymentPromptEvent(Player player, Player owner, CompanyTile tile, int toll, String preMessage) {
            super(String.valueOf(player.getId()), preMessage);
            this.player = player;
            this.owner = owner;
            this.tile = tile;
            this.toll = toll;
        }

        @Override
        public GameEvent interact() {
            // 获取可抵押的地块（包括国家和公司）
            List<CountryTile> availableCountryTiles = getAvailableTilesForMortgage(player);
            List<CompanyTile> availableCompanyTiles = getAvailableCompanyTilesForMortgage(player);
            
            if (availableCountryTiles.isEmpty() && availableCompanyTiles.isEmpty()) {
                // 没有可抵押的地块，破产
                int paid = player.getMoney();
                player.setMoney(0);
                owner.setMoney(owner.getMoney() + paid);
                playerRepository.save(owner);
                
                // 回收玩家的所有资产
                for (Integer pos : new HashSet<>(player.getOwnedTilePositions())) {
                    tileOwners.remove(pos);
                    propertyStates.remove(pos);
                }
                player.getOwnedTilePositions().clear();
                player.getMortgagedTilePositions().clear();
                
                var players = playerRepository.findAll().stream()
                        .filter(p -> p.getMoney() > 0 || !p.getOwnedTilePositions().isEmpty())
                        .toList();
                gameOver = players.size() <= 1;
                
                String message = "资金不足且无可抵押资产，玩家 " + player.getName() + " 破产出局。"
                        + (paid > 0 ? "已支付全部现金 " + formatMoney(paid) + "。" : "");
                playerRepository.save(player);
                String summary = buildResultSummary("破产", message, player);
                return new TurnSummaryEvent(player, summary);
            }
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("\n[抵押选择] 需要支付过路费 ").append(formatMoney(toll))
                    .append("，当前现金 ").append(formatMoney(player.getMoney()))
                    .append("，还差 ").append(formatMoney(toll - player.getMoney()))
                    .append("\n可抵押的地块：\n");
            int index = 1;
            for (CountryTile t : availableCountryTiles) {
                PropertyState state = getPropertyState(t.getPosition());
                int mortgageValue = calculateMortgageValue(t, state);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            for (CompanyTile t : availableCompanyTiles) {
                int mortgageValue = calculateCompanyMortgageValue(t);
                prompt.append("  ").append(index++).append(" = ").append(t.getName())
                        .append("（抵押价值：").append(formatMoney(mortgageValue)).append("）\n");
            }
            prompt.append("  0 = 破产（如果无法支付）\n请选择: ");
            
            int choice = decisionPort.requestInt(prompt.toString());
            String message;
            
            int totalOptions = availableCountryTiles.size() + availableCompanyTiles.size();
            if (choice > 0 && choice <= totalOptions) {
                if (choice <= availableCountryTiles.size()) {
                    // 选择了国家地块
                    CountryTile selectedTile = availableCountryTiles.get(choice - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateMortgageValue(selectedTile, state);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                } else {
                    // 选择了公司地块
                    CompanyTile selectedTile = availableCompanyTiles.get(choice - availableCountryTiles.size() - 1);
                    PropertyState state = getPropertyState(selectedTile.getPosition());
                    int mortgageValue = calculateCompanyMortgageValue(selectedTile);
                    
                    state.setMortgaged(true);
                    player.setMoney(player.getMoney() + mortgageValue);
                    player.addMortgagedTile(selectedTile.getPosition());
                    message = "玩家 " + player.getName() + " 将 [" + selectedTile.getName() + "] 抵押给银行，获得 " + formatMoney(mortgageValue);
                }
                playerRepository.save(player);
                
                // 检查现在是否有足够的钱支付过路费
                if (player.getMoney() >= toll) {
                    payMoney(player, toll);
                    owner.setMoney(owner.getMoney() + toll);
                    playerRepository.save(owner);
                    message += "\n已向 " + owner.getName() + " 支付过路费 " + formatMoney(toll)
                            + "。\n您的现金：" + formatMoney(player.getMoney())
                            + "；" + owner.getName() + " 现金：" + formatMoney(owner.getMoney());
                    String summary = buildResultSummary("支付过路费", message, player);
                    return new TurnSummaryEvent(player, summary);
                } else {
                    // 还需要继续抵押
                    return new CompanyTollPaymentPromptEvent(player, owner, tile, toll,
                            buildResultSummary("抵押结果", message + "\n还需要继续抵押。", player));
                }
            } else {
                // 选择破产
                int paid = player.getMoney();
                player.setMoney(0);
                owner.setMoney(owner.getMoney() + paid);
                playerRepository.save(owner);
                
                // 回收玩家的所有资产
                for (Integer pos : new HashSet<>(player.getOwnedTilePositions())) {
                    tileOwners.remove(pos);
                    propertyStates.remove(pos);
                }
                player.getOwnedTilePositions().clear();
                player.getMortgagedTilePositions().clear();
                
                var players = playerRepository.findAll().stream()
                        .filter(p -> p.getMoney() > 0 || !p.getOwnedTilePositions().isEmpty())
                        .toList();
                gameOver = players.size() <= 1;
                
                message = "玩家 " + player.getName() + " 选择破产出局。"
                        + (paid > 0 ? "已支付全部现金 " + formatMoney(paid) + "。" : "");
                playerRepository.save(player);
                String summary = buildResultSummary("破产", message, player);
                return new TurnSummaryEvent(player, summary);
            }
        }
    }
}

