package uk.ac.napier.SET08103.reports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.napier.SET08103.model.concepts.City;
import uk.ac.napier.SET08103.model.concepts.Continent;
import uk.ac.napier.SET08103.model.concepts.Country;
import uk.ac.napier.SET08103.model.concepts.Region;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class for testing App
 */
@ExtendWith(MockitoExtension.class)
public final class CountryReportUnitTest {

    @Mock
    Country mockCountry;

    @Mock
    Region mockRegion;
    @Mock
    Continent mockContinent;
    @Mock
    City mockCapital;

    @Test
    void printNullCollection(){
        assertAll(() -> CountryReport.print(null));
    }

    @Test
    void printNullMember(){
        ArrayList<Country> countries = new ArrayList<>();
        countries.add(null);
        assertAll(() -> CountryReport.print(countries));
    }

    @Test
    void printValid(){
        ArrayList<Country> countries = new ArrayList<>();

        mockCountry = mock(Country.class);
        when(mockCountry.getPrimaryKey()).thenReturn("Primary Key");
        when(mockCountry.toString()).thenReturn("Country name");
        ReflectionTestUtils.setField(mockCountry, "population", 0);

        when(mockContinent.toString()).thenReturn("A continent");
        ReflectionTestUtils.setField(mockCountry, "continent", mockContinent);

        when(mockRegion.toString()).thenReturn("A region");
        ReflectionTestUtils.setField(mockCountry, "region", mockRegion);

        when(mockCapital.toString()).thenReturn("A capital city");
        ReflectionTestUtils.setField(mockCountry, "capital", mockCapital);

        countries.add(mockCountry);

        assertAll(() -> CountryReport.print(countries));
    }
}