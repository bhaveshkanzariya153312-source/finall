package com.library.controller;

import com.library.dao.ReturnDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/returnBook")
public class ReturnBookServlet extends HttpServlet {
    private final ReturnDAO dao = new ReturnDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int issueId = Integer.parseInt(req.getParameter("id"));
            boolean ok = dao.returnBook(issueId);
            if (ok) {
                resp.sendRedirect(req.getContextPath() + "/admin/return-book.jsp?msg=returned&receipt=" + issueId);
            } else {
                resp.sendRedirect(req.getContextPath() + "/admin/return-book.jsp?msg=failed");
            }
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/return-book.jsp?msg=failed");
        } catch (Exception e) {
            throw new ServletException("Unable to return book", e);
        }
    }
}
