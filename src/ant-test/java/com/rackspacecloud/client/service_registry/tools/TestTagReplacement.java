package com.rackspacecloud.client.service_registry.tools;

import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TestTagReplacement {
    
    @Test
    public void testCwd() {
        System.err.println(new java.io.File(".").getAbsolutePath());
    }
    
    @Test
    public void testIt() throws IOException {
        final File markdown = File.createTempFile("markdown_copy", ".rsr_source.txt");
        copyFile(new File("src/ant-test/resources/test.md"), markdown);
        final File java = new File("src/ant-test/java/com/rackspacecloud/client/service_registry/tools/TestTagReplacement.java");

        Assert.assertTrue(markdown.exists());
        Assert.assertTrue("Expected: " + java.getAbsolutePath(), java.exists());
        
        // replacement text should not be present.
        String replacedContents = readFile(markdown);
        Assert.assertFalse(replacedContents.contains("this is the contents of tag_1"));
        Assert.assertFalse(replacedContents.contains("this is the contents of tag_2"));
        Assert.assertFalse(replacedContents.contains("this is the contents of tag_3"));

        Collection<File> javaFiles = new ArrayList<File>() {{ add(java); }};
        Map<File, File> markdownFiles = new HashMap<File, File>() {{ put(markdown, null); }};
        TagReplacer.replace(javaFiles, markdownFiles);
        
        // replacement text should be present.
        replacedContents = readFile(markdown);
        Assert.assertTrue(replacedContents.contains("this is the contents of tag_1"));
        Assert.assertTrue(replacedContents.contains("this is the contents of tag_2"));
        Assert.assertTrue(replacedContents.contains("this is the contents of tag_3"));
        
        markdown.deleteOnExit();
    }
    
    @Example(tag="tag_1")
    public void tag1() {
        String s = "this is the contents of tag_1";
    }
    
    @Example(tag="tag_2")
    public void tag2() {
        String s = "this is the contents of tag_2";
    }
    
    @Example(tag="tag_3")
    public void tag3() {
        String s = "this is the contents of tag_3";
    }
    
    private static void copyFile(File src, File dst) throws IOException {
        if (!dst.exists())
            dst.createNewFile();
        FileChannel srcCh = new FileInputStream(src).getChannel();
        FileChannel dstCh = new FileOutputStream(dst).getChannel();
        dstCh.transferFrom(srcCh, 0, srcCh.size());
        srcCh.close();
        dstCh.close();
    }
    
    private static String readFile(File src) throws IOException {
        FileInputStream in = new FileInputStream(src);
        FileChannel fc = in.getChannel();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        String s = Charset.defaultCharset().decode(bb).toString();
        in.close();
        return s;
    }
}