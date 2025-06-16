package com.skrybe.sdk.model;

public class Subscriber {
    private String id;
    private String email;
    private String name;
    private String listId;

    public Subscriber() {}

    public Subscriber(String id, String email, String name, String listId) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.listId = listId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", listId='" + listId + '\'' +
                '}';
    }
} 