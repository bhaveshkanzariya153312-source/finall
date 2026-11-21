package com.library.controller;

import com.library.dao.DatabaseConnection;
import com.library.dao.UserDAO;
import com.library.model.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Admin login. Default account: admin / admin123 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        try {
            User user = userDAO.authenticate(username, password);
            if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
                HttpSession old = req.getSession(false);
                if (old != null) old.invalidate();
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(30 * 60);
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard.jsp");
            } else {
                resp.sendRedirect(req.getContextPath() + "/login.jsp?error=1");
            }
        } catch (Throwable e) {
            req.setAttribute("dbError", DatabaseConnection.describeError(e));
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
