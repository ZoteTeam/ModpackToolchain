package com.reider745.innercoretoolchain.util;

import org.fusesource.jansi.Ansi;

public class Logs {
    public static void process(String started, String stopped, Runnable runnable) throws InterruptedException {
        System.out.print(Ansi.ansi()
                .fg(Ansi.Color.GREEN)
                .a(started));

        final Thread thread = new Thread(runnable);
        thread.start();

        final String POINT = ".";

        int counter = 0;

        while(thread.isAlive()) {
            Ansi ansi = new Ansi().fg(Ansi.Color.GREEN).a(started);

            for(int i = 0;i < counter + 1;i++) {
                ansi.a(POINT);
            }

            ansi.a("                     ");

            counter = (counter + 1) % 4;

            System.out.print("\r");
            System.out.print(ansi);
            System.out.flush();
            Thread.sleep(500L);
        }
        System.out.print("\r");
        System.out.print(Ansi.ansi()
                .fg(Ansi.Color.GREEN)
                .a(stopped)
                .fg(Ansi.Color.WHITE)
                .a("                     "));
        System.out.println("\r");
        System.out.flush();
    }

    public static void message(String message) {
        System.out.println(Ansi.ansi()
                .fg(Ansi.Color.GREEN)
                .a(message)
                .fg(Ansi.Color.WHITE));
    }
}
