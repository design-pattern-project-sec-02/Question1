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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.derby.jdbc.EmbeddedDriver;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class SwimmersManager extends Application {

    private SwimmersDataController dbManager;
    private ObservableList<Swimmer> swimmers;

    @Override
    public void start(Stage myStage) {

        dbManager = new SwimmersDataController();

        myStage.setTitle("Swimmers Manager");

        GridPane rootNode = new GridPane();
        rootNode.setPadding(new Insets(15));
        rootNode.setHgap(5);
        rootNode.setVgap(5);
        rootNode.setAlignment(Pos.CENTER);

        Scene myScene = new Scene(rootNode, 800, 600);

        int row = -1;

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
        rootNode.add(new Label("Average Time:"), 0, ++row);
        TextField txtTime = new TextField();
        rootNode.add(txtTime, 1, row);

        // total distance
        rootNode.add(new Label("Distance Sum:"), 0, ++row);
        TextField txtDistance = new TextField();
        rootNode.add(txtDistance, 1, row);

        // number of strokes
        rootNode.add(new Label("Number of Strokes:"), 0, ++row);
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
            RadioButton rb = (RadioButton)sexGroup.getSelectedToggle();
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

            if(success) {
                swimmers.add(swimmer);
                status.setText("Successfully added!");
            } else {
                status.setText("Not added!");
            }

        });

        TableView<Swimmer> table = new TableView<>();

        TableColumn<Swimmer, String> firstNameCol = new TableColumn<>("First Name");
        TableColumn<Swimmer, String> lastNameCol = new TableColumn<>("Last Name");
        TableColumn<Swimmer, String> sexCol = new TableColumn<>("Sex");
        TableColumn<Swimmer, Integer> ageCol = new TableColumn<>("Age");
        TableColumn<Swimmer, Float> timeCol = new TableColumn<>("Time");

        // Defines how to fill data for each cell.
        // Get value from property of UserAccount. .

        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        sexCol.setCellValueFactory(new PropertyValueFactory<>("sex"));
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("bestAverageTime"));

        // Set Sort type for userName column
        firstNameCol.setSortType(TableColumn.SortType.DESCENDING);

        // Display row data
        swimmers = FXCollections.observableArrayList(dbManager.getSwimmers());
        table.setItems(swimmers);

        //noinspection unchecked
        table.getColumns().addAll(firstNameCol, lastNameCol, sexCol, ageCol, timeCol);

        StackPane tableRoot = new StackPane();
        tableRoot.getChildren().add(table);

        rootNode.add(tableRoot, 2, 0);
        GridPane.setRowSpan(tableRoot, row+1);
        GridPane.setValignment(tableRoot, VPos.TOP);

        myStage.setScene(myScene);

        myStage.show();

    }


    @Override
    public void stop(){
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
                System.out.println("Swimmers length: "+swimmers.size());
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

    }
}
