package com.library.controller;

import com.library.dao.IssueDAO;
import com.library.model.Issue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet({"/issueReceipt", "/returnReceipt"})
public class ReceiptServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            Issue issue = new IssueDAO().findById(id);
            if (issue == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Transaction not found.");
                return;
            }

            boolean returned = "/returnReceipt".equals(req.getServletPath());
            if (returned && "ISSUED".equals(issue.getStatus())) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "This book has not been returned yet.");
                return;
            }
            // Fine = Rs. 5 per day after the due date (until return date, or until today if still issued).
            java.time.LocalDate due = java.time.LocalDate.parse(issue.getDueDate());
            java.time.LocalDate end = issue.getReturnDate().isEmpty()
                    ? java.time.LocalDate.now() : java.time.LocalDate.parse(issue.getReturnDate());
            int daysLate = (int) Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(due, end));
            double fine = daysLate * 5.0;

            String title = returned ? "BOOK RETURN RECEIPT" : "BOOK ISSUE RECEIPT";
            String[] lines = {
                "SMART LIBRARY MANAGEMENT SYSTEM",
                title,
                "----------------------------------------",
                "Transaction ID : " + issue.getId(),
                "Student        : " + issue.getStudentName(),
                "Student ID     : " + issue.getStudentId(),
                "Book           : " + issue.getBookTitle(),
                "Issue Date     : " + issue.getIssueDate(),
                "Due Date       : " + issue.getDueDate(),
                "Return Date    : " + (issue.getReturnDate().isEmpty() ? "-" : issue.getReturnDate()),
                "Status         : " + issue.getStatus(),
                "Overdue Days   : " + daysLate,
                "Fine           : Rs. " + String.format("%.0f", fine),
                "----------------------------------------",
                "Thank you for using Smart Library."
            };
            byte[] pdf = SimplePdf.create(lines);
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=" +
                    (returned ? "return-receipt-" : "issue-receipt-") + id + ".pdf");
            resp.setContentLength(pdf.length);
            resp.getOutputStream().write(pdf);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid transaction id.");
        } catch (Exception e) {
            throw new ServletException("Unable to generate PDF receipt", e);
        }
    }

    private static final class SimplePdf {
        static byte[] create(String[] lines) throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            java.util.List<Integer> offsets = new java.util.ArrayList<Integer>();
            write(out, "%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n");
            offsets.add(out.size()); write(out, "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n");
            offsets.add(out.size()); write(out, "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n");
            offsets.add(out.size()); write(out, "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n");
            offsets.add(out.size()); write(out, "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n");

            StringBuilder content = new StringBuilder();
            content.append("BT\n/F1 14 Tf\n50 790 Td\n");
            for (int i = 0; i < lines.length; i++) {
                content.append("(").append(escape(lines[i])).append(") Tj\n");
                content.append("0 -22 Td\n");
                if (i == 1) content.append("/F1 11 Tf\n");
            }
            content.append("ET\n");
            byte[] stream = content.toString().getBytes(StandardCharsets.ISO_8859_1);
            offsets.add(out.size());
            write(out, "5 0 obj << /Length " + stream.length + " >> stream\n");
            out.write(stream); write(out, "\nendstream endobj\n");

            int xref = out.size();
            write(out, "xref\n0 6\n0000000000 65535 f \n");
            for (Integer off : offsets) write(out, String.format("%010d 00000 n \n", off));
            write(out, "trailer << /Size 6 /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF");
            return out.toByteArray();
        }

        private static String escape(String s) {
            return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
        }
        private static void write(ByteArrayOutputStream out, String s) throws Exception {
            out.write(s.getBytes(StandardCharsets.ISO_8859_1));
        }
    }
}
