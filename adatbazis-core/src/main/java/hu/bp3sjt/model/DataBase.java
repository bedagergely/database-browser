package hu.bp3sjt.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class DataBase {

    private String url;
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final ObjectProperty<ObservableList<Table>> tables = new SimpleObjectProperty<>(this, "tables");
    private final StringProperty errorMessage = new SimpleStringProperty(this, "errorMessage");

    public String getErrorMessage() {
        return errorMessage.get();
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage.set(errorMessage);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = "jdbc:sqlite:" + url.replaceAll("\\\\", "/");
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

    public ObservableList<Table> getTables() {
        return tables.get();
    }

    public ObjectProperty<ObservableList<Table>> tablesProperty() {
        return tables;
    }

    public void setTables(ObservableList<Table> tables) {
        this.tables.set(tables);
    }
}
