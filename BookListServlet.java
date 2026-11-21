package com.library.controller;

import com.library.dao.BookDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/booklist")
public class BookListServlet extends HttpServlet {
    private final BookDAO dao = new BookDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("books", dao.getAll(req.getParameter("q")));
            req.getRequestDispatcher("/admin/books.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException("Unable to load books", e);
        }
    }
}
