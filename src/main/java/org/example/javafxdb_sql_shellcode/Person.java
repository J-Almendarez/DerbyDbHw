package org.example.javafxdb_sql_shellcode;

public class Person {

    private Integer id;        // primary key
    private String firstName;
    private String lastName;
    private String dept;
    private String major;
    private String email;      // must be unique in the database
    private String imageURL;   // file  of the profile picture (can be null)

    /** Empty constructor (used when we fill the fields with setters). */
    public Person() {
    }

    /** Constructor used before inserting */
    public Person(String firstName, String lastName, String dept, String major,
                  String email, String imageURL) {
        this(null, firstName, lastName, dept, major, email, imageURL);
    }

    /** Full constructor used when reading a row back from the database. */
    public Person(Integer id, String firstName, String lastName, String dept,
                  String major, String email, String imageURL) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dept = dept;
        this.major = major;
        this.email = email;
        this.imageURL = imageURL;
    }

    // ---------- getters and setters ----------

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    /** Used when printing a person in the console menu. */
    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + firstName + " " + lastName
                + ", Dept: " + dept + ", Major: " + major + ", Email: " + email;
    }
}
