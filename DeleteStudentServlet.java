package com.library.controller;

import com.library.dao.StudentDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/deletestudent")
public class DeleteStudentServlet extends HttpServlet {
    private final StudentDAO dao = new StudentDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            boolean deleted = dao.delete(id);
            resp.sendRedirect(req.getContextPath() + "/admin/students.jsp?msg=" +
                    (deleted ? "deleted" : "cannotDelete"));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/students.jsp");
        } catch (Exception e) {
            throw new ServletException("Unable to delete student", e);
        }
    }
}
