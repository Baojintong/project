package com.notebook.cloud.controller;

import com.google.gson.Gson;
import com.notebook.cloud.bean.NoteContextBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/operation")
public class OperationController {

    @RequestMapping("/save")
    public @ResponseBody String save(@RequestParam String text,String name) throws IOException {
        FileWriter fileWriter=new FileWriter("note/a.md");
        int ch=0;
        fileWriter.write(text);
        fileWriter.close();
        System.out.println(text);
        return "";
    }

    @RequestMapping("/read")
    public String read(String name, Model model) throws Exception {
        File file=new File("note/"+name+".md");
        StringBuffer stringBuffer=new StringBuffer();
        FileReader fileReader=new FileReader(file);
        BufferedReader bufferedReader=new BufferedReader(fileReader);
        while(bufferedReader.read()!=-1){
            stringBuffer.append(bufferedReader.readLine()+"\n");
        }
        NoteContextBean context=new NoteContextBean();
        context.setContext(stringBuffer.toString());
        model.addAttribute("context",context);
        IndexController.getNoteList(model);
        return "edit";
    }

    @RequestMapping("/readList")
    public @ResponseBody String readList(Model model) throws Exception {
        Gson gson=new Gson();
        File file=new File("note");
        List<NoteContextBean> beans=new ArrayList<>();
        NoteContextBean bean=null;
        for(String s:file.list()){
            bean=new NoteContextBean();
            bean.setTitle(s.replaceAll("\\.md",""));
        }
        return gson.toJson(file.list()).replaceAll("\\.md","");
    }
}
