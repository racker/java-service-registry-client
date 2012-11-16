package com.rackspacecloud.client.service_registry.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Rules:
 * 1. source lines contain one tag per line
 * 2. tags are by themselves on a line.
 * 
 * Indentation is preserved.
 */
public class TagReplacer {
    static void replace(Collection<File> javaSources, Map<File, File> markupSources) {
        
        CodeExtractor extractor = new CodeExtractor();
        for (File java : javaSources) {
            System.out.println("Scanning " + java.getAbsolutePath());
            extractor.extractTaggedMethods(java);
            System.out.println("Finished scan " + java.getAbsolutePath());
        }
        
        BufferedWriter writer = null;
        BufferedReader reader = null;
        for (File markup : markupSources.keySet()) {
            try {
                reader = new BufferedReader(new FileReader(markup));
                File tmp = File.createTempFile(markup.getName(), ".rsr_replace.txt");
                writer = new BufferedWriter(new FileWriter(tmp));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    String tag = CodeExtractor.getTag(line.trim());
                    String source = extractor.getSource(tag);
                    if (tag != null && source != null) {
                        int indent = line.indexOf("@");
                        
                        // place the all lines into a list of lines.
                        BufferedReader replacementReader = new BufferedReader(new StringReader(source));
                        List<String> replacementsLines = new ArrayList<String>();
                        for (String replacementLine = replacementReader.readLine(); replacementLine != null; replacementLine = replacementReader.readLine())
                            replacementsLines.add(replacementLine);
                        
                        // nip the first and last lines, which are braces
                        replacementsLines.remove(0);
                        replacementsLines.remove(replacementsLines.size() - 1);
                        
                        // rewrite this part of the file.
                        for (String replacementLine : replacementsLines) {
                            for (int i = 0; i < indent; i++)
                                writer.write(" ");
                            writer.write(replacementLine);
                            writer.write(System.getProperty("line.separator"));
                        }
                    } else {
                        writer.write(line);
                        writer.write(System.getProperty("line.separator"));
                    }
                }
                writer.close();
                reader.close();
                File dstFile = markupSources.get(markup);
                if (dstFile == null)
                    dstFile = markup;
                else
                    dstFile.createNewFile();
                tmp.renameTo(dstFile);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                for (Closeable closeme : new Closeable[] {writer, reader}) {
                    if (closeme != null) {
                        try {
                            closeme.close();
                        } catch (IOException fuuuuu) {
                            // no sense in dying. No sense in complaining. But we must do something.
                            fuuuuu.printStackTrace(System.err);
                        }
                    }
                }
            }
            
        }
    }
}