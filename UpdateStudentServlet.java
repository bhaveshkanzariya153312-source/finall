package com.library.controller;

import com.library.dao.StudentDAO;
import com.library.model.Student;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/updatestudent")
public class UpdateStudentServlet extends HttpServlet {
    private final StudentDAO dao = new StudentDAO();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Student s = new Student();
            s.setId(Integer.parseInt(req.getParameter("id")));
            String name = req.getParameter("name") == null ? "" : req.getParameter("name").trim();
            if (name.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/admin/edit-student.jsp?id=" + s.getId());
                return;
            }
            s.setName(name);
            s.setEmail(req.getParameter("email"));
            s.setPhone(req.getParameter("phone"));
            s.setCourse(req.getParameter("course"));
            dao.update(s);
            resp.sendRedirect(req.getContextPath() + "/admin/students.jsp?msg=updated");
        } catch (Exception e) {
            throw new ServletException("Unable to update student", e);
        }
    }
}
