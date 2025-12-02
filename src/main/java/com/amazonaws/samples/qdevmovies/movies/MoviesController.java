package com.amazonaws.samples.qdevmovies.movies;

import com.amazonaws.samples.qdevmovies.utils.MovieIconUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

@Controller
public class MoviesController {
    private static final Logger logger = LogManager.getLogger(MoviesController.class);

    @Autowired
    private MovieService movieService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/movies")
    public String getMovies(org.springframework.ui.Model model) {
        logger.info("Fetching movies");
        model.addAttribute("movies", movieService.getAllMovies());
        return "movies";
    }

    @GetMapping("/movies/{id}/details")
    public String getMovieDetails(@PathVariable("id") Long movieId, org.springframework.ui.Model model) {
        logger.info("Fetching details for movie ID: {}", movieId);
        
        Optional<Movie> movieOpt = movieService.getMovieById(movieId);
        if (!movieOpt.isPresent()) {
            logger.warn("Movie with ID {} not found", movieId);
            model.addAttribute("title", "Movie Not Found");
            model.addAttribute("message", "Movie with ID " + movieId + " was not found.");
            return "error";
        }
        
        Movie movie = movieOpt.get();
        model.addAttribute("movie", movie);
        model.addAttribute("movieIcon", MovieIconUtils.getMovieIcon(movie.getMovieName()));
        model.addAttribute("allReviews", reviewService.getReviewsForMovie(movie.getId()));
        
        return "movie-details";
    }

    /**
     * Ahoy matey! Search endpoint for findin' movies by name, id, or genre
     * This endpoint supports both HTML form submissions and JSON API calls
     * 
     * @param name Movie name to search for (optional)
     * @param id Movie ID to search for (optional)
     * @param genre Genre to filter by (optional)
     * @param model Spring model for template rendering
     * @return Template name for HTML response or JSON for API calls
     */
    @GetMapping("/movies/search")
    public String searchMovies(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "genre", required = false) String genre,
            org.springframework.ui.Model model) {
        
        logger.info("Ahoy! Searchin' for movies with criteria - name: '{}', id: '{}', genre: '{}'", 
                   name, id, genre);
        
        try {
            // Validate ID parameter if provided
            if (id != null && id <= 0) {
                logger.warn("Arrr! Invalid movie ID provided: {}", id);
                model.addAttribute("title", "Search Error - Invalid ID");
                model.addAttribute("message", "Shiver me timbers! That ID be invalid, matey! Movie IDs must be greater than 0.");
                model.addAttribute("searchError", true);
                return "error";
            }
            
            List<Movie> searchResults = movieService.searchMovies(name, id, genre);
            
            // Add search results and criteria to model
            model.addAttribute("movies", searchResults);
            model.addAttribute("searchName", name != null ? name : "");
            model.addAttribute("searchId", id != null ? id.toString() : "");
            model.addAttribute("searchGenre", genre != null ? genre : "");
            model.addAttribute("isSearchResult", true);
            model.addAttribute("allGenres", movieService.getAllGenres());
            
            // Add pirate-themed messages based on results
            if (searchResults.isEmpty()) {
                if (isAllParametersEmpty(name, id, genre)) {
                    model.addAttribute("searchMessage", "Ahoy! Ready to search for treasure? Use the form below to find yer favorite movies!");
                } else {
                    model.addAttribute("searchMessage", "Blimey! No movies found matchin' yer search criteria. Try different terms, ye scallywag!");
                }
            } else {
                String resultMessage = String.format("Yo ho ho! Found %d movie%s matchin' yer search, matey!", 
                                                   searchResults.size(), 
                                                   searchResults.size() == 1 ? "" : "s");
                model.addAttribute("searchMessage", resultMessage);
            }
            
            logger.info("Search completed successfully. Found {} movies.", searchResults.size());
            return "movies";
            
        } catch (Exception e) {
            logger.error("Arrr! Error occurred during movie search: {}", e.getMessage(), e);
            model.addAttribute("title", "Search Error");
            model.addAttribute("message", "Batten down the hatches! An error occurred while searchin' for movies. Please try again, ye brave soul!");
            model.addAttribute("searchError", true);
            return "error";
        }
    }

    /**
     * JSON API endpoint for movie search - for programmatic access, savvy?
     * Returns search results as JSON for API consumers
     * 
     * @param name Movie name to search for (optional)
     * @param id Movie ID to search for (optional) 
     * @param genre Genre to filter by (optional)
     * @return ResponseEntity with search results or error message
     */
    @GetMapping("/api/movies/search")
    @ResponseBody
    public ResponseEntity<?> searchMoviesApi(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "genre", required = false) String genre) {
        
        logger.info("API search request - name: '{}', id: '{}', genre: '{}'", name, id, genre);
        
        try {
            // Validate ID parameter if provided
            if (id != null && id <= 0) {
                logger.warn("Invalid movie ID provided to API: {}", id);
                return ResponseEntity.badRequest()
                    .body(new SearchErrorResponse("Arrr! Invalid movie ID, matey! Must be greater than 0."));
            }
            
            List<Movie> searchResults = movieService.searchMovies(name, id, genre);
            
            SearchResponse response = new SearchResponse();
            response.setMovies(searchResults);
            response.setTotalResults(searchResults.size());
            response.setSearchCriteria(new SearchCriteria(name, id, genre));
            
            if (searchResults.isEmpty() && !isAllParametersEmpty(name, id, genre)) {
                response.setMessage("No movies found matchin' yer search criteria, ye landlubber!");
            } else if (!searchResults.isEmpty()) {
                response.setMessage(String.format("Found %d movie%s in our treasure chest!", 
                                                searchResults.size(), 
                                                searchResults.size() == 1 ? "" : "s"));
            } else {
                response.setMessage("All movies from our treasure chest!");
            }
            
            logger.info("API search completed successfully. Found {} movies.", searchResults.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error occurred during API movie search: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new SearchErrorResponse("Shiver me timbers! An error occurred while searchin'. Please try again!"));
        }
    }

    /**
     * Helper method to check if all search parameters are empty
     */
    private boolean isAllParametersEmpty(String name, Long id, String genre) {
        return (name == null || name.trim().isEmpty()) && 
               id == null && 
               (genre == null || genre.trim().isEmpty());
    }

    /**
     * Response class for API search results
     */
    public static class SearchResponse {
        private List<Movie> movies;
        private int totalResults;
        private String message;
        private SearchCriteria searchCriteria;

        // Getters and setters
        public List<Movie> getMovies() { return movies; }
        public void setMovies(List<Movie> movies) { this.movies = movies; }
        
        public int getTotalResults() { return totalResults; }
        public void setTotalResults(int totalResults) { this.totalResults = totalResults; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public SearchCriteria getSearchCriteria() { return searchCriteria; }
        public void setSearchCriteria(SearchCriteria searchCriteria) { this.searchCriteria = searchCriteria; }
    }

    /**
     * Search criteria class for API responses
     */
    public static class SearchCriteria {
        private String name;
        private Long id;
        private String genre;

        public SearchCriteria(String name, Long id, String genre) {
            this.name = name;
            this.id = id;
            this.genre = genre;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }
    }

    /**
     * Error response class for API errors
     */
    public static class SearchErrorResponse {
        private String error;
        private String timestamp;

        public SearchErrorResponse(String error) {
            this.error = error;
            this.timestamp = java.time.Instant.now().toString();
        }

        // Getters and setters
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}