package myrpc.services;


public class Hello implements Rzk {
    public String  hi(String msg){
        return "hello "+msg;
    }
}
