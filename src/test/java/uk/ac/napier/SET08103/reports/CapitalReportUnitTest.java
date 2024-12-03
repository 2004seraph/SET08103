package uk.ac.napier.SET08103.reports;

/**
 * Class for testing App
 */
//@ExtendWith(MockitoExtension.class)
//public final class CapitalReportUnitTest {
//    @Mock
//    Connection conn;
//
//    @Mock
//    City mockCity;
//    @Mock
//    Country mockCountry;
//
//    @Test
//    void printNullMember(){
//        conn = mock(Connection.class);
//
//        ArrayList<City> countries = new ArrayList<>();
//        countries.add(null);
//        assertAll(() -> CapitalReport.print(countries, conn));
//    }
//
//    @Test
//    void printValidNoDistrict(){
//        conn = mock(Connection.class);
//
//        ArrayList<City> cities = new ArrayList<>();
//
//        mockCity = mock(City.class);
//        {
//            when(mockCity.toString()).thenReturn("City name");
//            when(mockCity.getTotalPopulation(conn)).thenReturn(0L);
//
//            mockCountry = mock(Country.class);
//            when(mockCountry.toString()).thenReturn("Country name");
//            when(mockCity.getCountry()).thenReturn(mockCountry);
//        }
//
//        cities.add(mockCity);
//
//        assertAll(() -> CapitalReport.print(cities, conn));
//    }
//}