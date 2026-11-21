package com.library.controller;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/searchbook")
public class SearchBookServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String q = req.getParameter("q");
        resp.sendRedirect(req.getContextPath() + "/admin/books.jsp?q=" +
                java.net.URLEncoder.encode(q == null ? "" : q, "UTF-8"));
    }
}
