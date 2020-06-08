package com.rajansurani.taskapp.Model;

import java.io.Serializable;

public class Files implements Serializable {

    private String name, url;

    public Files(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public Files(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
