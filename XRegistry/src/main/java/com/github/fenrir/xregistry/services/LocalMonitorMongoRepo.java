package com.github.fenrir.xregistry.services;

import com.github.fenrir.xcommon.clients.xregistry.entities.mongo.LocalMonitor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalMonitorMongoRepo extends MongoRepository<LocalMonitor, String> {
}
