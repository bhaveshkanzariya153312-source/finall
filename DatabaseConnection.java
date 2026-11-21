package com.library.dao;

import com.library.util.PasswordUtil;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central JDBC connection helper.
 *
 * - Reads settings from environment variables, system properties or db.properties.
 * - If the configured password is wrong it also tries common local passwords
 *   (root / empty / admin / password / 1234 ...) so the project works on most laptops.
 * - Creates the database, tables and demo data automatically if they are missing,
 *   so importing database.sql manually is optional.
 */
public final class DatabaseConnection {
    private static final Logger LOG = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String PARAMS =
            "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
          + "&useUnicode=true&characterEncoding=UTF-8&connectTimeout=5000";
    private static final String[] COMMON_PASSWORDS =
            {"root", "", "admin", "password", "1234", "12345", "123456", "mysql", "admin123"};

    private static final Properties CONFIG = loadConfig();
    private static volatile String workingPassword;
    private static volatile boolean schemaReady;

    private DatabaseConnection() { }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "MySQL Connector/J not found. Run Maven 'Clean and Build' so the driver jar is added to WEB-INF/lib.");
        }
    }

    private static Properties loadConfig() {
        Properties p = new Properties();
        p.setProperty("db.host", "localhost");
        p.setProperty("db.port", "3306");
        p.setProperty("db.name", "smart_library");
        p.setProperty("db.user", "root");
        p.setProperty("db.password", "root");
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) p.load(in);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not read db.properties, using defaults", e);
        }
        return p;
    }

    private static String setting(String envName, String propName) {
        String v = System.getenv(envName);
        if (v == null) v = System.getProperty(envName);
        return v != null ? v : CONFIG.getProperty(propName);
    }

    private static String dbName() {
        String name = setting("SMART_LIBRARY_DB_NAME", "db.name");
        return name == null || name.trim().isEmpty() ? "smart_library" : name.trim();
    }

    private static String user() {
        String u = setting("SMART_LIBRARY_DB_USER", "db.user");
        return u == null || u.trim().isEmpty() ? "root" : u.trim();
    }

    private static String hostPort() {
        return CONFIG.getProperty("db.host", "localhost").trim() + ":" + CONFIG.getProperty("db.port", "3306").trim();
    }

    private static String urlWithDb() {
        String custom = System.getenv("SMART_LIBRARY_DB_URL");
        if (custom != null && !custom.trim().isEmpty()) return custom.trim();
        return "jdbc:mysql://" + hostPort() + "/" + dbName() + "?" + PARAMS;
    }

    private static String urlServerOnly() {
        return "jdbc:mysql://" + hostPort() + "/?" + PARAMS;
    }

    private static boolean isAccessDenied(SQLException e) {
        return e.getErrorCode() == 1045 || "28000".equals(e.getSQLState());
    }

    private static boolean isUnknownDatabase(SQLException e) {
        return e.getErrorCode() == 1049 || "42000".equals(e.getSQLState()) && String.valueOf(e.getMessage()).contains("Unknown database");
    }

    /** Opens a raw connection (to the server, or to the database) trying the known passwords. */
    private static Connection open(String url) throws SQLException {
        String user = user();
        String configured = setting("SMART_LIBRARY_DB_PASSWORD", "db.password");
        if (configured == null) configured = "";

        java.util.LinkedHashSet<String> candidates = new java.util.LinkedHashSet<String>();
        if (workingPassword != null) candidates.add(workingPassword);
        candidates.add(configured);
        for (String p : COMMON_PASSWORDS) candidates.add(p);

        SQLException last = null;
        for (String pw : candidates) {
            try {
                Connection con = DriverManager.getConnection(url, user, pw);
                workingPassword = pw;
                return con;
            } catch (SQLException e) {
                last = e;
                if (!isAccessDenied(e)) throw e;   // MySQL stopped / unknown db etc. -> no point trying other passwords
            }
        }
        throw last;
    }

    public static Connection getConnection() throws SQLException {
        if (!schemaReady) initializeSchema();
        try {
            return open(urlWithDb());
        } catch (SQLException e) {
            if (isUnknownDatabase(e)) {
                schemaReady = false;
                initializeSchema();
                return open(urlWithDb());
            }
            throw e;
        }
    }

    /** Creates database, tables and demo data when they do not exist yet. Safe to call many times. */
    public static synchronized void initializeSchema() throws SQLException {
        if (schemaReady) return;
        String custom = System.getenv("SMART_LIBRARY_DB_URL");
        if (custom == null || custom.trim().isEmpty()) {
            try (Connection con = open(urlServerOnly()); Statement st = con.createStatement()) {
                st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName() + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }
        }
        try (Connection con = open(urlWithDb()); Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "username VARCHAR(50) NOT NULL UNIQUE,"
                + "password VARCHAR(64) NOT NULL,"
                + "role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) DEFAULT CHARSET=utf8mb4");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS books ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "title VARCHAR(150) NOT NULL,"
                + "author VARCHAR(100) NOT NULL,"
                + "category VARCHAR(80),"
                + "isbn VARCHAR(50) UNIQUE,"
                + "quantity INT NOT NULL DEFAULT 1,"
                + "available INT NOT NULL DEFAULT 1,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "INDEX idx_books_title (title), INDEX idx_books_author (author),"
                + "INDEX idx_books_category (category)) DEFAULT CHARSET=utf8mb4");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS students ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "name VARCHAR(120) NOT NULL,"
                + "email VARCHAR(120),"
                + "phone VARCHAR(30),"
                + "course VARCHAR(80),"
                + "password VARCHAR(64) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "INDEX idx_students_name (name)) DEFAULT CHARSET=utf8mb4");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS issues ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "book_id INT NOT NULL,"
                + "student_id INT NOT NULL,"
                + "issue_date DATE NOT NULL,"
                + "due_date DATE NOT NULL,"
                + "return_date DATE NULL,"
                + "status VARCHAR(20) NOT NULL DEFAULT 'ISSUED',"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "CONSTRAINT fk_issue_book FOREIGN KEY (book_id) REFERENCES books(id),"
                + "CONSTRAINT fk_issue_student FOREIGN KEY (student_id) REFERENCES students(id),"
                + "INDEX idx_issue_status (status), INDEX idx_issue_student (student_id),"
                + "INDEX idx_issue_due (due_date)) DEFAULT CHARSET=utf8mb4");

            seed(con);
        }
        schemaReady = true;
        LOG.info("Smart Library database is ready.");
    }

    private static void seed(Connection con) throws SQLException {
        // Admin: admin / admin123   (the old demo 'user' account is removed)
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users(username,password,role) VALUES('admin',?,'ADMIN') "
              + "ON DUPLICATE KEY UPDATE password=VALUES(password), role='ADMIN'")) {
            ps.setString(1, PasswordUtil.hash("admin123"));
            ps.executeUpdate();
        }
        try (Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM users WHERE username='user'");
        }

        // Demo student: ID 1 / student123
        boolean hasStudents;
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students")) {
            rs.next();
            hasStudents = rs.getInt(1) > 0;
        }
        if (!hasStudents) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO students(id,name,email,phone,course,password) VALUES(1,'Demo Student','student@example.com','9876543210','BCA',?)")) {
                ps.setString(1, PasswordUtil.hash("student123"));
                ps.executeUpdate();
            }
        }
        // Student 1 always logs in with student123.
        try (PreparedStatement ps = con.prepareStatement("UPDATE students SET password=? WHERE id=1")) {
            ps.setString(1, PasswordUtil.hash("student123"));
            ps.executeUpdate();
        }

        boolean hasBooks;
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM books")) {
            rs.next();
            hasBooks = rs.getInt(1) > 0;
        }
        if (!hasBooks) {
            String[][] books = {
                {"Java Programming", "Herbert Schildt", "Programming", "9780001", "5"},
                {"JSP & Servlets", "Marty Hall", "Web Technology", "9780002", "4"},
                {"Database Management", "Raghu Ramakrishnan", "Database", "9780003", "6"},
                {"Computer Networks", "Andrew Tanenbaum", "Networking", "9780004", "3"},
                {"Software Engineering", "Ian Sommerville", "Software", "9780005", "5"}
            };
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT IGNORE INTO books(title,author,category,isbn,quantity,available) VALUES(?,?,?,?,?,?)")) {
                for (String[] b : books) {
                    ps.setString(1, b[0]);
                    ps.setString(2, b[1]);
                    ps.setString(3, b[2]);
                    ps.setString(4, b[3]);
                    ps.setInt(5, Integer.parseInt(b[4]));
                    ps.setInt(6, Integer.parseInt(b[4]));
                    ps.executeUpdate();
                }
            }
        }
    }

    /** Human friendly explanation of a database failure (shown on dashboard / login / error page). */
    public static String describeError(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        String msg = String.valueOf(root.getMessage());
        if (root instanceof SQLException) {
            SQLException se = (SQLException) root;
            if (isAccessDenied(se)) {
                return "MySQL username/password galat hai. src/main/resources/db.properties mein db.user aur db.password apne MySQL ke hisaab se badlo, phir Clean and Build karo.";
            }
            if (isUnknownDatabase(se)) {
                return "Database '" + dbName() + "' nahi mila. database.sql ek baar run karo (ya app dobara open karo, wo khud bana dega).";
            }
        }
        if (msg.contains("Communications link failure") || msg.contains("Connection refused") || msg.contains("CommunicationsException")) {
            return "MySQL server se connect nahi ho paa raha. MySQL service start karo aur port " + CONFIG.getProperty("db.port", "3306") + " check karo.";
        }
        if (msg.contains("MySQL Connector/J not found") || root instanceof ClassNotFoundException || t instanceof ExceptionInInitializerError || t instanceof NoClassDefFoundError) {
            return "MySQL driver (mysql-connector-j) nahi mila. Project par Clean and Build karo taaki jar WEB-INF/lib mein aa jaye.";
        }
        return msg;
    }
}
