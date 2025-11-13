package Monopoly.core.service.impl;

import Monopoly.core.domain.entity.player.Player;
import Monopoly.core.domain.entity.tile.*;
import Monopoly.core.event.GameEvent;
import Monopoly.core.event.InteractiveEvent;
import Monopoly.core.ports.DecisionPort;
import Monopoly.core.repo.PlayerRepository;
import Monopoly.core.repo.TileRepository;
import Monopoly.core.service.TurnService;

import java.util.*;

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

        if (player.isPaused()) {
            player.setPaused(false);
            playerRepository.save(player);
            String pausedSummary = buildPausedSummary(turnCounter, player, fromName, player.getPosition());
            return new TurnSummaryEvent(player, pausedSummary);
        }

        int dice = rollDice();
        int oldPos = player.getPosition();
        int newPos = movePlayer(player, dice);

        Tile tile = tileRepository.findByPosition(newPos).orElse(null);
        String toName = tile != null ? tile.getName() : getTileName(newPos);
        String header = buildTurnHeader(turnCounter, player, dice, oldPos, fromName, newPos, toName);

        if (tile == null) {
            String summary = buildSummary(header, "落点：未知地块\n", "无额外事件。", player);
            return new TurnSummaryEvent(player, summary);
        }

        String locationLine = "落点：" + toName + "(#" + newPos + ")\n";

        GameEvent event;
        switch (tile.getTileType()) {
            case COUNTRY -> event = createCountryEvent(players, player, (CountryTile) tile, header, locationLine);
            case SPECIAL -> {
                String detail = handleSpecialTile(player, (SpecialTile) tile);
                event = new TurnSummaryEvent(player, buildSummary(header, locationLine, detail, player));
            }
            case TRAIN_STATION -> {
                String body = "火车站逻辑待实现——暂不交易/收费。";
                event = new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
            }
            case COMPANY -> {
                String body = "公用事业逻辑待实现——暂不交易/收费。";
                event = new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
            }
            case CHANCE, FATE -> {
                String body = "抽牌逻辑待实现，暂时无效果。";
                event = new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
            }
            default -> {
                String body = "暂无额外事件。";
                event = new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
            }
        }
        playerRepository.save(player);
        return event;
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

    private String handleSpecialTile(Player player, SpecialTile tile) {
        return switch (tile.getCategory()) {
            case GO -> "经过/停留起点，若经过已奖励 2000，现金：" + formatMoney(player.getMoney());
            case TAX -> {
                int tax = tile.getName().contains("2000") ? 2000 : 1000;
                payMoney(player, tax);
                yield "支付税费 " + formatMoney(tax) + "，现金：" + formatMoney(player.getMoney());
            }
            case JAIL -> "只是路过监狱，无需停留。";
            case GO_TO_JAIL -> {
                player.setPaused(true);
                yield "被送入监狱，下回合暂停行动。";
            }
            case FREE_PARKING -> "免费停车休息，无额外事件。";
            default -> "暂无额外事件。";
        };
    }

    private GameEvent createCountryEvent(List<Player> players, Player player, CountryTile tile,
                                         String header, String locationLine) {
        int position = tile.getPosition();
        Integer ownerId = tileOwners.get(position);
        if (ownerId == null) {
            int price = tile.getSellPrice();
            if (player.getMoney() < price) {
                String body = "该国家无人持有，售价 " + formatMoney(price)
                        + "，但您的现金不足(" + formatMoney(player.getMoney()) + ")。";
                return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
            }
            String body = "该国家无人持有，售价 " + formatMoney(price)
                    + "，基础过路费 " + formatMoney(tile.getBaseToll())
                    + "。\n当前现金：" + formatMoney(player.getMoney())
                    + "，稍后将询问是否购买。";
            String preMessage = buildSummary(header, locationLine, body, player);
            return new PurchasePromptEvent(player, tile, preMessage);
        }
        if (ownerId.equals(player.getId())) {
            String body = "这是您自己的国家，未来可以在此建造房屋/旅馆（功能待实现）。";
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        }
        Optional<Player> ownerOpt = players.stream().filter(p -> p.getId() == ownerId).findFirst();
        if (ownerOpt.isEmpty()) {
            String body = "该国家原有者不存在，暂不收费。";
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        }
        Player owner = ownerOpt.get();
        int toll = tile.getBaseToll();
        if (player.getMoney() >= toll) {
            payMoney(player, toll);
            owner.setMoney(owner.getMoney() + toll);
            playerRepository.save(owner);
            String body = "需向 " + owner.getName() + " 支付过路费 " + formatMoney(toll)
                    + "。\n您的现金：" + formatMoney(player.getMoney())
                    + "；" + owner.getName() + " 现金：" + formatMoney(owner.getMoney());
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        } else {
            int paid = player.getMoney();
            player.setMoney(0);
            owner.setMoney(owner.getMoney() + paid);
            playerRepository.save(owner);
            boolean remainingPlayers = players.stream().filter(p -> p.getMoney() > 0).count() > 1;
            gameOver = !remainingPlayers;
            String body = "资金不足，向 " + owner.getName() + " 支付全部现金 " + formatMoney(paid)
                    + "，您的余额归零。"
                    + (gameOver ? "\n其余玩家资金均耗尽，游戏即将结束。" : "");
            return new TurnSummaryEvent(player, buildSummary(header, locationLine, body, player));
        }
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
                } else {
                    message = "玩家 " + player.getName() + " 试图购买 [" + tile.getName()
                            + "]，但现金不足(" + formatMoney(player.getMoney()) + "/" + formatMoney(price) + ")。";
                }
            } else {
                message = "玩家 " + player.getName() + " 放弃购买 [" + tile.getName() + "]。";
            }
            playerRepository.save(player);
            String summary = buildResultSummary("购地结果", message, player);
            return new TurnSummaryEvent(player, summary);
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

    private String describeAssets(Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append("- 现金：").append(formatMoney(player.getMoney())).append("\n");
        sb.append("- 地产：");
        if (player.getOwnedTilePositions().isEmpty()) {
            sb.append("无");
        } else {
            String owned = player.getOwnedTilePositions().stream()
                    .sorted()
                    .map(pos -> tileRepository.findByPosition(pos)
                            .map(tile -> tile.getName() + "(#" + pos + ")")
                            .orElse("格" + pos))
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
                + "移动：" + fromName + "(#" + oldPos + ") -> " + toName + "(#" + newPos + ")\n\n";
    }

    private String buildPausedSummary(int turn, Player player, String tileName, int position) {
        StringBuilder sb = new StringBuilder();
        sb.append("==================== 回合 ").append(turn).append(" ====================\n");
        sb.append("玩家：").append(player.getName()).append("\n");
        sb.append("状态：暂停一回合\n");
        sb.append("当前位置：").append(tileName).append("(#").append(position).append(")\n\n");
        sb.append("事件：玩家被暂停，跳过本次行动。\n\n");
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
}

