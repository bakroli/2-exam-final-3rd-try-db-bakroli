# Kávéház
Ez a szoftver egy kávéháznak készül, ahol egy interaktív táblán rendelhetnek a vendégek. Írd meg azt az adatforrást lekérdező osztályt, ami majd a különböző típusú italok listájával kiszolgálja a felhasználói felületet.

# Adatbázis

Az adatbázis két táblából áll amelynek nevei `products` és `categories`.

A `products` tábla következő oszlopokal rendelkezik:

- drink_name VARCHAR(255)
- drink_category_id INT
- price INT

Például:
| drink_name            | drink_category_id | price  |
|:----------------------|:------------------|:-------|
| Espresso              | 2                 | 790    |
| Jasmine               | 1                 | 590    |
| Cappuccino            | 2                 | 1450   |
| Americano             | 2                 | 1290   |
| Earl Grey             | 1                 | 590    |
| Caffe Latte           | 2                 | 1450   |
| Classic hot chocolate | 3                 | 1390   |

A `categories` tábla következő oszlopokal rendelkezik:

- drink_category_id SERIAL
- drink_category_name VARCHAR(255)

Például:
| drink_id  | drink_category_name |
|:----------|:--------------------|
| 1         |  HOT_TEA            |  
| 2         |  COFFEE             |  
| 3         |  CHOCOLATE          |
| 4         |  SMOOTHIE           |

# Java alkalmazás

Az `CoffeeHouse` osztály konstruktora a következő paraméterekkel rendelkezik:

- `String dbUrl` az url amin az adatbázis elérhető.
- `String dbUser` felhasználónév amivel csatlakozhatunk az adatbázishoz.
- `String dbPassword`  A `dbUser`-hez tartozó jelszó.

Készítsd el a `CoffeeHouse` osztály `getListOfDrinksByCategory(String categoryName)` metódusát!
Abban az esetben ha az adatbázis üres a metódus térjen vissza egy üres `Drink` listával. 
Egyéb esetben a metódus térjen vissza egy `Drink` listában az összes olyan itallal ami a megadott kategóriába tartozik, áruk szerint növekvő sorrendben. 
A `Drink` osztálynak három mezője van, egy `String drinkName`, `String category` és egy `int price`. 
Készítsd el ezt az osztályt is, definiálj neki konstruktort és ne felejtsd felülírni a hozzá tartozó equals függvényt.

A megoldáshoz használj `PreparedStatement`-et!

# Test-ek

```java
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
```

# Pontozás

Egy feladatra 0 pontot ér, ha:

- nem fordul le
- lefordul, de egy teszteset sem fut le sikeresen.
- ha a forráskód olvashatatlan, nem felel meg a konvencióknak, nem követi a clean code alapelveket.

0 pont adandó továbbá, ha:

- kielégíti a teszteseteket, de a szöveges követelményeknek nem felel meg

Pontokat a további működési funkciók megfelelősségének arányában kell adni a vizsgafeladatra:

- 5 pont: az adott projekt lefordul, néhány teszteset sikeresen lefut, és ezek funkcionálisan is helyesek. Azonban több
  teszteset nem fut le, és a kód is olvashatatlan.
- 10 pont: a projekt lefordul, a tesztesetek legtöbbje lefut, ezek funkcionálisan is helyesek, és a clean code elvek
  nagyrészt betartásra kerültek.
- 20 pont: ha a projekt lefordul, a tesztesetek lefutnak, funkcionálisan helyesek, és csak apróbb funkcionális vagy
  clean code hibák szerepelnek a megoldásban.

Gyakorlati pontozás a project feladatokhoz:

- Alap pontszám egy feladatra(max 20): lefutó egység tesztek száma / összes egység tesztek száma * 20, feltéve, hogy a
  megoldás a szövegben megfogalmazott feladatot valósítja meg
- Clean kód, programozási elvek, bevett gyakorlat, kód formázás megsértéséért - pontlevonás jár. Szintén
  pontlevonás jár, ha valaki a feladatot nem a leghatékonyabb módszerrel oldja meg - amennyiben ez értelmezhető.
