package hu.bp3sjt.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Column {

    private StringProperty name = new SimpleStringProperty(this, "name");
    private StringProperty dType = new SimpleStringProperty(this, "dType");
    private IntegerProperty index = new SimpleIntegerProperty(this, "index");

    public int getIndex() {
        return index.get();
    }

    public IntegerProperty indexProperty() {
        return index;
    }

    public void setIndex(int index) {
        this.index.set(index);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getdType() {
        return dType.get();
    }

    public StringProperty dTypeProperty() {
        return dType;
    }

    public void setdType(String dType) {
        this.dType.set(dType);
    }
}
