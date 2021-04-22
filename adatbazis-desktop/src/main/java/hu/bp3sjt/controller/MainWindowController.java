package hu.bp3sjt.controller;

import hu.bp3sjt.dao.DataBaseDAO;
import hu.bp3sjt.dao.DataBaseDaoImp;
import hu.bp3sjt.model.Column;
import hu.bp3sjt.model.DataBase;
import hu.bp3sjt.model.Table;
import hu.bp3sjt.model.TableItem;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.When;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {


    private DataBase dataBase;
    private DataBaseDAO dao = new DataBaseDaoImp();

    @FXML
    private Label dbNameLabel;

    @FXML
    private ListView<Table> tableListView;

    @FXML
    private TableView<TableItem> columnsTableView;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private void onExit(){
        Platform.exit();
    }


    @FXML
    public void onOpenDatabase(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database files", "*.db"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if(selectedFile != null){
            dataBase = new DataBase();
            dataBase.setUrl(selectedFile.getAbsolutePath());
            dataBase.setName(selectedFile.getName());
            List<Table> tables = dao.findAllTables(dataBase);
            dataBase.setTables(FXCollections.observableArrayList(tables));
            tableListView.itemsProperty().bind(dataBase.tablesProperty());
            dbNameLabel.textProperty().bind(dataBase.nameProperty());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tableListView.setCellFactory(param -> {
            ListCell<Table> cell = new ListCell<>();
            StringBinding cellTextBinding = new When(cell.itemProperty().isNotNull()).then(cell.itemProperty().asString()).otherwise("");
            cell.textProperty().bind(cellTextBinding);
            return cell;
        });

        tableListView.getSelectionModel().selectedItemProperty().addListener((observable, oldTable, newTable)->{
            if (newTable == null || dataBase == null){
                columnsTableView.getColumns().removeAll(columnsTableView.getColumns());
            }else if(dataBase != null){
                columnsTableView.getColumns().removeAll(columnsTableView.getColumns());
                List<Column> columns = dao.findAllColumns(dataBase, newTable);
                dao.findTableScheme(dataBase, newTable); //********************
                newTable.setColumns(FXCollections.observableArrayList(columns));
                columns.forEach(column -> {
                    TableColumn tableColumn = new TableColumn<TableItem, String>(column.getName());
                    tableColumn.setMinWidth(80);
                    tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableItem, String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue call(TableColumn.CellDataFeatures param) {
                            return new SimpleStringProperty(((TableItem)param.getValue()).getFields().get(column.getIndex()));
                        }
                    });
                    columnsTableView.getColumns().add(tableColumn);
                });
                //passes the table items to the tableview
                columnsTableView.getItems().setAll(FXCollections.observableArrayList(dao.findAllItems(dataBase, newTable)));
            }
        });
    }
}
