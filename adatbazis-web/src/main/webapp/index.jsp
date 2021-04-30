<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>
<%@ page isELIgnored="true" %>
<html>
<body>

<form action="db-upload" enctype = "multipart/form-data" method="post">
    <label for="db">Select database:</label>
    <input type="file" id="db" name="db" accept=".db">
    <input type="submit">
</form>

</body>
</html>
