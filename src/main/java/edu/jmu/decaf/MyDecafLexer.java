package edu.jmu.decaf;

import edu.jmu.decaf.Token.Type;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

class MyDecafLexer extends DecafLexer {
   public Queue<Token> lex(BufferedReader input, String filename) throws IOException, InvalidTokenException {
      Queue<Token> tokens = new ArrayDeque();
      if (input == null) {
         throw new IllegalArgumentException("Invalid token input stream.");
      } else if (filename == null) {
         throw new IllegalArgumentException("Invalid filename.");
      } else {
         this.addIgnoredPattern("\\s+");
         this.addIgnoredPattern("//.*");
         this.addTokenPattern(Type.HEX, "0x(0|[1-9a-fA-F][0-9a-fA-F]*)");
         this.addTokenPattern(Type.DEC, "0|[1-9][0-9]*");
         this.addTokenPattern(Type.STR, "\"([^\\\\\"]+|\\\\[nt\"\\\\])*\"");
         this.addTokenPattern(Type.SYM, "<=|>=|&&|\\|\\||==|!=");
         this.addTokenPattern(Type.SYM, "\\(|\\)|\\[|\\]|\\{|\\}|,|\\.|;|=|\\+|-|\\*|/|%|!|<|>");
         this.addTokenPattern(Type.ID, "[a-zA-Z]\\w*");
         String[] KEYWORDS = new String[]{"def", "if", "while", "return", "break", "continue", "else", "int", "bool", "void", "true", "false"};
         String[] RESERVED = new String[]{"for", "callout", "class", "interface", "extends", "implements", "new", "this", "string", "float", "double", "null"};
         Set<String> keywords = new HashSet(Arrays.asList(KEYWORDS));
         Set<String> reserved = new HashSet(Arrays.asList(RESERVED));

         String line;
         for(int lineNumber = 1; (line = input.readLine()) != null; ++lineNumber) {
            SourceInfo source = new SourceInfo(filename, lineNumber);
            StringBuffer buffer = new StringBuffer(line);
            this.discardIgnored(buffer);

            while(buffer.length() > 0) {
               Token t = this.nextToken(buffer);
               if (t == null) {
                  throw new InvalidTokenException("Invalid text at " + source.toString());
               }

               if (keywords.contains(t.text)) {
                  t = new Token(Type.KEY, t.text);
               } else if (reserved.contains(t.text)) {
                  throw new InvalidTokenException("Invalid use of reserved word \"" + t.text + "\" at " + source.toString());
               }

               t.source = source;
               tokens.add(t);
               this.discardIgnored(buffer);
            }
         }

         input.close();
         return tokens;
      }
   }
}
