package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import model.Movie;
import model.Showtime;
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
        Map<String, Map<Movie, List<Showtime>>> scheduleByDate = movieService.getSchedulesByDate("", null, null);
        
        request.setAttribute("featuredMovies", movieService.getFeaturedMovies());
        request.setAttribute("nowShowingMovies", movieService.getNowShowingMovies());
        request.setAttribute("comingSoonMovies", movieService.getComingSoonMovies());
        request.setAttribute("priceByMovie", movieService.getMinPriceByMovie(scheduleByDate));
        request.setAttribute("cinemas", cinemaService.getActiveCinemas());
        
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
