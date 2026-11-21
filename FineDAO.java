package com.library.dao;

import com.library.model.Fine;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {
    public List<Fine> getAllActiveFines() throws Exception {
        return query(0);
    }

    public List<Fine> getStudentFines(int studentId) throws Exception {
        return query(studentId);
    }

    public double getStudentFine(int studentId) throws Exception {
        String sql = "SELECT COALESCE(SUM(GREATEST(DATEDIFF(CURDATE(), due_date),0) * 5),0) " +
                     "FROM issues WHERE student_id=? AND status='ISSUED'";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    private List<Fine> query(int studentId) throws Exception {
        List<Fine> list = new ArrayList<Fine>();
        String sql = "SELECT i.id AS issue_id, i.student_id, s.name AS student_name, " +
                     "b.title AS book_title, i.due_date, " +
                     "GREATEST(DATEDIFF(CURDATE(),i.due_date),0) AS days_late " +
                     "FROM issues i JOIN students s ON i.student_id=s.id " +
                     "JOIN books b ON i.book_id=b.id WHERE i.status='ISSUED' ";
        if (studentId > 0) sql += "AND i.student_id=? ";
        sql += "ORDER BY days_late DESC, i.id DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (studentId > 0) ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Fine f = new Fine();
                    f.setIssueId(rs.getInt("issue_id"));
                    f.setStudentId(rs.getInt("student_id"));
                    f.setStudentName(rs.getString("student_name"));
                    f.setBookTitle(rs.getString("book_title"));
                    f.setDueDate(rs.getString("due_date"));
                    f.setDays(rs.getInt("days_late"));
                    f.setAmount(f.getDays() * 5.0);
                    list.add(f);
                }
            }
        }
        return list;
    }
}
