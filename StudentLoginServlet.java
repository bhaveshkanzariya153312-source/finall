package com.library.controller;

import com.library.dao.DatabaseConnection;
import com.library.dao.StudentDAO;
import com.library.model.Student;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Student login. Default account: Student ID 1 / student123 */
@WebServlet("/studentLogin")
public class StudentLoginServlet extends HttpServlet {
    private final StudentDAO dao = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/student-login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id;
        try {
            id = Integer.parseInt(req.getParameter("id").trim());
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/student-login.jsp?error=1");
            return;
        }
        try {
            Student student = dao.authenticate(id, req.getParameter("password"));
            if (student != null) {
                HttpSession old = req.getSession(false);
                if (old != null) old.invalidate();
                HttpSession session = req.getSession(true);
                session.setAttribute("student", student);
                session.setAttribute("studentId", student.getId());
                session.setMaxInactiveInterval(30 * 60);
                resp.sendRedirect(req.getContextPath() + "/student/dashboard.jsp");
            } else {
                resp.sendRedirect(req.getContextPath() + "/student-login.jsp?error=1");
            }
        } catch (Throwable e) {
            req.setAttribute("dbError", DatabaseConnection.describeError(e));
            req.getRequestDispatcher("/student-login.jsp").forward(req, resp);
        }
    }
}
