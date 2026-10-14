package com.realestate.model;

import java.util.*;

public final class Schema {
  private Schema() {}

  public record Field(String name, String type) {}

  public record Module(String key, String title, String prefix, List<Field> fields) {}

  public static final List<String> KEYS =
      List.of("users", "properties", "agents", "appointments", "offers", "reviews", "admins");

  public static Module module(String key) {
    return switch (key) {
      case "users" ->
          new Module(
              "users",
              "User",
              "U",
              List.of(
                  new Field("username", "text"),
                  new Field("password", "password"),
                  new Field("email", "email"),
                  new Field("phone", "tel"),
                  new Field("userType", "BUYER,SELLER,AGENT")));
      case "properties" ->
          new Module(
              "properties",
              "Property",
              "P",
              List.of(
                  new Field("title", "text"),
                  new Field("propertyType", "HOUSE,APARTMENT,LAND"),
                  new Field("listingType", "SALE,RENT"),
                  new Field("city", "text"),
                  new Field("address", "text"),
                  new Field("price", "number"),
                  new Field("bedrooms", "number"),
                  new Field("bathrooms", "number"),
                  new Field("areaSqft", "number"),
                  new Field("ownerId", "@users"),
                  new Field("agentId", "@agents"),
                  new Field("status", "AVAILABLE,SOLD,RENTED"),
                  new Field("extraValue", "number")));
      case "agents" ->
          new Module(
              "agents",
              "Agent",
              "A",
              List.of(
                  new Field("fullName", "text"),
                  new Field("email", "email"),
                  new Field("phone", "tel"),
                  new Field("licenseNo", "text"),
                  new Field("city", "text"),
                  new Field("experienceYears", "number"),
                  new Field("agentType", "FULLTIME,PARTTIME"),
                  new Field("commissionRate", "number")));
      case "appointments" ->
          new Module(
              "appointments",
              "Appointment",
              "AP",
              List.of(
                  new Field("propertyId", "@properties"),
                  new Field("buyerId", "@users"),
                  new Field("agentId", "@agents"),
                  new Field("viewDate", "date"),
                  new Field("viewTime", "time"),
                  new Field("viewType", "ONSITE,VIRTUAL"),
                  new Field("status", "PENDING,CONFIRMED,COMPLETED,REJECTED,CANCELLED"),
                  new Field("note", "text")));
      case "offers" ->
          new Module(
              "offers",
              "Offer",
              "O",
              List.of(
                  new Field("propertyId", "@properties"),
                  new Field("buyerId", "@users"),
                  new Field("offerType", "PURCHASE,RENTAL"),
                  new Field("offerAmount", "number"),
                  new Field("durationMonths", "number"),
                  new Field("offerDate", "date"),
                  new Field("status", "PENDING,ACCEPTED,REJECTED,COUNTERED")));
      case "reviews" ->
          new Module(
              "reviews",
              "Review",
              "R",
              List.of(
                  new Field("reviewerId", "@users"),
                  new Field("targetId", "text"),
                  new Field("reviewType", "PROPERTY,AGENT"),
                  new Field("rating", "number"),
                  new Field("comment", "textarea"),
                  new Field("reviewDate", "date")));
      case "admins" ->
          new Module(
              "admins",
              "Admin",
              "AD",
              List.of(
                  new Field("username", "text"),
                  new Field("password", "password"),
                  new Field("email", "email"),
                  new Field("adminLevel", "SUPER,CONTENT")));
      default -> throw new IllegalArgumentException("Unknown module");
    };
  }

  public static Entity create(String key, String id, Map<String, String> fields) {
    return switch (key) {
      case "users" ->
          switch (fields.getOrDefault("userType", "")) {
            case "BUYER" -> new BuyerUser(id, fields);
            case "SELLER" -> new SellerUser(id, fields);
            case "AGENT" -> new AgentUser(id, fields);
            default -> throw new IllegalArgumentException("Invalid userType");
          };
      case "properties" ->
          switch (fields.getOrDefault("propertyType", "")) {
            case "HOUSE" -> new House(id, fields);
            case "APARTMENT" -> new Apartment(id, fields);
            case "LAND" -> new Land(id, fields);
            default -> throw new IllegalArgumentException("Invalid propertyType");
          };
      case "agents" ->
          switch (fields.getOrDefault("agentType", "")) {
            case "FULLTIME" -> new FullTimeAgent(id, fields);
            case "PARTTIME" -> new PartTimeAgent(id, fields);
            default -> throw new IllegalArgumentException("Invalid agentType");
          };
      case "appointments" ->
          switch (fields.getOrDefault("viewType", "")) {
            case "ONSITE" -> new OnsiteViewing(id, fields);
            case "VIRTUAL" -> new VirtualViewing(id, fields);
            default -> throw new IllegalArgumentException("Invalid viewType");
          };
      case "offers" ->
          switch (fields.getOrDefault("offerType", "")) {
            case "PURCHASE" -> new PurchaseOffer(id, fields);
            case "RENTAL" -> new RentalOffer(id, fields);
            default -> throw new IllegalArgumentException("Invalid offerType");
          };
      case "reviews" ->
          switch (fields.getOrDefault("reviewType", "")) {
            case "PROPERTY" -> new PropertyReview(id, fields);
            case "AGENT" -> new AgentReview(id, fields);
            default -> throw new IllegalArgumentException("Invalid reviewType");
          };
      case "admins" ->
          switch (fields.getOrDefault("adminLevel", "")) {
            case "SUPER" -> new SuperAdmin(id, fields);
            case "CONTENT" -> new ContentAdmin(id, fields);
            default -> throw new IllegalArgumentException("Invalid adminLevel");
          };
      default -> throw new IllegalArgumentException("Unknown module");
    };
  }
}
