<%--
  Created by IntelliJ IDEA.
  User: Gergely Béda
  Date: 2021. 04. 29.
  Time: 20:50
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="hu.bp3sjt.dao.DataBaseDaoImp" %>
<%@ page import="hu.bp3sjt.model.DataBase" %>
<%@ page import="java.util.List" %>
<%@ page import="hu.bp3sjt.model.Table" %>
<%@ page import="hu.bp3sjt.model.Column" %>
<%@ page import="hu.bp3sjt.model.TableItem" %>
<%@ page import="javafx.collections.FXCollections" %>
<%@ page isELIgnored="true" %>
<%@ page session="false" %>
<html>
<head>
    <%
        DataBase db = (DataBase) application.getAttribute("db");
        DataBaseDaoImp dao = (DataBaseDaoImp) application.getAttribute("dao");
    %>
    <title>database</title>
</head>
<body>
<a href="../index.jsp">Upload new File</a>
    <div>
        <%
            String view = request.getParameter("view");
            String tableName = request.getParameter("tables");
            if(view == null) view = "";

          switch(view){
              case "1":
                  Table t = new Table();
                  t.setName(tableName);
                  List<Column> columns = dao.findAllColumns(db, t);
                  t.setColumns(FXCollections.observableArrayList(columns));
                  List<TableItem> rows = dao.findAllItems(db, t);
                  //table view
                  //teable header
                  out.print("<table>\n" +
                          "  <tr>");
                  //columns
                  for (Column c: columns){
                      out.print(String.format("<th>%s</th>", c.getName()));
                  }

                  out.print("</tr>");
                  //rows
                  for (TableItem row: rows){
                      out.print("<tr>");
                      for (String s: row.getFields()){
                          out.print(String.format("<td>%s</td>", s));
                      }
                      out.print("</tr>");
                  }

                  //end table
                  out.print("</table>");
                  break;
              case "2":
                  //schema view
                  out.print(String.format("</br>Table: %s</br>", tableName));
                  Table t2 = new Table();
                  t2.setName(tableName);
                  out.print(dao.findTableScheme(db, t2).replaceAll(",", ",</br>"));
                  break;
              default:
                  break;
          }

        %>
    </div>
    <%
        List<Table> tables = dao.findAllTables(db);
    %>
    <form action="main.jsp" method="post">
        <label for="tables">Choose a table:</label>
        <select name="tables" id="tables">
            <%
                for (Table t: tables) {
                    out.print(String.format("<option value=\"%s\">%s</option>", t.getName(), t.getName()));
                }
            %>
        </select>
        <br><br>
        <input type="radio" id="table" name="view" value="1">
        <label for="table">records</label><br>
        <input type="radio" id="schema" name="view" value="2">
        <label for="schema">schema</label><br>
        <input type="submit" value="Submit">
        <br><br>
    </form>
</body>
</html>
