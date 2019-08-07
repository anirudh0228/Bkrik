package com.starkcorp.bkrik;

public class Reviews {
    private String UserName;
    private String Review;
    private String ratings;
    public Reviews(String userName, String review,String Ratings) {
        UserName = userName;
        Review = review;
        ratings=Ratings;
    }

    public String getRatings() {
        return ratings;
    }

    public void setRatings(String ratings) {
        this.ratings = ratings;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }


    public String getReview() {
        return Review;
    }

    public void setReview(String review) {
        Review = review;
    }
}
