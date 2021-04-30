package hu.bp3sjt.dao;

import hu.bp3sjt.model.Column;
import hu.bp3sjt.model.DataBase;
import hu.bp3sjt.model.Table;
import hu.bp3sjt.model.TableItem;
import javafx.collections.ObservableList;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseDaoImp implements DataBaseDAO{

    private static final String SELECT_ALL_ITEMS = "SELECT * FROM %s";
    private static final String FIND_TABLE_SCHEMA = "SELECT * FROM sqlite_schema WHERE type='table' AND name=?";
    private static boolean classIsLoaded = false;

    public DataBaseDaoImp(){
        if (!classIsLoaded) {
            try {
                Class.forName("org.sqlite.JDBC");
                classIsLoaded = true;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Table> findAllTables(DataBase db) {
        List<Table> result = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(db.getUrl())){
            ResultSet resultSet = connection.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
            while (resultSet.next()){
                String tableName = resultSet.getString("TABLE_NAME");
                Table table = new Table();
                table.setName(tableName);
                table.setParent(db);
                table.setSchema(this.findTableScheme(db, table));
                result.add(table);
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Column> findAllColumns(DataBase db, Table table) {
        List<Column> result = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(db.getUrl())){
            ResultSet resultSet = connection.getMetaData().getColumns(null, null, table.getName(), null);
            int index = 0;
            while (resultSet.next()){
                String columnName = resultSet.getString("COLUMN_NAME");
                String datatype = resultSet.getString("TYPE_NAME");
                Column column = new Column();
                column.setName(columnName);
                column.setdType(datatype);
                column.setIndex(index++);
                result.add(column);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public List<TableItem> findAllItems(DataBase db, Table table) {
        List<TableItem> result = new ArrayList<>();
        try(Connection connection = DriverManager.getConnection(db.getUrl())){
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format(SELECT_ALL_ITEMS, table.getName()));
            while (resultSet.next()){
                TableItem tableItem = new TableItem();
                tableItem.setParent(table);
                for (int i = 1; i <= table.getColumns().size(); i++){
                    tableItem.getFields().add(resultSet.getString(i));
                }
                result.add(tableItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String findTableScheme(DataBase db, Table table) {
        try (Connection connection = DriverManager.getConnection(db.getUrl())){

            PreparedStatement preparedStatement = connection.prepareStatement(FIND_TABLE_SCHEMA);
            preparedStatement.setString(1, table.getName());

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            String schema = resultSet.getString("sql");

            return schema;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Boolean updateTable(DataBase db, TableItem oldItem, TableItem newItem, ObservableList<Column> columns) {
        try (Connection connection = DriverManager.getConnection(db.getUrl())){

            String sql = String.format("UPDATE %s SET", oldItem.getParent().getName());
            int i;
            for(i = 0; i < columns.size(); i++){
                if(i != columns.size()-1){
                    sql += String.format(" %s = ?,", columns.get(i).getName());
                }else {
                    sql += String.format(" %s = ? WHERE", columns.get(i).getName());
                }
            }

            for(i = 0; i < columns.size(); i++){
                if(i != columns.size()-1){
                    sql += String.format(" %s = ? AND", columns.get(i).getName());
                }else {
                    sql += String.format(" %s = ? ;", columns.get(i).getName());
                }
            }

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            for(i = 1; i <= columns.size(); i++){
                preparedStatement.setString(i, newItem.getFields().get(i-1));
            }

            for(i = columns.size()+1; i <= columns.size()*2; i++){
                preparedStatement.setString(i, oldItem.getFields().get(i-columns.size()-1));
            }
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            db.setErrorMessage(e.getMessage());
            db.setErrorCode(e.getErrorCode());
            return false;
        }
        return true;
    }

    @Override
    public void deleteItem(DataBase db, TableItem tableItem) {
        try (Connection connection = DriverManager.getConnection(db.getUrl())){
            String sql = String.format("DELETE FROM %s WHERE", tableItem.getParent().getName());
            List<Column> columns = tableItem.getParent().getColumns();

            int i;
            for(i = 0; i < columns.size(); i++){
                if(i != columns.size()-1){
                    sql += String.format(" %s = ? AND", columns.get(i).getName());
                }else {
                    sql += String.format(" %s = ? ;", columns.get(i).getName());
                }
            }

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            for(i = 1; i <= columns.size(); i++){
                preparedStatement.setString(i, tableItem.getFields().get(i-1));
            }

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean insertIntoTable(DataBase db, TableItem tableItem, ObservableList<Column> columns) {
        try (Connection connection = DriverManager.getConnection(db.getUrl())){

            String sql = String.format("INSERT INTO %s(", tableItem.getParent().getName());
            int i;
            for(i = 0; i < columns.size(); i++){
                if(i != columns.size()-1){
                    sql += String.format(" %s,", columns.get(i).getName());
                }else {
                    sql += String.format(" %s)", columns.get(i).getName());
                }
            }

            sql += "VALUES(";

            for(i = 0; i < columns.size(); i++){
                if(i != columns.size()-1){
                    sql += "?,";
                }else {
                    sql += "?);";
                }
            }

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            for(i = 1; i <= columns.size(); i++){
                preparedStatement.setString(i, tableItem.getFields().get(i-1));
            }

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            db.setErrorMessage(e.getMessage());
            db.setErrorCode(e.getErrorCode());
            return false;
        }
        return true;
    }

    @Override
    public ResultSet executeSQL(DataBase db, String sql) {
        try{
            Connection connection = DriverManager.getConnection(db.getUrl());
            Statement statement = connection.createStatement();
            ResultSet resultSet;

            if(sql.startsWith("INSERT") || sql.startsWith("UPDATE") || sql.startsWith("DELETE") ||
            sql.startsWith("insert") || sql.startsWith("update") || sql.startsWith("delete")){
                int ar = statement.executeUpdate(sql);
                SQLException sqlException = new SQLException(String.format("Number of affected rows: %d", ar), "Succes", 4242);
                throw  sqlException;
            }else {
                resultSet = statement.executeQuery(sql);
            }
            return resultSet;
        } catch (SQLException e) {
            if(e.getErrorCode() == 101 || e.getErrorCode() == 4242){
                //No resultSet
            }else {
                e.printStackTrace();
            }
            db.setErrorMessage(e.getMessage());
            db.setErrorCode(e.getErrorCode());
            return null;
        }
    }
}
