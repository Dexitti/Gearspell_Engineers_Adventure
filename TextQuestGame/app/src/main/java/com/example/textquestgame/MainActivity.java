package com.example.textquestgame;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

        StringBuilder itemsText = new StringBuilder();
        for (InventoryItem item : game.getInventory().getItems()) {
            itemsText.append("• ").append(item.getName())
                    .append("\n  ").append(item.getDescription())
                    .append("\n\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("📦 Инвентарь (" + game.getInventory().getSize() + "/5)")
                .setMessage(itemsText.toString())
                .setPositiveButton("Закрыть", null)
                .show();
    }

    @SuppressWarnings("SetTextI18n")
    private void updateScreen() {
        if (game == null) return;

        // Текст истории
        String story = game.getCurrentEvent().getText();
        storyText.setText(story);

        // Фон
        int bgResId = getBackgroundForEvent(game.getCurrentEventId());
        if (bgResId != 0) {
            storyBackground.setImageResource(bgResId);
            storyBackground.setVisibility(View.VISIBLE);
        } else {
            storyBackground.setVisibility(View.GONE);
        }

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

    private int getBackgroundForEvent(int eventId) {
        // Здесь можно задать фоны для конкретных eventId
        // Например: if (eventId == 8) return R.drawable.crystal_beach;
        return 0; // 0 - нет фона
    }

    private void addChoiceButtons() {
        for (int i = 0; i < game.getCurrentEvent().getChoices().size(); i++) {
            Button btn = new Button(this);
            btn.setText(game.getCurrentEvent().getChoices().get(i).getText());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            btn.setLayoutParams(params);

            // Проверяем, использован ли выбор
            boolean isUsed = game.isChoiceUsed(i);

            if (isUsed) {
                // Заблокированная (серая) кнопка
                btn.setEnabled(false);
                btn.setAlpha(0.5f);
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