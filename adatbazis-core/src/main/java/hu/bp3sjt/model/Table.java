package hu.bp3sjt.model;

import javafx.beans.property.*;
import javafx.collections.ObservableList;

public class Table {

    private StringProperty name = new SimpleStringProperty(this, "name");
    private ObjectProperty<DataBase> parent = new SimpleObjectProperty<>(this, "parent");
    private ObjectProperty<ObservableList<Column>> columns = new SimpleObjectProperty<>(this, "columns");
    private ObjectProperty<TableItem> item = new SimpleObjectProperty<>(this, "item");

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public DataBase getParent() {
        return parent.get();
    }

    public ObjectProperty<DataBase> parentProperty() {
        return parent;
    }

    public void setParent(DataBase parent) {
        this.parent.set(parent);
    }

    public ObservableList<Column> getColumns() {
        return columns.get();
    }

    public ObjectProperty<ObservableList<Column>> columnsProperty() {
        return columns;
    }

    public void setColumns(ObservableList<Column> columns) {
        this.columns.set(columns);
    }

    @Override
    public String toString() {
        return name.get();
    }

    public TableItem getItem() {
        return item.get();
    }

    public ObjectProperty<TableItem> itemProperty() {
        return item;
    }

    public void setItem(TableItem item) {
        item.setParent(this);
        this.item.set(item);
    }
}
