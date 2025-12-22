package com.example.crud_service.service;

import com.couchbase.client.java.Collection;
//import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.query.QueryResult;
import com.example.crud_service.model.Item;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.core.error.DocumentExistsException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.example.crud_service.exception.NotFoundException;
import com.example.crud_service.exception.ConflictException;
import java.util.List;
import org.springframework.stereotype.Service;
//import com.couchbase.client.core.error.DocumentNotFoundException;

@Service
public class ItemService {
  private final Collection collection;
  private final Cluster cluster;

  public ItemService(Collection collection, Cluster cluster) {
    this.collection = collection;
    this.cluster = cluster;
  }

  public Item create(Item item) {
    try {
      collection.insert(item.getId(), item);
      return item;
    } catch (DocumentExistsException e) {
      throw new ConflictException("Item already exists: " + item.getId());
    }
  }

  public Item get(String id) {
    try {
      return collection.get(id).contentAs(Item.class);
    } catch (DocumentNotFoundException e) {
      throw new NotFoundException("Item not found: " + id);
    }
  }

  public List<Item> getAll() {
    QueryResult result = cluster.query("""
          SELECT RAW {
            "id": i.id,
            "name": i.name,
            "description": i.description
          }
          FROM appdata.crud.items AS i
          WHERE i.id IS NOT MISSING
          ORDER BY i.id
        """);

    return result.rowsAs(Item.class);
  }

  public Item update(String id, Item item) {
    item.setId(id);
    collection.upsert(id, item);
    return item;
  }

  public void delete(String id) {
    try {
      collection.remove(id);
    } catch (DocumentNotFoundException e) {
      throw new NotFoundException("Item not found: " + id);
    }
  }
}
