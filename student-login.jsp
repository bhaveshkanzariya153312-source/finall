<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.library.util.Html" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Student Portal</title>
    <link rel="stylesheet" href="assets/css/style.css">
</head>
<body class="landing">
<div class="hero">
    <div class="login-card">
        <div class="logo-circle">&#127891;</div>
        <h2>Student Portal</h2>
        <p>Secure student login</p>
        <% if (request.getAttribute("dbError") != null) { %>
            <div class="alert error"><strong>Database error:</strong> <%=Html.esc(request.getAttribute("dbError"))%></div>
        <% } else if ("1".equals(request.getParameter("error"))) { %>
            <div class="alert error">Invalid Student ID or password.</div>
        <% } %>
        <form method="post" action="studentLogin">
            <label>Student ID</label>
            <input type="number" name="id" min="1" required placeholder="e.g. 1" autofocus>
            <label>Password</label>
            <input type="password" name="password" required placeholder="Enter password">
            <button class="btn full" type="submit">Login to Portal</button>
        </form>
        <p class="hint">Student ID: <strong>1</strong> / Password: <strong>student123</strong></p>
        <a href="index.jsp">&#8592; Back to Home</a>
    </div>
</div>
</body>
</html>
