package com.library.controller;

import com.library.dao.StudentDAO;
import com.library.model.Student;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/addStudent")
public class AddStudentServlet extends HttpServlet {
    private final StudentDAO dao = new StudentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String ctx = req.getContextPath();
        try {
            String name = req.getParameter("name") == null ? "" : req.getParameter("name").trim();
            if (name.isEmpty()) {
                resp.sendRedirect(ctx + "/admin/add-student.jsp?error=invalid");
                return;
            }
            Student s = new Student();
            s.setName(name);
            s.setEmail(req.getParameter("email"));
            s.setPhone(req.getParameter("phone"));
            s.setCourse(req.getParameter("course"));
            int newId = dao.add(s);
            resp.sendRedirect(ctx + "/admin/students.jsp?msg=added&newId=" + newId);
        } catch (Exception e) {
            throw new ServletException("Unable to add student", e);
        }
    }
}
