<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.library.util.Html" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Admin Login</title>
    <link rel="stylesheet" href="assets/css/style.css">
</head>
<body class="landing">
<div class="hero">
    <div class="login-card">
        <div class="logo-circle">&#128272;</div>
        <h2>Admin Login</h2>
        <p>Sign in to manage the library.</p>
        <% if (request.getAttribute("dbError") != null) { %>
            <div class="alert error"><strong>Database error:</strong> <%=Html.esc(request.getAttribute("dbError"))%></div>
        <% } else if ("1".equals(request.getParameter("error"))) { %>
            <div class="alert error">Invalid username or password.</div>
        <% } %>
        <form method="post" action="login">
            <label>Username</label>
            <input type="text" name="username" required autocomplete="username" autofocus>
            <label>Password</label>
            <input type="password" name="password" required autocomplete="current-password">
            <button class="btn full" type="submit">Login</button>
        </form>
        <p class="hint">Admin: <strong>admin</strong> / <strong>admin123</strong></p>
        <a href="index.jsp">&#8592; Back to Home</a>
    </div>
</div>
</body>
</html>
