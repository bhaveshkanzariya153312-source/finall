package com.library.controller;

import com.library.dao.DatabaseConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/exportReport")
public class ExportReportServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/csv;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=library_report.csv");
        try (PrintWriter out = resp.getWriter();
             Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            out.println("Smart Library Management System");
            out.println("Metric,Value");
            String[][] queries = {
                {"Total Book Titles","SELECT COUNT(*) FROM books"},
                {"Total Copies","SELECT COALESCE(SUM(quantity),0) FROM books"},
                {"Available Copies","SELECT COALESCE(SUM(available),0) FROM books"},
                {"Registered Students","SELECT COUNT(*) FROM students"},
                {"Currently Issued","SELECT COUNT(*) FROM issues WHERE status='ISSUED'"},
                {"Returned Transactions","SELECT COUNT(*) FROM issues WHERE status='RETURNED'"},
                {"Overdue Books","SELECT COUNT(*) FROM issues WHERE status='ISSUED' AND due_date < CURDATE()"}
            };
            for (String[] q : queries) {
                try (ResultSet rs = st.executeQuery(q[1])) {
                    rs.next();
                    out.println(q[0] + "," + rs.getLong(1));
                }
            }
        } catch (Exception e) {
            throw new ServletException("Unable to export report", e);
        }
    }
}
