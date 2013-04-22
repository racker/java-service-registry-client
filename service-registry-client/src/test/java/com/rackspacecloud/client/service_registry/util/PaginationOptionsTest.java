package com.rackspacecloud.client.service_registry.util;

import com.rackspacecloud.client.service_registry.MethodOptions;
import junit.framework.Assert;
import org.junit.Test;

public class PaginationOptionsTest {
    @Test
    public void testPaginationOptions() {
        int actualLimit;
        String actualMarker;

        MethodOptions po1 = new MethodOptions().withLimit(55)
                                      .withMarker("markera");
        MethodOptions po2 = new MethodOptions(100, "markerb");

        actualLimit = po1.getLimit();
        actualMarker = po1.getMarker();

        Assert.assertEquals(55, actualLimit);
        Assert.assertEquals("markera", actualMarker);

        actualLimit = po2.getLimit();
        actualMarker = po2.getMarker();

        Assert.assertEquals(100, actualLimit);
        Assert.assertEquals("markerb", actualMarker);
    }
}
