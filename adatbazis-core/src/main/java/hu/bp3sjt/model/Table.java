package hu.bp3sjt.model;

import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.util.List;

public class Table {

    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final ObjectProperty<DataBase> parent = new SimpleObjectProperty<>(this, "parent");
    private final ObjectProperty<ObservableList<Column>> columns = new SimpleObjectProperty<>(this, "columns");
    private final StringProperty schema = new SimpleStringProperty(this, "schema");

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

    public String getSchema() {
        return schema.get();
    }

    public StringProperty schemaProperty() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema.set(schema);
    }
}
