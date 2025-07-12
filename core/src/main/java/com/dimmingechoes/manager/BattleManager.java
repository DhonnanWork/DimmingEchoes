package com.dimmingechoes.manager;

import com.dimmingechoes.entities.Enemy;
import com.dimmingechoes.entities.Player;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BattleManager {
    private final Player player;
    private final List<Enemy> enemies;
    private final Queue<Object> turnQueue; // Object can be Player or Enemy
    private boolean battleOver = false;
    private boolean playerTurn = true;

    public BattleManager(Player player, List<Enemy> enemies) {
        this.player = player;
        this.enemies = new ArrayList<>(enemies);
        this.turnQueue = new LinkedList<>();
        turnQueue.add(player);
        turnQueue.addAll(enemies);
    }

    public boolean isBattleOver() {
        return battleOver;
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void nextTurn() {
        Object current = turnQueue.poll();
        turnQueue.offer(current);
        playerTurn = (turnQueue.peek() instanceof Player);
    }

    public void playerAttack(Enemy target) {
        if (!playerTurn || battleOver) return;
        int damage = Math.max(1, player.getAttackPower() - target.getDefense());
        target.setHealth(target.getHealth() - damage);
        if (target.getHealth() <= 0) {
            enemies.remove(target);
            turnQueue.remove(target);
        }
        checkBattleEnd();
        nextTurn();
    }

    public void playerUseItem() {
        // Implement item logic as needed
        nextTurn();
    }

    public void playerFlee() {
        // Implement flee logic (e.g., random chance)
        battleOver = true;
    }

    public void enemyTurn(Enemy enemy) {
        if (battleOver) return;
        int damage = Math.max(1, enemy.getAttackPower() - player.getDefense());
        player.setHealth(player.getHealth() - damage);
        if (player.getHealth() <= 0) {
            battleOver = true;
        }
        nextTurn();
    }

    private void checkBattleEnd() {
        if (enemies.isEmpty()) {
            battleOver = true;
        }
        if (player.getHealth() <= 0) {
            battleOver = true;
        }
    }
} 