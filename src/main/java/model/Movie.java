package model;

public class Movie {
    private int ageRating;
    private String screeningStart,screeningEnd;
    public int getAgeRating(){return ageRating;}public void setAgeRating(int a){ageRating=a;}
    public String getScreeningStart(){return screeningStart;}public void setScreeningStart(String s){screeningStart=s;}
    public String getScreeningEnd(){return screeningEnd;}public void setScreeningEnd(String s){screeningEnd=s;}


    private String movieID;
    private String title;
    private String description;
    private int duration;
    private float basePrice;
    private boolean status;
    private String imageURL;

    public Movie() {
    }

    public Movie(String movieID, String title, String description, int duration,
            float basePrice, boolean status, String imageURL) {
        this.movieID = movieID;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.basePrice = basePrice;
        this.status = status;
        this.imageURL = imageURL;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(float basePrice) {
        this.basePrice = basePrice;
    }

    public boolean isStatus() {
        return status;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
