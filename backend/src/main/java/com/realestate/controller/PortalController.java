package com.realestate.controller;
import com.realestate.config.Database;
import com.realestate.dao.ActivityLogDAO;
import com.realestate.model.*;
import com.realestate.service.PortalService;
import jakarta.servlet.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;
import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public final class PortalController {
 private final PortalService service=new PortalService();
 private final TransactionTemplate tx;
 public PortalController(TransactionTemplate tx,Database initialized) { this.tx=tx; }
 @FunctionalInterface private interface Work<T> { T run() throws IOException; }
 private <T> T work(Work<T> task) {
  synchronized(PortalService.LOCK) {
   return tx.execute(status->{try{return task.run();}catch(IOException e){throw new UncheckedIOException(e);}});
  }
 }
 private void require(boolean ok) { if(!ok)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Access denied"); }
 private Entity actor(HttpServletRequest req) throws IOException {
  return service.fresh((Entity)req.getSession().getAttribute("actor"));
 }
 private String token(HttpServletRequest req) {
  HttpSession s=req.getSession(); String t=(String)s.getAttribute("csrf");
  if(t==null){t=UUID.randomUUID().toString();s.setAttribute("csrf",t);} return t;
 }
 private void csrf(HttpServletRequest req) {require(token(req).equals(req.getHeader("X-CSRF-Token")));}
 private Map<String,Object> dto(String key,Entity e,Entity a) throws IOException {
  Map<String,String> fields=new LinkedHashMap<>(e.fields()); fields.remove("password");
  Map<String,Object> out=new LinkedHashMap<>();
  out.put("id",e.getId());out.put("label",e.label());out.put("summary",e.summary());out.put("fields",fields);
  out.put("canEdit",service.canEdit(key,e,a));out.put("canDelete",service.canDelete(key,e,a));
  if(e instanceof Property p)out.put("calculation","Listing fee: LKR "+p.calculateListingFee());
  if(e instanceof Agent p)out.put("calculation","Commission on LKR 10,000,000: LKR "+p.calculateCommission(10000000));
  if(e instanceof Appointment p)out.put("calculation",p.viewingInstructions());
  if(e instanceof Offer p)out.put("calculation","Total payable: LKR "+p.calculateTotalPayable());
  if(e instanceof Review p)out.put("calculation",p.displayReview()+" · Average: "+service.averageRating(e.get("targetId")));
  return out;
 }
 private Map<String,Object> identity(HttpServletRequest req,Entity a) throws IOException {
  Map<String,Object> out=new LinkedHashMap<>();out.put("csrf",token(req));
  out.put("actor",a==null?null:dto(a instanceof Admin?"admins":"users",a,a));return out;
 }
 @GetMapping("/auth/me") public Object me(HttpServletRequest r) {return work(()->identity(r,actor(r)));}
 @PostMapping("/auth/login") public Object login(@RequestBody Map<String,String> d,HttpServletRequest r) {
  return work(()->{csrf(r);Entity a=service.login(d.get("username"),d.get("password"));
   if(a==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Username or password is incorrect");
   r.getSession().invalidate();r.getSession(true).setAttribute("actor",a);return identity(r,a);});
 }
 @PostMapping("/auth/logout") public Object logout(HttpServletRequest r) {
  return work(()->{csrf(r);r.getSession().invalidate();return identity(r,null);});
 }
 @GetMapping("/schema/{key}") public Object schema(@PathVariable String key,HttpServletRequest r) {
  return work(()->{var s=Schema.module(key);Entity a=actor(r);
   List<Schema.Field> fields=new ArrayList<>();
   for(var f:s.fields())fields.add(new Schema.Field(f.name(),key.equals("users")&&f.name().equals("userType")&&!(a instanceof SuperAdmin)?"BUYER,SELLER":f.type()));
   return Map.of("title",s.title(),"fields",fields,"canCreate",service.canCreate(key,a));});
 }
 @GetMapping("/choices/{key}") public Object choices(@PathVariable String key,@RequestParam(defaultValue="") String id,HttpServletRequest r) {
  return work(()->{Schema.module(key);Entity a=actor(r);Entity edit=id.isBlank()?null:service.dao(key).findById(id).orElse(null);
   require(edit==null?service.canCreate(key,a):service.canEdit(key,edit,a));
   Map<String,List<Map<String,String>>> out=new LinkedHashMap<>();
   for(String other:List.of("users","agents","properties")){
    List<Map<String,String>> options=new ArrayList<>();
    for(Entity e:service.dao(other).findAll()){
     boolean visible=!other.equals("users")||a instanceof SuperAdmin||(a!=null&&e.getId().equals(a.getId()))
      ||(edit!=null&&((key.equals("appointments")&&a instanceof AgentUser)||(key.equals("offers")&&a instanceof SellerUser))&&e.getId().equals(edit.get("buyerId")))
      ||(key.equals("properties")&&(a instanceof AgentUser||a instanceof ContentAdmin)&&e instanceof SellerUser);
     if(visible)options.add(Map.of("id",e.getId(),"label",e.label(),"userType",e.get("userType"),"agentId",e.get("agentId")));
    }out.put(other,options);
   }return out;});
 }
 @GetMapping("/records/{key}") public Object list(@PathVariable String key,@RequestParam Map<String,String> q,HttpServletRequest r) {
  return work(()->{Entity a=actor(r);Schema.module(key);require(!key.equals("admins")||a instanceof SuperAdmin);
   List<Entity> records=key.equals("properties")?service.searchProperties(a,q.get("q"),q.get("city"),q.get("type"),q.get("minPrice"),q.get("maxPrice")):service.list(key,a,q.get("q"));
   List<Object> out=new ArrayList<>();for(Entity e:records)out.add(dto(key,e,a));return out;});
 }
 @GetMapping("/records/{key}/{id}") public Object detail(@PathVariable String key,@PathVariable String id,HttpServletRequest r) {
  return work(()->{Entity a=actor(r);Entity e=service.dao(key).findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Record not found"));require(service.canRead(key,e,a));return dto(key,e,a);});
 }
 @PostMapping("/records/{key}") public Object create(@PathVariable String key,@RequestBody Map<String,String> d,HttpServletRequest r) {
  return work(()->{csrf(r);Entity a=actor(r);require(service.canCreate(key,a));Entity e=service.save(key,"",d,a);return dto(key,e,a);});
 }
 @PutMapping("/records/{key}/{id}") public Object update(@PathVariable String key,@PathVariable String id,@RequestBody Map<String,String> d,HttpServletRequest r) {
  return work(()->{csrf(r);Entity a=actor(r);Entity old=service.dao(key).findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Record not found"));require(service.canEdit(key,old,a));return dto(key,service.save(key,id,d,a),a);});
 }
 @DeleteMapping("/records/{key}/{id}") public Object delete(@PathVariable String key,@PathVariable String id,HttpServletRequest r) {
  return work(()->{csrf(r);Entity a=actor(r);Entity e=service.dao(key).findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Record not found"));require(service.canDelete(key,e,a));service.delete(key,id,a);return Map.of("message","Record deleted");});
 }
 @GetMapping("/activity") public Object activity(HttpServletRequest r) {return work(()->{require(actor(r) instanceof Admin);return new ActivityLogDAO().findAll();});}
}
