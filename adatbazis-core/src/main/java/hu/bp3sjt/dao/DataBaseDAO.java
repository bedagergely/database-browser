package hu.bp3sjt.dao;

import hu.bp3sjt.model.Column;
import hu.bp3sjt.model.DataBase;
import hu.bp3sjt.model.Table;
import hu.bp3sjt.model.TableItem;
import javafx.collections.ObservableList;

import java.util.List;

public interface DataBaseDAO {

    List<Table> findAllTables(DataBase db);
    List<Column> findAllColumns(DataBase db, Table table);
    List<TableItem> findAllItems(DataBase db, Table table);
    String findTableScheme(DataBase db, Table table);
    Boolean updateTable(DataBase db, TableItem oldItem, TableItem newItem, ObservableList<Column> columns);
    Boolean insertIntoTable(DataBase db, TableItem tableItem, ObservableList<Column> columns);
    void deleteItem(DataBase db, TableItem tableItem);

}
