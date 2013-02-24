package com.rackspacecloud.client.service_registry;

public class PaginationOptions {
    private Integer limit;
    private String marker;

    public PaginationOptions(Integer limit, String marker) {
        this.limit = limit;
        this.marker = marker;
    }

    public Integer getLimit() {
        return limit;
    }

    public String getMarker() {
        return marker;
    }
}
