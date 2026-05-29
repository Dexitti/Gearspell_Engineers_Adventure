package com.example.textquestgame;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private LinearLayout menuContainer;
    private ImageView menuBackground;
    private LinearLayout gameContainer;
    private TextView storyText;
    private TextView towerStatus;
    private TextView playerHpStatus;
    private TextView energyStatus;
    private TextView stageStatus;
    private LinearLayout choicesContainer;
    private ImageView storyBackground;
    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuContainer = findViewById(R.id.menuContainer);
        menuBackground = findViewById(R.id.menuBackground);
        gameContainer = findViewById(R.id.gameContainer);
        storyText = findViewById(R.id.storyText);
        storyBackground = findViewById(R.id.storyBackground);
        towerStatus = findViewById(R.id.towerStatus);
        playerHpStatus = findViewById(R.id.playerHpStatus);
        energyStatus = findViewById(R.id.energyStatus);
        stageStatus = findViewById(R.id.stageStatus);
        choicesContainer = findViewById(R.id.choicesContainer);

        // Устанавливаем фон
        menuBackground.setImageResource(R.drawable.bg_td_fight);
        menuBackground.setVisibility(View.VISIBLE);

        findViewById(R.id.startButton).setOnClickListener(v -> startGame());

        Button inventoryBtn = findViewById(R.id.inventoryButton);
        inventoryBtn.setOnClickListener(v -> showInventoryDialog());
    }

    private void startGame() {
        game = new Game("Инженер", this);
        menuContainer.setVisibility(View.GONE);
        gameContainer.setVisibility(View.VISIBLE);
        updateScreen();
    }

    private void showInventoryDialog() {
        if (game == null || game.getInventory().isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("📦 Инвентарь")
                    .setMessage("Инвентарь пуст")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        for (String item : game.getInventory().getItems()) {
            TextView itemView = new TextView(this);
            itemView.setText("• " + item);
            itemView.setTextSize(16);
            itemView.setPadding(8, 8, 8, 8);
            layout.addView(itemView);
        }

        scrollView.addView(layout);

        new AlertDialog.Builder(this)
                .setTitle("📦 Инвентарь (" + game.getInventory().getSize() + ")")
                .setView(scrollView)
                .setPositiveButton("Закрыть", null)
                .show();
    }

    @SuppressWarnings("SetTextI18n")
    private void updateScreen() {
        if (game == null) return;

        // Текст истории
        String story = game.getCurrentEvent().getText();
        storyText.setText(story);

        // Можно добавить фон

        // Статус
        towerStatus.setText("🏰: " + game.getTower().getHealth());
        playerHpStatus.setText("❤: " + game.getPlayer().getHealth());
        energyStatus.setText("⚡: " + game.getPlayer().getEnergy());
        stageStatus.setText("Этап: " + game.getStage());

        // Кнопки
        choicesContainer.removeAllViews();

        if (game.isGameWon()) {
            storyText.setText("ПОБЕДА");
            addRestartButton();
        } else if (game.isGameOver()) {
            storyText.setText("ПОРАЖЕНИЕ");
            addRestartButton();
        } else {
            addChoiceButtons();
        }
    }

    private void addChoiceButtons() {
        for (int i = 0; i < game.getCurrentEvent().getChoices().size(); i++) {
            Button btn = new Button(this);
            String choiceText = game.getCurrentEvent().getChoices().get(i).getText();
            btn.setText(choiceText);

            String requiredItem = getRequiredItemForChoice(game.getCurrentEventId(), i);
            boolean hasRequiredItem = requiredItem != null && game.getInventory().hasItem(requiredItem);
            boolean isUsed = game.isChoiceUsed(i);

            if (isUsed) {
                // Заблокированная (серая) кнопка
                btn.setEnabled(false);
                btn.setAlpha(0.5f);
            } else if (requiredItem != null && !hasRequiredItem) {
                // Если требуемый предмет отсутствует — блокируем кнопку
                btn.setEnabled(false);
                btn.setAlpha(0.3f);
                btn.setText(choiceText + " (" + requiredItem + ")");
            } else {
                // Активная кнопка
                btn.setEnabled(true);
                btn.setAlpha(1f);
                int choice = i;
                btn.setOnClickListener(v -> {
                    game.makeChoice(choice);
                    updateScreen();
                });
            }

            choicesContainer.addView(btn);
        }
    }

    private String getRequiredItemForChoice(int eventId, int choiceIndex) {
        try {
            for (int i = 0; i < game.stages.length(); i++) {
                JSONObject stageObj = game.stages.getJSONObject(i);
                if (stageObj.getInt("id") == eventId) {
                    JSONArray choices = stageObj.getJSONArray("choices");
                    JSONObject choiceObj = choices.getJSONObject(choiceIndex);
                    if (choiceObj.has("effects")) {
                        JSONObject effects = choiceObj.getJSONObject("effects");
                        if (effects.has("requireItem")) {
                            Object requireObj = effects.get("requireItem");
                            if (requireObj instanceof JSONArray) {
                                JSONArray items = (JSONArray) requireObj;
                                StringBuilder sb = new StringBuilder();
                                for (int j = 0; j < items.length(); j++) {
                                    if (j > 0) sb.append(", ");
                                    sb.append(items.getString(j));
                                }
                                return sb.toString();
                            } else {
                                return effects.getString("requireItem");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}
        return null;
    }

    private void addRestartButton() {
        Button btn = new Button(this);
        btn.setText("Сыграть еще");
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setOnClickListener(v -> restart());
        choicesContainer.addView(btn);
    }

    private void restart() {
        menuContainer.setVisibility(View.VISIBLE);
        gameContainer.setVisibility(View.GONE);
        choicesContainer.removeAllViews();
        game = null;
    }
}