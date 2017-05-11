<%--
  Created by IntelliJ IDEA.
  User: caiwen
  Date: 2017/5/9
  Time: 19:09
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>users</title>
</head>
<body>
    <c:forEach items="${userList}" var="user">
        <li id="user_<c:out value="user.username"/>">
            <div class="userPassword">
                <c:out value="${user.password}"/>
            </div>
            <div>
                <span class="userAge"><c:out value="${user.age}"/></span>
            </div>
        </li>
    </c:forEach>
</body>
</html>
