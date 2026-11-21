package com.library.controller;

import com.library.dao.IssueDAO;
import java.io.IOException;
import java.sql.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/issueBook")
public class IssueBookServlet extends HttpServlet {
    private final IssueDAO dao = new IssueDAO();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int bookId = Integer.parseInt(req.getParameter("book_id"));
            int studentId = Integer.parseInt(req.getParameter("student_id"));
            Date dueDate = Date.valueOf(req.getParameter("due_date"));
            int issueId = dao.issueBook(bookId, studentId, dueDate);
            if (issueId > 0) {
                resp.sendRedirect(req.getContextPath() + "/admin/issue-book.jsp?msg=issued&receipt=" + issueId);
            } else {
                resp.sendRedirect(req.getContextPath() + "/admin/issue-book.jsp?msg=failed");
            }
        } catch (IllegalArgumentException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/issue-book.jsp?msg=failed");
        } catch (Exception e) {
            throw new ServletException("Unable to issue book", e);
        }
    }
}
