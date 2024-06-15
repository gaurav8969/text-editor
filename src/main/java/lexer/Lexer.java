package lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Lexer {
    private static final String KEYWORD_REGEX = "\\b(if|else|while|for|return|public|static|void|main|" +
            "String|new|private)\\b";
    private static final String ANNOTATION_REGEX = "(?<![\\w@])@Override(?![\\w@])";
    private static final String IDENTIFIER_REGEX = "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b";
    private static final String NUMBER_REGEX = "\\b(\\d+)\\b";
    private static final String OPERATOR_REGEX = "(==|!=|<=|>=|&&|\\|\\||[+\\-*/=<>!&|^?:%])";
    private static final String SYMBOL_REGEX = "[;,\\[\\]\\(\\){}.<]";
    private static final String WHITESPACE_REGEX = "\\s+";
    private static final String STRING_REGEX = "\"[a-zA-Z0-9_\\s\\n;,\\+\\(\\)\\[\\]{}.<>\\\\]*\"";
    private static final String FUNCTION_REGEX = "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?=\\()";

    private static final Pattern Token_PATTERN = Pattern.compile(
        String.format("(?<ANNOTATION>%s)|(?<KEYWORD>%s)|(?<FUNCTION>%s)|(?<IDENTIFIER>%s)|(?<NUMBER>%s)|(?<OPERATOR>%s)" +
                        "|(?<STRING>%s)|(?<SYMBOL>%s)|(?<WHITESPACE>%s)",
                ANNOTATION_REGEX, KEYWORD_REGEX,FUNCTION_REGEX, IDENTIFIER_REGEX, NUMBER_REGEX, OPERATOR_REGEX, STRING_REGEX,
                SYMBOL_REGEX,WHITESPACE_REGEX)
    );

    public List<Token> tokenize(String input){
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = Token_PATTERN.matcher(input);

        while (matcher.find()) {
            if (matcher.group("FUNCTION") != null) {
                tokens.add(new Token(TokenType.FUNCTION, matcher.group("FUNCTION"), matcher.start(),matcher.end()));
            } else if(matcher.group("ANNOTATION") != null){
                tokens.add(new Token(TokenType.ANNOTATION, matcher.group("ANNOTATION"), matcher.start(), matcher.end()));
            } else if (matcher.group("KEYWORD") != null) {
                tokens.add(new Token(TokenType.KEYWORD, matcher.group("KEYWORD"), matcher.start(),matcher.end()));
            } else if (matcher.group("IDENTIFIER") != null) {
                tokens.add(new Token(TokenType.IDENTIFIER, matcher.group("IDENTIFIER"),matcher.start(), matcher.end()));
            } else if (matcher.group("NUMBER") != null) {
                tokens.add(new Token(TokenType.NUMBER, matcher.group("NUMBER"), matcher.start(), matcher.end()));
            } else if(matcher.group("OPERATOR") != null){
                tokens.add(new Token(TokenType.OPERATOR,matcher.group("OPERATOR"),matcher.start(),matcher.end()));
            } else if(matcher.group("STRING") != null){
                tokens.add(new Token(TokenType.STRING, matcher.group(), matcher.start(), matcher.end()));
            } else if (matcher.group("SYMBOL") != null) {
                tokens.add(new Token(TokenType.SYMBOL, matcher.group("SYMBOL"), matcher.start(), matcher.end()));
            } else if (matcher.group("WHITESPACE") != null) {
                tokens.add(new Token(TokenType.WHITESPACE, matcher.group("WHITESPACE"), matcher.start(), matcher.end()));
            }
        }

        return tokens;
    }
}