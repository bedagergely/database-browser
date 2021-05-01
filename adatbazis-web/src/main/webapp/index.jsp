<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>
<%@ page isELIgnored="true" %>
<html>
<head>
    <link rel="stylesheet" href="css/pages.css">
</head>
<body>
<div class="centered">
    <div id="upload_file">
    <form action="db-upload" enctype = "multipart/form-data" method="post">
        <label for="db">Select database:</label>
        <input type="file" id="db" name="db" accept=".db">
        <input type="submit">
    </form>
    </div>
</div>
</body>
</html>
