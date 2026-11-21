package com.library.filter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

/** Only a logged-in admin can open admin pages or call admin servlets. */
@WebFilter(filterName = "AdminAuthFilter", urlPatterns = {
        "/admin/*", "/dashboard", "/addBook", "/updateBook", "/deleteBook", "/booklist", "/searchbook",
        "/addStudent", "/updatestudent", "/deletestudent", "/studentlist",
        "/issueBook", "/returnBook", "/issueReceipt", "/returnReceipt",
        "/fine", "/report", "/exportReport"})
public class AdminAuthFilter implements Filter {
    @Override public void init(FilterConfig filterConfig) { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        resp.setHeader("Cache-Control", "no-store");
        chain.doFilter(request, response);
    }

    @Override public void destroy() { }
}
