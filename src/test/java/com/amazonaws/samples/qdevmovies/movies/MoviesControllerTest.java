package com.amazonaws.samples.qdevmovies.movies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.ui.ExtendedModelMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesControllerTest {

    private MoviesController moviesController;
    private Model model;
    private MovieService mockMovieService;
    private ReviewService mockReviewService;

    @BeforeEach
    public void setUp() {
        moviesController = new MoviesController();
        model = new ExtendedModelMap();
        
        // Create mock services with enhanced search functionality
        mockMovieService = new MovieService() {
            private final List<Movie> testMovies = Arrays.asList(
                new Movie(1L, "The Prison Escape", "John Director", 1994, "Drama", "Test description", 142, 5.0),
                new Movie(2L, "The Family Boss", "Michael Filmmaker", 1972, "Crime/Drama", "Test description", 175, 5.0),
                new Movie(3L, "The Masked Hero", "Chris Moviemaker", 2008, "Action/Crime", "Test description", 152, 5.0)
            );
            
            @Override
            public List<Movie> getAllMovies() {
                return testMovies;
            }
            
            @Override
            public Optional<Movie> getMovieById(Long id) {
                return testMovies.stream().filter(m -> m.getId() == id).findFirst();
            }
            
            @Override
            public List<Movie> searchMovies(String name, Long id, String genre) {
                List<Movie> results = new ArrayList<>();
                
                // If all parameters are null/empty, return all movies
                if ((name == null || name.trim().isEmpty()) && 
                    id == null && 
                    (genre == null || genre.trim().isEmpty())) {
                    return new ArrayList<>(testMovies);
                }
                
                for (Movie movie : testMovies) {
                    boolean matches = true;
                    
                    if (name != null && !name.trim().isEmpty()) {
                        matches = matches && movie.getMovieName().toLowerCase().contains(name.toLowerCase());
                    }
                    
                    if (id != null) {
                        matches = matches && movie.getId() == id.longValue();
                    }
                    
                    if (genre != null && !genre.trim().isEmpty()) {
                        matches = matches && movie.getGenre().toLowerCase().contains(genre.toLowerCase());
                    }
                    
                    if (matches) {
                        results.add(movie);
                    }
                }
                
                return results;
            }
            
            @Override
            public List<Movie> searchMoviesByName(String name) {
                if (name == null || name.trim().isEmpty()) {
                    return new ArrayList<>();
                }
                return testMovies.stream()
                    .filter(movie -> movie.getMovieName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            @Override
            public List<Movie> searchMoviesByGenre(String genre) {
                if (genre == null || genre.trim().isEmpty()) {
                    return new ArrayList<>();
                }
                return testMovies.stream()
                    .filter(movie -> movie.getGenre().toLowerCase().contains(genre.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            @Override
            public List<String> getAllGenres() {
                return Arrays.asList("Action/Crime", "Crime/Drama", "Drama");
            }
        };
        
        mockReviewService = new ReviewService() {
            @Override
            public List<Review> getReviewsForMovie(long movieId) {
                return new ArrayList<>();
            }
        };
        
        // Inject mocks using reflection
        try {
            java.lang.reflect.Field movieServiceField = MoviesController.class.getDeclaredField("movieService");
            movieServiceField.setAccessible(true);
            movieServiceField.set(moviesController, mockMovieService);
            
            java.lang.reflect.Field reviewServiceField = MoviesController.class.getDeclaredField("reviewService");
            reviewServiceField.setAccessible(true);
            reviewServiceField.set(moviesController, mockReviewService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock services", e);
        }
    }

    // Original tests
    @Test
    public void testGetMovies() {
        String result = moviesController.getMovies(model);
        assertNotNull(result);
        assertEquals("movies", result);
    }

    @Test
    public void testGetMovieDetails() {
        String result = moviesController.getMovieDetails(1L, model);
        assertNotNull(result);
        assertEquals("movie-details", result);
    }

    @Test
    public void testGetMovieDetailsNotFound() {
        String result = moviesController.getMovieDetails(999L, model);
        assertNotNull(result);
        assertEquals("error", result);
    }

    @Test
    public void testMovieServiceIntegration() {
        List<Movie> movies = mockMovieService.getAllMovies();
        assertEquals(3, movies.size());
        assertEquals("The Prison Escape", movies.get(0).getMovieName());
    }

    // New search functionality tests
    @Test
    public void testSearchMoviesWithNoParameters() {
        String result = moviesController.searchMovies(null, null, null, model);
        assertEquals("movies", result);
        
        @SuppressWarnings("unchecked")
        List<Movie> movies = (List<Movie>) model.getAttribute("movies");
        assertEquals(3, movies.size()); // Should return all movies
        assertTrue(model.getAttribute("isSearchResult") != null);
    }

    @Test
    public void testSearchMoviesByName() {
        String result = moviesController.searchMovies("Prison", null, null, model);
        assertEquals("movies", result);
        
        @SuppressWarnings("unchecked")
        List<Movie> movies = (List<Movie>) model.getAttribute("movies");
        assertEquals(1, movies.size());
        assertEquals("The Prison Escape", movies.get(0).getMovieName());
        
        String searchMessage = (String) model.getAttribute("searchMessage");
        assertTrue(searchMessage.contains("Found 1 movie"));
        assertTrue(searchMessage.contains("matey"));
    }

    @Test
    public void testSearchMoviesByNameCaseInsensitive() {
        String result = moviesController.searchMovies("FAMILY", null, null, model);
        assertEquals("movies", result);
        
        @SuppressWarnings("unchecked")
        List<Movie> movies = (List<Movie>) model.getAttribute("movies");
        assertEquals(1, movies.size());
        assertEquals("The Family Boss", movies.get(0).getMovieName());
    }

    @Test
    public void testSearchMoviesById() {
        String result = moviesController.searchMovies(null, 2L, null, model);
        assertEquals("movies", result);
        
        @SuppressWarnings("unchecked")
        List<Movie> movies = (List<Movie>) model.getAttribute("movies");
        assertEquals(1, movies.size());
        assertEquals("The Family Boss", movies.get(0).getMovieName());
    }

    @Test
    public void testSearchMoviesByGenre() {
        String result = moviesController.searchMovies(null, null, "Drama", model);
        assertEquals("movies", result);
        
        @SuppressWarnings("unchecked")
        List<Movie> movies = (List<Movie>) model.getAttribute("movies");
        assertEquals(2, movies.size()); // Should find "Drama" and "Crime/Drama"
    }

    @Test
    public void testSearchMoviesNoResults() {
        String result = moviesController.searchMovies("NonexistentMovie", null, null, model);
        assertEquals("movies", result);
        
        @SuppressWarnings("unchecked")
        List<Movie> movies = (List<Movie>) model.getAttribute("movies");
        assertEquals(0, movies.size());
        
        String searchMessage = (String) model.getAttribute("searchMessage");
        assertTrue(searchMessage.contains("No movies found"));
        assertTrue(searchMessage.contains("scallywag"));
    }

    @Test
    public void testSearchMoviesInvalidId() {
        String result = moviesController.searchMovies(null, -1L, null, model);
        assertEquals("error", result);
        
        String errorMessage = (String) model.getAttribute("message");
        assertTrue(errorMessage.contains("invalid"));
        assertTrue(errorMessage.contains("matey"));
    }

    // API endpoint tests
    @Test
    public void testSearchMoviesApiSuccess() {
        ResponseEntity<?> response = moviesController.searchMoviesApi("Prison", null, null);
        assertEquals(200, response.getStatusCodeValue());
        
        MoviesController.SearchResponse searchResponse = (MoviesController.SearchResponse) response.getBody();
        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getTotalResults());
        assertEquals("The Prison Escape", searchResponse.getMovies().get(0).getMovieName());
        assertTrue(searchResponse.getMessage().contains("Found 1 movie"));
    }

    @Test
    public void testSearchMoviesApiInvalidId() {
        ResponseEntity<?> response = moviesController.searchMoviesApi(null, -1L, null);
        assertEquals(400, response.getStatusCodeValue());
        
        MoviesController.SearchErrorResponse errorResponse = (MoviesController.SearchErrorResponse) response.getBody();
        assertNotNull(errorResponse);
        assertTrue(errorResponse.getError().contains("Invalid movie ID"));
        assertTrue(errorResponse.getError().contains("matey"));
        assertNotNull(errorResponse.getTimestamp());
    }
}
