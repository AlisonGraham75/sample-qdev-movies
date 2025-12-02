package com.amazonaws.samples.qdevmovies.movies;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify the movie search functionality works end-to-end
 * This test uses the actual MovieService with real data from movies.json
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class SearchIntegrationTest {

    @Test
    public void testMovieServiceBasicFunctionality() {
        // Test that MovieService can be instantiated and loads data
        MovieService movieService = new MovieService();
        
        // Verify basic functionality
        List<Movie> allMovies = movieService.getAllMovies();
        assertNotNull(allMovies, "Movie service should load movies");
        assertFalse(allMovies.isEmpty(), "Should have movies loaded from JSON");
        
        // Verify we have the expected number of movies from movies.json
        assertEquals(12, allMovies.size(), "Should have 12 movies from movies.json");
        
        // Test getMovieById functionality
        Optional<Movie> movie1 = movieService.getMovieById(1L);
        assertTrue(movie1.isPresent(), "Should find movie with ID 1");
        assertEquals("The Prison Escape", movie1.get().getMovieName(), "Should load correct movie name");
    }

    @Test
    public void testSearchFunctionalityBasics() {
        MovieService movieService = new MovieService();
        
        // Test search by name
        List<Movie> prisonMovies = movieService.searchMovies("Prison", null, null);
        assertNotNull(prisonMovies, "Search should return non-null result");
        assertEquals(1, prisonMovies.size(), "Should find exactly one movie with 'Prison' in name");
        assertEquals("The Prison Escape", prisonMovies.get(0).getMovieName(), "Should find the correct movie");
        
        // Test search by genre
        List<Movie> dramaMovies = movieService.searchMovies(null, null, "Drama");
        assertNotNull(dramaMovies, "Genre search should return non-null result");
        assertTrue(dramaMovies.size() >= 1, "Should find at least one drama movie");
        
        // Test search by ID
        List<Movie> movieById = movieService.searchMovies(null, 1L, null);
        assertNotNull(movieById, "ID search should return non-null result");
        assertEquals(1, movieById.size(), "Should find exactly one movie with ID 1");
        assertEquals(1L, movieById.get(0).getId(), "Should find the correct movie by ID");
        
        // Test empty search (should return all movies)
        List<Movie> allMovies = movieService.searchMovies(null, null, null);
        assertNotNull(allMovies, "Empty search should return non-null result");
        assertEquals(12, allMovies.size(), "Empty search should return all movies");
    }

    @Test
    public void testSearchEdgeCases() {
        MovieService movieService = new MovieService();
        
        // Test case-insensitive search
        List<Movie> upperCaseSearch = movieService.searchMovies("PRISON", null, null);
        List<Movie> lowerCaseSearch = movieService.searchMovies("prison", null, null);
        assertEquals(upperCaseSearch.size(), lowerCaseSearch.size(), "Case-insensitive search should work");
        
        // Test no results
        List<Movie> noResults = movieService.searchMovies("NonexistentMovie", null, null);
        assertNotNull(noResults, "No results search should return non-null");
        assertEquals(0, noResults.size(), "Should return empty list for non-existent movie");
        
        // Test empty string parameters
        List<Movie> emptyStringSearch = movieService.searchMovies("", null, "");
        assertNotNull(emptyStringSearch, "Empty string search should return non-null");
        assertEquals(12, emptyStringSearch.size(), "Empty strings should be treated as no criteria");
    }

    @Test
    public void testGenreFunctionality() {
        MovieService movieService = new MovieService();
        
        // Test getAllGenres
        List<String> genres = movieService.getAllGenres();
        assertNotNull(genres, "Should return non-null genre list");
        assertFalse(genres.isEmpty(), "Should have genres available");
        
        // Verify genres are sorted (basic check)
        for (int i = 1; i < genres.size(); i++) {
            assertTrue(genres.get(i).compareTo(genres.get(i-1)) >= 0, 
                      "Genres should be sorted alphabetically");
        }
        
        // Test searchMoviesByGenre
        List<Movie> crimeMovies = movieService.searchMoviesByGenre("Crime");
        assertNotNull(crimeMovies, "Genre-only search should return non-null");
        assertTrue(crimeMovies.size() >= 1, "Should find crime movies");
        
        // Verify all returned movies contain the genre
        for (Movie movie : crimeMovies) {
            assertTrue(movie.getGenre().toLowerCase().contains("crime"), 
                      "All returned movies should contain 'crime' in genre");
        }
    }

    @Test
    public void testNameOnlySearch() {
        MovieService movieService = new MovieService();
        
        // Test searchMoviesByName
        List<Movie> heroMovies = movieService.searchMoviesByName("Hero");
        assertNotNull(heroMovies, "Name-only search should return non-null");
        assertEquals(1, heroMovies.size(), "Should find exactly one movie with 'Hero' in name");
        assertEquals("The Masked Hero", heroMovies.get(0).getMovieName(), "Should find the correct movie");
        
        // Test empty name search
        List<Movie> emptyNameSearch = movieService.searchMoviesByName("");
        assertNotNull(emptyNameSearch, "Empty name search should return non-null");
        assertEquals(0, emptyNameSearch.size(), "Empty name should return no results");
        
        // Test null name search
        List<Movie> nullNameSearch = movieService.searchMoviesByName(null);
        assertNotNull(nullNameSearch, "Null name search should return non-null");
        assertEquals(0, nullNameSearch.size(), "Null name should return no results");
    }

    @Test
    public void testMultipleCriteriaSearch() {
        MovieService movieService = new MovieService();
        
        // Test combining name and genre
        List<Movie> combinedSearch = movieService.searchMovies("Prison", null, "Drama");
        assertNotNull(combinedSearch, "Combined search should return non-null");
        assertEquals(1, combinedSearch.size(), "Should find movie matching both criteria");
        
        Movie foundMovie = combinedSearch.get(0);
        assertTrue(foundMovie.getMovieName().toLowerCase().contains("prison"), 
                  "Found movie should contain 'prison' in name");
        assertTrue(foundMovie.getGenre().toLowerCase().contains("drama"), 
                  "Found movie should contain 'drama' in genre");
        
        // Test conflicting criteria (should return no results)
        List<Movie> conflictingSearch = movieService.searchMovies("Prison", 2L, null);
        assertNotNull(conflictingSearch, "Conflicting search should return non-null");
        assertEquals(0, conflictingSearch.size(), "Conflicting criteria should return no results");
    }

    @Test
    public void testDataIntegrity() {
        MovieService movieService = new MovieService();
        
        // Get original data
        List<Movie> originalMovies = movieService.getAllMovies();
        int originalCount = originalMovies.size();
        
        // Perform various searches
        movieService.searchMovies("Test", null, null);
        movieService.searchMovies(null, 999L, null);
        movieService.searchMoviesByName("Nonexistent");
        movieService.searchMoviesByGenre("Nonexistent");
        
        // Verify data hasn't been modified
        List<Movie> afterSearchMovies = movieService.getAllMovies();
        assertEquals(originalCount, afterSearchMovies.size(), 
                    "Search operations should not modify original data");
        
        // Verify specific movie data is unchanged
        Optional<Movie> movie1 = movieService.getMovieById(1L);
        assertTrue(movie1.isPresent(), "Original movie should still exist");
        assertEquals("The Prison Escape", movie1.get().getMovieName(), 
                    "Original movie data should be unchanged");
    }
}