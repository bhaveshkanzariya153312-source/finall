package com.library.controller;

import com.library.dao.StudentDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/studentlist")
public class StudentListServlet extends HttpServlet {
    private final StudentDAO dao = new StudentDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("students", dao.getAll());
            req.getRequestDispatcher("/admin/students.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException("Unable to load students", e);
        }
    }
}
