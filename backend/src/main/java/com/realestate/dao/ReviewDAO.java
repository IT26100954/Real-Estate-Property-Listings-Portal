package com.realestate.dao;
import com.realestate.model.Review;
public final class ReviewDAO extends SqlDAO<Review> {
  public ReviewDAO() { super("reviews"); }
}
