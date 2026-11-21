package com.library.dao;

import com.library.model.Book;
import com.library.util.Html;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private Book map(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setCategory(rs.getString("category"));
        b.setIsbn(rs.getString("isbn"));
        b.setQuantity(rs.getInt("quantity"));
        b.setAvailable(rs.getInt("available"));
        return b;
    }

    public List<Book> getAll(String search) throws Exception {
        List<Book> list = new ArrayList<Book>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR category LIKE ? OR isbn LIKE ? ORDER BY id DESC";
        String q = "%" + (search == null ? "" : search.trim()) + "%";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
            ps.setString(4, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Book> getAvailable() throws Exception {
        List<Book> list = new ArrayList<Book>();
        String sql = "SELECT * FROM books WHERE available > 0 ORDER BY title";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Book findById(int id) throws Exception {
        String sql = "SELECT * FROM books WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean add(Book b) throws Exception {
        String sql = "INSERT INTO books(title,author,category,isbn,quantity,available) VALUES(?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, b.getTitle());
            ps.setString(2, b.getAuthor());
            ps.setString(3, b.getCategory());
            ps.setString(4, Html.blankToNull(b.getIsbn()));
            ps.setInt(5, b.getQuantity());
            ps.setInt(6, b.getQuantity());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Book b) throws Exception {
        String select = "SELECT quantity, available FROM books WHERE id=?";
        int oldQty;
        int oldAvailable;
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(select)) {
            ps.setInt(1, b.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                oldQty = rs.getInt("quantity");
                oldAvailable = rs.getInt("available");
            }
        }

        int issued = oldQty - oldAvailable;
        if (b.getQuantity() < issued) return false;
        int newAvailable = b.getQuantity() - issued;

        String sql = "UPDATE books SET title=?,author=?,category=?,isbn=?,quantity=?,available=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, b.getTitle());
            ps.setString(2, b.getAuthor());
            ps.setString(3, b.getCategory());
            ps.setString(4, Html.blankToNull(b.getIsbn()));
            ps.setInt(5, b.getQuantity());
            ps.setInt(6, newAvailable);
            ps.setInt(7, b.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a book. Not allowed while a copy is still issued.
     * Old (returned) history rows of that book are removed together with it so the foreign key does not block deletion.
     */
    public boolean delete(int id) throws Exception {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) FROM issues WHERE book_id=? AND status='ISSUED'")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) { con.rollback(); return false; }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM issues WHERE book_id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                int rows;
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM books WHERE id=?")) {
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
