package hu.bp3sjt.controller;

import hu.bp3sjt.App;
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
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {


    private DataBase dataBase;
    private DataBaseDAO dao = new DataBaseDaoImp();
    private Node leftSideNode;
    private Table selectedTable;
    private TableItem selectedTableItem;


    @FXML
    private ListView<Table> tableListView;

    @FXML
    private TableView<TableItem> columnsTableView;

    @FXML
    private BorderPane borderPane;

    @FXML
    private void onExit(){
        Platform.exit();
    }


    @FXML
    public void onOpenDataBase(ActionEvent actionEvent) {
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
            App.setStageTitle(dataBase.getName());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tableListView.setCellFactory(param -> {
            ListCell<Table> cell = new ListCell<>();
            StringBinding cellTextBinding = new When(cell.itemProperty().isNotNull()).then(cell.itemProperty().asString()).otherwise("");
            cell.textProperty().bind(cellTextBinding);
            cell.setOnMouseClicked(mouseEvent -> {
                if(cell.isSelected() && mouseEvent.getClickCount() >= 2){
                    showTableSchema(cell.getItem());
                }
            });
            return cell;
        });

        tableListView.getSelectionModel().selectedItemProperty().addListener((observable, oldTable, newTable)->{
            if (newTable == null || dataBase == null){
                columnsTableView.getColumns().removeAll(columnsTableView.getColumns());
            }else if(dataBase != null){
                columnsTableView.getColumns().removeAll(columnsTableView.getColumns());
                selectedTable = newTable;
                List<Column> columns = dao.findAllColumns(dataBase, newTable);
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
                List<TableItem> tableItems = dao.findAllItems(dataBase, newTable);
                columnsTableView.getItems().setAll(FXCollections.observableArrayList(tableItems));
            }
        });

        columnsTableView.getSelectionModel().selectedItemProperty().addListener(((observableValue, oldRow, newRow) -> {
            selectedTableItem = newRow;
        }));
    }

    public void onCloseDataBase(ActionEvent actionEvent) {
        dataBase = null;
        tableListView.getItems().removeAll(tableListView.getItems());
        columnsTableView.getItems().removeAll(columnsTableView.getItems());
    }

    public void onEditMenu(ActionEvent actionEvent) {
        if(borderPane.leftProperty().isNull().get())  borderPane.leftProperty().set(leftSideNode);
        borderPane.bottomProperty().set(null);
        VBox rightVbox = new VBox();
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");
        Button addButton = new Button("Add");

        editButton.setTranslateY(25);
        deleteButton.setTranslateY(25);
        addButton.setTranslateY(25);

        editButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setMaxWidth(Double.MAX_VALUE);

        editButton.setOnAction(actionEvent1 -> {
            showEditItem();
        });

        deleteButton.setOnAction(actionEvent1 -> {
        });

        addButton.setOnAction(actionEvent1 -> {
        });

        rightVbox.setSpacing(20);

        rightVbox.getChildren().setAll(editButton, deleteButton, addButton);
        borderPane.rightProperty().set(rightVbox);
    }

    public void onNormalMenu(ActionEvent actionEvent) {
        borderPane.rightProperty().set(null);
        borderPane.bottomProperty().set(null);
        borderPane.leftProperty().set(leftSideNode);
    }

    public void onSQLMenu(ActionEvent actionEvent) {
        borderPane.rightProperty().set(null);
        leftSideNode = borderPane.leftProperty().get();
        borderPane.leftProperty().set(null);

        VBox bottomVbox = new VBox();
        TextArea sqlText = new TextArea();

        bottomVbox.getChildren().add(sqlText);

        borderPane.setBottom(bottomVbox);
    }

    private void showEditItem(){
        if(selectedTable == null || selectedTableItem == null) return;

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        VBox root = new VBox();
        HBox main = new HBox();
        VBox left = new VBox();
        VBox right = new VBox();

        List<Label> labels = new ArrayList<>();
        List<TextField> textFields = new ArrayList<>();
        ObservableList<Column> columns = selectedTable.getColumns();

        for(int i = 0; i < columns.size(); i++){
          Label label = new Label(columns.get(i).getName());
          TextField textField = new TextField(selectedTableItem.getFields().get(i));

          label.minHeightProperty().bind(textField.heightProperty());

          left.getChildren().add(label);
          right.getChildren().add(textField);
          labels.add(label);
          textFields.add(textField);
        }

        Button ok = new Button("ok");
        Button cancel = new Button("cancel");
        HBox buttonBox = new HBox();
        ok.minWidthProperty().bind(main.widthProperty().divide(2).subtract(1));
        cancel.minWidthProperty().bind(main.widthProperty().divide(2).subtract(1));

        buttonBox.getChildren().addAll(ok, cancel);
        main.getChildren().addAll(left, right);
        root.getChildren().addAll(main, buttonBox);

        Scene scene = new Scene(root);
        stage.setTitle(selectedTable.getName());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

    }

    private void showTableSchema(Table table){
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox();
        TextArea schemaTA = new TextArea();
        schemaTA.setText(table.getSchema());
        schemaTA.setEditable(false);
        root.getChildren().add(schemaTA);

        Scene scene = new Scene(root);

        stage.setTitle(table.getName());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();
    }
}
