package org.example.javafxdb_sql_shellcode;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;


public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    void onLogin() throws IOException {
        // Validate the email format with RegEx
        if (!Validator.isValid(Validator.EMAIL, emailField.getText())) {
            errorLabel.setText("Please enter a valid email (example: name@farmingdale.edu).");
            emailField.requestFocus();
            return;
        }
        if (passwordField.getText().isEmpty()) {
            errorLabel.setText("Please enter your password.");
            passwordField.requestFocus();
            return;
        }
        // Input looks fine -> go to the main database screen
        App.setRoot("db_interface_gui");
    }

    /** "Create an account" link -> registration page. */
    @FXML
    void goToRegister() throws IOException {
        App.setRoot("registration");
    }

    /** Small theme button on the login card. */
    @FXML
    void toggleTheme() {
        ThemeManager.toggle(App.getScene());
    }
}
