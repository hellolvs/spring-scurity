<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>提示</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/config.css">
</head>
<body>
<%
    String param = request.getParameter("errMsg");
    String errMsg = new String(param.getBytes("iso-8859-1"), "utf-8");
%>
<div class="imporMsg"><%=errMsg%>
</div>
</body>
</html>
