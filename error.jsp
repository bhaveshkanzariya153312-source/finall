<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="com.library.util.Html,com.library.dao.DatabaseConnection" %>
<%
Throwable err = exception;
if (err == null) err = (Throwable) request.getAttribute("javax.servlet.error.exception");
String help = err == null ? null : DatabaseConnection.describeError(err);
Object code = request.getAttribute("javax.servlet.error.status_code");
if (code != null && ((Integer) code) == 404 && err == null) help = "Ye page nahi mila.";
String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Something went wrong</title>
    <link rel="stylesheet" href="<%=ctx%>/assets/css/style.css">
</head>
<body class="landing">
<div class="hero">
    <div class="login-card">
        <div class="logo-circle">&#9888;</div>
        <h2>Something went wrong</h2>
        <% if (help != null) { %><div class="alert error"><%=Html.esc(help)%></div><% } %>
        <p class="hint">Agar database ka error hai to MySQL start karo aur <strong>src/main/resources/db.properties</strong> mein password check karo.</p>
        <p><a class="btn" href="<%=ctx%>/index.jsp">Go to Home</a></p>
    </div>
</div>
</body>
</html>
