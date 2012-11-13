package com.rackspacecloud.client.service_registry.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Generator {
    public static void main(String args[]) {
        // we need this class.
        try {
            Class.forName("com.sun.source.util.Trees");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        // ensure we have source to scan.
        File examplesDir = new File(args[0]);
        if (!examplesDir.exists() || !examplesDir.isDirectory())
            throw new RuntimeException("Cannot scan: " + examplesDir.getAbsolutePath());
        else
            System.out.println("Will scan " + examplesDir.getAbsolutePath());
        
        // load output generation map.
        Properties props = new Properties();
        try {
            props.load(Generator.class.getClassLoader().getResourceAsStream("generate.properties"));    
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return file.getName().endsWith(".java");
            }
        };
        Collection<File> javaSources = walk(examplesDir, filter);
        
        // Replaces the mapped files.
        Map<File, File> filesToReplace = new HashMap<File, File>();
        for (Map.Entry<?, ?> entry : props.entrySet()) {
            File source = new File(entry.getKey().toString());
            if (!source.exists())
                continue;
            File dest = new File(entry.getValue().toString());
            try {
                if (!dest.exists() & !dest.createNewFile())
                    continue;
            } catch (IOException ex) {
                throw new RuntimeException("Could not generate: " + dest.getAbsolutePath());
            }
            filesToReplace.put(source, dest);
        }
        TagReplacer.replace(javaSources, filesToReplace);
    }
    
    private static Collection<File> walk(File f, FileFilter filter) {
        ArrayList<File> files = new ArrayList<File>();
        if (f.isDirectory()) {
            for (File child : f.listFiles(filter)) {
                files.addAll(walk(child, filter));
            }
        } else if (f.isFile() && filter.accept(f)) {
            files.add(f);
        }
        return files;
    }
}
