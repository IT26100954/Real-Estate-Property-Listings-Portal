package com.realestate.dao;
import com.realestate.config.Database;
import com.realestate.model.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
/** Identifiers come only from Schema; all request values use prepared-statement parameters. */
public class SqlDAO<T extends Entity> {
 private final String key;
 public SqlDAO(String key) { Schema.module(key); this.key=key; }
 private String table() { return "`"+key+"`"; }
 private List<Schema.Field> fields() { return Schema.module(key).fields(); }
 @SuppressWarnings("unchecked")
 public List<T> findAll() throws IOException { return select("",new Object[0]); }
 @SuppressWarnings("unchecked")
 private List<T> select(String suffix,Object[] args) {
  return Database.jdbc().query("SELECT * FROM "+table()+suffix+" ORDER BY id",(rs,row)->{
   Map<String,String> d=new LinkedHashMap<>();
   for(var f:fields()) d.put(f.name(),Optional.ofNullable(rs.getString(f.name())).orElse(""));
   return (T)Schema.create(key,rs.getString("id"),d);
  },args);
 }
 public Optional<T> findById(String id) throws IOException {
  return select(" WHERE id = ?",new Object[]{id}).stream().findFirst();
 }
 public void create(T e) throws IOException {
  String columns="`id`,"+fields().stream().map(f->"`"+f.name()+"`").collect(Collectors.joining(","));
  String marks=String.join(",",Collections.nCopies(fields().size()+1,"?"));
  List<Object> values=new ArrayList<>(); values.add(e.getId());
  fields().forEach(f->values.add(e.get(f.name())));
  Database.jdbc().update("INSERT INTO "+table()+" ("+columns+") VALUES ("+marks+")",values.toArray());
 }
 public void update(T e) throws IOException {
  String sets=fields().stream().map(f->"`"+f.name()+"` = ?").collect(Collectors.joining(","));
  List<Object> values=new ArrayList<>(); fields().forEach(f->values.add(e.get(f.name()))); values.add(e.getId());
  if(Database.jdbc().update("UPDATE "+table()+" SET "+sets+" WHERE id = ?",values.toArray())!=1)
   throw new IllegalArgumentException("Record no longer exists");
 }
 public void delete(String id) throws IOException {
  if(Database.jdbc().update("DELETE FROM "+table()+" WHERE id = ?",id)!=1)
   throw new IllegalArgumentException("Record no longer exists");
 }
}
