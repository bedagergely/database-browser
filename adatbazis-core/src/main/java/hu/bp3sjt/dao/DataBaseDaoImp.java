package hu.bp3sjt.dao;

import hu.bp3sjt.model.Column;
import hu.bp3sjt.model.DataBase;
import hu.bp3sjt.model.Table;
import hu.bp3sjt.model.TableItem;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseDaoImp implements DataBaseDAO{

    private static final String SELECT_ALL_ITEMS = "SELECT * FROM %s";
    private static final String FIND_TABLE_SCHEMA = "SELECT * FROM sqlite_schema WHERE type='table' AND name=?";

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
                String columnSize = resultSet.getString("COLUMN_SIZE");
                String datatype = resultSet.getString("DATA_TYPE");
                String isNullable = resultSet.getString("IS_NULLABLE");
                String isAutoIncrement = resultSet.getString("IS_AUTOINCREMENT");

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
    public void findTableScheme(DataBase db, Table table) {
        try (Connection connection = DriverManager.getConnection(db.getUrl())){

            PreparedStatement preparedStatement = connection.prepareStatement(FIND_TABLE_SCHEMA);
            preparedStatement.setString(1, table.getName());

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
                System.out.println(resultSet.getString(2));
                System.out.println(resultSet.getString(3));
                System.out.println(resultSet.getInt(4));
                System.out.println(resultSet.getString("sql"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
