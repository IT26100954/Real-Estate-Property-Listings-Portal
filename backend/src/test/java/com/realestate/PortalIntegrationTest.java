package com.realestate;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import javax.sql.DataSource;
import java.net.*;
import java.net.http.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
 "spring.datasource.url=jdbc:h2:mem:portal;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
 "spring.datasource.username=sa","spring.datasource.password=","spring.datasource.driver-class-name=org.h2.Driver",
 "spring.sql.init.mode=always","spring.sql.init.schema-locations=classpath:test-schema.sql",
 "spring.sql.init.data-locations=classpath:test-data.sql"})
class PortalIntegrationTest {
 @Autowired Environment env;
 @Autowired JdbcTemplate jdbc;
 @Autowired DataSource source;
 private final JsonMapper json=JsonMapper.builder().build();
 private HttpClient client;
 private String csrf;
 @BeforeEach void reset() throws Exception {
  for(String table:List.of("activity_log","reviews","offers","appointments","properties","agents","admins","users"))jdbc.update("DELETE FROM `"+table+"`");
  new ResourceDatabasePopulator(new ClassPathResource("test-data.sql")).execute(source);
  client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
  csrf=parse(call("GET","/auth/me",null)).get("csrf").asText();
 }
 private HttpResponse<String> call(String method,String path,Object body)throws Exception{
  var b=HttpRequest.newBuilder(URI.create("http://localhost:"+env.getProperty("local.server.port")+"/api"+path));
  if(!method.equals("GET"))b.header("X-CSRF-Token",csrf);
  b.header("Content-Type","application/json");
  b.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
  return client.send(b.build(),HttpResponse.BodyHandlers.ofString());
 }
 private JsonNode parse(HttpResponse<String> r){return json.readTree(r.body());}
 private void login(String user,String password)throws Exception{
  var r=call("POST","/auth/login",Map.of("username",user,"password",password));assertEquals(200,r.statusCode(),r.body());csrf=parse(r).get("csrf").asText();
 }
 private Map<String,String> fields(String key){
  return new LinkedHashMap<>(switch(key){
   case "users"->Map.of("username","crud_test","password","TestPass@123","email","crud@example.test","phone","0771234567","userType","BUYER");
   case "agents"->Map.of("fullName","Test Agent","email","test-agent@example.test","phone","0771234567","licenseNo","TEST-LICENSE","city","Colombo","experienceYears","2","agentType","PARTTIME","commissionRate","2");
   case "properties"->Map.ofEntries(Map.entry("title","Test House"),Map.entry("propertyType","HOUSE"),Map.entry("listingType","SALE"),Map.entry("city","Colombo"),Map.entry("address","Test Road"),Map.entry("price","10000000"),Map.entry("bedrooms","3"),Map.entry("bathrooms","2"),Map.entry("areaSqft","1500"),Map.entry("ownerId","U002"),Map.entry("agentId","A001"),Map.entry("status","AVAILABLE"),Map.entry("extraValue","2"));
   case "appointments"->Map.of("propertyId","P001","buyerId","U001","agentId","A001","viewDate",LocalDate.now().plusDays(5).toString(),"viewTime","10:30","viewType","ONSITE","status","PENDING","note","Test viewing");
   case "offers"->Map.of("propertyId","P001","buyerId","U001","offerType","PURCHASE","offerAmount","9000000","durationMonths","0","offerDate",LocalDate.now().toString(),"status","PENDING");
   case "reviews"->Map.of("reviewerId","U001","targetId","P001","reviewType","PROPERTY","rating","4","comment","Useful listing","reviewDate",LocalDate.now().toString());
   case "admins"->Map.of("username","temp_content","password","TestPass@123","email","admin@example.test","adminLevel","CONTENT");
   default->throw new IllegalArgumentException();
  });
 }
 @Test void allSevenModulesPersistFullCrudAndHideHashes()throws Exception{
  login("admin_main","Admin@123");
  for(String key:List.of("users","agents","properties","appointments","offers","reviews","admins")){
   var data=fields(key);var created=call("POST","/records/"+key,data);assertEquals(200,created.statusCode(),key+created.body());
   var record=parse(created);String id=record.get("id").asText();assertFalse(record.get("fields").has("password"));
   assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM `"+key+"` WHERE id=?",Integer.class,id));
   assertEquals(200,call("GET","/records/"+key+"/"+id,null).statusCode());
   if(key.equals("users")||key.equals("admins"))data.put("password","");
   switch(key){case "users","admins"->data.put("email","edited-"+key+"@example.test");case "agents"->data.put("city","Kandy");case "properties"->data.put("title","Edited House");case "appointments"->data.put("note","Edited note");case "offers"->data.put("offerAmount","9100000");case "reviews"->data.put("comment","Edited comment");}
   var updated=call("PUT","/records/"+key+"/"+id,data);assertEquals(200,updated.statusCode(),key+updated.body());
   assertEquals(200,call("DELETE","/records/"+key+"/"+id,null).statusCode());
   assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM `"+key+"` WHERE id=?",Integer.class,id));
   assertEquals(404,call("GET","/records/"+key+"/"+id,null).statusCode());
  }
  assertEquals(21,jdbc.queryForObject("SELECT COUNT(*) FROM activity_log",Integer.class));
 }
 @Test void csrfRoleValidationAndReferenceProtection()throws Exception{
  var publicUser=fields("users");publicUser.put("userType","AGENT");assertEquals(400,call("POST","/records/users",publicUser).statusCode());
  login("kamal_buyer","Buyer@123");assertEquals(403,call("POST","/records/admins",fields("admins")).statusCode());
  String real=csrf;csrf="incorrect";assertEquals(403,call("POST","/records/reviews",fields("reviews")).statusCode());csrf=real;
  var bad=fields("reviews");bad.put("rating","6");assertEquals(400,call("POST","/records/reviews",bad).statusCode());
  login("admin_main","Admin@123");assertEquals(400,call("DELETE","/records/agents/A001",null).statusCode());
  assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM agents WHERE id='A001'",Integer.class));
 }
 @Test void auditFailureRollsBackRecordWrite()throws Exception{
  login("admin_main","Admin@123");
  jdbc.execute("ALTER TABLE activity_log ADD CONSTRAINT fail_audit CHECK (action = 'NEVER')");
  try{
   assertEquals(400,call("POST","/records/users",fields("users")).statusCode());
   assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username='crud_test'",Integer.class));
  }finally{jdbc.execute("ALTER TABLE activity_log DROP CONSTRAINT fail_audit");}
 }
}
