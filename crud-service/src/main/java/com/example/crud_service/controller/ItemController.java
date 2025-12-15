package com.example.crud_service.controller;

import com.example.crud_service.model.Item;
import com.example.crud_service.service.ItemService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

  private final ItemService service;

  public ItemController(ItemService service) {
    this.service = service;
  }

  @PostMapping(consumes = "application/json", produces = "application/json")
  public Item create(@RequestBody Item item) {
    return service.create(item);
  }

  @GetMapping("/{id}")
  public Item get(@PathVariable String id) {
    return service.get(id);
  }

  @GetMapping
  public List<Item> getAll() {
    return service.getAll();
  }

  @PutMapping("/{id}")
  public Item update(@PathVariable String id, @RequestBody Item item) {
    return service.update(id, item);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable String id) {
    service.delete(id);
  }
}
