package com.library.dao;

import com.library.model.Issue;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IssueDAO {
    private Issue map(ResultSet rs) throws SQLException {
        Issue i = new Issue();
        i.setId(rs.getInt("id"));
        i.setBookId(rs.getInt("book_id"));
        i.setStudentId(rs.getInt("student_id"));
        i.setBookTitle(rs.getString("book_title"));
        i.setStudentName(rs.getString("student_name"));
        // getString on DATE columns gives yyyy-MM-dd without any time-zone shifting.
        i.setIssueDate(rs.getString("issue_date"));
        i.setDueDate(rs.getString("due_date"));
        String rd = rs.getString("return_date");
        i.setReturnDate(rd == null ? "" : rd);
        i.setStatus(rs.getString("status"));
        return i;
    }

    public int issueBook(int bookId, int studentId, Date dueDate) throws Exception {
        if (dueDate == null || dueDate.toLocalDate().isBefore(java.time.LocalDate.now())) return 0;

        String checkBook = "SELECT available FROM books WHERE id=? FOR UPDATE";
        String checkStudent = "SELECT id FROM students WHERE id=?";
        String insert = "INSERT INTO issues(book_id,student_id,issue_date,due_date,status) VALUES(?,?,CURDATE(),?,'ISSUED')";
        String update = "UPDATE books SET available=available-1 WHERE id=? AND available>0";

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(checkStudent)) {
                    ps.setInt(1, studentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { con.rollback(); return 0; }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(checkBook)) {
                    ps.setInt(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getInt("available") <= 0) {
                            con.rollback();
                            return 0;
                        }
                    }
                }

                int issueId;
                try (PreparedStatement ps = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, bookId);
                    ps.setInt(2, studentId);
                    ps.setObject(3, dueDate.toLocalDate());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) { con.rollback(); return 0; }
                        issueId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(update)) {
                    ps.setInt(1, bookId);
                    if (ps.executeUpdate() != 1) { con.rollback(); return 0; }
                }

                con.commit();
                return issueId;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public boolean returnBook(int issueId) throws Exception {
        String find = "SELECT book_id FROM issues WHERE id=? AND status='ISSUED' FOR UPDATE";
        String updateIssue = "UPDATE issues SET return_date=CURDATE(),status='RETURNED' WHERE id=?";
        String updateBook = "UPDATE books SET available=LEAST(quantity,available+1) WHERE id=?";

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int bookId;
                try (PreparedStatement ps = con.prepareStatement(find)) {
                    ps.setInt(1, issueId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { con.rollback(); return false; }
                        bookId = rs.getInt("book_id");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(updateIssue)) {
                    ps.setInt(1, issueId);
                    if (ps.executeUpdate() != 1) { con.rollback(); return false; }
                }
                try (PreparedStatement ps = con.prepareStatement(updateBook)) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public Issue findById(int id) throws Exception {
        String sql = "SELECT i.*, b.title AS book_title, s.name AS student_name " +
                     "FROM issues i JOIN books b ON i.book_id=b.id JOIN students s ON i.student_id=s.id WHERE i.id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Issue> getActive() throws Exception {
        return getIssues("WHERE i.status='ISSUED'", 0);
    }

    public List<Issue> getAllForStudent(int studentId) throws Exception {
        return getIssues("WHERE i.student_id=?", studentId);
    }

    private List<Issue> getIssues(String where, int studentId) throws Exception {
        List<Issue> list = new ArrayList<Issue>();
        String sql = "SELECT i.*, b.title AS book_title, s.name AS student_name " +
                     "FROM issues i JOIN books b ON i.book_id=b.id JOIN students s ON i.student_id=s.id " +
                     where + " ORDER BY i.id DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (where.contains("?")) ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }
}
