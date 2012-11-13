package com.rackspacecloud.client.service_registry.tools;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.Pretty;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeExtractor {
    // captures the tag from the annotation.
    public static final Pattern pattern = Pattern.compile("@Example\\W*\\(tag\\W*=\\W*\\\"(\\w+)\\W*\\).*");
    
    private final Map<String, String> taggedMethods;
    
    public CodeExtractor() {
        taggedMethods = new HashMap<String, String>();
    }
    
    public void extractTaggedMethods(File sourcePath) {
        // generate AST.
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null)
            throw new RuntimeException("Could not get a compiler");
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourcePath);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits);
        
        // process AST, looking for methods annotated with Example.
        LinkedList<AbstractProcessor> processors = new LinkedList<AbstractProcessor>();
        processors.add(new TreeProcessor());
        task.setProcessors(processors);
        task.call();
    }
    
    public String getSource(String tag) {
        if (tag == null) return null;
        return taggedMethods.get(tag);
    }
    
    public static String getTag(String text) {
        Matcher m = pattern.matcher(text);
        if (m.matches())
            return m.group(1);
        else return null;
    }
    
    
    private static JavacTrees getJavacTrees(Class<?> argType, Object arg) {
        try {
            ClassLoader cl = arg.getClass().getClassLoader();
//            ClassLoader cl = new URLClassLoader(new URL[] { new File("lib/tools-1.6.0.jar").toURL()});
            Class<?> c = Class.forName("com.sun.tools.javac.api.JavacTrees", false, cl);
            argType = Class.forName(argType.getName(), false, cl);
            Method m = c.getMethod("instance", new Class[] { argType });
            return (JavacTrees)m.invoke(null, new Object[] { arg });
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @SupportedSourceVersion(SourceVersion.RELEASE_6)
    @SupportedAnnotationTypes("*")
    private class TreeProcessor extends AbstractProcessor {

//        private Trees trees;
        private JavacTrees trees;

        @Override
        public synchronized void init(ProcessingEnvironment processingEnvironment) {
            super.init(processingEnvironment);
            try {
                trees = getJavacTrees(ProcessingEnvironment.class, processingEnvironment);
//                trees = Trees.instance(processingEnvironment);
            } catch (AssertionError e) {
                e.getCause().printStackTrace();
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnvironment) {
            ExampleSearchVisitor visitor = new ExampleSearchVisitor();
            for (Element e : roundEnvironment.getRootElements()) {
                TreePath tp = trees.getPath(e);
                visitor.scan(tp, trees);
            }
            return true;
        }
    }

    private class ExampleSearchVisitor extends TreePathScanner<Object, Trees> {

        @Override
        public Object visitClass(ClassTree classTree, Trees trees) {
            return super.visitClass(classTree, trees);
        }

        @Override
        public Object visitMethod(MethodTree methodTree, Trees trees) {
            for (AnnotationTree at : methodTree.getModifiers().getAnnotations()) {
                // todo: not good to rely on toString().
                String matchedTag = getTag(at.toString());
                if (matchedTag != null) {
                    StringWriter sw = new StringWriter();
                    Pretty pretty = new Pretty(sw, true);
                    try {
                        pretty.printExpr((JCTree) methodTree.getBody());
                        taggedMethods.put(matchedTag, sw.toString());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            return super.visitMethod(methodTree, trees);
        }
    }
}