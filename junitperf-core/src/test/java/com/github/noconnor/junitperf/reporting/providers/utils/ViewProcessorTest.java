package com.github.noconnor.junitperf.reporting.providers.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ViewProcessorTest {

    
    @Test
    public void templateShouldBePopulatedWithObjectData() throws IllegalAccessException {
        Data data = new Data();
        data.name = "test";
        data.email = "a@b.ie";
        data.salary = 123.56F;
        
        String template = "Name: {{ d.name }}\n Email: {{ d.email }}";
        String result = ViewProcessor.populateTemplate(data, "d", template);
        assertEquals( "Name: test\n Email: a@b.ie" , result);
    }

    @Test
    public void templateShouldDuplicatedWhenListDataIsPassed() throws IllegalAccessException {
        Data data1 = new Data();
        data1.name = "test";
        data1.email = "a@b.ie";
        data1.salary = 123.56F;

        Data data2 = new Data();
        data2.email = "t@d.ie";
        data2.salary = 13.4F;

        List<Data> listData = new ArrayList<>();
        listData.add(data1);
        listData.add(data2);

        String template = "Name: {{ ctxt.name }}\n Email: {{ ctxt.email }}\n Salary: {{ ctxt.salary }}";
        String expected = "Name: test\n Email: a@b.ie\n Salary: 123.56\nName: null\n Email: t@d.ie\n Salary: 13.4\n";
        
        String result = ViewProcessor.populateTemplate(listData, "ctxt", template);
        assertEquals( expected , result);
    }


    static class Data {
        int id;
        String name;
        String email;
        float salary;
    }
}