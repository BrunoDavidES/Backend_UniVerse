package models;

public class FeedbackData {
    private int rating;

    public FeedbackData() {}

    public boolean validate() {
        return rating >= 1 && rating <= 5;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}