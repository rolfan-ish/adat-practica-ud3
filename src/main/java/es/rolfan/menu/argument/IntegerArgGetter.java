package es.rolfan.menu.argument;

import java.util.Scanner;

public class IntegerArgGetter implements ArgGetter<Integer> {
    @Override
    public Integer get(String param, String name) {
        System.out.print(param.isEmpty() ? "Introduce el entero \"" + name + "\": " : param);
        return new Scanner(System.in).nextInt();
    }
}
