package com.example.textquestgame;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class Game {
    private Tower tower;
    private Player player;
    private Inventory inventory;
    private int currentEventId = 1;
    private Event currentEvent;
    private int stage = 1;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private Context context;
    private JSONArray stages;

    // Множество для хранения уже сделанных выборов
    private Set<String> usedChoices = new HashSet<>();

    public Game(String playerName, Context context) {
        this.tower = new Tower();
        this.player = new Player(playerName);
        this.inventory = new Inventory();
        this.context = context;
        loadStages();
        this.currentEvent = getEventById(1);
    }

    private void loadStages() {
        try {
            InputStream is = context.getAssets().open("events.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONObject obj = new JSONObject(json);
            stages = obj.getJSONArray("stages");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Event getEventById(int id) {
        try {
            for (int i = 0; i < stages.length(); i++) {
                JSONObject stageObj = stages.getJSONObject(i);
                if (stageObj.getInt("id") == id) {
                    Event event = new Event(stageObj.getString("description"));

                    JSONArray choices = stageObj.getJSONArray("choices");
                    for (int j = 0; j < choices.length(); j++) {
                        JSONObject choiceObj = choices.getJSONObject(j);
                        String choiceText = choiceObj.getString("text");
                        event.addChoice(choiceText);

                    }
                    return event;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void makeChoice(int choiceIndex) {
        if (gameOver || gameWon) return;

        try {
            // Находим текущее событие по ID
            for (int i = 0; i < stages.length(); i++) {
                JSONObject stageObj = stages.getJSONObject(i);
                if (stageObj.getInt("id") == currentEventId) {
                    JSONArray choices = stageObj.getJSONArray("choices");
                    JSONObject choiceObj = choices.getJSONObject(choiceIndex);
                    JSONObject effects = choiceObj.getJSONObject("effects");

                    // Получаем текст выбора для блокировки
                    String choiceText = choiceObj.getString("text");
                    String choiceKey = currentEventId + "_" + choiceText;

                    // Применяем эффекты
                    int energyChange = effects.optInt("playerEnergy", 0);
                    int towerChange = effects.optInt("towerHealth", 0);
                    int playerHealthChange = effects.optInt("playerHealth", 0);

                    if (energyChange > 0) {
                        player.addEnergy(energyChange);
                    } else if (energyChange < 0) {
                        player.useEnergy(-energyChange);
                    }

                    if (towerChange > 0) {
                        tower.repair(towerChange);
                    } else if (towerChange < 0) {
                        tower.takeDamage(-towerChange);
                    }

                    if (playerHealthChange < 0) {
                        player.takeDamage(-playerHealthChange);
                    } else if (playerHealthChange > 0) {
                        player.heal(playerHealthChange);
                    }

                    // Добавление предметов
                    if (effects.has("addItem")) {
                        JSONObject addItem = effects.getJSONObject("addItem");
                        inventory.addItem(addItem.getString("name"), addItem.getString("desc"));
                    }

                    // Удаление предметов
                    if (effects.has("removeItem")) {
                        inventory.removeItem(effects.getString("removeItem"));
                    }

                    // Переход на следующий ID события (с проверкой альт. перехода)
                    int nextId = effects.getInt("nextId");
                    if (effects.has("requireItem") && effects.has("altNextId")) {
                        String requiredItem = effects.getString("requireItem");
                        int altNextId = effects.getInt("altNextId");
                        if (inventory.hasItem(requiredItem)) {
                            nextId = altNextId;
                        }
                    }

                    // Проверяем условия победы/поражения
                    if (effects.has("winCondition")) {
                        JSONObject winCond = effects.getJSONObject("winCondition");
                        String type = winCond.getString("type");

                        if (type.equals("win")) gameWon = true;
                        else if (type.equals("lose")) gameOver = true;

                    } else {
                        if (nextId != -1) {
                            currentEventId = nextId;
                            stage++;
                        }
                    }
                    usedChoices.add(choiceKey);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Проверка на смерть
        if (tower.getHealth() <= 0 || player.getHealth() <= 0) gameOver = true;
        if (player.getEnergy() < 0) player.addEnergy(0);

        // Получаем следующее событие если игра не закончена
        if (!gameOver && !gameWon) {
            currentEvent = getEventById(currentEventId);
            if (currentEvent == null) gameOver = true;
        }
    }

    // Метод для проверки, использован ли выбор
    public boolean isChoiceUsed(int choiceIndex) {
        try {
            for (int i = 0; i < stages.length(); i++) {
                JSONObject stageObj = stages.getJSONObject(i);
                if (stageObj.getInt("id") == currentEventId) {
                    JSONArray choices = stageObj.getJSONArray("choices");
                    JSONObject choiceObj = choices.getJSONObject(choiceIndex);
                    String choiceText = choiceObj.getString("text");
                    String choiceKey = currentEventId + "_" + choiceText;
                    return usedChoices.contains(choiceKey);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Геттеры
    public Tower getTower() { return tower; }
    public Player getPlayer() { return player; }
    public Inventory getInventory() { return inventory; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
    public Event getCurrentEvent() { return currentEvent; }
    public int getCurrentEventId() { return currentEventId; }
    public int getStage() { return stage; }
}