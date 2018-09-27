package cn.jt.demo.action;

import cn.jt.demo.service.IDemoService;
import cn.jt.mvcframework.annotation.JTAutowired;
import cn.jt.mvcframework.annotation.JTController;
import cn.jt.mvcframework.annotation.JTRequestMapping;
import cn.jt.mvcframework.annotation.JTRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@JTController
@JTRequestMapping("demo")
public class DemoAction {
    @JTAutowired
    private IDemoService service;

    @JTRequestMapping("query")
    public void query(HttpServletRequest req, HttpServletResponse rep, @JTRequestParam("name") String name){

        String result=service.get(name);
        try{

            rep.getWriter().write(result);

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @JTRequestMapping("add")
    public void add(HttpServletRequest req, HttpServletResponse rep, @JTRequestParam("a") Integer a, @JTRequestParam("b") Integer b){
        try{
            rep.getWriter().write(a+"+"+b+"="+(a+b));
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @JTRequestMapping("remove")
    public void remove(HttpServletRequest req, HttpServletResponse rep, @JTRequestParam("id") Integer id){

    }
}
