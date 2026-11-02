package org.example.javafxdb_sql_shellcode;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Splash screen: shows the logo for a few seconds, then moves on to the
 * Login page by itself. The user can also click anywhere to skip the wait.
 */
public class SplashScreenController {

    private static final double SPLASH_SECONDS = 3;
    private boolean leaving = false; // stops us from switching pages twice

    /** Called automatically by FXMLLoader after the FXML is loaded. */
    @FXML
    public void initialize() {
        PauseTransition wait = new PauseTransition(Duration.seconds(SPLASH_SECONDS));
        wait.setOnFinished(e -> goToLogin());
        wait.play();
    }

    /** onMouseClicked handler from splash_screen.fxml: skip the wait. */
    @FXML
    void clickme() {
        goToLogin();
    }

    private void goToLogin() {
        if (leaving) return;
        leaving = true;
        try {
            App.setRoot("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
