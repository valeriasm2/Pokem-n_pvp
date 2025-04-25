package com.projecte.Controladores;

import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

import com.projecte.BaseController;

public class ControllerAttackResult extends BaseController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Vista AttackResult cargada");
        // Mostrar resultado del ataque: daño, estamina, si falló, puntos, etc.
    }
}
