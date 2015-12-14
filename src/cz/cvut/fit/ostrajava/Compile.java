package cz.cvut.fit.ostrajava;

/**
 * Created by tomaskohout on 12/13/15.
 */

import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Interpreter.ClassPool;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Array;
import cz.cvut.fit.ostrajava.Parser.ASTCompilationUnit;
import cz.cvut.fit.ostrajava.Parser.Node;
import cz.cvut.fit.ostrajava.Parser.OSTRAJavaParser;
import cz.cvut.fit.ostrajava.Parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.cvut.fit.ostrajava.Compiler.Class;

public class Compile {

    final static String CLASS_TYPE_EXTENSION = "tryda";

    public static void printHelp(){
        System.out.println("Pouziti: ostrajavac <moznosti> <soubory zdrojove bo slozka>\n"+
                "kaj moznosti muzu byt: \n" +
                "-d slozka pro vygenerovane .tryda soubory \n");
    }

    public static void exec(String[] args) throws Exception
    {
        if (args.length == 0) {
            printHelp();
            System.exit(0);
        }

        List<String> filenames = new ArrayList<>(Arrays.asList(args));
        String outputDirectory = ".";

        for (int i = 0; i < args.length-1; i ++) {
            String param = args[i];
            String value = args[i+1];

            if (param.equals("-d")){
                filenames.remove(i);
                filenames.remove(i);
                outputDirectory = value;
            }
        }

        if (filenames.size() == 0){
            printHelp();
            System.exit(0);
        }


        List<Node> rootNodeList = parse(filenames);

        List<Class> classList = new ArrayList<>();
        OSTRAJavaCompiler compiler = new OSTRAJavaCompiler();

        //First stage - precompilation
        for (Node node: rootNodeList){
            classList.addAll(compiler.precompile(node));
        }

        //Second stage - compilation
        ClassPool classPool = new ClassPool(classList);

        classList.clear();

        for (Node node: rootNodeList){
            classList.addAll(compiler.compile(node, classPool));
        }

        removeClassfiles(outputDirectory);

        //Generate files
        for (Class clazz: classList){
            Classfile.toFile(clazz, outputDirectory + clazz.getClassName() + "." + CLASS_TYPE_EXTENSION);
        }


    }

    protected static void removeClassfiles(String dir){
        File file = new File(dir);
        if (!file.isDirectory()){
            System.out.println(dir + " is not a directory");
            System.exit(0);
        }

        for (File dirFile: file.listFiles()){
            String extension = "";

            int i = dirFile.getName().lastIndexOf('.');
            if (i > 0) {
                extension = dirFile.getName().substring(i + 1);
            }

            if (extension.equals(Compile.CLASS_TYPE_EXTENSION)) {
                dirFile.delete();
            }

        }


    }

    protected static List<File> listAllFiles(List<String> filenames){
        List<File> files = new ArrayList<>();

        for (String fileName: filenames) {
            File file = new File(fileName);
            files.addAll(getFilesRecursively(file));
        }

        return files;
    }

    protected static List<File>  getFilesRecursively(File file){
        List<File> files = new ArrayList<>();

        if (file.isDirectory()){
            File[] dirFiles = file.listFiles();
            for (File dirFile: dirFiles){
                files.addAll(getFilesRecursively(dirFile));
            }

        }else{
            files.add(file);
        }

        return files;
    }

    protected static List<Node> parse(List<String> filenames) throws FileNotFoundException, ParseException {
        Reader fr = null;
        OSTRAJavaParser jp = null;

        List<Node> rootNodeList = new ArrayList<>();

        for (File file: listAllFiles(filenames)) {

            fr = new InputStreamReader(new FileInputStream(file));

            if (jp == null){
                jp = new OSTRAJavaParser(fr);
            }else{
                jp.ReInit(fr);
            }

            try {
                //Parse
                jp.CompilationUnit();
                ASTCompilationUnit node = (ASTCompilationUnit) jp.rootNode();

                rootNodeList.add(node);
            } catch (ParseException e) {
                System.out.println("Parsing exception in file " + file.getName());
                throw e;
            }
        }

        return rootNodeList;
    }


}
