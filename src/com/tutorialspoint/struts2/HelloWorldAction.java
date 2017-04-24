package com.tutorialspoint.struts2;

import com.opensymphony.xwork2.*;
import com.opensymphony.xwork2.util.ValueStack;

import java.util.HashMap;
import java.util.Map;

class MyAction extends ActionSupport{
    public static String GOOD = SUCCESS;
    public static String BAD = ERROR;
}



public class HelloWorldAction extends ActionSupport{
    private String name;

    public HelloWorldAction() {
    }

    public String execute() throws Exception {
        System.out.println("Inside action....");
        /*if ("HAHA".equals(name)) {
            return MyAction.GOOD;
        }*/

        ValueStack stack = ActionContext.getContext().getValueStack();
        Map<String, String> context = new HashMap<String, String>();

        context.put("key1", "This is key1");
        context.put("key2", "This is key2");
        stack.push(context);

        System.out.println("Size of the valueStack: " + stack.size());
        return MyAction.GOOD;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}