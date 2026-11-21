package com.library.controller;

import com.library.dao.BookDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/deleteBook")
public class DeleteBookServlet extends HttpServlet {
    private final BookDAO dao = new BookDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            boolean deleted = dao.delete(id);
            resp.sendRedirect(req.getContextPath() + "/admin/books.jsp?msg=" + (deleted ? "deleted" : "cannotDelete"));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/books.jsp");
        } catch (Exception e) {
            throw new ServletException("Unable to delete book", e);
        }
    }
}
