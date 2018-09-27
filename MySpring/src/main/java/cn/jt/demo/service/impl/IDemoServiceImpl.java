package cn.jt.demo.service.impl;

import cn.jt.demo.service.IDemoService;
import cn.jt.mvcframework.annotation.JTService;

@JTService
public class IDemoServiceImpl implements IDemoService{

    public String get(String name) {
        return "666";
    }
}
