package es.rolfan.menu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class FileArgGetter implements ArgGetter<FileReader> {
    @Override
    public FileReader get(String param, String name) {
        System.out.print(param.isEmpty() ? "Introduce el archivo \"" + name + "\": " : param);
        try {
            return new FileReader(new Scanner(System.in).nextLine());
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
