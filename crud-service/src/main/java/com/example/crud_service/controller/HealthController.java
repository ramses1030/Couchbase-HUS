package com.example.crud_service.controller;

import com.couchbase.client.java.Collection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;;

@RestController
public class HealthController {

  private final Collection collection;

  public HealthController(Collection collection) {
    this.collection = collection;
  }

  @GetMapping("/api/v1/health")
  public String health() {
    // operación mínima para confirmar conexión (upsert temporal)
    collection.upsert("health::ping", java.util.Map.of("ts", System.currentTimeMillis()));
    return "OK";
  }
}
