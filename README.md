# Movie Service - Spring Boot Demo Application 🏴‍☠️

A simple movie catalog web application built with Spring Boot, demonstrating Java application development best practices with a pirate-themed search feature!

## Features

- **Movie Catalog**: Browse 12 classic movies with detailed information
- **🔍 Pirate-Themed Movie Search**: Search for movies by name, ID, or genre with swashbuckling flair!
- **Movie Details**: View comprehensive information including director, year, genre, duration, and description
- **Customer Reviews**: Each movie includes authentic customer reviews with ratings and avatars
- **Responsive Design**: Mobile-first design that works on all devices
- **Modern UI**: Dark theme with gradient backgrounds and smooth animations
- **REST API**: JSON API endpoints for programmatic access

## Technology Stack

- **Java 8**
- **Spring Boot 2.7.18**
- **Maven** for dependency management
- **Thymeleaf** for templating
- **Log4j 2.20.0**
- **JUnit 5.8.2**

## Quick Start

### Prerequisites

- Java 8 or higher
- Maven 3.6+

### Run the Application

```bash
git clone https://github.com/<youruser>/sample-qdev-movies.git
cd sample-qdev-movies
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Access the Application

- **Movie List**: http://localhost:8080/movies
- **Movie Details**: http://localhost:8080/movies/{id}/details (where {id} is 1-12)
- **🏴‍☠️ Movie Search**: Use the search form on the movies page or access directly via `/movies/search`

## Building for Production

```bash
mvn clean package
java -jar target/sample-qdev-movies-0.1.0.jar
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/amazonaws/samples/qdevmovies/
│   │       ├── movies/
│   │       │   ├── MoviesApplication.java    # Main Spring Boot application
│   │       │   ├── MoviesController.java     # REST controller with search endpoints
│   │       │   ├── MovieService.java         # Business logic with pirate-themed search
│   │       │   ├── Movie.java                # Movie data model
│   │       │   ├── Review.java               # Review data model
│   │       │   └── ReviewService.java        # Review business logic
│   │       └── utils/
│   │           ├── MovieIconUtils.java       # Movie icon utilities
│   │           └── MovieUtils.java           # Movie validation utilities
│   └── resources/
│       ├── templates/
│       │   ├── movies.html                   # Enhanced with pirate search form
│       │   └── movie-details.html            # Movie details template
│       ├── static/css/                       # Stylesheets
│       ├── application.yml                   # Application configuration
│       ├── movies.json                       # Movie data
│       ├── mock-reviews.json                 # Mock review data
│       └── log4j2.xml                        # Logging configuration
└── test/                                     # Comprehensive unit tests
    └── java/
        └── com/amazonaws/samples/qdevmovies/movies/
            ├── MoviesControllerTest.java     # Controller tests with search functionality
            ├── MovieServiceSearchTest.java   # Dedicated search functionality tests
            └── MovieTest.java                # Basic movie model tests
```

## API Endpoints

### Get All Movies
```
GET /movies
```
Returns an HTML page displaying all movies with ratings, basic information, and a pirate-themed search form.

### 🏴‍☠️ Search Movies (HTML Interface)
```
GET /movies/search?name={name}&id={id}&genre={genre}
```
Returns an HTML page with search results using the pirate-themed interface.

**Parameters (all optional):**
- `name` (query parameter): Movie name to search for (case-insensitive, partial matching)
- `id` (query parameter): Specific movie ID to find (exact match)
- `genre` (query parameter): Genre to filter by (case-insensitive, partial matching)

**Examples:**
```
# Search by movie name
http://localhost:8080/movies/search?name=Prison

# Search by genre
http://localhost:8080/movies/search?genre=Drama

# Search by ID
http://localhost:8080/movies/search?id=1

# Combined search
http://localhost:8080/movies/search?name=Hero&genre=Action

# Empty search (returns all movies)
http://localhost:8080/movies/search
```

### 🏴‍☠️ Search Movies (JSON API)
```
GET /api/movies/search?name={name}&id={id}&genre={genre}
```
Returns JSON response with search results for programmatic access.

**Response Format:**
```json
{
  "movies": [
    {
      "id": 1,
      "movieName": "The Prison Escape",
      "director": "John Director",
      "year": 1994,
      "genre": "Drama",
      "description": "Two imprisoned men bond over a number of years...",
      "duration": 142,
      "imdbRating": 5.0,
      "icon": "🎬"
    }
  ],
  "totalResults": 1,
  "message": "Found 1 movie in our treasure chest!",
  "searchCriteria": {
    "name": "Prison",
    "id": null,
    "genre": null
  }
}
```

**Error Response:**
```json
{
  "error": "Arrr! Invalid movie ID, matey! Must be greater than 0.",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Get Movie Details
```
GET /movies/{id}/details
```
Returns an HTML page with detailed movie information and customer reviews.

**Parameters:**
- `id` (path parameter): Movie ID (1-12)

**Example:**
```
http://localhost:8080/movies/1/details
```

## 🏴‍☠️ Pirate-Themed Search Features

### Search Capabilities
- **Case-insensitive search**: Search for "PRISON" or "prison" - both work!
- **Partial matching**: Search "Hero" to find "The Masked Hero"
- **Multiple criteria**: Combine name, ID, and genre filters
- **Genre filtering**: Search "Crime" to find all crime-related movies
- **Empty search handling**: Returns all movies when no criteria provided

### Pirate Language Integration
- **Search messages**: "Yo ho ho! Found 2 movies matchin' yer search, matey!"
- **Error messages**: "Shiver me timbers! That ID be invalid, matey!"
- **Empty results**: "Blimey! No movies found matchin' yer search criteria. Try different terms, ye scallywag!"
- **Logging**: Pirate-themed log messages for debugging

### User Interface
- **Treasure chest design**: Search form styled like a pirate treasure chest
- **Pirate emojis**: 🏴‍☠️, ⚓, 🎬, 🆔, 🎭 throughout the interface
- **Responsive design**: Works on desktop, tablet, and mobile devices
- **Visual feedback**: Different message styles for success, info, and warnings

## Testing

Run the comprehensive test suite:

```bash
# Run all tests
mvn test

# Run specific test classes
mvn test -Dtest=MovieServiceSearchTest
mvn test -Dtest=MoviesControllerTest

# Run tests with coverage
mvn test jacoco:report
```

### Test Coverage
- **MovieService**: Complete search functionality testing
- **MoviesController**: HTML and API endpoint testing
- **Edge cases**: Invalid parameters, empty results, performance testing
- **Pirate language**: Verification of themed messages and responses

## Troubleshooting

### Port 8080 already in use

Run on a different port:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Build failures

Clean and rebuild:
```bash
mvn clean compile
```

### Search not working

1. Check that the application started successfully
2. Verify the movies.json file is loaded (check logs for "Ahoy! Searchin' for movies...")
3. Try the API endpoint directly: `/api/movies/search?name=Prison`

## Contributing

This project is designed as a demonstration application. Feel free to:
- Add more movies to the catalog
- Enhance the UI/UX with more pirate themes
- Add new search features (director search, year range filtering)
- Improve the responsive design
- Add more pirate language elements

### Adding New Movies

Edit `src/main/resources/movies.json` and add new movie objects:

```json
{
  "id": 13,
  "movieName": "Your Movie Title",
  "director": "Director Name",
  "year": 2024,
  "genre": "Your Genre",
  "description": "Movie description...",
  "duration": 120,
  "imdbRating": 4.5
}
```

## License

This sample code is licensed under the MIT-0 License. See the LICENSE file.

---

*Ahoy matey! May yer movie searches be fruitful and yer code be bug-free! 🏴‍☠️*
