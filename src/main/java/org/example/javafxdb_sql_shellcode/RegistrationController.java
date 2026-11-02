package org.example.javafxdb_sql_shellcode;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;


public class RegistrationController {

    @FXML private TextField firstNameField, lastNameField, emailField;
    @FXML private PasswordField passwordField, confirmField;
    @FXML private Label errorLabel;

    @FXML
    void onRegister() throws IOException {
        // Check each field in order and show the first problem found
        String error = null;
        if (!Validator.isValid(Validator.NAME, firstNameField.getText())) {
            error = "First name: letters only, 2-25 characters.";
        } else if (!Validator.isValid(Validator.NAME, lastNameField.getText())) {
            error = "Last name: letters only, 2-25 characters.";
        } else if (!Validator.isValid(Validator.EMAIL, emailField.getText())) {
            error = "Please enter a valid email address.";
        } else if (!Validator.isValid(Validator.PASSWORD, passwordField.getText())) {
            error = "Password: at least 8 characters with a letter and a number.";
        } else if (!passwordField.getText().equals(confirmField.getText())) {
            error = "Passwords do not match.";
        }

        if (error != null) {
            errorLabel.setText(error);
            return;
        }

        // All valid
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration");
        alert.setHeaderText("Account form is valid");
        alert.setContentText("Registration is UI-only for this lab, so the account is not saved. "
                + "You can now sign in.");
        ThemeManager.style(alert);
        alert.showAndWait();
        App.setRoot("login");
    }

    /** "Back to sign in" link. */
    @FXML
    void goToLogin() throws IOException {
        App.setRoot("login");
    }
}
