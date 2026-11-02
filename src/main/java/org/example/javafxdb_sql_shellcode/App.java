package org.example.javafxdb_sql_shellcode;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.javafxdb_sql_shellcode.db.ConnDbOps;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class App extends Application {

    private static Scene scene;
    private static ConnDbOps cdbop;
    private static boolean guiLaunched = false; // JavaFX can only be launched once per run

    @Override
    public void start(Stage stage) throws IOException {
        // Make sure the table exists before any screen tries to read it
        getDb().connectToDatabase();

        scene = new Scene(loadFXML("splash_screen"), 1000, 640);
        ThemeManager.apply(scene);                 // light theme by default

        stage.setTitle("FSC Student Records");
        stage.getIcons().add(new Image(App.class.getResourceAsStream("images/suny_farmingdale.png")));
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    /** Swaps the current page for another FXML page with a short fade-in */
    static void setRoot(String fxml) throws IOException {
        Parent root = loadFXML(fxml);
        root.setOpacity(0);
        scene.setRoot(root);
        FadeTransition fade = new FadeTransition(Duration.millis(400), root);
        fade.setToValue(1);
        fade.play();
    }

    /** The single Scene */
    static Scene getScene() {
        return scene;
    }

    /** Shared database object so every controller uses the same one. */
    public static ConnDbOps getDb() {
        if (cdbop == null) {
            cdbop = new ConnDbOps();
        }
        return cdbop;
    }

    /** Loads an FXML file */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    // ======================================================================
    //  Console (text) menu from the shell code, updated for the Person table
    //  and with the new Edit ('u') and Delete ('d') options.
    // ======================================================================
    public static void main(String[] args) {
        cdbop = getDb();
        Scanner scan = new Scanner(System.in);

        char input;
        do {
            System.out.println(" ");
            System.out.println("============== Menu ==============");
            System.out.println("| To start GUI,           press 'g' |");
            System.out.println("| To connect to DB,       press 'c' |");
            System.out.println("| To display all users,   press 'a' |");
            System.out.println("| To insert to the DB,    press 'i' |");
            System.out.println("| To edit a user,         press 'u' |");
            System.out.println("| To delete a user,       press 'd' |");
            System.out.println("| To query by name,       press 'q' |");
            System.out.println("| To exit,                press 'e' |");
            System.out.println("===================================");
            System.out.print("Enter your choice: ");
            input = scan.next().charAt(0);
            scan.nextLine(); // throw away the rest of the line  so nextLine() works below

            switch (input) {
                case 'g':
                    if (guiLaunched) {
                        // Application.launch() throws an error if called twice
                        System.out.println("The GUI was already opened once. Restart the program to open it again.");
                    } else {
                        guiLaunched = true;
                        launch(args); // blocks until the GUI window is closed
                    }
                    break;

                case 'c':
                    boolean hasRecords = cdbop.connectToDatabase();
                    System.out.println("Connected. Table has records: " + hasRecords);
                    break;

                case 'a':
                    cdbop.listAllUsers();
                    break;

                case 'i': {
                    // nextLine() names with spaces work
                    Person p = readPersonFromConsole(scan, null);
                    try {
                        cdbop.insertPerson(p);
                        System.out.println("A new user was inserted successfully. ID = " + p.getId());
                    } catch (SQLException e) {
                        System.out.println(ConnDbOps.isDuplicateKey(e)
                                ? "That email is already in the database." : "Insert failed: " + e.getMessage());
                    }
                    break;
                }

                case 'u': {
                    Integer id = readId(scan, "Enter the ID of the user to edit: ");
                    if (id == null) break;
                    Person p = readPersonFromConsole(scan, id);
                    try {
                        System.out.println(cdbop.updatePerson(p) ? "User updated." : "No user with ID " + id);
                    } catch (SQLException e) {
                        System.out.println(ConnDbOps.isDuplicateKey(e)
                                ? "That email is already used by another user." : "Update failed: " + e.getMessage());
                    }
                    break;
                }

                case 'd': {
                    Integer id = readId(scan, "Enter the ID of the user to delete: ");
                    if (id == null) break;
                    System.out.print("Are you sure? (y/n): ");
                    if (scan.nextLine().trim().equalsIgnoreCase("y")) {
                        try {
                            System.out.println(cdbop.deletePerson(id) ? "User deleted." : "No user with ID " + id);
                        } catch (SQLException e) {
                            System.out.println("Delete failed: " + e.getMessage());
                        }
                    }
                    break;
                }

                case 'q':
                    System.out.print("Enter the name to query: ");
                    cdbop.queryUserByName(scan.nextLine());
                    break;

                case 'e':
                    System.out.println("Exiting...");
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
            System.out.println(" ");
        } while (input != 'e');

        scan.close();
        System.exit(0); // make sure the program fully stops
    }

    /** Asks for every Person field in the console, re-asking until each one passes its RegEx. */
    private static Person readPersonFromConsole(Scanner scan, Integer id) {
        Person p = new Person();
        p.setId(id);
        p.setFirstName(ask(scan, "Enter First Name: ", Validator.NAME));
        p.setLastName(ask(scan, "Enter Last Name: ", Validator.NAME));
        p.setDept(ask(scan, "Enter Department: ", Validator.DEPT_MAJOR));
        p.setMajor(ask(scan, "Enter Major: ", Validator.DEPT_MAJOR));
        p.setEmail(ask(scan, "Enter Email: ", Validator.EMAIL));
        return p;
    }

    /** Prints a prompt and keeps asking until the input matches the pattern. */
    private static String ask(Scanner scan, String prompt, Pattern pattern) {
        while (true) {
            System.out.print(prompt);
            String value = scan.nextLine().trim();
            if (Validator.isValid(pattern, value)) {
                return value;
            }
            System.out.println("  Invalid input, please try again.");
        }
    }

    /** Reads a whole number ID; returns null if the user typed something else. */
    private static Integer readId(Scanner scan, String prompt) {
        System.out.print(prompt);
        String text = scan.nextLine().trim();
        if (!text.matches("\\d+")) {               // RegEx: one or more digits
            System.out.println("ID must be a number.");
            return null;
        }
        return Integer.parseInt(text);
    }
}
