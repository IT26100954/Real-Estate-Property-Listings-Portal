package com.realestate.dao;
import com.realestate.config.Database;
import com.realestate.model.ActivityLog;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
public final class ActivityLogDAO {
 public void append(String actor,String action) throws IOException {
  Database.jdbc().update("INSERT INTO activity_log (logId,actorId,action,dateTime) VALUES (?,?,?,?)",
   UUID.randomUUID().toString(),actor,action,LocalDateTime.now());
 }
 public List<ActivityLog> findAll() throws IOException {
  return Database.jdbc().query("SELECT * FROM activity_log ORDER BY dateTime DESC, logId",
   (rs,n)->new ActivityLog(rs.getString("logId"),rs.getString("actorId"),rs.getString("action"),rs.getString("dateTime")));
 }
}
