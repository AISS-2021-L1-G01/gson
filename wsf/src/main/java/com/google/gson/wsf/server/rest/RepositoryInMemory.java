/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson.wsf.server.rest;

import java.util.Map;

import com.google.gson.webservice.definition.rest.Id;
import com.google.gson.webservice.definition.rest.RestResource;
import com.google.inject.internal.Maps;
import com.google.inject.internal.Preconditions;

/**
 * An in-memory map of rest resources
 *
 * @author inder
 *
 * @param <R> Type variable for the resource
 */
public class RepositoryInMemory<R extends RestResource<R>> implements Repository<R> {
  private final IdMap<R> resources;
  private final Map<Id<R>, MetaData<R>> metaDataMap;

  public RepositoryInMemory(Class<? super R> classOfResource) {
    this.resources = IdMap.create(classOfResource);
    this.metaDataMap = Maps.newHashMap();
  }

  @Override
  public R get(Id<R> resourceId) {
    return resources.get(resourceId);
  }

  public boolean isFreshlyAssignedId(Id<R> resourceId) {
    MetaData<R> metaData = metaDataMap.get(resourceId);
    if (metaData == null) {
      return false;
    }
    return metaData.isFreshlyAssignedId();
  }

  @Override
  public R put(R resource) {
    if (!resource.hasId()) {
      // insert semantics
      assignId(resource);
    } else {
      Id<R> id = resource.getId();
      if (!isFreshlyAssignedId(id)) {
        // update semantics
        Preconditions.checkState(resources.exists(resource.getId()));
      }
    }
    resource = resources.put(resource);
    metaDataMap.remove(resource.getId());
    return resource;
  }

  @Override
  public void delete(Id<R> resourceId) {
    resources.delete(resourceId);
  }

  @Override
  public boolean exists(Id<R> resourceId) {
    return resources.exists(resourceId);
  }

  @Override
  public Id<R> getNextId() {
    return resources.getNextId();
  }

  @Override
  public Id<R> assignId(R resource) {
    if (resource.getId() == null) {
      Id<R> id = resources.getNextId();
      resource.setId(id);
      metaDataMap.put(id, new MetaData<R>(true));
    }
    return resource.getId();
  }
}