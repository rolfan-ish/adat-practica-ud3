package es.rolfan.menu;

import java.util.Scanner;

public class IntegerArgGetter implements ArgGetter<Integer> {

    @Override
    public Integer get(String param, String name) {
        System.out.print(param.isEmpty() ? "Introduce el entero \"" + name + "\": " : param);
        try {
            return new Scanner(System.in).nextInt();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
