package com.example.myapp.self;

public class MyLogin {
    private boolean logined;
    private static MyLogin myLogin;
    public static MyLogin getMyLogin(){
        if(myLogin==null){
            myLogin=new MyLogin();
        }
        return myLogin;
    }

    public boolean isLogin() {
        return logined;
    }

    public void setLogin(boolean logined) {
        this.logined = logined;
    }
}
