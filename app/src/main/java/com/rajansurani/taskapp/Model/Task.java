package com.rajansurani.taskapp.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class Task implements Serializable {

    private String title, content, createdBy;
    private ArrayList<String> members;
    private ArrayList<Files> files;

    public Task(String title, String content, String createdBy, ArrayList<String> members, ArrayList<Files> files) {
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.members = members;
        this.files = files;
    }
    public Task(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public ArrayList<Files> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<Files> files) {
        this.files = files;
    }
}
