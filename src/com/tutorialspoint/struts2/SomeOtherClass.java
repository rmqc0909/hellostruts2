package com.tutorialspoint.struts2;

import com.opensymphony.xwork2.ActionSupport;

/**
 * Created by xiedan on 2017/4/19.
 */
public class SomeOtherClass extends ActionSupport {
    public String execute()
    {
        return MyAction.GOOD;
    }
}
