package com.github.noconnor.junitperf.reporting.providers.utils;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.List;

@UtilityClass
public class ViewProcessor {

    @SuppressWarnings("rawtypes")
    public static String populateTemplate(Object obj, String prefix, String template) throws IllegalAccessException {
        String temp = template;
        Field[] fields = obj.getClass().getDeclaredFields();

        if (obj instanceof Iterable) {
            StringBuilder result = new StringBuilder();
            for (Object data : (List) obj) {
                String tmp = populateTemplate(data, prefix, template);
                result.append(tmp).append("\n");
            }
            temp = result.toString();
        } else {
            for (Field f : fields) {
                f.setAccessible(true);
                String target = "\\{\\{ " + prefix + "." + f.getName() + " \\}\\}";
                Object value = f.get(obj);
                temp = temp.replaceAll(target, String.valueOf(value));
            }
        }
        return temp;
    }

}
