package com.zip.Engine;

public class Logger {
    public enum Color {
        //Color end string, color reset
        RESET("RESET","\033[0m"),

        // Regular Colors. Normal color, no bold, background color etc.
        BLACK("BLACK","\033[0;30m"),    // BLACK
        RED("RED","\033[0;31m"),      // RED
        GREEN("GREEN","\033[0;32m"),    // GREEN
        YELLOW("YELLOW","\033[0;33m"),   // YELLOW
        BLUE("BLUE","\033[0;34m"),     // BLUE
        MAGENTA("MAGENTA","\033[0;35m"),  // MAGENTA
        CYAN("CYAN","\033[0;36m"),     // CYAN
        WHITE("WHITE","\033[0;37m"),    // WHITE

        // High Intensity
        BLACK_BRIGHT("black","\033[0;90m"),     // BLACK
        RED_BRIGHT("red","\033[0;91m"),       // RED
        GREEN_BRIGHT("green","\033[0;92m"),     // GREEN
        YELLOW_BRIGHT("yellow","\033[0;93m"),    // YELLOW
        BLUE_BRIGHT("blue","\033[0;94m"),      // BLUE
        MAGENTA_BRIGHT("magenta","\033[0;95m"),   // MAGENTA
        CYAN_BRIGHT("cyan","\033[0;96m"),      // CYAN
        WHITE_BRIGHT("white","\033[0;97m");     // WHITE

        private final String key;
        private final String code;

        Color(String key, String code) {
            this.key = key;
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }
    public enum Level {
        INFO("{black}[{white}Info{black}] {white}{message}"),
        WARNING("{black}[{yellow}Warning{black}] {white}{message}"),
        Error("{black}[{red}Error{black}] {white}{message}"),
        LINE("{black} > {white}{message}");
        private final String message;

        Level(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }
    public static String parseLog(Level level, String Message) {
        String template = level.message;

        for (int i = 0; i < Color.values().length; i++) {
            template = template.replace("{"+Color.values()[i].key+"}",Color.values()[i].code);
        }
        template = template.replace("{message}",Message);
        return template;
    }
    public static void Print(Level level, String Message) {
        System.out.println(parseLog(level, Message));
    }
    public static void Print(String... Message) {
        for (int i = 0; i < Message.length; i++) {
            String[] tmp = Message[i].split("\n");
            for (int j = 0; j < tmp.length; j++) {
                System.out.println(parseLog(Level.LINE, tmp[j]));
            }
        }
    }
    public static void Info(String Message) {
        System.out.println(parseLog(Level.INFO, Message));
    }
    public static void Warning(String Message) {
        System.out.println(parseLog(Level.WARNING, Message));
    }
    public static void Error(String Message) {
        System.out.println(parseLog(Level.Error, Message));
    }
}
