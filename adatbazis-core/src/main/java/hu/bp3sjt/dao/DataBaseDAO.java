package hu.bp3sjt.dao;

import hu.bp3sjt.model.Column;
import hu.bp3sjt.model.DataBase;
import hu.bp3sjt.model.Table;
import hu.bp3sjt.model.TableItem;

import java.util.List;

public interface DataBaseDAO {

    public List<Table> findAllTables(DataBase db);
    public List<Column> findAllColumns(DataBase db, Table table);
    public List<TableItem> findAllItems(DataBase db, Table table);
    public void findTableScheme(DataBase db, Table table);

}
