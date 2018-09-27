package com.notebook.cloud.controller;

import com.google.gson.Gson;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

@RestController
@RequestMapping("/operation")
public class OperationController {

    @RequestMapping("/save")
    public String save(@RequestParam String text) throws IOException {
        FileWriter fileWriter=new FileWriter("note/a.md");
        int ch=0;
        fileWriter.write(text);
        fileWriter.close();
        System.out.println(text);
        return "";
    }

    @RequestMapping("/readList")
    public String readList(){
        Gson gson=new Gson();
        File file=new File("note");
        return gson.toJson(file.list()).replaceAll("\\.md","");
    }

    @RequestMapping("/read")
    public String read(@RequestParam String name) throws Exception {
        Gson gson=new Gson();
        File file=new File("note/"+name+".md");
        StringBuffer stringBuffer=new StringBuffer();
        int BUFFER_SIZE=1024;
        FileReader fileReader=new FileReader(file);
        BufferedReader bufferedReader=new BufferedReader(fileReader);
        bufferedReader.readLine();
        while(bufferedReader.read()!=-1){
            stringBuffer.append(bufferedReader.readLine()+"\n");
        }
        return stringBuffer.toString();
    }
}
