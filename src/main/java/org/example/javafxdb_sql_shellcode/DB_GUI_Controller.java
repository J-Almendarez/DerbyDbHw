package org.example.javafxdb_sql_shellcode;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.example.javafxdb_sql_shellcode.db.ConnDbOps;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;


public class DB_GUI_Controller implements Initializable {

    // List shown in the TableView
    private final ObservableList<Person> data = FXCollections.observableArrayList();

    // Database helper
    private final ConnDbOps db = App.getDb();

    // Location of the picture currently shown in the profile ImageView
    private String currentImageURL = null;

    // ---------- controls from the FXML ----------
    @FXML TextField first_name, last_name, department, major, email, searchField;
    @FXML private TableView<Person> tv;
    @FXML private TableColumn<Person, Integer> tv_id;
    @FXML private TableColumn<Person, String> tv_fn, tv_ln, tv_dept, tv_major, tv_email;
    @FXML ImageView img_view;
    @FXML private Label statusLabel, errorLabel;
    @FXML private Button addBtn, editBtn, deleteBtn;
    @FXML private MenuItem addItem, editItem, deleteItem;
    @FXML private RadioMenuItem lightItem, darkItem;

    /** Runs once after the FXML is loaded: set up the table and load data from Derby. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Tell each column which Person getter to use
        tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tv_dept.setCellValueFactory(new PropertyValueFactory<>("dept"));
        tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
        tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
        tv.setItems(data);

        // When a row is selected copy it into the form
        tv.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> selectedItemTV());

        // Re-check the RegEx rules every time the user types in a field
        for (TextField f : new TextField[]{first_name, last_name, department, major, email}) {
            f.textProperty().addListener((obs, o, n) -> validateForm());
        }

        // Live search: reload the table every time the search text changes
        searchField.textProperty().addListener((obs, o, n) -> loadData());

        // Show the profile picture as a circle
        img_view.setClip(new Circle(60, 60, 60));
        showDefaultImage();

        // Tick the correct theme in the View menu
        darkItem.setSelected(ThemeManager.isDark());
        lightItem.setSelected(!ThemeManager.isDark());

        loadData();
        validateForm();
    }

    // =====================================================================
    //  Database actions (buttons + "Database" menu)
    // =====================================================================

    /** Reloads the table from the database */
    @FXML
    protected void loadData() {
        String search = searchField.getText();
        data.setAll(search == null || search.isBlank() ? db.getAllPersons() : db.searchByName(search));
        setStatus(data.size() + " record(s) loaded.");
    }

    /** ADD: inserts the form as a new person. */
    @FXML
    protected void addNewRecord() {
        if (!validateForm()) return;          // never write invalid data to the DB
        Person p = personFromForm();
        try {
            db.insertPerson(p);               // Derby fills in the new id
            loadData();
            tv.getSelectionModel().select(findById(p.getId()));
            setStatus("Added " + p.getFirstName() + " " + p.getLastName() + " (ID " + p.getId() + ").");
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    /** EDIT: saves the form over the selected person. */
    @FXML
    protected void editRecord() {
        Person selected = tv.getSelectionModel().getSelectedItem();
        if (selected == null || !validateForm()) return;
        Person p = personFromForm();
        p.setId(selected.getId());            // keep the same id -> UPDATE that row
        try {
            db.updatePerson(p);
            loadData();
            tv.getSelectionModel().select(findById(p.getId()));
            setStatus("Updated record ID " + p.getId() + ".");
        } catch (SQLException e) {
            showDbError(e);
        }
    }

    /** DELETE: removes the selected person after asking "Are you sure?". */
    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + p.getFirstName() + " " + p.getLastName() + "? This cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Delete record");
        ThemeManager.style(confirm);
        Optional<ButtonType> answer = confirm.showAndWait();

        if (answer.isPresent() && answer.get() == ButtonType.YES) {
            try {
                db.deletePerson(p.getId());
                loadData();
                clearForm();
                setStatus("Deleted record ID " + p.getId() + ".");
            } catch (SQLException e) {
                showDbError(e);
            }
        }
    }

    /** Empties the form and un-selects the table row. */
    @FXML
    protected void clearForm() {
        tv.getSelectionModel().clearSelection();
        first_name.clear();
        last_name.clear();
        department.clear();
        major.clear();
        email.clear();
        showDefaultImage();
        first_name.requestFocus();
        validateForm();
    }

    /** Copies the selected table row into the form fields and shows their picture. */
    @FXML
    protected void selectedItemTV() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDept());
            major.setText(p.getMajor());
            email.setText(p.getEmail());
            showImage(p.getImageURL());
        }
        validateForm(); // also enables/disables Edit and Delete
    }

    // =====================================================================
    //  Profile picture
    // =====================================================================

    /** Opens a file chooser so the user can pick a profile picture Ctrl+F */
    @FXML
    protected void showImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a profile picture");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = chooser.showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            showImage(file.toURI().toString());
            setStatus("Picture selected. Click Add (new person) or Update (selected person) to save it.");
        }
    }

    /** Shows the picture at this URL, or the default icon if it's missing/broken. */
    private void showImage(String url) {
        if (url == null || url.isBlank()) {
            showDefaultImage();
            return;
        }
        Image image = new Image(url, 120, 120, false, true);
        if (image.isError()) {               // e.g. the file was moved or deleted
            showDefaultImage();
        } else {
            img_view.setImage(image);
            currentImageURL = url;
        }
    }

    private void showDefaultImage() {
        img_view.setImage(new Image(App.class.getResourceAsStream("images/profile.png")));
        currentImageURL = null;
    }

    // =====================================================================
    //  Validation (RegEx)
    // =====================================================================

    /**
     * Checks every field against its RegEx. Invalid fields get a red border
     * (CSS class "error"), and Add / Edit / Delete are only enabled when they make sense.
     * @return true if the whole form is valid.
     */
    private boolean validateForm() {
        boolean ok = check(first_name, Validator.NAME)
                & check(last_name, Validator.NAME)        // single & so ALL fields get checked
                & check(department, Validator.DEPT_MAJOR)
                & check(major, Validator.DEPT_MAJOR)
                & check(email, Validator.EMAIL);

        boolean selected = tv.getSelectionModel().getSelectedItem() != null;
        addBtn.setDisable(!ok);
        addItem.setDisable(!ok);
        editBtn.setDisable(!ok || !selected);
        editItem.setDisable(!ok || !selected);
        deleteBtn.setDisable(!selected);
        deleteItem.setDisable(!selected);

        errorLabel.setText(ok ? "" : "Fill in every field. Red = invalid format.");
        return ok;
    }

    /** Checks one field */
    private boolean check(TextField field, Pattern pattern) {
        String text = field.getText();
        boolean valid = Validator.isValid(pattern, text);
        field.getStyleClass().remove("error");
        if (!valid && text != null && !text.isEmpty()) {
            field.getStyleClass().add("error");
        }
        return valid;
    }

    // =====================================================================
    //  Menu actions that are not database related
    // =====================================================================

    @FXML protected void focusSearch() { searchField.requestFocus(); }

    @FXML protected void setLightTheme() { ThemeManager.setDark(false, App.getScene()); }

    @FXML protected void setDarkTheme() { ThemeManager.setDark(true, App.getScene()); }

    /** Ctrl+T: flip the theme and update the tick in the View menu. */
    @FXML
    protected void toggleTheme() {
        ThemeManager.toggle(App.getScene());
        darkItem.setSelected(ThemeManager.isDark());
        lightItem.setSelected(!ThemeManager.isDark());
    }

    @FXML
    protected void logout() throws IOException {
        App.setRoot("login");
    }

    @FXML
    protected void closeApplication() {
        Platform.exit(); // closes the GUI and returns to the console menu
    }

    @FXML
    protected void showShortcuts() {
        showText("Keyboard Shortcuts",
                "Ctrl+F   Upload profile picture (open file)\n"
                        + "Ctrl+N   New / clear form\n"
                        + "Ctrl+S   Add (save) new record\n"
                        + "Ctrl+E   Update (edit) selected record\n"
                        + "Ctrl+D   Delete selected record\n"
                        + "F5       Refresh table from the database\n"
                        + "Ctrl+K   Search by name\n"
                        + "Ctrl+T   Toggle light / dark theme\n"
                        + "Ctrl+L   Log out\n"
                        + "Ctrl+Q   Exit\n"
                        + "F1       This list\n"
                        + "Ctrl+H   Challenges & solutions");
    }

    /** Help menu: the challenges I ran into while integrating Derby and how I fixed them. */
    @FXML
    protected void showChallenges() {
        showText("Challenges & Solutions",
                "1. Console input skipped prompts (Enter Email / Enter Phone printed together).\n"
                        + "   Cause: scan.next() only reads one word, so \"John G\" filled two fields.\n"
                        + "   Solution: use scan.nextLine() and throw away the leftover Enter after the menu choice.\n\n"
                        + "2. After copying the GUI files into the shell project: LoadException / IllegalAccessException.\n"
                        + "   Cause: the files still had the old package name com.example.module03_basicgui_db_interface.\n"
                        + "   Solution: changed every 'package' line and every fx:controller in the FXML files.\n\n"
                        + "3. Changing the table columns had no effect.\n"
                        + "   Cause: the code ignores the 'table already exists' error (X0Y32), so the old table stayed.\n"
                        + "   Solution: used a new table name (persons). Deleting the DBname folder also resets it.\n\n"
                        + "4. \"Another instance of Derby may have already booted the database\".\n"
                        + "   Cause: embedded Derby lets only one program use the database at a time.\n"
                        + "   Solution: stop the old run before starting a new one.\n\n"
                        + "5. Git: push went to the wrong place / 'remote origin already exists'.\n"
                        + "   Solution: pointed the remote to my own private repo and added DBname/ and derby.log to .gitignore.\n\n"
                        + "6. Duplicate emails crashed the insert.\n"
                        + "   Solution: email is UNIQUE in the table; SQLState 23505 is caught and shown as a friendly message.");
    }

    @FXML
    protected void showAbout() {
        showText("About",
                "FSC Student Records\nCSC311 Advanced Programming - Module 10\n\n"
                        + "JavaFX + embedded Apache Derby database.\n"
                        + "Add, edit, delete and search students, upload profile pictures,\n"
                        + "and switch between light and dark themes.");
    }

    // =====================================================================
    //  Small helpers
    // =====================================================================

    /** Builds a Person from what is typed in the form. */
    private Person personFromForm() {
        return new Person(first_name.getText().trim(), last_name.getText().trim(),
                department.getText().trim(), major.getText().trim(),
                email.getText().trim(), currentImageURL);
    }

    /** Finds a person in the table list by id (used to re-select a row after reloading). */
    private Person findById(Integer id) {
        for (Person p : data) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    /** Shows a friendly message for database errors. */
    private void showDbError(SQLException e) {
        String msg = ConnDbOps.isDuplicateKey(e)
                ? "That email is already used by another record."
                : "Database error: " + e.getMessage();
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setHeaderText("Could not save");
        ThemeManager.style(alert);
        alert.showAndWait();
        setStatus(msg);
    }

    /** Shows a read-only text window (used by the Help menu). */
    private void showText(String title, String text) {
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefSize(620, 360);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.getDialogPane().setContent(area);
        ThemeManager.style(alert);
        alert.showAndWait();
    }
}
