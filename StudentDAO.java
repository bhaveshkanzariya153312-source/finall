package com.library.dao;

import com.library.model.Student;
import com.library.util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private Student map(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setCourse(rs.getString("course"));
        return s;
    }

    public List<Student> getAll() throws Exception {
        List<Student> list = new ArrayList<Student>();
        String sql = "SELECT id,name,email,phone,course FROM students ORDER BY id DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Student findById(int id) throws Exception {
        String sql = "SELECT id,name,email,phone,course FROM students WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Student authenticate(int id, String password) throws Exception {
        if (password == null) return null;
        String sql = "SELECT id,name,email,phone,course FROM students WHERE id=? AND password=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, PasswordUtil.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Adds a student and returns the generated Student ID (0 on failure). Default password: student123 */
    public int add(Student s) throws Exception {
        String sql = "INSERT INTO students(name,email,phone,course,password) VALUES(?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getCourse());
            ps.setString(5, PasswordUtil.hash("student123"));
            if (ps.executeUpdate() == 0) return 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public boolean update(Student s) throws Exception {
        String sql = "UPDATE students SET name=?,email=?,phone=?,course=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getCourse());
            ps.setInt(5, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Deletes a student unless a book is still issued to them. Returned history is removed with the student. */
    public boolean delete(int id) throws Exception {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) FROM issues WHERE student_id=? AND status='ISSUED'")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) { con.rollback(); return false; }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM issues WHERE student_id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                int rows;
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM students WHERE id=?")) {
                    ps.setInt(1, id);
                    rows = ps.executeUpdate();
                }
                con.commit();
                return rows > 0;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
