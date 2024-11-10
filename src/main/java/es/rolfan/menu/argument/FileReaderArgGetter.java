package es.rolfan.menu.argument;

import java.io.FileReader;
import java.util.Scanner;

public class FileReaderArgGetter implements ArgGetter<FileReader> {
    @Override
    public FileReader get(String param, String name) throws Exception {
        System.out.print(param.isEmpty() ? "Introduce el archivo \"" + name + "\": " : param);
        return new FileReader(new Scanner(System.in).nextLine());
    }
}
