package com.github.tammo.fun;

import com.github.tammo.fun.gen.ByteCodeGenerator;
import com.github.tammo.fun.gen.CodeGenerator;
import com.github.tammo.fun.parse.AntlrParser;
import com.github.tammo.fun.parse.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        final String input = getInput();
        final Parser parser = new AntlrParser();
        final CodeGenerator backendCodeGenerator = new ByteCodeGenerator();
        final var compilationUnit = parser.parse(input);
        final var code = backendCodeGenerator.generate(compilationUnit);

        try {
            writeCodeToFile(compilationUnit.fullyQualifiedClassName() + ".class", code);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getInput() {
        try {
            return Files.readString(Paths.get("test.fun"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeCodeToFile(String fileName, byte[] code) throws IOException {
        Path path = Paths.get(fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, code);
    }

}
