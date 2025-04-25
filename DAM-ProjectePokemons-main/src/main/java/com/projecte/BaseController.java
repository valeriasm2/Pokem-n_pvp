package com.projecte;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import com.utils.UtilsViews;

public class BaseController {

    protected static final String[] views = {
        "ViewStart",
        "ViewMenu",
        "ViewManagement",
        "ViewStats",
        "ViewPokemonSettings",
        "ViewHistory",
        "ViewBattleOptions",
        "ViewBattleAttack",
        "ViewAttackResult",
        "ViewBattleResult"
    };

    protected static int currentIndex = 0;

    protected void loadViewByIndex(int index) {
        if (index >= 0 && index < views.length) {
            currentIndex = index;
            String viewName = views[index];
            try {
                UtilsViews.addView(getClass(), viewName, "/assets/" + viewName.toLowerCase() + ".fxml");
            } catch (Exception e) {
                System.err.println("Error cargando vista: " + viewName);
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void previous(ActionEvent event) {
        if (currentIndex > 0) {
            loadViewByIndex(currentIndex - 1);
        } else {
            System.out.println("Ya estás en la primera vista");
        }
    }

    @FXML
    public void next(ActionEvent event) {
        if (currentIndex < views.length - 1) {
            loadViewByIndex(currentIndex + 1);
        } else {
            System.out.println("Ya estás en la última vista");
        }
    }
}