package com.digdes.school;

import java.io.*;
import java.util.*;


class JavaSchoolStarter{
    List<Map<String, Object>> data;
    public JavaSchoolStarter(List<Map<String, Object>> data) {
        this.data = data;
    }

    private static Object parseValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return false;
        } else if (value.matches("\\d+")) {
            return Integer.parseInt(value);
        } else if (value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        } else {
            return value;
            //throw new IllegalArgumentException("Invalid value: " + value);
        }
    }

    public List<Map<String, Object>> executeCommand(String command) {
        //String[] tokens = command.split("\\s+");
        String[] tokens = command.split("\\s+|(?<=\\w)(?=\\=)|(?<=\\=)(?=\\w)|(?<=\\,)(?=\\w)");
        String commandType = tokens[0];
        commandType.toLowerCase();

        switch (commandType.toUpperCase()) {
            case "INSERT":
                return insert(tokens);
            case "UPDATE":
                return update(tokens);
            case "DELETE":
                return delete(tokens);
            case "SELECT":
                return select(tokens);
            default:
                throw new IllegalArgumentException("Invalid command type");
        }
    }

    private String getWhereClause(String[] tokens) {
        for (int i = 1; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("where")) {
                StringBuilder sb = new StringBuilder();
                for (int j = i + 1; j < tokens.length; j++) {
                    sb.append(tokens[j]).append(" ");
                }
                return sb.toString().trim();
            }
        }
        return null;
    }

    private boolean matchesWhereClause(Map<String, Object> row, String whereClause) {
// парсим выражение услови€
        String[] conditions = whereClause.split("(?i)where")[0].split("(?i)and|or");
        String conditionsKey = whereClause.split("(?i)like|ilike|=|!=|>=|<=|>|<")[0].replace("С", "").replace("С","");
// парсим операторы сравнени€ и значени€
        String[] opsAndVals = new String[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            String[] parts = conditions[i].split("(?i)like|ilike|=|!=|>=|<=|>|<");
            String operator = conditions[i].substring(parts[0].length(), conditions[i].length() - parts[1].length()).trim();
            String value = parts[1].trim();
            opsAndVals[i] = operator + "," + value;
        }
// провер€ем условие выборки дл€ каждой строки
        for (String condition : opsAndVals) {
            String[] parts = condition.split(",");
            String operator = parts[0];
            String value = parts[1];

            boolean match = false;
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String columnName = entry.getKey().replace("'", "");
                columnName = columnName.replace("С","").replace("Т","");
                Object columnValue = entry.getValue();

                // провер€ем соответствие имени колонки if ((row.containsKey(columnName)))
                //columnName.equalsIgnoreCase(operator)
                if (conditionsKey.equalsIgnoreCase(columnName)) {
                    // провер€ем соответствие значени€ в колонке
                    switch (operator) {
                        case "like":
                        case "ilike":
                            match = columnValue.toString().toLowerCase().contains(value.toLowerCase());
                            break;
                        case "=":
                            match = columnValue.equals(parseValue(value));
                            break;
                        case "!=":
                            match = !columnValue.equals(parseValue(value));
                            break;
                        case ">=":
                            match = ((Number) columnValue).doubleValue() >= ((Number) parseValue(value)).doubleValue();
                            break;
                        case "<=":
                            match = ((Number) columnValue).doubleValue() <= ((Number) parseValue(value)).doubleValue();
                            break;
                        case ">":
                            match = ((Number) columnValue).doubleValue() > ((Number) parseValue(value)).doubleValue();
                            break;
                        case "<":
                            match = ((Number) columnValue).doubleValue() < ((Number) parseValue(value)).doubleValue();
                            break;
                    }

                }
            }
            // если хот€ бы одно условие не совпало, возвращаем false
            if (!match) {
                return false;
            }
        }
// все услови€ совпали
        return true;
    }

    private List<Map<String, Object>> insert(String[] tokens) {

        Map<String, Object> row = new HashMap<>();

        for (int i = 2; i < tokens.length; i += 2) {
            String columnName = tokens[i].replace("'", "");
            String value = tokens[i + 1].replace("'", "");
            if(value != null && value.equals("="))
            {
                value = tokens[i + 2].replace("'", "");
                i+=2;
            }
            value = value.replace(",", "");
            Object parsedValue = parseValue(value);
            if (columnName.endsWith("=")) {
                columnName = columnName.substring(0, columnName.length() - 1);
            }
            row.put(columnName, parsedValue);
        }
        data.add(row);
        System.out.println(data);
        return Collections.singletonList(row);
    }

    private List<Map<String, Object>> delete(String[] tokens) {
        String whereClause = getWhereClause(tokens);
        List<Map<String, Object>> deletedRows = new ArrayList<>();
        Iterator<Map<String, Object>> iterator = data.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> row = iterator.next();
            if (matchesWhereClause(row, whereClause)) {
                iterator.remove();
                deletedRows.add(row);
            }
        }
        System.out.println(data);
        return deletedRows;
    }

    private List<Map<String, Object>> update(String[] tokens) {
        String whereClause = getWhereClause(tokens);
        List<Map<String, Object>> updatedRows = new ArrayList<>();
        List<String> keysToRemove = new ArrayList<>();

        for (Map<String, Object> row : data) {
            if (matchesWhereClause(row, whereClause)) {
                for (int i = 2; i < tokens.length; i += 2) {

                    if(tokens[i] != null && tokens[i].equals("where"))
                    {
                        i++;
                        continue;
                    }

                    String columnName = tokens[i].replace("'", "");
                    columnName = columnName.replace("=", "");

                    String value = tokens[i+1].replace("'", "");
                    Object parsedValue = parseValue(value);
                    columnName = "'" + columnName + "'";

                    if (row.containsKey(columnName)) {
                        row.replace(columnName, parsedValue);
                    } else {
                        row.put(columnName, parsedValue);
                    }
                    row.put(columnName, parsedValue);
                }
                updatedRows.add(row);
            }
        }

        System.out.println(data);
        return updatedRows;
    }

    private List<Map<String, Object>> select(String[] tokens) {
        String whereClause = getWhereClause(tokens);
        List<Map<String, Object>> selectedRows = new ArrayList<>();
        for (Map<String, Object> row : data) {
            if (matchesWhereClause(row, whereClause)) {
                selectedRows.add(row);
            }
        }
        System.out.println(selectedRows);
        return selectedRows;
    }


    public static void main(String[] args) throws IOException {

        //Map<String, Object> row = new HashMap<>();

        InputStreamReader in = new InputStreamReader(System.in);
        BufferedReader bf = new BufferedReader(in);


        List<Map<String, Object>> data = new ArrayList<>();

        var obj =new JavaSchoolStarter(data);
        System.out.println("¬ведите команду: ");


            //obj.executeCommand(bf.readLine());
            obj.executeCommand("INSERT VALUES СlastNameС = СѕетровС , СidС=1, СageС=25, СcostС=3.5, СactiveС=false");
            obj.executeCommand("INSERT VALUES СlastNameС = С‘едоровС , СidС=3, СageС=40, СactiveС=true");
            obj.executeCommand("select where СidС<3");

    }
}


class Keyin {

    //*******************************
    //   support methods
    //*******************************
    //Method to display the user's prompt string
    public static void printPrompt(String prompt) {
        System.out.print(prompt + " ");
        System.out.flush();
    }

    //Method to make sure no data is available in the
    //input stream
    public static void inputFlush() {
        int dummy;
        int bAvail;

        try {
            while ((System.in.available()) != 0)
                dummy = System.in.read();
        } catch (java.io.IOException e) {
            System.out.println("Input error");
        }
    }
    public static String inString() {
        int aChar;
        String s = "";
        boolean finished = false;

        while (!finished) {
            try {
                aChar = System.in.read();
                if (aChar < 0 || (char) aChar == '\n')
                    finished = true;
                else if ((char) aChar != '\r')
                    s = s + (char) aChar; // Enter into string
            }

            catch (java.io.IOException e) {
                System.out.println("Input error");
                finished = true;
            }
        }
        return s;
    }

    public static int inInt(String prompt) {
        while (true) {
            inputFlush();
            printPrompt(prompt);
            try {
                return Integer.valueOf(inString().trim()).intValue();
            }

            catch (NumberFormatException e) {
                System.out.println("Invalid input. Not an integer");
            }
        }
    }

}