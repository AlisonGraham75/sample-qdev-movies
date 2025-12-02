package com.amazonaws.samples.qdevmovies.movies;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

@Service
public class MovieService {
    private static final Logger logger = LogManager.getLogger(MovieService.class);
    private final List<Movie> movies;
    private final Map<Long, Movie> movieMap;

    public MovieService() {
        this.movies = loadMoviesFromJson();
        this.movieMap = new HashMap<>();
        for (Movie movie : movies) {
            movieMap.put(movie.getId(), movie);
        }
    }

    private List<Movie> loadMoviesFromJson() {
        List<Movie> movieList = new ArrayList<>();
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("movies.json");
            if (inputStream != null) {
                Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name());
                String jsonContent = scanner.useDelimiter("\\A").next();
                scanner.close();
                
                JSONArray moviesArray = new JSONArray(jsonContent);
                for (int i = 0; i < moviesArray.length(); i++) {
                    JSONObject movieObj = moviesArray.getJSONObject(i);
                    movieList.add(new Movie(
                        movieObj.getLong("id"),
                        movieObj.getString("movieName"),
                        movieObj.getString("director"),
                        movieObj.getInt("year"),
                        movieObj.getString("genre"),
                        movieObj.getString("description"),
                        movieObj.getInt("duration"),
                        movieObj.getDouble("imdbRating")
                    ));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load movies from JSON: {}", e.getMessage());
        }
        return movieList;
    }

    public List<Movie> getAllMovies() {
        return movies;
    }

    public Optional<Movie> getMovieById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(movieMap.get(id));
    }

    /**
     * Searches for movies based on the provided criteria with pirate flair!
     * Arrr! This method be searchin' through our treasure chest of movies, matey!
     * 
     * @param name Movie name to search for (case-insensitive partial matching)
     * @param id Specific movie ID to find
     * @param genre Genre to filter by (case-insensitive partial matching)
     * @return List of movies matching the search criteria
     */
    public List<Movie> searchMovies(String name, Long id, String genre) {
        logger.info("Ahoy! Searchin' for movies with criteria - name: '{}', id: '{}', genre: '{}'", 
                   name, id, genre);
        
        List<Movie> results = new ArrayList<>();
        
        // If all parameters be null or empty, return all movies, ye landlubber!
        if (isEmptyString(name) && id == null && isEmptyString(genre)) {
            logger.info("Arrr! No search criteria provided, returnin' all movies from the treasure chest!");
            return new ArrayList<>(movies);
        }
        
        for (Movie movie : movies) {
            boolean matches = true;
            
            // Check name criteria, case-insensitive like a true pirate!
            if (!isEmptyString(name)) {
                matches = matches && movie.getMovieName().toLowerCase().contains(name.toLowerCase());
            }
            
            // Check ID criteria - exact match, no room for error on the high seas!
            if (id != null) {
                matches = matches && movie.getId() == id.longValue();
            }
            
            // Check genre criteria, case-insensitive partial matching, savvy?
            if (!isEmptyString(genre)) {
                matches = matches && movie.getGenre().toLowerCase().contains(genre.toLowerCase());
            }
            
            if (matches) {
                results.add(movie);
            }
        }
        
        logger.info("Shiver me timbers! Found {} movies matchin' yer search criteria!", results.size());
        return results;
    }

    /**
     * Searches movies by name only - for when ye only remember the title, matey!
     * 
     * @param name Movie name to search for (case-insensitive partial matching)
     * @return List of movies with names containing the search term
     */
    public List<Movie> searchMoviesByName(String name) {
        if (isEmptyString(name)) {
            logger.warn("Arrr! Empty name provided for search, returnin' empty results!");
            return new ArrayList<>();
        }
        
        logger.info("Searchin' for movies with name containin': '{}'", name);
        return movies.stream()
                .filter(movie -> movie.getMovieName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Searches movies by genre only - for when ye want a specific type of adventure!
     * 
     * @param genre Genre to filter by (case-insensitive partial matching)
     * @return List of movies matching the genre criteria
     */
    public List<Movie> searchMoviesByGenre(String genre) {
        if (isEmptyString(genre)) {
            logger.warn("Arrr! Empty genre provided for search, returnin' empty results!");
            return new ArrayList<>();
        }
        
        logger.info("Searchin' for movies with genre containin': '{}'", genre);
        return movies.stream()
                .filter(movie -> movie.getGenre().toLowerCase().contains(genre.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Gets all unique genres from our movie treasure chest
     * 
     * @return List of unique genres available
     */
    public List<String> getAllGenres() {
        return movies.stream()
                .map(Movie::getGenre)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Helper method to check if a string be empty or null, like a ghost ship!
     * 
     * @param str String to check
     * @return true if string is null, empty, or only whitespace
     */
    private boolean isEmptyString(String str) {
        return str == null || str.trim().isEmpty();
    }
}
