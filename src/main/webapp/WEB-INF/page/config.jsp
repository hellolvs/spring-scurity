<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>配置管理</title>
    <link rel="stylesheet" type="text/css" href="http://common.qunarzz.com/lib/prd/bootstrap/3.3.7/css/bootstrap.css">
    <link rel="stylesheet" type="text/css" href="http://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.1/bootstrap-table.min.css">
    <link rel="stylesheet" type="text/css" href="http://cdnjs.cloudflare.com/ajax/libs/bootstrap3-dialog/1.34.7/css/bootstrap-dialog.min.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/config.css">
</head>
<body>
<%--将权限信息隐藏在页面中，由js获得--%>
<sec:authentication property="principal.authorities" var="authorities"/>
<form style="display: none">
    <input type="hidden" id="authorities" value="${authorities}"/>
</form>

<nav class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container-fluid">
        <div class="navbar-header navbar-left">
            <span class="navbar-brand glyphicon glyphicon-cog"></span>
            <span class="navbar-brand">HotConfig Management</span>
        </div>
        <div>
            <ul class="nav navbar-nav">
                <li><a href="/configView"> 配置管理</a></li>
                <li><a href="/permissionView">权限管理</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container-fluid">
    <form id="form" class="form-inline" role="form">
        <div class="form-group">
            <label class="input-lg">配置文件名:
                <select class="form-control" id="selectBox">
                    <option value="-">请选择</option>
                </select>
            </label>
        </div>
        <div class="form-group">
            <button class="btn btn-primary btnQuery" type="button" id="btnQuery"> 查询</button>
            <button class="btn btn-info btnDiff" type="button" id="btnDiff" style="display:none"> diff</button>
            <button class="btn btn-danger btnRelease" type="button" id="btnRelease" style="display:none"> 发布</button>
        </div>
    </form>
    <div id="toolbar" class="btn-group">
        <button id="btnAdd" type="button" class="btn btn-warning" style="display:none">
            <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> 新增
        </button>
        <button id="btnEdit" type="button" class="btn btn-warning" style="display:none">
            <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span> 修改
        </button>
        <button id="btnDelete" type="button" class="btn btn-warning" style="display:none">
            <span class="glyphicon glyphicon-remove" aria-hidden="true"></span> 删除
        </button>
    </div>
    <table id="bootstrap-table"></table>
</div>

<script src="http://common.qunarzz.com/lib/prd/jquery/3.1.1/jquery.js"></script>
<script src="http://common.qunarzz.com/lib/prd/bootstrap/3.3.7/js/bootstrap.js"></script>
<script src="http://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.1/bootstrap-table.min.js"></script>
<script src="http://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.11.1/locale/bootstrap-table-zh-CN.min.js"></script>
<script src="http://cdnjs.cloudflare.com/ajax/libs/bootstrap3-dialog/1.34.7/js/bootstrap-dialog.min.js"></script>
<script src="${pageContext.request.contextPath}/resources/js/config.js"></script>
<script src="${pageContext.request.contextPath}/resources/js/ajaxSetup.js"></script>
</body>
</html>