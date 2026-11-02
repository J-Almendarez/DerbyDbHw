package org.example.javafxdb_sql_shellcode;

import javafx.scene.Scene;
import javafx.scene.control.Dialog;

import java.util.List;


public final class ThemeManager {

    private ThemeManager() { }

    /** true = dark theme is active */
    private static boolean dark = false;

    public static boolean isDark() {
        return dark;
    }

    /** Full list of stylesheet URLs for the current theme. */
    public static List<String> stylesheets() {
        String base = App.class.getResource("styling/style.css").toExternalForm();
        String theme = App.class.getResource(dark ? "styling/dark.css" : "styling/light.css").toExternalForm();
        return List.of(theme, base);
    }

    /** Applies the current theme to a scene. */
    public static void apply(Scene scene) {
        if (scene != null) {
            scene.getStylesheets().setAll(stylesheets());
        }
    }

    /** Sets the theme (true = dark) and re-styles the scene right away. */
    public static void setDark(boolean value, Scene scene) {
        dark = value;
        apply(scene);
    }

    /** Flips between light and dark. */
    public static void toggle(Scene scene) {
        setDark(!dark, scene);
    }

    /** Pop-up dialogs (Alerts) have their own scene, so they need the theme too. */
    public static void style(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().setAll(stylesheets());
    }
}
