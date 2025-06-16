package com.skrybe.sdk.model;

public class SkrybeList {
    private String id;
    private String name;

    public SkrybeList() {}

    public SkrybeList(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "SkrybeList{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
} 