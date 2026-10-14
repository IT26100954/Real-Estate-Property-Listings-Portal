package com.realestate.service;

import com.realestate.dao.*;
import com.realestate.model.*;
import com.realestate.util.Passwords;
import java.io.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Business rules and authorization live here, never in React components. */
public final class PortalService {
  public static final Object LOCK = new Object();
  private final ActivityLogDAO logs = new ActivityLogDAO();

  public SqlDAO<Entity> dao(String key) {
    return switch (key) {
      case "users" -> asEntityRepository(new UserDAO());
      case "properties" -> asEntityRepository(new PropertyDAO());
      case "agents" -> asEntityRepository(new AgentDAO());
      case "appointments" -> asEntityRepository(new AppointmentDAO());
      case "offers" -> asEntityRepository(new OfferDAO());
      case "reviews" -> asEntityRepository(new ReviewDAO());
      case "admins" -> asEntityRepository(new AdminDAO());
      default -> throw new IllegalArgumentException("Unknown module");
    };
  }

  /** The module schema guarantees that writes use the matching concrete model family. */
  @SuppressWarnings("unchecked")
  private static <T extends Entity> SqlDAO<Entity> asEntityRepository(SqlDAO<T> repository) {
    return (SqlDAO<Entity>) (SqlDAO<?>) repository;
  }

  private boolean superAdmin(Entity actor) {
    return actor instanceof SuperAdmin;
  }

  private boolean contentAdmin(Entity actor) {
    return actor instanceof ContentAdmin;
  }

  private boolean buyer(Entity actor) {
    return actor instanceof BuyerUser;
  }

  private boolean seller(Entity actor) {
    return actor instanceof SellerUser;
  }

  private boolean same(Entity actor, String id) {
    return actor != null && actor.getId().equals(id);
  }

  public Entity fresh(Entity actor) throws IOException {
    if (actor == null) return null;
    return dao(actor instanceof Admin ? "admins" : "users").findById(actor.getId()).orElse(null);
  }

  public Entity login(String username, String password) throws IOException {
    if (username == null || password == null) return null;
    synchronized (LOCK) {
      for (String key : List.of("users", "admins"))
        for (Entity e : dao(key).findAll())
          if (e.get("username").equalsIgnoreCase(username)
              && Passwords.verify(password, e.get("password"))) return e;
      return null;
    }
  }

  public String agentId(Entity actor) throws IOException {
    if (!(actor instanceof AgentUser)) return "";
    return dao("agents").findAll().stream()
        .filter(a -> a.get("email").equalsIgnoreCase(actor.get("email")))
        .map(Entity::getId)
        .findFirst()
        .orElse("");
  }

  private boolean ownsProperty(Entity actor, String propertyId) throws IOException {
    Entity p = dao("properties").findById(propertyId).orElse(null);
    return p != null && same(actor, p.get("ownerId"));
  }

  public boolean canCreate(String key, Entity actor) {
    if (key.equals("users") && actor == null) return true;
    if (superAdmin(actor)) return true;
    return switch (key) {
      case "properties" -> seller(actor) || actor instanceof AgentUser || contentAdmin(actor);
      case "appointments", "offers" -> buyer(actor);
      case "reviews" -> buyer(actor) || seller(actor) || actor instanceof AgentUser;
      default -> false;
    };
  }

  public boolean canRead(String key, Entity e, Entity actor) throws IOException {
    if (List.of("properties", "agents", "reviews").contains(key)) return true;
    if (superAdmin(actor) || contentAdmin(actor)) return !key.equals("admins") || superAdmin(actor);
    return switch (key) {
      case "users" -> same(actor, e.getId());
      case "appointments" ->
          same(actor, e.get("buyerId"))
              || (!agentId(actor).isBlank() && agentId(actor).equals(e.get("agentId")));
      case "offers" -> same(actor, e.get("buyerId")) || ownsProperty(actor, e.get("propertyId"));
      default -> false;
    };
  }

  public boolean canEdit(String key, Entity e, Entity actor) throws IOException {
    if (superAdmin(actor)) return true;
    if (actor == null) return false;
    return switch (key) {
      case "users" -> same(actor, e.getId());
      case "properties" ->
          contentAdmin(actor)
              || same(actor, e.get("ownerId"))
              || (!agentId(actor).isBlank() && agentId(actor).equals(e.get("agentId")));
      case "appointments" ->
          same(actor, e.get("buyerId"))
              || (!agentId(actor).isBlank() && agentId(actor).equals(e.get("agentId")));
      case "offers" -> same(actor, e.get("buyerId")) || ownsProperty(actor, e.get("propertyId"));
      case "reviews" -> same(actor, e.get("reviewerId"));
      default -> false;
    };
  }

  public boolean canDelete(String key, Entity e, Entity actor) throws IOException {
    if (contentAdmin(actor)) return Set.of("properties", "reviews").contains(key);
    if (key.equals("offers") && !superAdmin(actor))
      return same(actor, e.get("buyerId"))
          && Set.of("PENDING", "COUNTERED").contains(e.get("status"));
    return canEdit(key, e, actor);
  }

  public List<Entity> list(String key, Entity actor, String query) throws IOException {
    synchronized (LOCK) {
      List<Entity> result = new ArrayList<>();
      for (Entity e : dao(key).findAll())
        if (canRead(key, e, actor)
            && (query == null
                || query.isBlank()
                || searchable(e).contains(query.toLowerCase(Locale.ROOT)))) result.add(e);
      return result;
    }
  }

  private String searchable(Entity e) {
    StringBuilder s = new StringBuilder(e.getId());
    e.fields()
        .forEach(
            (k, v) -> {
              if (!k.equals("password")) s.append(' ').append(v);
            });
    return s.toString().toLowerCase(Locale.ROOT);
  }

  public List<Entity> searchProperties(
      Entity actor, String query, String city, String type, String minPrice, String maxPrice)
      throws IOException {
    BigDecimal min = filterAmount(minPrice), max = filterAmount(maxPrice);
    check(
        min == null || max == null || min.compareTo(max) <= 0,
        "Minimum price exceeds maximum price");
    return list("properties", actor, query).stream()
        .filter(
            e ->
                city == null
                    || city.isBlank()
                    || e.get("city")
                        .toLowerCase(Locale.ROOT)
                        .contains(city.toLowerCase(Locale.ROOT)))
        .filter(e -> type == null || type.isBlank() || e.get("propertyType").equals(type))
        .filter(e -> min == null || new BigDecimal(e.get("price")).compareTo(min) >= 0)
        .filter(e -> max == null || new BigDecimal(e.get("price")).compareTo(max) <= 0)
        .toList();
  }

  private BigDecimal filterAmount(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      BigDecimal amount = new BigDecimal(value);
      check(amount.signum() >= 0, "Price filters cannot be negative");
      return amount;
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid price filter");
    }
  }

  private Entity require(String key, String id) throws IOException {
    return dao(key)
        .findById(id)
        .orElseThrow(
            () -> new IllegalArgumentException("Referenced " + key + " record does not exist"));
  }

  private void check(boolean ok, String message) {
    if (!ok) throw new IllegalArgumentException(message);
  }

  private BigDecimal amount(Map<String, String> d, String field) {
    try {
      BigDecimal value = new BigDecimal(d.get(field));
      check(
          value.signum() >= 0 && value.compareTo(new BigDecimal("1000000000000")) <= 0,
          "Invalid " + field);
      return value;
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid number: " + field);
    }
  }

  public Entity save(String key, String id, Map<String, String> input, Entity actor)
      throws IOException {
    synchronized (LOCK) {
      Entity old = id == null || id.isBlank() ? null : require(key, id);
      boolean creating = old == null;
      check(
          creating ? canCreate(key, actor) : canEdit(key, old, actor),
          "You do not have permission for this action");
      Map<String, String> d = new LinkedHashMap<>();
      for (var f : Schema.module(key).fields()) {
        String v = input.getOrDefault(f.name(), "").trim();
        check(v.length() <= 2000, "Field is too long: " + f.name());
        if (f.type().equals("password") && v.isBlank() && old != null) v = old.get(f.name());
        check(!v.isBlank() || f.name().equals("note"), "Required: " + f.name());
        d.put(f.name(), v);
      }
      if (!superAdmin(actor)) {
        switch (key) {
          case "users" -> {
            if (old != null) d.put("userType", old.get("userType"));
            else
              check(
                  Set.of("BUYER", "SELLER").contains(d.get("userType")),
                  "Public registration supports buyers and sellers only");
          }
          case "properties" -> {
            if (seller(actor)) d.put("ownerId", actor.getId());
            if (actor instanceof AgentUser) {
              d.put("agentId", agentId(actor));
              if (old != null) d.put("ownerId", old.get("ownerId"));
            }
          }
          case "appointments" -> {
            if (buyer(actor)) {
              d.put("buyerId", actor.getId());
              d.put(
                  "status",
                  creating
                      ? "PENDING"
                      : Set.of("PENDING", "CANCELLED").contains(d.get("status"))
                          ? d.get("status")
                          : "PENDING");
              if (old != null)
                check(
                    !Set.of("COMPLETED", "REJECTED", "CANCELLED").contains(old.get("status")),
                    "This appointment is closed");
            } else {
              for (String f : d.keySet())
                if (!f.equals("status") && !f.equals("note")) d.put(f, old.get(f));
              check(
                  Set.of("CONFIRMED", "COMPLETED", "REJECTED", "CANCELLED")
                      .contains(d.get("status")),
                  "Agent must confirm, complete, reject or cancel");
            }
          }
          case "offers" -> {
            if (buyer(actor)) {
              d.put("buyerId", actor.getId());
              if (old != null)
                check(
                    Set.of("PENDING", "COUNTERED").contains(old.get("status")),
                    "Only pending or countered offers can be revised");
              d.put("status", "PENDING");
            } else {
              check(
                  old != null && Set.of("PENDING", "COUNTERED").contains(old.get("status")),
                  "Offer is already closed");
              for (String f : d.keySet())
                if (!Set.of("status", "offerAmount").contains(f)) d.put(f, old.get(f));
              check(
                  Set.of("ACCEPTED", "REJECTED", "COUNTERED").contains(d.get("status")),
                  "Choose an offer decision");
              if (!d.get("status").equals("COUNTERED"))
                d.put("offerAmount", old.get("offerAmount"));
            }
          }
          case "reviews" -> {
            d.put("reviewerId", actor.getId());
            if (old != null) {
              d.put("targetId", old.get("targetId"));
              d.put("reviewType", old.get("reviewType"));
            }
          }
          default -> {}
        }
      }
      for (var f : Schema.module(key).fields()) {
        String v = d.get(f.name()), t = f.type();
        if (t.contains(","))
          check(Arrays.asList(t.split(",")).contains(v), "Invalid option: " + f.name());
        if (t.startsWith("@")) require(t.substring(1), v);
        if (t.equals("email"))
          check(v.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"), "Enter a valid email");
        if (t.equals("tel")) check(v.matches("[+0-9 ()-]{7,20}"), "Enter a valid phone number");
        if (t.equals("number")) amount(d, f.name());
        if (t.equals("date"))
          try {
            d.put(f.name(), LocalDate.parse(v).toString());
          } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid date");
          }
        if (t.equals("time"))
          try {
            d.put(f.name(), LocalTime.parse(v).toString());
          } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid time");
          }
      }
      for (String integer :
          List.of("bedrooms", "bathrooms", "experienceYears", "durationMonths", "rating"))
        if (d.containsKey(integer)) {
          check(
              amount(d, integer).stripTrailingZeros().scale() <= 0,
              "Whole number required: " + integer);
          d.put(integer, amount(d, integer).toBigIntegerExact().toString());
        }
      if (key.equals("users") || key.equals("admins")) {
        check(
            d.get("username").matches("[A-Za-z0-9_.-]{3,40}"),
            "Username must contain 3–40 letters, numbers, dots, underscores or hyphens");
        for (String accounts : List.of("users", "admins"))
          for (Entity e : dao(accounts).findAll())
            if (old == null || !accounts.equals(key) || !e.getId().equals(id))
              check(
                  !e.get("username").equalsIgnoreCase(d.get("username")),
                  "Username is already used");
        if (creating || !input.getOrDefault("password", "").isBlank()) {
          String password = input.get("password");
          check(
              password != null && password.length() >= 8 && password.length() <= 128,
              "Password must contain 8–128 characters");
          d.put("password", Passwords.hash(password));
        }
        if (key.equals("users") && d.get("userType").equals("AGENT"))
          check(
              dao("agents").findAll().stream()
                  .anyMatch(e -> e.get("email").equalsIgnoreCase(d.get("email"))),
              "Create an agent profile with matching email first");
      }
      if (key.equals("users") && old != null && !d.get("userType").equals(old.get("userType"))) {
        for (Entity p : dao("properties").findAll())
          check(
              !p.get("ownerId").equals(id), "Reassign owned properties before changing user role");
        for (String linked : List.of("appointments", "offers"))
          for (Entity r : dao(linked).findAll())
            check(
                !r.get("buyerId").equals(id),
                "Remove buyer transactions before changing user role");
      }
      if (key.equals("agents"))
        for (Entity e : dao(key).findAll())
          if (old == null || !e.getId().equals(id)) {
            check(!e.get("email").equalsIgnoreCase(d.get("email")), "Agent email already exists");
            check(
                !e.get("licenseNo").equalsIgnoreCase(d.get("licenseNo")), "License already exists");
          }
      if (key.equals("agents")) {
        check(
            amount(d, "commissionRate").compareTo(new BigDecimal("10")) <= 0,
            "Commission rate must be between 0 and 10%");
      }
      if (key.equals("properties")) {
        check(
            require("users", d.get("ownerId")) instanceof SellerUser,
            "Property owner must be a seller");
        check(
            amount(d, "price").signum() > 0
                && amount(d, "areaSqft").signum() > 0
                && amount(d, "extraValue").signum() > 0,
            "Price, area and extra value must be positive");
      }
      if (key.equals("appointments") || key.equals("offers")) {
        Entity p = require("properties", d.get("propertyId"));
        check(require("users", d.get("buyerId")) instanceof BuyerUser, "Buyer account is required");
        if (creating || !old.get("propertyId").equals(d.get("propertyId")))
          check(p.get("status").equals("AVAILABLE"), "Property is not available");
        if (key.equals("appointments")) {
          if (old != null && !old.get("status").equals(d.get("status"))) {
            Set<String> allowed =
                switch (old.get("status")) {
                  case "PENDING" -> Set.of("CONFIRMED", "REJECTED", "CANCELLED");
                  case "CONFIRMED" ->
                      buyer(actor)
                          ? Set.of("PENDING", "CANCELLED")
                          : Set.of("COMPLETED", "CANCELLED");
                  default -> Set.of();
                };
            check(allowed.contains(d.get("status")), "Invalid appointment status transition");
          }
          check(p.get("agentId").equals(d.get("agentId")), "Select the property's assigned agent");
          boolean active = Set.of("PENDING", "CONFIRMED").contains(d.get("status"));
          if (active) {
            check(
                LocalDateTime.of(
                        LocalDate.parse(d.get("viewDate")), LocalTime.parse(d.get("viewTime")))
                    .isAfter(LocalDateTime.now()),
                "Viewing must be in the future");
            for (Entity e : dao(key).findAll())
              if ((old == null || !e.getId().equals(id))
                  && Set.of("PENDING", "CONFIRMED").contains(e.get("status"))
                  && e.get("viewDate").equals(d.get("viewDate"))
                  && e.get("viewTime").equals(d.get("viewTime")))
                check(
                    !e.get("agentId").equals(d.get("agentId"))
                        && !e.get("buyerId").equals(d.get("buyerId")),
                    "Agent or buyer already has a viewing at this time");
          }
        } else {
          if (old != null)
            check(
                Set.of("PENDING", "COUNTERED").contains(old.get("status")),
                "Closed offers cannot be changed");
          check(amount(d, "offerAmount").signum() > 0, "Offer must be positive");
          check(
              p.get("listingType").equals(d.get("offerType").equals("RENTAL") ? "RENT" : "SALE"),
              "Offer type must match listing type");
          check(
              d.get("offerType").equals("RENTAL")
                  ? amount(d, "durationMonths").signum() > 0
                  : amount(d, "durationMonths").signum() == 0,
              "Rental needs a duration; purchase duration must be zero");
          check(
              !LocalDate.parse(d.get("offerDate")).isAfter(LocalDate.now()),
              "Offer date cannot be in the future");
        }
      }
      if (key.equals("reviews")) {
        check(
            d.get("comment").length() <= 300,
            "Review comments must contain at most 300 characters");
        require("users", d.get("reviewerId"));
        require(
            d.get("reviewType").equals("PROPERTY") ? "properties" : "agents", d.get("targetId"));
        check(
            amount(d, "rating").intValue() >= 1 && amount(d, "rating").intValue() <= 5,
            "Rating must be 1–5");
        check(
            !LocalDate.parse(d.get("reviewDate")).isAfter(LocalDate.now()),
            "Review date cannot be in the future");
      }
      if (key.equals("admins") && old instanceof SuperAdmin && !d.get("adminLevel").equals("SUPER"))
        protectLastSuper(id);
      String newId =
          creating
              ? Schema.module(key).prefix()
                  + UUID.randomUUID().toString().replace("-", "").substring(0, 12)
              : id;
      Entity entity = Schema.create(key, newId, d);
      if (creating) dao(key).create(entity);
      else dao(key).update(entity);
      if (key.equals("agents")
          && old != null
          && !old.get("email").equalsIgnoreCase(entity.get("email"))) {
        // Keep the login identity linked when the profile's contact email changes.
        for (Entity account : dao("users").findAll()) {
          if (account instanceof AgentUser
              && account.get("email").equalsIgnoreCase(old.get("email"))) {
            Map<String, String> updated = new LinkedHashMap<>(account.fields());
            updated.put("email", entity.get("email"));
            dao("users").update(Schema.create("users", account.getId(), updated));
          }
        }
      }
      logs.append(
          actor == null ? newId : actor.getId(),
          (creating ? "Created " : "Updated ") + key + " " + newId);
      return entity;
    }
  }

  private void protectLastSuper(String id) throws IOException {
    check(
        dao("admins").findAll().stream()
            .anyMatch(e -> e instanceof SuperAdmin && !e.getId().equals(id)),
        "The last super administrator must remain");
  }

  public void delete(String key, String id, Entity actor) throws IOException {
    synchronized (LOCK) {
      Entity e = require(key, id);
      check(canDelete(key, e, actor), "You do not have permission to delete this record");
      if (e instanceof SuperAdmin) protectLastSuper(id);
      for (String other : Schema.KEYS)
        for (Entity dependent : dao(other).findAll()) {
          boolean referenced = false;
          for (var f : Schema.module(other).fields())
            if (f.type().equals("@" + key) && dependent.get(f.name()).equals(id)) referenced = true;
          if (other.equals("reviews")
              && dependent.get("targetId").equals(id)
              && dependent
                  .get("reviewType")
                  .equals(
                      key.equals("properties") ? "PROPERTY" : key.equals("agents") ? "AGENT" : ""))
            referenced = true;
          if (key.equals("agents")
              && other.equals("users")
              && dependent.get("userType").equals("AGENT")
              && dependent.get("email").equalsIgnoreCase(e.get("email"))) referenced = true;
          check(
              !referenced,
              "This record is referenced by "
                  + other
                  + ". Remove or reassign linked records first.");
        }
      dao(key).delete(id);
      logs.append(actor.getId(), "Deleted " + key + " " + id);
    }
  }

  public double averageRating(String targetId) throws IOException {
    synchronized (LOCK) {
      return dao("reviews").findAll().stream()
          .filter(e -> e.get("targetId").equals(targetId))
          .mapToInt(e -> Integer.parseInt(e.get("rating")))
          .average()
          .orElse(0);
    }
  }
}
