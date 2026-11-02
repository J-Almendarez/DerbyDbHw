package org.example.javafxdb_sql_shellcode.db;

import org.example.javafxdb_sql_shellcode.Person;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ConnDbOps {
    final String DB_URL = "jdbc:derby:DBname;create=true";

    // New table name
    private static final String TABLE = "persons";

    /** Opens a new connection to the Derby database. */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Connects to Derby and creates the PERSONS table if it isn't there yet.
     * @return true if the table already has at least one person in it.
     */
    public boolean connectToDatabase() {
        boolean hasRecords = false;
        try (Connection conn = getConnection();
             Statement statement = conn.createStatement()) {

            String sql = "CREATE TABLE " + TABLE + " ("
                    + "id INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "first_name VARCHAR(50) NOT NULL,"
                    + "last_name VARCHAR(50) NOT NULL,"
                    + "dept VARCHAR(50),"
                    + "major VARCHAR(50),"
                    + "email VARCHAR(200) NOT NULL UNIQUE,"
                    + "image_url VARCHAR(1000)"
                    + ")";
            try {
                statement.executeUpdate(sql);
            } catch (SQLException ex) {
                if (!"X0Y32".equals(ex.getSQLState())) {
                    throw ex;
                }
            }

            // Count rows to know if the table is empty
            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + TABLE)) {
                if (rs.next()) {
                    hasRecords = rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hasRecords;
    }

    /** CREATE: inserts a person and stores the id Derby generated back into the object. */
    public void insertPerson(Person p) throws SQLException {
        String sql = "INSERT INTO " + TABLE
                + " (first_name, last_name, dept, major, email, image_url) VALUES (?, ?, ?, ?, ?, ?)";
        // PreparedStatement with ? placeholders prevents SQL injection
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setString(3, p.getDept());
            ps.setString(4, p.getMajor());
            ps.setString(5, p.getEmail());
            ps.setString(6, p.getImageURL());
            ps.executeUpdate();

            // Read back the auto-generated id
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
        }
    }

    /** READ: returns every person in the table, ordered by id. */
    public List<Person> getAllPersons() {
        List<Person> list = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE + " ORDER BY id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(fromRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Person> searchByName(String name) {
        List<Person> list = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE
                + " WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? ORDER BY id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + name.toLowerCase().trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(fromRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** UPDATE (Edit): overwrites the row whose id matches p.getId(). Returns true if a row changed. */
    public boolean updatePerson(Person p) throws SQLException {
        String sql = "UPDATE " + TABLE
                + " SET first_name=?, last_name=?, dept=?, major=?, email=?, image_url=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setString(3, p.getDept());
            ps.setString(4, p.getMajor());
            ps.setString(5, p.getEmail());
            ps.setString(6, p.getImageURL());
            ps.setInt(7, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** DELETE: removes the row with this id. Returns true if a row was deleted. */
    public boolean deletePerson(int id) throws SQLException {
        String sql = "DELETE FROM " + TABLE + " WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Helper: turns the current ResultSet row into a Person object. */
    private Person fromRow(ResultSet rs) throws SQLException {
        return new Person(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("dept"),
                rs.getString("major"),
                rs.getString("email"),
                rs.getString("image_url"));
    }

    public static boolean isDuplicateKey(SQLException e) {
        return "23505".equals(e.getSQLState());
    }

    // ----- Console helpers kept from the original shell code (used by App's text menu) -----

    /** Prints every person to the console. */
    public void listAllUsers() {
        List<Person> all = getAllPersons();
        if (all.isEmpty()) {
            System.out.println("No records in the database.");
        }
        all.forEach(System.out::println);
    }

    /** Prints people matching a name to the console. */
    public void queryUserByName(String name) {
        List<Person> found = searchByName(name);
        if (found.isEmpty()) {
            System.out.println("No match for: " + name);
        }
        found.forEach(System.out::println);
    }
}
