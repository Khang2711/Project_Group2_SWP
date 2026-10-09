package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Movie;
import service.CinemaService;
import service.MovieService;

@WebServlet("/home")
public class HomeController extends HttpServlet {

    private MovieService movieService = new MovieService();
    private CinemaService cinemaService = new CinemaService();

//    @Override
//    public void init() {
//        movieService = new MovieService();
//    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Movie> nowShowingMovies = movieService.getNowShowingMovies();
        List<Movie> comingSoonMovies = movieService.getComingSoonMovies();
        List<Movie> featuredMovies = movieService.getFeaturedMovies();

        List<Movie> homeMovies = new ArrayList<>();
        homeMovies.addAll(nowShowingMovies);
        homeMovies.addAll(comingSoonMovies);

        request.setAttribute("featuredMovies", featuredMovies);
        request.setAttribute("nowShowingMovies", nowShowingMovies);
        request.setAttribute("comingSoonMovies", comingSoonMovies);
        request.setAttribute("genresByMovie", movieService.getGenresByMovie(homeMovies));
        request.setAttribute("cinemas", cinemaService.getActiveCinemas());

        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
