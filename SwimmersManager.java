import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.derby.jdbc.EmbeddedDriver;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SwimmersManager extends Application {

    private SwimmersDataController dbManager;
    private ObservableList<Swimmer> userViewSwimmers, originalViewSwimmers;
    private TableView<Swimmer> userView, originalView;
    private SortedState sortedState;
    private List<Swimmer> swimmers;

    @Override
    public void start(Stage myStage) {

        dbManager = new SwimmersDataController();
        swimmers = dbManager.getSwimmers();
        sortedState = new TimeSortedState(swimmers);
        sortedState.sort();

        myStage.setTitle("Swimmers Manager");

        GridPane rootNode = new GridPane();
        rootNode.setPadding(new Insets(15));
        rootNode.setHgap(5);
        rootNode.setVgap(5);
        rootNode.setAlignment(Pos.CENTER);

        Scene myScene = new Scene(rootNode, 1350, 600);

        int row = 2;

        // first name
        rootNode.add(new Label("First Name:"), 0, ++row);
        TextField txtFirstName = new TextField();
        rootNode.add(txtFirstName, 1, row);

        // last name
        rootNode.add(new Label("Last Name:"), 0, ++row);
        TextField txtLastName = new TextField();
        rootNode.add(txtLastName, 1, row);

        // birth date
        rootNode.add(new Label("Birth Day:"), 0, ++row);
        DatePicker datePicker = new DatePicker();
        rootNode.add(datePicker, 1, row);

        // sex
        rootNode.add(new Label("Sex :"), 0, ++row);
        final ToggleGroup sexGroup = new ToggleGroup();

        RadioButton rbFemale = new RadioButton("Female");
        rbFemale.setToggleGroup(sexGroup);
        rbFemale.setSelected(true);
        rootNode.add(rbFemale, 1, row);
        GridPane.setHalignment(rbFemale, HPos.LEFT);

        RadioButton rbMale = new RadioButton("Male");
        rbMale.setToggleGroup(sexGroup);
        rootNode.add(rbMale, 1, row);
        GridPane.setHalignment(rbMale, HPos.RIGHT);

        // age
        rootNode.add(new Label("Age:"), 0, ++row);
        TextField txtAge = new TextField();
        rootNode.add(txtAge, 1, row);

        // age
        rootNode.add(new Label("Time:"), 0, ++row);
        TextField txtTime = new TextField();
        rootNode.add(txtTime, 1, row);

        // total distance
        rootNode.add(new Label("Distance:"), 0, ++row);
        TextField txtDistance = new TextField();
        rootNode.add(txtDistance, 1, row);

        // number of strokes
        rootNode.add(new Label("Strokes:"), 0, ++row);
        TextField txtStrokes = new TextField();
        rootNode.add(txtStrokes, 1, row);

        Button btnAdd = new Button("Add Swimmer");
        rootNode.add(btnAdd, 1, ++row);
        GridPane.setHalignment(btnAdd, HPos.LEFT);

        // status label
        Label status = new Label();
        GridPane.setColumnSpan(status, 2);
        rootNode.add(status, 0, ++row);
        GridPane.setHalignment(status, HPos.CENTER);

        // add handler
        btnAdd.setOnAction(e -> {

            String firstName = txtFirstName.getText();
            String lastName = txtLastName.getText();
            LocalDate birthDate = datePicker.getValue();
            RadioButton rb = (RadioButton) sexGroup.getSelectedToggle();
            String sex = rb != null ? rb.getText() : "Male";
            int age = Integer.parseInt(txtAge.getText());
            int strokes = Integer.parseInt(txtStrokes.getText());
            float time = Float.parseFloat(txtTime.getText());
            float distance = Float.parseFloat(txtDistance.getText());

            Swimmer swimmer = new Swimmer();
            swimmer.setFirstName(firstName);
            swimmer.setLastName(lastName);
            swimmer.setBirthDay(birthDate);
            swimmer.setSex(sex);
            swimmer.setAge(age);
            swimmer.setBestAverageTime(time);
            swimmer.setTotalNumberOfStrokes(strokes);
            swimmer.setDistanceSwumInMeters(distance);

            boolean success = dbManager.addSwimmer(swimmer);

            if (success) {
                userViewSwimmers.add(swimmer);
                originalViewSwimmers.add(swimmer);
                status.setText("Successfully added!");
                userView.refresh();
                originalView.refresh();
            } else {
                status.setText("Not added!");
            }

        });

        userView = new TableView<>();

        TableColumn<Swimmer, String> firstNameCol = new TableColumn<>("First");
        TableColumn<Swimmer, String> lastNameCol = new TableColumn<>("Last");
        TableColumn<Swimmer, String> sexCol = new TableColumn<>("Sex");
        TableColumn<Swimmer, Integer> ageCol = new TableColumn<>("Age");
        TableColumn<Swimmer, String> ageGroupCol = new TableColumn<>("AgeG");
        TableColumn<Swimmer, Float> timeCol = new TableColumn<>("Time");

        // Defines how to fill data for each cell.
        // Get value from property of UserAccount. .

        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        sexCol.setCellValueFactory(new PropertyValueFactory<>("sex"));
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageGroupCol.setCellValueFactory(new PropertyValueFactory<>("ageGroup"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("bestAverageTime"));

        // Display row data
        userViewSwimmers = FXCollections.observableArrayList(swimmers);
        userView.setItems(userViewSwimmers);

        //noinspection unchecked
        userView.getColumns().addAll(firstNameCol, lastNameCol, sexCol, ageCol, ageGroupCol, timeCol);

        for (TableColumn tc : userView.getColumns()) {
            tc.setStyle("-fx-alignment:CENTER;");
            tc.setSortable(false);
        }

        userView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox userViewBox = new VBox();
        userViewBox.setAlignment(Pos.CENTER);
        userViewBox.getChildren().add(new Label("User View"));
        userViewBox.getChildren().add(userView);

        rootNode.add(userViewBox, 2, 0);
        GridPane.setRowSpan(userViewBox, row);
        GridPane.setValignment(userViewBox, VPos.TOP);

        // Create the Label
        Label sortLabel = new Label("Sort By:");

        // Create the ComboBox
        final ComboBox<String> sortFields = new ComboBox<>();
        // Add the Months to the ComboBox
        sortFields.getItems().addAll("Sex", "Time", "Age");
        sortFields.setValue("Time");
        // Set the Limit of visible sortFields to 5
        sortFields.setVisibleRowCount(4);
        // listen to sort field change
        sortFields.setOnAction(e -> {
            String sortField = sortFields.getValue();
            switch (sortField) {
                case "Time":
                    sortedState = new TimeSortedState(swimmers);
                    break;
                case "Age":
                    sortedState = new AgeSortedState(swimmers);
                    break;
                case "Sex":
                    sortedState = new SexSortedState(swimmers);
                    break;
                default:
                    break;
            }
        });

        // Create the HBox for the Months
        HBox sortBox = new HBox();
        sortBox.getChildren().addAll(sortLabel, sortFields);

        rootNode.add(sortBox, 2, row + 1);
        GridPane.setHalignment(sortBox, HPos.LEFT);

        Button btnSort = new Button("Sort User View");
        rootNode.add(btnSort, 2, row + 1);
        GridPane.setHalignment(btnSort, HPos.RIGHT);

        // add handler
        btnSort.setOnAction(e -> {

            sortedState.sort();
            userViewSwimmers.setAll(swimmers);
            userView.refresh();

        });

        GridPane.setHalignment(sortBox, HPos.CENTER);

        ////////////////////////////////////////
        /////  ORIGINAL VIEW
        ////////////////////////////////////////

        originalView = new TableView<>();

        TableColumn<Swimmer, String> firstNameColOriginal = new TableColumn<>("First");
        TableColumn<Swimmer, String> lastNameColOriginal = new TableColumn<>("Last");
        TableColumn<Swimmer, String> sexColOriginal = new TableColumn<>("Sex");
        TableColumn<Swimmer, Integer> ageColOriginal = new TableColumn<>("Age");
        TableColumn<Swimmer, Integer> ageGroupColOriginal = new TableColumn<>("AgeG");
        TableColumn<Swimmer, Float> timeColOriginal = new TableColumn<>("Time");

        // Defines how to fill data for each cell.
        // Get value from property of UserAccount. .

        firstNameColOriginal.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColOriginal.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        sexColOriginal.setCellValueFactory(new PropertyValueFactory<>("sex"));
        ageColOriginal.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageGroupColOriginal.setCellValueFactory(new PropertyValueFactory<>("ageGroup"));
        timeColOriginal.setCellValueFactory(new PropertyValueFactory<>("bestAverageTime"));

        // Display row data
        originalViewSwimmers = FXCollections.observableArrayList(dbManager.getSwimmers());
        originalView.setItems(originalViewSwimmers);

        //noinspection unchecked
        originalView.getColumns().addAll(firstNameColOriginal, lastNameColOriginal,
                sexColOriginal, ageColOriginal, ageGroupColOriginal, timeColOriginal);

        for (TableColumn tc : originalView.getColumns()) {
            tc.setStyle("-fx-alignment:CENTER;");
            tc.setSortable(false);
        }

        originalView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox originalViewBox = new VBox();
        originalViewBox.setAlignment(Pos.CENTER);
        originalViewBox.getChildren().add(new Label("Original View"));
        originalViewBox.getChildren().add(originalView);

        rootNode.add(originalViewBox, 3, 0);
        GridPane.setRowSpan(originalViewBox, row);
        GridPane.setValignment(originalViewBox, VPos.TOP);

        myStage.setScene(myScene);

        myStage.show();

    }

    @Override
    public void stop() {
        dbManager.closeDatabase();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class SwimmersDataController {

        private PreparedStatement insertStatement;
        private Connection conn;
        private ResultSet rs;
        private Statement stmt;

        SwimmersDataController() {
            setUpDatabase();
        }

        private void setUpDatabase() {

            String createSQL = "create table swimmer ("
                    + "swimmer_id integer not null generated always as"
                    + " identity (start with 1, increment by 1), "
                    + "first_name varchar(30) not null, last_name varchar(30) not null, sex varchar(30) not null, "
                    + "age int, strokes int, distance float, time float, birth_day date, "
                    + "constraint swimmer_primary_key primary key (swimmer_id))";

            try {
                Driver derbyEmbeddedDriver = new EmbeddedDriver();
                DriverManager.registerDriver(derbyEmbeddedDriver);
                conn = DriverManager.getConnection
                        ("jdbc:derby:database;create=true");
                stmt = conn.createStatement();
                conn.setAutoCommit(false);

                DatabaseMetaData dbm = conn.getMetaData();
                rs = dbm.getTables(null, "APP", "SWIMMER", null);
                if (rs.next()) {
                    System.out.println("Table exists");
                } else {
                    System.out.println("Table does not exist");
                    stmt.execute(createSQL);
                    conn.commit();

                }

                // prepare statement;
                insertStatement = conn.prepareStatement(
                        "insert into swimmer (first_name, last_name, sex," +
                                " age, distance, time, strokes, birth_day) values(?,?,?,?,?,?,?,?)");

            } catch (SQLException ex) {
                System.out.println("in connection" + ex);
            }

        }

        ArrayList<Swimmer> getSwimmers() {
            ArrayList<Swimmer> swimmers = new ArrayList<>();
            try {
                rs = stmt.executeQuery("select * from swimmer");
                while (rs.next()) {
                    Swimmer swimmer = new Swimmer();
                    swimmer.setFirstName(rs.getString("first_name"));
                    swimmer.setLastName(rs.getString("last_name"));
                    swimmer.setSex(rs.getString("sex"));
                    swimmer.setAge(rs.getInt("age"));
                    swimmer.setTotalNumberOfStrokes(rs.getInt("strokes"));
                    swimmer.setDistanceSwumInMeters(rs.getFloat("distance"));
                    swimmer.setBestAverageTime(rs.getFloat("time"));
                    swimmer.setBirthDay(rs.getDate("birth_day").toLocalDate());
                    swimmers.add(swimmer);
                }
                System.out.println("Swimmers length: " + swimmers.size());
                return swimmers;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        }

        boolean addSwimmer(Swimmer swimmer) {
            try {
                insertStatement.setString(1, swimmer.getFirstName());
                insertStatement.setString(2, swimmer.getLastName());
                insertStatement.setString(3, swimmer.getSex());
                insertStatement.setInt(4, swimmer.getAge());
                insertStatement.setFloat(5, swimmer.getDistanceSwumInMeters());
                insertStatement.setFloat(6, swimmer.getBestAverageTime());
                insertStatement.setInt(7, swimmer.getTotalNumberOfStrokes());
                insertStatement.setDate(8, Date.valueOf(swimmer.getBirthDay()));
                insertStatement.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        void closeDatabase() {
            try {
                DriverManager.getConnection
                        ("jdbc:derby:;shutdown=true");
            } catch (SQLException ex) {
                if (((ex.getErrorCode() == 50000) &&
                        ("XJ015".equals(ex.getSQLState())))) {
                    System.out.println("Derby shut down normally");
                } else {
                    System.err.println("Derby did not shut down normally");
                    System.err.println(ex.getMessage());
                }
            }
        }

    }

    @SuppressWarnings("WeakerAccess")
    public static class Swimmer {

        private LocalDate birthDay;
        private float bestAverageTime;
        private String sex;
        private int age;
        private String firstName;
        private String lastName;
        private float distanceSwumInMeters;
        private int totalNumberOfStrokes;

        public LocalDate getBirthDay() {
            return birthDay;
        }

        public void setBirthDay(LocalDate birthDay) {
            this.birthDay = birthDay;
        }

        public float getBestAverageTime() {
            return bestAverageTime;
        }

        public void setBestAverageTime(float bestAverageTime) {
            this.bestAverageTime = bestAverageTime;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public float getDistanceSwumInMeters() {
            return distanceSwumInMeters;
        }

        public void setDistanceSwumInMeters(float distanceSwumInMeters) {
            this.distanceSwumInMeters = distanceSwumInMeters;
        }

        public int getTotalNumberOfStrokes() {
            return totalNumberOfStrokes;
        }

        public void setTotalNumberOfStrokes(int totalNumberOfStrokes) {
            this.totalNumberOfStrokes = totalNumberOfStrokes;
        }

        @SuppressWarnings("unused")
        public String getAgeGroup() {
            if (age < 14) {
                return "1 - 13";
            } else if (age < 18) {
                return "14 - 18";
            } else if (age < 25) {
                return "19 - 25";
            } else if (age < 30) {
                return "26 - 30";
            } else {
                return "31 - INF";
            }
        }

    }

    static abstract class SortedState {

        List<Swimmer> swimmers;

        SortedState(List<Swimmer> swimmers) {
            this.swimmers = swimmers;
        }

        abstract void sort();

    }

    static class SexSortedState extends SortedState {

        SexSortedState(List<Swimmer> swimmers) {
            super(swimmers);
        }

        @Override
        void sort() {
            swimmers.sort((swimmerOne, swimmerTwo) -> {
                String sex1 = swimmerOne.getSex();
                String sex2 = swimmerTwo.getSex();
                return sex1.compareTo(sex2);
            });
        }
    }

    static class AgeSortedState extends SortedState {

        AgeSortedState(List<Swimmer> swimmers) {
            super(swimmers);
        }

        @Override
        void sort() {
            swimmers.sort((swimmerOne, swimmerTwo) -> {
                int age1 = swimmerOne.getAge();
                int age2 = swimmerTwo.getAge();
                return Integer.compare(age1, age2);
            });
        }
    }

    static class TimeSortedState extends SortedState {


        TimeSortedState(List<Swimmer> swimmers) {
            super(swimmers);
        }

        @Override
        void sort() {
            swimmers.sort((swimmerOne, swimmerTwo) -> {
                float t1 = swimmerOne.getBestAverageTime();
                float t2 = swimmerTwo.getBestAverageTime();
                return Float.compare(t1, t2);
            });
        }
    }


}
