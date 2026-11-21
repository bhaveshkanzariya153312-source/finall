package com.library.controller;

import com.library.dao.BookDAO;
import com.library.model.Book;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/updateBook")
public class UpdateBookServlet extends HttpServlet {
    private final BookDAO dao = new BookDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String ctx = req.getContextPath();
        String idParam = req.getParameter("id");
        try {
            Book b = new Book();
            b.setId(Integer.parseInt(idParam.trim()));
            b.setTitle(req.getParameter("title").trim());
            b.setAuthor(req.getParameter("author").trim());
            b.setCategory(req.getParameter("category"));
            b.setIsbn(req.getParameter("isbn"));
            b.setQuantity(Integer.parseInt(req.getParameter("quantity").trim()));
            if (b.getTitle().isEmpty() || b.getAuthor().isEmpty() || b.getQuantity() < 1) {
                resp.sendRedirect(ctx + "/admin/edit-book.jsp?id=" + b.getId() + "&error=invalid");
                return;
            }
            if (!dao.update(b)) {
                resp.sendRedirect(ctx + "/admin/edit-book.jsp?id=" + b.getId() + "&error=1");
                return;
            }
            resp.sendRedirect(ctx + "/admin/books.jsp?msg=updated");
        } catch (SQLIntegrityConstraintViolationException e) {
            resp.sendRedirect(ctx + "/admin/edit-book.jsp?id=" + idParam + "&error=duplicate");
        } catch (NumberFormatException | NullPointerException e) {
            resp.sendRedirect(ctx + "/admin/books.jsp");
        } catch (Exception e) {
            throw new ServletException("Unable to update book", e);
        }
    }
}
