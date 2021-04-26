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
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {


    private DataBase dataBase;
    private final DataBaseDAO dao = new DataBaseDaoImp();
    private Node leftSideNode = null;
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
            refreshTableList();
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
            }else if(dataBase != null && newTable != oldTable){
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
        App.setStageTitle("");
        tableListView.getItems().removeAll(tableListView.getItems());
        columnsTableView.getItems().removeAll(columnsTableView.getItems());
    }

    public void onEditMenu(ActionEvent actionEvent) {
        if(borderPane.leftProperty().isNull().get())  borderPane.leftProperty().set(leftSideNode);
        if(dataBase != null) refreshTableList();
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
            showDeleteItem();
        });

        addButton.setOnAction(actionEvent1 -> {
            showAddItem();
        });

        rightVbox.setSpacing(20);

        rightVbox.getChildren().setAll(editButton, deleteButton, addButton);
        borderPane.rightProperty().set(rightVbox);
    }

    public void onNormalMenu(ActionEvent actionEvent) {
        borderPane.rightProperty().set(null);
        borderPane.bottomProperty().set(null);
        if(leftSideNode != null) borderPane.leftProperty().set(leftSideNode);
        if(dataBase != null) refreshTableList();
    }

    public void onSQLMenu(ActionEvent actionEvent) {
        borderPane.rightProperty().set(null);
        leftSideNode = borderPane.leftProperty().get();
        borderPane.leftProperty().set(null);
        columnsTableView.getItems().removeAll(columnsTableView.getItems());
        columnsTableView.getColumns().removeAll(columnsTableView.getColumns());

        VBox bottomVbox = new VBox();
        TextArea sqlText = new TextArea();

        bottomVbox.getChildren().add(sqlText);
        borderPane.setBottom(bottomVbox);

        sqlText.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if(dataBase != null && keyEvent.getCode() == KeyCode.F5){
                columnsTableView.getItems().removeAll(columnsTableView.getItems());
                columnsTableView.getColumns().removeAll(columnsTableView.getColumns());
               ResultSet resultSet = dao.executeSQL(dataBase, sqlText.getText());
               if(resultSet == null){
                   showDataBaseError();
               }else{
                   try{
                       ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                       int columnCount = resultSetMetaData.getColumnCount();
                       List<String> columnNames = FXCollections.observableArrayList();
                       for(int i = 1; i <= columnCount; i++){
                           String cname = resultSetMetaData.getColumnName(i);
                           columnNames.add(cname);
                       }

                       for(int i = 0; i < columnCount; i++){
                           TableColumn tableColumn = new TableColumn<TableItem, String>(columnNames.get(i));
                           tableColumn.setMinWidth(80);
                           final int index = i;
                           tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableItem, String>, ObservableValue<String>>() {
                               @Override
                               public ObservableValue call(TableColumn.CellDataFeatures param) {
                                   return new SimpleStringProperty(((TableItem)param.getValue()).getFields().get(index));
                               }
                           });
                           columnsTableView.getColumns().add(tableColumn);
                       }

                       resultSet.close();
                   } catch (SQLException e) {
                       e.printStackTrace();
                   }
               }
            }
        });
    }

    private void showDeleteItem() {
        if(selectedTable == null || selectedTableItem == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, null, ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Are you sure you want to delete?");
        confirm.setHeaderText("Are you sure you want to delete?");

        Optional<ButtonType> answer = confirm.showAndWait();

        if (answer.get() == ButtonType.YES) {
            dao.deleteItem(dataBase, selectedTableItem);
            columnsTableView.getItems().remove(selectedTableItem);
        }
    }

    private void showAddItem() {
        if(selectedTable == null) return;

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        VBox root = new VBox();
        HBox main = new HBox();
        VBox left = new VBox();
        VBox right = new VBox();

        TableItem newItem = new TableItem();
        newItem.setParent(selectedTable);

        ObservableList<Column> columns = newItem.getParent().getColumns();
        ObservableList<String> newItemFields = newItem.getFields();
        List<TextField> textFields = new ArrayList<>();

        for(int i = 0; i < columns.size(); i++){
            Label label = new Label(columns.get(i).getName());
            TextField textField = new TextField(columns.get(i).getdType());
            textFields.add(textField);

            label.minHeightProperty().bind(textField.heightProperty());

            left.getChildren().add(label);
            right.getChildren().add(textField);
        }

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");
        HBox buttonBox = new HBox();
        save.minWidthProperty().bind(main.widthProperty().divide(2).subtract(1));
        cancel.minWidthProperty().bind(main.widthProperty().divide(2).subtract(1));

        buttonBox.getChildren().addAll(save, cancel);
        main.getChildren().addAll(left, right);
        root.getChildren().addAll(main, buttonBox);

        cancel.setOnAction(actionEvent -> {
            stage.close();
        });

        save.setOnAction(actionEvent -> {
            for(int i = 0; i < textFields.size(); i++){
                newItemFields.add(textFields.get(i).getText());
            }
            Boolean done = dao.insertIntoTable(dataBase, newItem, columns);
            if(done){
                columnsTableView.getItems().add(newItem);
            }else {
                showDataBaseError();
            }
            stage.close();
        });

        Scene scene = new Scene(root);
        stage.setTitle(selectedTable.getName());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();
    }

    private void showEditItem(){
        if(selectedTable == null || selectedTableItem == null) return;

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        VBox root = new VBox();
        HBox main = new HBox();
        VBox left = new VBox();
        VBox right = new VBox();

        TableItem oldItem = selectedTableItem;
        TableItem newItem = new TableItem();
        newItem.setParent(selectedTableItem.getParent());

        ObservableList<Column> columns = oldItem.getParent().getColumns();
        ObservableList<String> newItemFields = newItem.getFields();
        List<TextField> textFields = new ArrayList<>();

        for(int i = 0; i < columns.size(); i++){
          Label label = new Label(columns.get(i).getName());
          TextField textField = new TextField(selectedTableItem.getFields().get(i));
          textFields.add(textField);

          label.minHeightProperty().bind(textField.heightProperty());

          left.getChildren().add(label);
          right.getChildren().add(textField);
        }

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");
        HBox buttonBox = new HBox();
        save.minWidthProperty().bind(main.widthProperty().divide(2).subtract(1));
        cancel.minWidthProperty().bind(main.widthProperty().divide(2).subtract(1));

        buttonBox.getChildren().addAll(save, cancel);
        main.getChildren().addAll(left, right);
        root.getChildren().addAll(main, buttonBox);

        cancel.setOnAction(actionEvent -> {
            stage.close();
        });

        save.setOnAction(actionEvent -> {
            for(int i = 0; i < textFields.size(); i++){
                newItemFields.add(textFields.get(i).getText());
            }
            Boolean done = dao.updateTable(dataBase, oldItem, newItem, columns);
            if(done){
                columnsTableView.getItems().remove(oldItem);
                columnsTableView.getItems().add(newItem);
            }else {
                Alert alert = new Alert(Alert.AlertType.ERROR,  null, ButtonType.OK);
                alert.setTitle("SQL ERROR");
                alert.setHeaderText(dataBase.getErrorMessage());
                alert.showAndWait();
            }
            stage.close();
        });

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

    private void showDataBaseError(){
        Alert alert = new Alert(Alert.AlertType.ERROR,  null, ButtonType.OK);
        alert.setTitle("SQL ERROR");
        alert.setHeaderText(dataBase.getErrorMessage());
        alert.showAndWait();
    }

    private void refreshTableList() {
        List<Table> tables = dao.findAllTables(dataBase);
        dataBase.setTables(FXCollections.observableArrayList(tables));
        tableListView.itemsProperty().bind(dataBase.tablesProperty());
    }
}
