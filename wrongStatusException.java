package application;

public class wrongStatusException extends IllegalArgumentException{
    public wrongStatusException(String s) {
        super(s);
    }
}
