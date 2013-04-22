package com.rackspacecloud.client.service_registry.containers;

import com.rackspacecloud.client.service_registry.objects.HasId;

import java.util.List;

public interface ContainerMeta<T extends HasId> {
    public List<T> getValues();
    public String getNextMarker();
    public String getMarker();
    public Integer getLimit();
    public Integer getCount();
}
