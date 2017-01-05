import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;


public class Series {
    private String name;
    private String overview;
    private String picture;
    private String imdb;
    private String slug;
    private String tmdb;
    private String trakt;
    private String tvdb;
    private String tvrage;
    private double rating;
    private int year;
    private int runtime;

    public Series(Resource origin, Model model) {
        name = origin.getProperty(model.getProperty("series:name")).getString();
        overview = origin.getProperty(model.getProperty("series:overview")).getString();
        picture = origin.getProperty(model.getProperty("series:picture")).getString();
        imdb = origin.getProperty(model.getProperty("series:imdb")).getString();
        slug = origin.getProperty(model.getProperty("series:slug")).getString();
        tmdb = origin.getProperty(model.getProperty("series:tmdb")).getString();
        trakt = origin.getProperty(model.getProperty("series:trakt")).getString();
        tvdb = origin.getProperty(model.getProperty("series:tvdb")).getString();
        tvrage = origin.getProperty(model.getProperty("series:tvrage")).getString();
        rating = origin.getProperty(model.getProperty("series:rating")).getDouble();
        year = origin.getProperty(model.getProperty("series:year")).getInt();
        runtime = origin.getProperty(model.getProperty("series:runtime")).getInt();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTmdb() {
        return tmdb;
    }

    public void setTmdb(String tmdb) {
        this.tmdb = tmdb;
    }

    public String getTrakt() {
        return trakt;
    }

    public void setTrakt(String trakt) {
        this.trakt = trakt;
    }

    public String getTvdb() {
        return tvdb;
    }

    public void setTvdb(String tvdb) {
        this.tvdb = tvdb;
    }

    public String getTvrage() {
        return tvrage;
    }

    public void setTvrage(String tvrage) {
        this.tvrage = tvrage;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }
}
