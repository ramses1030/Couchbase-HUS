package com.example.crud_service.config;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CouchbaseConfig {

  @Bean
  public Cluster couchbaseCluster(
      @Value("${spring.couchbase.connection-string}") String connStr,
      @Value("${spring.couchbase.username}") String username,
      @Value("${spring.couchbase.password}") String password) {
    return Cluster.connect(connStr, ClusterOptions.clusterOptions(username, password));
  }

  @Bean
  public Bucket couchbaseBucket(
      Cluster cluster,
      @Value("${spring.data.couchbase.bucket-name}") String bucketName) {
    return cluster.bucket(bucketName);
  }

  @Bean
  public Scope couchbaseScope(
      Bucket bucket,
      @Value("${app.scope}") String scopeName) {
    return bucket.scope(scopeName);
  }

  @Bean
  public Collection couchbaseCollection(
      Scope scope,
      @Value("${app.collection}") String collectionName) {
    return scope.collection(collectionName);
  }
}
