package com.amazonaws.samples.qdevmovies.movies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for MovieService search functionality
 * Tests the pirate-themed search methods with various scenarios
 */
public class MovieServiceSearchTest {

    private MovieService movieService;

    @BeforeEach
    public void setUp() {
        movieService = new MovieService();
    }

    @Test
    public void testGetAllMovies() {
        List<Movie> movies = movieService.getAllMovies();
        assertNotNull(movies);
        assertFalse(movies.isEmpty());
        assertEquals(12, movies.size()); // Based on movies.json
    }

    @Test
    public void testGetMovieById() {
        Optional<Movie> movie = movieService.getMovieById(1L);
        assertTrue(movie.isPresent());
        assertEquals("The Prison Escape", movie.get().getMovieName());
    }

    @Test
    public void testGetMovieByIdNotFound() {
        Optional<Movie> movie = movieService.getMovieById(999L);
        assertFalse(movie.isPresent());
    }

    @Test
    public void testGetMovieByIdInvalid() {
        Optional<Movie> movie = movieService.getMovieById(-1L);
        assertFalse(movie.isPresent());
        
        Optional<Movie> movieNull = movieService.getMovieById(null);
        assertFalse(movieNull.isPresent());
    }

    // Search functionality tests
    @Test
    public void testSearchMoviesWithAllNullParameters() {
        List<Movie> results = movieService.searchMovies(null, null, null);
        assertNotNull(results);
        assertEquals(12, results.size()); // Should return all movies
    }

    @Test
    public void testSearchMoviesWithAllEmptyParameters() {
        List<Movie> results = movieService.searchMovies("", null, "");
        assertNotNull(results);
        assertEquals(12, results.size()); // Should return all movies
    }

    @Test
    public void testSearchMoviesWithWhitespaceParameters() {
        List<Movie> results = movieService.searchMovies("   ", null, "  ");
        assertNotNull(results);
        assertEquals(12, results.size()); // Should return all movies
    }

    @Test
    public void testSearchMoviesByNameExactMatch() {
        List<Movie> results = movieService.searchMovies("The Prison Escape", null, null);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("The Prison Escape", results.get(0).getMovieName());
    }

    @Test
    public void testSearchMoviesByNamePartialMatch() {
        List<Movie> results = movieService.searchMovies("Prison", null, null);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("The Prison Escape", results.get(0).getMovieName());
    }

    @Test
    public void testSearchMoviesByNameCaseInsensitive() {
        List<Movie> results = movieService.searchMovies("PRISON", null, null);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("The Prison Escape", results.get(0).getMovieName());
        
        List<Movie> results2 = movieService.searchMovies("family", null, null);
        assertNotNull(results2);
        assertEquals(1, results2.size());
        assertEquals("The Family Boss", results2.get(0).getMovieName());
    }

    @Test
    public void testSearchMoviesByNameNoResults() {
        List<Movie> results = movieService.searchMovies("NonexistentMovie", null, null);
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    public void testSearchMoviesById() {
        List<Movie> results = movieService.searchMovies(null, 1L, null);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("The Prison Escape", results.get(0).getMovieName());
        assertEquals(1L, results.get(0).getId());
    }

    @Test
    public void testSearchMoviesByIdNotFound() {
        List<Movie> results = movieService.searchMovies(null, 999L, null);
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    public void testSearchMoviesByGenreExactMatch() {
        List<Movie> results = movieService.searchMovies(null, null, "Drama");
        assertNotNull(results);
        assertTrue(results.size() >= 1);
        // Should find movies with "Drama" genre
        assertTrue(results.stream().anyMatch(m -> m.getGenre().contains("Drama")));
    }

    @Test
    public void testSearchMoviesByGenrePartialMatch() {
        List<Movie> results = movieService.searchMovies(null, null, "Crime");
        assertNotNull(results);
        assertTrue(results.size() >= 1);
        // Should find movies with "Crime" in genre (like "Crime/Drama")
        assertTrue(results.stream().allMatch(m -> m.getGenre().toLowerCase().contains("crime")));
    }

    @Test
    public void testSearchMoviesByGenreCaseInsensitive() {
        List<Movie> results = movieService.searchMovies(null, null, "ACTION");
        assertNotNull(results);
        assertTrue(results.size() >= 1);
        assertTrue(results.stream().allMatch(m -> m.getGenre().toLowerCase().contains("action")));
    }

    @Test
    public void testSearchMoviesByGenreNoResults() {
        List<Movie> results = movieService.searchMovies(null, null, "NonexistentGenre");
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    public void testSearchMoviesWithMultipleCriteria() {
        // Search for a specific movie using multiple criteria
        List<Movie> results = movieService.searchMovies("Prison", 1L, "Drama");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("The Prison Escape", results.get(0).getMovieName());
        assertEquals(1L, results.get(0).getId());
        assertTrue(results.get(0).getGenre().contains("Drama"));
    }

    @Test
    public void testSearchMoviesWithConflictingCriteria() {
        // Search with criteria that don't match any single movie
        List<Movie> results = movieService.searchMovies("Prison", 2L, null);
        assertNotNull(results);
        assertEquals(0, results.size()); // No movie has both "Prison" in name AND ID 2
    }

    @Test
    public void testSearchMoviesByNameOnly() {
        List<Movie> results = movieService.searchMoviesByName("Hero");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("The Masked Hero", results.get(0).getMovieName());
    }

    @Test
    public void testSearchMoviesByNameOnlyEmpty() {
        List<Movie> results = movieService.searchMoviesByName("");
        assertNotNull(results);
        assertEquals(0, results.size());
        
        List<Movie> resultsNull = movieService.searchMoviesByName(null);
        assertNotNull(resultsNull);
        assertEquals(0, resultsNull.size());
    }

    @Test
    public void testSearchMoviesByNameOnlyCaseInsensitive() {
        List<Movie> results = movieService.searchMoviesByName("HERO");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("The Masked Hero", results.get(0).getMovieName());
    }

    @Test
    public void testSearchMoviesByGenreOnly() {
        List<Movie> results = movieService.searchMoviesByGenre("Sci-Fi");
        assertNotNull(results);
        assertTrue(results.size() >= 1);
        assertTrue(results.stream().allMatch(m -> m.getGenre().toLowerCase().contains("sci-fi")));
    }

    @Test
    public void testSearchMoviesByGenreOnlyEmpty() {
        List<Movie> results = movieService.searchMoviesByGenre("");
        assertNotNull(results);
        assertEquals(0, results.size());
        
        List<Movie> resultsNull = movieService.searchMoviesByGenre(null);
        assertNotNull(resultsNull);
        assertEquals(0, resultsNull.size());
    }

    @Test
    public void testSearchMoviesByGenreOnlyCaseInsensitive() {
        List<Movie> results = movieService.searchMoviesByGenre("DRAMA");
        assertNotNull(results);
        assertTrue(results.size() >= 1);
        assertTrue(results.stream().allMatch(m -> m.getGenre().toLowerCase().contains("drama")));
    }

    @Test
    public void testGetAllGenres() {
        List<String> genres = movieService.getAllGenres();
        assertNotNull(genres);
        assertFalse(genres.isEmpty());
        
        // Check that genres are unique and sorted
        for (int i = 1; i < genres.size(); i++) {
            assertTrue(genres.get(i).compareTo(genres.get(i-1)) >= 0, "Genres should be sorted");
        }
        
        // Check for expected genres from movies.json
        assertTrue(genres.contains("Drama"));
        assertTrue(genres.contains("Crime/Drama"));
        assertTrue(genres.contains("Action/Crime"));
    }

    @Test
    public void testSearchMoviesPerformance() {
        // Test that search operations complete in reasonable time
        long startTime = System.currentTimeMillis();
        
        // Perform multiple search operations
        for (int i = 0; i < 100; i++) {
            movieService.searchMovies("The", null, null);
            movieService.searchMovies(null, 1L, null);
            movieService.searchMovies(null, null, "Drama");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time (less than 1 second for 300 operations)
        assertTrue(duration < 1000, "Search operations should be fast");
    }

    @Test
    public void testSearchMoviesDataIntegrity() {
        // Ensure search doesn't modify original data
        List<Movie> originalMovies = movieService.getAllMovies();
        int originalSize = originalMovies.size();
        
        // Perform various searches
        movieService.searchMovies("Test", null, null);
        movieService.searchMovies(null, 999L, null);
        movieService.searchMovies(null, null, "NonexistentGenre");
        
        // Verify original data is unchanged
        List<Movie> moviesAfterSearch = movieService.getAllMovies();
        assertEquals(originalSize, moviesAfterSearch.size());
        
        // Verify specific movie data is unchanged
        Optional<Movie> movie1 = movieService.getMovieById(1L);
        assertTrue(movie1.isPresent());
        assertEquals("The Prison Escape", movie1.get().getMovieName());
    }
}