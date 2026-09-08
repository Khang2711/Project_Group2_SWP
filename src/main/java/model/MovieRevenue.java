package model;

public class MovieRevenue {

    private String movieID;
    private String movieTitle;
    private int orderCount;
    private int ticketCount;
    private float revenue;

    public MovieRevenue() {
    }

    public MovieRevenue(String movieID, String movieTitle, int orderCount, int ticketCount, float revenue) {
        this.movieID = movieID;
        this.movieTitle = movieTitle;
        this.orderCount = orderCount;
        this.ticketCount = ticketCount;
        this.revenue = revenue;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(int ticketCount) {
        this.ticketCount = ticketCount;
    }

    public float getRevenue() {
        return revenue;
    }

    public void setRevenue(float revenue) {
        this.revenue = revenue;
    }
}
