package com.realestate.dao;
import com.realestate.model.Offer;
public final class OfferDAO extends SqlDAO<Offer> {
  public OfferDAO() { super("offers"); }
}
