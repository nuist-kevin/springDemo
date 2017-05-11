<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>User</title>
</head>
<body>
<c:out value="${user.id}"/>
<c:out value="${user.username}"/>
<c:out value="${user.password}"/>
<c:out value="${user.age}"/>
</body>
</html>
