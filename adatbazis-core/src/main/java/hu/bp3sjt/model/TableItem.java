package hu.bp3sjt.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TableItem {

    private ObjectProperty<Table> parent = new SimpleObjectProperty<>(this, "parent");
    private ObservableList<String> fields = FXCollections.observableArrayList();

    public ObservableList<String> getFields() {
        return fields;
    }

    public void setFields(ObservableList<String> fields) {
        this.fields = fields;
    }

    public Table getParent() {
        return parent.get();
    }

    public ObjectProperty<Table> parentProperty() {
        return parent;
    }

    public void setParent(Table parent) {
        this.parent.set(parent);
    }
}
