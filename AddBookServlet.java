package com.library.controller;

import com.library.dao.BookDAO;
import com.library.model.Book;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/addBook")
public class AddBookServlet extends HttpServlet {
    private final BookDAO dao = new BookDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String ctx = req.getContextPath();
        try {
            Book b = new Book();
            b.setTitle(req.getParameter("title").trim());
            b.setAuthor(req.getParameter("author").trim());
            b.setCategory(req.getParameter("category"));
            b.setIsbn(req.getParameter("isbn"));
            b.setQuantity(Integer.parseInt(req.getParameter("quantity").trim()));
            if (b.getTitle().isEmpty() || b.getAuthor().isEmpty() || b.getQuantity() < 1) {
                resp.sendRedirect(ctx + "/admin/add-book.jsp?error=invalid");
                return;
            }
            dao.add(b);
            resp.sendRedirect(ctx + "/admin/books.jsp?msg=added");
        } catch (SQLIntegrityConstraintViolationException e) {
            resp.sendRedirect(ctx + "/admin/add-book.jsp?error=duplicate");
        } catch (NumberFormatException | NullPointerException e) {
            resp.sendRedirect(ctx + "/admin/add-book.jsp?error=invalid");
        } catch (Exception e) {
            throw new ServletException("Unable to add book", e);
        }
    }
}
