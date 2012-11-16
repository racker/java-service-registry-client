package com.rackspacecloud.client.service_registry.tools;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class TestCodeExtraction {
    private CodeExtractor extractor;
    
    @Before
    public void initExtractor() {
        extractor = new CodeExtractor();
        File source = new File("src/ant-test/java/com/rackspacecloud/client/service_registry/tools/TestCodeExtraction.java");
        if (!source.exists())
            throw new RuntimeException("Missing " + source.getAbsolutePath());
        extractor.extractTaggedMethods(source);
    }
    
    @Example(tag="extract_1")
    public void method1() {
        String s = "tag_is_extract_1";
    }
    
    @Example(tag="extract_2")
    public void method2() {
        String s = "tag_is_extract_2";
    }
    
    @Example(tag="extract_3")
    public void method2(int anyArg) {
        // methods have the same name, but different tags. should extract the right method.
        String s = "tag_is_extract_3";
    }
    
    @Example(tag ="space_variation_1")
    public void method3() {
        String s = "space_variation_1";
    }
    
    @Example( tag = "space_variation_2")
    public void method4() {
        String s = "space_variation_2";
    }
    
    @Example(   tag= "space_variation_3"  )
    public void method5() {
        String s = "space_variation_3";
    }
    
    @Test
    public void testSimpleExtraction() {
        Assert.assertTrue(extractor.getSource("extract_1").contains("tag_is_extract_1"));
        Assert.assertTrue(extractor.getSource("extract_2").contains("tag_is_extract_2"));
        Assert.assertTrue(extractor.getSource("extract_3").contains("tag_is_extract_3"));
    }
    
    @Test
    public void testMissingExtraction() {
        Assert.assertNull(extractor.getSource("fizz_buzz_bar"));
    }
    
    @Test
    public void testSpaceVariations() {
        // make sure that the regex in CodeExtractor tolerates funky spacing in the annotation.
        Assert.assertNotNull(extractor.getSource("space_variation_1"));
        Assert.assertNotNull(extractor.getSource("space_variation_2"));
        Assert.assertNotNull(extractor.getSource("space_variation_3"));
        Assert.assertTrue(extractor.getSource("space_variation_1").contains("space_variation_1"));
        Assert.assertTrue(extractor.getSource("space_variation_2").contains("space_variation_2"));
        Assert.assertTrue(extractor.getSource("space_variation_3").contains("space_variation_3"));
    }
}