package application;

public class noExsistedException extends IllegalArgumentException{
    public noExsistedException(String s) {
        super(s);
    }
}
