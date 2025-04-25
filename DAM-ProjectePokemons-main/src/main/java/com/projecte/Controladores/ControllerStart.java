package com.projecte.Controladores;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class ControllerStart implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicialización si hace falta
    }

    @FXML
    public void previous(ActionEvent event) {
        System.out.println("To previous view");
        // Aquí pon lógica para cambiar vista anterior
    }

    @FXML
    public void next(ActionEvent event) {
        System.out.println("To next view");
        // Aquí pon lógica para cambiar vista siguiente
    }
}
