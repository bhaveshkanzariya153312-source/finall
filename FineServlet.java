package com.library.controller;

import com.library.dao.FineDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/fine")
public class FineServlet extends HttpServlet {
    private final FineDAO dao = new FineDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("fines", dao.getAllActiveFines());
            req.getRequestDispatcher("/admin/fines.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException("Unable to load fines", e);
        }
    }
}
