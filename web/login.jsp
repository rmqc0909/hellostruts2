<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Login</title>
</head>
<body>
<form action="loginaction" method="post">
    User:<br/><input type="text" name="user"/><br/>
    Password:<br/><input type="password" name="password"/><br/>
    <input type="submit" value="Login"/>
</form>
</body>
</html>
