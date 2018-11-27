package com.notebook.cloud.controller;

import com.google.gson.Gson;
import com.notebook.cloud.bean.NoteContextBean;
import com.notebook.cloud.lock.DistributedLock;
import com.notebook.cloud.util.PropertyReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Controller
public class IndexController {

    int n=500;

    private static JedisPool pool = null;

    private DistributedLock lock = new DistributedLock(pool);

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置最大连接数
        config.setMaxTotal(200);
        // 设置最大空闲数
        config.setMaxIdle(8);
        // 设置最大等待时间
        config.setMaxWaitMillis(1000 * 100);
        // 在borrow一个jedis实例时，是否需要验证，若为true，则所有jedis实例均是可用的
        config.setTestOnBorrow(true);
        pool = new JedisPool(config, "127.0.0.1", 6379, 3000);
    }

    @RequestMapping("/index")
    public String index(Model model) throws Exception {
        System.out.println(PropertyReader.getProperty("project.path"));
        getNoteList(model);
        return "/index";
    }

    @RequestMapping("/edit")
    public String edit() throws Exception {
        System.out.println("edit");
        return "/edit";
    }
    @RequestMapping("/lock1")
    public void testLock4() throws Exception {
        // 返回锁的value值，供释放锁时候进行判断
        //String indentifier = lock.lockWithTimeout("resource", 5000, 1000);
        System.out.println(Thread.currentThread().getName() + "获得了锁");
        Thread.sleep(1000);
        System.out.println(--n);
        //lock.releaseLock("resource", indentifier);
    }

    public static void getNoteList(Model model){
        File file=new File("note");
        List<NoteContextBean> beans=new ArrayList<>();
        if(file.isDirectory()) {
            String[] files=file.list();
            NoteContextBean bean=null;
            for(String s:files){
                bean=new NoteContextBean();
                bean.setTitle(s.replaceAll("\\.md",""));
                beans.add(bean);
            }
        }
        model.addAttribute("noteList",beans);
    }
}
