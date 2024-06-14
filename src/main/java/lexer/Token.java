package lexer;

public class Token {
    private TokenType type;
    private String value;
    private int startIndex;
    private int endIndex;

    public Token(TokenType type, String value, int startIndex, int endIndex){
        this.type = type;
        this.value = value;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public TokenType getType(){
        return type;
    }

    public String getValue(){
        return value;
    }

    public int getStartingIndex(){
        return startIndex;
    }

    public int getEndingIndex(){
        return endIndex;
    }

    @Override
    public String toString(){
        return "Token{type=" + type + ", value='" + value + '\''+  '}';
    }
}
