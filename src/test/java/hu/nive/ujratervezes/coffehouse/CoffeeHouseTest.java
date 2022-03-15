package hu.nive.ujratervezes.coffehouse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoffeeHouseTest {

    private static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private CoffeeHouse coffeeHouse;

    @BeforeEach
    void init() throws SQLException {
        coffeeHouse = new CoffeeHouse(DB_URL, DB_USER, DB_PASSWORD);
        createTable();
    }

    @AfterEach
    void destruct() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String dropTableDrinks = "DROP TABLE IF EXISTS products";
            Statement statementDrinks = connection.createStatement();
            statementDrinks.execute(dropTableDrinks);
        }
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String dropTableCategories = "DROP TABLE IF EXISTS categories";
            Statement statementCategories = connection.createStatement();
            statementCategories.execute(dropTableCategories);
        }
    }


    private void createTable() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String createTableDrinks = "CREATE TABLE IF NOT EXISTS products (" +
                    " drink_name VARCHAR(255)," +
                    " drink_category_id INT," +
                    " price INT" +
                    ");";
            Statement statementDrinks = connection.createStatement();
            statementDrinks.execute(createTableDrinks);
        }
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String createTableCategories = "CREATE TABLE IF NOT EXISTS categories (" +
                    "drink_category_id SERIAL, " +
                    "drink_category_name VARCHAR(255)" +
                    ");";
            Statement statementCategories = connection.createStatement();
            statementCategories.execute(createTableCategories);
        }
    }

    @Test
    void test_getUniqueDrinks_emptyDatabase() {
        assertEquals(List.of(), coffeeHouse.getListOfDrinksByCategory("CHOCOLATE"));
    }

    @Test
    void test_getUniqueDrinks_anyOrder() throws SQLException {
        insertMultipleDrinks();
        List<Drink> actualDrinks = coffeeHouse.getListOfDrinksByCategory("CHOCOLATE");
        final List<Drink> expected = List.of(
                new Drink("Classic hot chocolate", "CHOCOLATE", 1390)
        );
        assertEquals(expected.size(), actualDrinks.size());
        for (Drink expectedDrink : expected) {
            assertTrue(actualDrinks.contains(expectedDrink));
        }
    }

    @Test
    void test_getUniqueDrinks_oneDuplicate_orderedByPrice() throws SQLException {
        insertMultipleDrinks();
        final List<Drink> expected = List.of(
                new Drink("Espresso", "COFFEE", 790),
                new Drink("Americano", "COFFEE", 1290),
                new Drink("Cappuccino", "COFFEE", 1450),
                new Drink("Caffe Latte", "COFFEE", 1450)
        );
        List<Drink> actualDrinks = coffeeHouse.getListOfDrinksByCategory("COFFEE");
        assertEquals(expected.size(), actualDrinks.size());
        assertEquals(expected, actualDrinks);
    }

    @Test
    void test_getUniqueDrinks_orderedByPrice() throws SQLException {
        insertMultipleDrinks();
        final List<Drink> expected = List.of(
                new Drink("Jasmine", "HOT_TEA", 590),
                new Drink("Earl Grey", "HOT_TEA", 590)
        );
        List<Drink> actualDrinks = coffeeHouse.getListOfDrinksByCategory("HOT_TEA");
        assertEquals(expected, actualDrinks);
    }

    @Test
    void test_getUniqueDrinks_alphabeticOrder_extraCategory() throws SQLException {
        insertMultipleDrinks();
        insertNewCategory();
        List<Drink> actualDrinks = coffeeHouse.getListOfDrinksByCategory("SMOOTHIE");
        assertEquals(List.of(), actualDrinks);
    }


    private void insertMultipleDrinks() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertDrinks = "INSERT INTO products (drink_name, drink_category_id, price) VALUES " +
                    "('Espresso', 2, 790), " +
                    "('Jasmine', 1, 590), " +
                    "('Cappuccino', 2, 1450), " +
                    "('Americano', 2, 1290), " +
                    "('Earl Grey', 1 , 590), " +
                    "('Caffe Latte', 2, 1450), " +
                    "('Classic hot chocolate', 3, 1390);";
            Statement statement = connection.createStatement();
            statement.execute(insertDrinks);
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertDrinks = "INSERT INTO categories (drink_category_id, drink_category_name) VALUES " +
                    "(1,'HOT_TEA'), " +
                    "(2,'COFFEE'), " +
                    "(3,'CHOCOLATE');";
            Statement statement = connection.createStatement();
            statement.execute(insertDrinks);
        }
    }

    private void insertNewCategory() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertDrinks = "INSERT INTO categories (drink_category_id, drink_category_name) VALUES " +
                    "(4,'SMOOTHIE');";
            Statement statement = connection.createStatement();
            statement.execute(insertDrinks);
        }
    }
}
