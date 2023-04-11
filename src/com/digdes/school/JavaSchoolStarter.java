package com.digdes.school;

import java.io.*;
import java.util.*;


class JavaSchoolStarter{
    List<Map<String, Object>> data;
    public JavaSchoolStarter() {
        data = new ArrayList<>();
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
        }
    }

    public List<Map<String, Object>> execute(String command) {
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
                throw new IllegalArgumentException("Ќеверна€ команда");
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
        String[] conditions = whereClause.split("(?i)where")[0].split("(?i)and|or");
        String conditionsKey = whereClause.split("(?i)like|ilike|=|!=|>=|<=|>|<")[0].replace("'","").replace("С","'");
        String[] opsAndVals = new String[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            String[] parts = conditions[i].split("(?i)like|ilike|=|!=|>=|<=|>|<");
            String operator = conditions[i].substring(parts[0].length(), conditions[i].length() - parts[1].length()).trim();
            String value = parts[1].trim();
            opsAndVals[i] = operator + "," + value;
        }
        for (String condition : opsAndVals) {
            String[] parts = condition.split(",");
            String operator = parts[0];
            String value = parts[1];

            boolean match = false;
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String columnName = entry.getKey().replace("С", "");
                columnName = columnName.replace("С","'").replace("'","");
                Object columnValue = entry.getValue();

                if (conditionsKey.equalsIgnoreCase(columnName)) {
                    switch (operator) {
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
                        default:
                            throw new IllegalArgumentException("ƒанный оператор не поддерживаетс€");
                    }

                }
            }
            if (!match) {
                return false;
            }
        }
        return true;
    }

    private List<Map<String, Object>> insert(String[] tokens) {

        Map<String, Object> row = new HashMap<>();

        for (int i = 2; i < tokens.length; i += 2) {
            String columnName = tokens[i].replace("'", "С").replace("С", "");
            String value = tokens[i + 1].replace("'", "С").replace("С", "");
            if(value != null && value.equals("="))
            {
                value = tokens[i + 2].replace("'", "С").replace("С", "").replace(",", "");
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

                    String columnName = tokens[i].replace("'", "С").replace("С", "");
                    columnName = columnName.replace("=", "");

                    String value = tokens[i+1].replace("'", "С").replace("С", "");
                    Object parsedValue = parseValue(value);
                    //columnName = "С" + columnName + "С";

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
        InputStreamReader in = new InputStreamReader(System.in);
        BufferedReader bf = new BufferedReader(in);

        var obj =new JavaSchoolStarter();
        System.out.println("¬ведите команду: ");
        for(;;)
            obj.execute(bf.readLine());
    }
}