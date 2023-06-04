package com.github.noconnor.junitperf.reporting.providers.utils;

import java.lang.reflect.Field;
import java.util.List;

public class ViewProcessor {
    
    public static String populateTemplateFromCollection(List<?> objects, String prefix, String template) throws IllegalAccessException {
        StringBuilder result = new StringBuilder();
        for (Object data : objects) {
            String temp = populateTemplate(data, prefix, template);
            result.append(temp).append("\n");
        }
        return result.toString();
    }

    public static String populateTemplate(Object obj, String prefix, String template) throws IllegalAccessException {
        String temp = template;
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            String target = "\\{\\{ " + prefix + "." + f.getName() + " \\}\\}";
            Object value = f.get(obj);
            temp = temp.replaceAll(target, value.toString());
        }
        return temp;
    }

}
