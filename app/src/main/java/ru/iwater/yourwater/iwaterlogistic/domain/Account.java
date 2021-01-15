package ru.iwater.yourwater.iwaterlogistic.domain;

public class Account {
    String id = "";
    String company ="";
    String login = "";
    String password = "";
    String session = "";
    String token = "";

    public Account(String id, String company, String login, String password, String session){
        this.id = id;
        this.company = company;
        this.login = login;
        this.password = password;
        this.session = session;
    }

    public Account(){};

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompany(){
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
