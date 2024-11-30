package uk.ac.napier.SET08103.model;

//public final class WorldIntegrationTest extends AbstractIntegrationTest {
//
//    @Test
//    void zoneInfo() throws SQLException {
//        final Connection conn = getAppDatabaseConnection();
//
//        // ensure there are indeed 7 continents
//        assertEquals(World.INSTANCE.getInnerZones(conn).size(), 7);
//    }
//
//    @Test
//    void cities() {
//        final Connection conn = getAppDatabaseConnection();
//
//        try (PreparedStatement stmt = conn.prepareStatement(
//                Model.buildStatement(
//                        "SELECT", City.PRIMARY_KEY,
//                        "FROM", City.TABLE
//                )
//        ); ResultSet res = stmt.executeQuery()) {
//
//            List<City> citiesOfTheWorld = World.INSTANCE.getCities(conn);
//
//            int total = 0;
//            while (res.next()) {
//                total++;
//                Assertions.assertTrue(citiesOfTheWorld.contains(City.fromId(res.getInt(City.PRIMARY_KEY), conn)));
//            }
//            assertEquals(total, citiesOfTheWorld.size());
//
//            // test caching
//            List<City> citiesOfTheWorldFromCache = World.INSTANCE.getCities(conn);
//            Assertions.assertTrue(Testing.compareLists(wrapIZone(citiesOfTheWorldFromCache), wrapIZone(citiesOfTheWorld)));
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Test
//    void getPopulation() throws SQLException {
//        final Connection conn = getAppDatabaseConnection();
//
//        assertEquals(World.INSTANCE.getTotalPopulation(conn), 6078749450L);
//    }
//}
