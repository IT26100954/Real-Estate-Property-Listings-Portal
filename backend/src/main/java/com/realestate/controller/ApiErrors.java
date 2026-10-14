package com.realestate.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.*;
import java.util.Map;
@RestControllerAdvice
public class ApiErrors {
 private static final Logger log=LoggerFactory.getLogger(ApiErrors.class);
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> invalid(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));}
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<?> denied(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",e.getReason()==null?"Request rejected":e.getReason()));}
 @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> integrity(DataIntegrityViolationException e){return ResponseEntity.badRequest().body(Map.of("message","Duplicate, invalid or referenced database record. Check the submitted fields."));}
 @ExceptionHandler(Exception.class) ResponseEntity<?> failed(Exception e){log.error("Portal request failed",e);return ResponseEntity.internalServerError().body(Map.of("message","Server error. Check backend logs."));}
}
