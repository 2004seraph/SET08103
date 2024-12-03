package uk.ac.napier.SET08103.reports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.napier.SET08103.model.concepts.City;
import uk.ac.napier.SET08103.model.concepts.Country;
import uk.ac.napier.SET08103.model.concepts.District;

import java.sql.Connection;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class for testing App
 */
@ExtendWith(MockitoExtension.class)
public final class CityReportUnitTest {
    @Mock
    Connection conn;

    @Mock
    City mockCity;
    @Mock
    Country mockCountry;
    @Mock
    District mockDistrict;

    @Test
    void printNullCollection(){
        conn = mock(Connection.class);

        assertAll(() -> CityReport.print(null, conn));
    }

    @Test
    void printNullMember(){
        conn = mock(Connection.class);

        ArrayList<City> countries = new ArrayList<>();
        countries.add(null);
        assertAll(() -> CityReport.print(countries, conn));
    }

    @Test
    void printValidNoDistrict(){
        conn = mock(Connection.class);

        ArrayList<City> cities = new ArrayList<>();

        mockCity = mock(City.class);
        {
            when(mockCity.toString()).thenReturn("City name");
            when(mockCity.getTotalPopulation(conn)).thenReturn(0L);

            mockCountry = mock(Country.class);
            when(mockCountry.toString()).thenReturn("Country name");
            when(mockCity.getCountry()).thenReturn(mockCountry);

            when(mockCity.getDistrict()).thenReturn(null);
        }

        cities.add(mockCity);

        assertAll(() -> CityReport.print(cities, conn));
    }

    @Test
    void printValid(){
        conn = mock(Connection.class);

        ArrayList<City> cities = new ArrayList<>();

        mockCity = mock(City.class);
        {
            when(mockCity.toString()).thenReturn("City name");
            when(mockCity.getTotalPopulation(conn)).thenReturn(0L);

            mockCountry = mock(Country.class);
            when(mockCountry.toString()).thenReturn("Country name");
            when(mockCity.getCountry()).thenReturn(mockCountry);

            mockDistrict = mock(District.class);
            when(mockDistrict.toString()).thenReturn("District name");
            when(mockCity.getDistrict()).thenReturn(mockDistrict);
        }

        cities.add(mockCity);

        assertAll(() -> CityReport.print(cities, conn));
    }
}