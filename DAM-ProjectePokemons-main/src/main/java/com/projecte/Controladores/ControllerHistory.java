package com.projecte.Controladores;

import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

import com.projecte.BaseController;

public class ControllerHistory extends BaseController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Vista History cargada");
        // Mostrar historial de partidas guardadas en la base de datos
    }
}
