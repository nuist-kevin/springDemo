<%--
  Created by IntelliJ IDEA.
  User: caiwen
  Date: 2017/5/9
  Time: 18:26
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Home</title>
</head>
<body>
    <h1>Welcome Home</h1>
    <a href="<c:url value="/users" />">Home</a>
    <a href="<c:url value="/user/register"/>">Register</a>
</body>
</html>
