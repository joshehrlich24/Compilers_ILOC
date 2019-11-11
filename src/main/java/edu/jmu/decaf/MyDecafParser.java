package edu.jmu.decaf;

import edu.jmu.decaf.ASTBinaryExpr.BinOp;
import edu.jmu.decaf.ASTFunction.Parameter;
import edu.jmu.decaf.ASTNode.DataType;
import edu.jmu.decaf.ASTUnaryExpr.UnaryOp;
import edu.jmu.decaf.Token.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

class MyDecafParser extends DecafParser {
   public ASTProgram parse(Queue<Token> tokens) throws InvalidSyntaxException {
      return this.parseProgram(tokens);
   }

   public ASTProgram parseProgram(Queue<Token> tokens) throws InvalidSyntaxException {
      ASTProgram program = new ASTProgram();
      if (tokens == null) {
         throw new IllegalArgumentException("Invalid token input queue.");
      } else {
         SourceInfo src = this.getCurrentSourceInfo(tokens);
         program.setSourceInfo(src);

         while(tokens.size() > 0) {
            if (this.isNextTokenKeyword(tokens, "def")) {
               program.functions.add(this.parseFunction(tokens));
            } else {
               program.variables.add(this.parseVariable(tokens));
            }
         }

         if (tokens.size() > 0) {
            throw new InvalidSyntaxException("Extraneous input at " + this.getCurrentSourceInfo(tokens).toString());
         } else {
            return program;
         }
      }
   }

   public ASTFunction parseFunction(Queue<Token> tokens) throws InvalidSyntaxException {
      SourceInfo src = this.getCurrentSourceInfo(tokens);
      this.matchKeyword(tokens, "def");
      DataType type = this.parseType(tokens);
      String name = this.parseID(tokens);
      this.matchSymbol(tokens, "(");
      List<Parameter> params = new ArrayList();
      if (!this.isNextTokenSymbol(tokens, ")")) {
         params.add(this.parseParameter(tokens));

         while(!this.isNextTokenSymbol(tokens, ")")) {
            this.matchSymbol(tokens, ",");
            params.add(this.parseParameter(tokens));
         }
      }

      this.matchSymbol(tokens, ")");
      ASTBlock body = this.parseBlock(tokens);
      ASTFunction func = new ASTFunction(name, type, body);
      Iterator var8 = params.iterator();

      while(var8.hasNext()) {
         Parameter p = (Parameter)var8.next();
         func.parameters.add(p);
      }

      func.setSourceInfo(src);
      return func;
   }

   public Parameter parseParameter(Queue<Token> tokens) throws InvalidSyntaxException {
      DataType type = this.parseType(tokens);
      String name = this.parseID(tokens);
      return new Parameter(name, type);
   }

   public ASTVariable parseVariable(Queue<Token> tokens) throws InvalidSyntaxException {
      SourceInfo src = this.getCurrentSourceInfo(tokens);
      DataType type = this.parseType(tokens);
      String name = this.parseID(tokens);
      ASTVariable var;
      if (this.isNextTokenSymbol(tokens, "[")) {
         this.matchSymbol(tokens, "[");
         int len = this.parseInt(tokens);
         this.matchSymbol(tokens, "]");
         var = new ASTVariable(name, type, len);
      } else {
         var = new ASTVariable(name, type);
      }

      this.matchSymbol(tokens, ";");
      var.setSourceInfo(src);
      return var;
   }

   public ASTBlock parseBlock(Queue<Token> tokens) throws InvalidSyntaxException {
      SourceInfo src = this.getCurrentSourceInfo(tokens);
      ASTBlock block = new ASTBlock();
      this.matchSymbol(tokens, "{");

      while(this.isNextTokenKeyword(tokens, "int") || this.isNextTokenKeyword(tokens, "bool") || this.isNextTokenKeyword(tokens, "void")) {
         block.variables.add(this.parseVariable(tokens));
      }

      while(!this.isNextTokenSymbol(tokens, "}")) {
         block.statements.add(this.parseStatement(tokens));
      }

      this.matchSymbol(tokens, "}");
      block.setSourceInfo(src);
      return block;
   }

   public ASTStatement parseStatement(Queue<Token> tokens) throws InvalidSyntaxException {
      SourceInfo src = this.getCurrentSourceInfo(tokens);
      ASTStatement stmt = null;
      if (this.isNextToken(tokens, Type.ID)) {
         String name = this.parseID(tokens);
         if (!this.isNextTokenSymbol(tokens, "(")) {
            ASTExpression idx = null;
            if (this.isNextTokenSymbol(tokens, "[")) {
               this.matchSymbol(tokens, "[");
               idx = this.parseExpression(tokens);
               this.matchSymbol(tokens, "]");
            }

            ASTLocation loc = new ASTLocation(name, idx);
            loc.setSourceInfo(src);
            this.matchSymbol(tokens, "=");
            ASTExpression expr = this.parseExpression(tokens);
            stmt = new ASTAssignment(loc, expr);
         } else {
            ASTVoidFunctionCall fcall = new ASTVoidFunctionCall(name);
            this.matchSymbol(tokens, "(");
            if (!this.isNextTokenSymbol(tokens, ")")) {
               fcall.arguments.add(this.parseExpression(tokens));

               while(this.isNextTokenSymbol(tokens, ",")) {
                  this.matchSymbol(tokens, ",");
                  fcall.arguments.add(this.parseExpression(tokens));
               }
            }

            this.matchSymbol(tokens, ")");
            stmt = fcall;
         }

         this.matchSymbol(tokens, ";");
      } else {
         ASTExpression guard;
         ASTBlock ifBlock;
         if (this.isNextTokenKeyword(tokens, "if")) {
            this.matchKeyword(tokens, "if");
            this.matchSymbol(tokens, "(");
            guard = this.parseExpression(tokens);
            this.matchSymbol(tokens, ")");
            ifBlock = this.parseBlock(tokens);
            ASTBlock elseBlock = null;
            if (this.isNextTokenKeyword(tokens, "else")) {
               this.matchKeyword(tokens, "else");
               elseBlock = this.parseBlock(tokens);
            }

            stmt = new ASTConditional(guard, ifBlock, elseBlock);
         } else if (this.isNextTokenKeyword(tokens, "while")) {
            this.matchKeyword(tokens, "while");
            this.matchSymbol(tokens, "(");
            guard = this.parseExpression(tokens);
            this.matchSymbol(tokens, ")");
            ifBlock = this.parseBlock(tokens);
            stmt = new ASTWhileLoop(guard, ifBlock);
         } else if (this.isNextTokenKeyword(tokens, "return")) {
            this.matchKeyword(tokens, "return");
            guard = null;
            if (!this.isNextTokenSymbol(tokens, ";")) {
               guard = this.parseExpression(tokens);
            }

            stmt = new ASTReturn(guard);
            this.matchSymbol(tokens, ";");
         } else if (this.isNextTokenKeyword(tokens, "continue")) {
            this.matchKeyword(tokens, "continue");
            stmt = new ASTContinue();
            this.matchSymbol(tokens, ";");
         } else {
            if (!this.isNextTokenKeyword(tokens, "break")) {
               throw new InvalidSyntaxException("Invalid statement at " + this.getCurrentSourceInfo(tokens).toString());
            }

            this.matchKeyword(tokens, "break");
            stmt = new ASTBreak();
            this.matchSymbol(tokens, ";");
         }
      }

      ((ASTStatement)stmt).setSourceInfo(src);
      return (ASTStatement)stmt;
   }

   public ASTExpression parseExpression(Queue<Token> tokens) throws InvalidSyntaxException {
      return this.parseBinExpr(tokens, 0);
   }

   public ASTExpression parseBinExpr(Queue<Token> tokens, int level) throws InvalidSyntaxException {
      if (level >= 7) {
         return this.parseNotExpr(tokens);
      } else {
         SourceInfo source = this.getCurrentSourceInfo(tokens);
         ASTExpression root = this.parseBinExpr(tokens, level + 1);
         ((ASTExpression)root).setSourceInfo(source);

         while(this.isNextTokenBinOp(tokens, level)) {
            source = this.getCurrentSourceInfo(tokens);
            BinOp op = this.parseBinaryOperator(tokens);
            ASTExpression rhs = this.parseBinExpr(tokens, level + 1);
            root = new ASTBinaryExpr(op, (ASTExpression)root, rhs);
            ((ASTExpression)root).setSourceInfo(source);
         }

         return (ASTExpression)root;
      }
   }

   public boolean isNextTokenBinOp(Queue<Token> tokens, int level) {
      switch(level) {
      case 0:
         return this.isNextTokenSymbol(tokens, "||");
      case 1:
         return this.isNextTokenSymbol(tokens, "&&");
      case 2:
         return this.isNextTokenSymbol(tokens, "==") || this.isNextTokenSymbol(tokens, "!=");
      case 3:
         return this.isNextTokenSymbol(tokens, "<") || this.isNextTokenSymbol(tokens, ">") || this.isNextTokenSymbol(tokens, "<=") || this.isNextTokenSymbol(tokens, ">=");
      case 4:
         return this.isNextTokenSymbol(tokens, "<") || this.isNextTokenSymbol(tokens, ">") || this.isNextTokenSymbol(tokens, "<=") || this.isNextTokenSymbol(tokens, ">=");
      case 5:
         return this.isNextTokenSymbol(tokens, "+") || this.isNextTokenSymbol(tokens, "-");
      case 6:
         return this.isNextTokenSymbol(tokens, "*") || this.isNextTokenSymbol(tokens, "/") || this.isNextTokenSymbol(tokens, "%");
      default:
         return false;
      }
   }

   public ASTExpression parseNotExpr(Queue<Token> tokens) throws InvalidSyntaxException {
      Object root;
      if (this.isNextTokenSymbol(tokens, "!")) {
         SourceInfo source = this.getCurrentSourceInfo(tokens);
         UnaryOp op = this.parseUnaryOperator(tokens);
         ASTExpression child = this.parseNegExpr(tokens);
         root = new ASTUnaryExpr(op, child);
         ((ASTExpression)root).setSourceInfo(source);
      } else {
         root = this.parseNegExpr(tokens);
      }

      return (ASTExpression)root;
   }

   public ASTExpression parseNegExpr(Queue<Token> tokens) throws InvalidSyntaxException {
      Object root;
      if (this.isNextTokenSymbol(tokens, "-")) {
         SourceInfo source = this.getCurrentSourceInfo(tokens);
         UnaryOp op = this.parseUnaryOperator(tokens);
         ASTExpression child = this.parseBaseExpr(tokens);
         root = new ASTUnaryExpr(op, child);
         ((ASTExpression)root).setSourceInfo(source);
      } else {
         root = this.parseBaseExpr(tokens);
      }

      return (ASTExpression)root;
   }

   public ASTExpression parseBaseExpr(Queue<Token> tokens) throws InvalidSyntaxException {
      ASTExpression expr = null;
      if (this.isNextToken(tokens, Type.ID)) {
         SourceInfo source = this.getCurrentSourceInfo(tokens);
         String name = this.parseID(tokens);
         if (!this.isNextTokenSymbol(tokens, "(")) {
            ASTExpression idx = null;
            if (this.isNextTokenSymbol(tokens, "[")) {
               this.matchSymbol(tokens, "[");
               idx = this.parseExpression(tokens);
               this.matchSymbol(tokens, "]");
            }

            expr = new ASTLocation(name, idx);
         } else {
            ASTFunctionCall fcall = new ASTFunctionCall(name);
            this.matchSymbol(tokens, "(");
            if (!this.isNextTokenSymbol(tokens, ")")) {
               fcall.arguments.add(this.parseExpression(tokens));

               while(this.isNextTokenSymbol(tokens, ",")) {
                  this.matchSymbol(tokens, ",");
                  fcall.arguments.add(this.parseExpression(tokens));
               }
            }

            this.matchSymbol(tokens, ")");
            expr = fcall;
         }

         ((ASTExpression)expr).setSourceInfo(source);
      } else if (!this.isNextToken(tokens, Type.DEC) && !this.isNextToken(tokens, Type.HEX) && !this.isNextToken(tokens, Type.STR) && !this.isNextTokenKeyword(tokens, "true") && !this.isNextTokenKeyword(tokens, "false")) {
         if (!this.isNextTokenSymbol(tokens, "(")) {
            throw new InvalidSyntaxException("Missing expression at " + this.getCurrentSourceInfo(tokens).toString());
         }

         this.matchSymbol(tokens, "(");
         expr = this.parseExpression(tokens);
         this.matchSymbol(tokens, ")");
      } else {
         expr = this.parseLiteral(tokens);
      }

      return (ASTExpression)expr;
   }

   public ASTLiteral parseLiteral(Queue<Token> tokens) throws InvalidSyntaxException {
      if (tokens.size() == 0) {
         throw new InvalidSyntaxException("Missing literal at end of input");
      } else {
         SourceInfo source = this.getCurrentSourceInfo(tokens);
         ASTLiteral lit = null;
         Token peek = (Token)tokens.peek();
         if (peek.type != Type.DEC && peek.type != Type.HEX) {
            if (peek.type == Type.STR) {
               String s = ASTLiteral.removeEscapeCodes(peek.text);
               s = s.substring(1, s.length() - 1);
               lit = new ASTLiteral(DataType.STR, s);
               this.consumeNextToken(tokens);
            } else if (this.isNextTokenKeyword(tokens, "true")) {
               this.matchKeyword(tokens, "true");
               lit = new ASTLiteral(DataType.BOOL, Boolean.TRUE);
            } else {
               if (!this.isNextTokenKeyword(tokens, "false")) {
                  throw new InvalidSyntaxException("Invalid literal \"" + peek.text + "\" at " + peek.source.toString());
               }

               this.matchKeyword(tokens, "false");
               lit = new ASTLiteral(DataType.BOOL, Boolean.FALSE);
            }
         } else {
            Integer i = this.parseInt(tokens);
            lit = new ASTLiteral(DataType.INT, i);
         }

         lit.setSourceInfo(source);
         return lit;
      }
   }

   public DataType parseType(Queue<Token> tokens) throws InvalidSyntaxException {
      if (tokens.size() == 0) {
         throw new InvalidSyntaxException("Missing type specifier at end of input");
      } else {
         DataType t = DataType.VOID;
         Token peek = (Token)tokens.peek();
         if (peek.type == Type.KEY && peek.text.equals("int")) {
            this.consumeNextToken(tokens);
            t = DataType.INT;
         } else if (peek.type == Type.KEY && peek.text.equals("bool")) {
            this.consumeNextToken(tokens);
            t = DataType.BOOL;
         } else {
            if (peek.type != Type.KEY || !peek.text.equals("void")) {
               throw new InvalidSyntaxException("Missing type specifier at " + this.getCurrentSourceInfo(tokens).toString());
            }

            this.consumeNextToken(tokens);
            t = DataType.VOID;
         }

         return t;
      }
   }

   public int parseInt(Queue<Token> tokens) throws InvalidSyntaxException {
      if (tokens.size() == 0) {
         throw new InvalidSyntaxException("Missing integer at end of input");
      } else {
         int i = 0;
         Token peek = (Token)tokens.peek();
         if (peek.type == Type.DEC) {
            this.consumeNextToken(tokens);
            i = Integer.parseInt(peek.text);
         } else {
            if (peek.type != Type.HEX) {
               throw new InvalidSyntaxException("Missing integer constant at " + this.getCurrentSourceInfo(tokens).toString());
            }

            this.consumeNextToken(tokens);
            i = Integer.parseInt(peek.text.substring(2), 16);
         }

         return i;
      }
   }

   public String parseID(Queue<Token> tokens) throws InvalidSyntaxException {
      if (tokens.size() == 0) {
         throw new InvalidSyntaxException("Missing identifier at end of input");
      } else {
         String rval = "";
         Token peek = (Token)tokens.peek();
         if (peek != null && peek.type == Type.ID) {
            this.consumeNextToken(tokens);
            rval = peek.text;
            return rval;
         } else {
            throw new InvalidSyntaxException("Missing identifier at " + this.getCurrentSourceInfo(tokens).toString());
         }
      }
   }

   public BinOp parseBinaryOperator(Queue<Token> tokens) throws InvalidSyntaxException {
      if (tokens.size() == 0) {
         throw new InvalidSyntaxException("Missing binary operator at end of input");
      } else {
         Token peek = (Token)tokens.peek();
         BinOp op = BinOp.INVALID;
         if (peek != null && peek.type == Type.SYM) {
            this.consumeNextToken(tokens);
            if (peek.text.equals("||")) {
               op = BinOp.OR;
            } else if (peek.text.equals("&&")) {
               op = BinOp.AND;
            } else if (peek.text.equals("==")) {
               op = BinOp.EQ;
            } else if (peek.text.equals("!=")) {
               op = BinOp.NE;
            } else if (peek.text.equals("<")) {
               op = BinOp.LT;
            } else if (peek.text.equals(">")) {
               op = BinOp.GT;
            } else if (peek.text.equals("<=")) {
               op = BinOp.LE;
            } else if (peek.text.equals(">=")) {
               op = BinOp.GE;
            } else if (peek.text.equals("+")) {
               op = BinOp.ADD;
            } else if (peek.text.equals("-")) {
               op = BinOp.SUB;
            } else if (peek.text.equals("*")) {
               op = BinOp.MUL;
            } else if (peek.text.equals("/")) {
               op = BinOp.DIV;
            } else if (peek.text.equals("%")) {
               op = BinOp.MOD;
            } else {
               op = BinOp.INVALID;
            }

            return op;
         } else {
            throw new InvalidSyntaxException("Missing binary operator at " + this.getCurrentSourceInfo(tokens).toString());
         }
      }
   }

   public UnaryOp parseUnaryOperator(Queue<Token> tokens) throws InvalidSyntaxException {
      if (tokens.size() == 0) {
         throw new InvalidSyntaxException("Missing unary operator at end of input");
      } else {
         Token peek = (Token)tokens.peek();
         UnaryOp op = UnaryOp.INVALID;
         if (peek != null && peek.type == Type.SYM) {
            this.consumeNextToken(tokens);
            if (peek.text.equals("!")) {
               op = UnaryOp.NOT;
            } else if (peek.text.equals("-")) {
               op = UnaryOp.NEG;
            } else {
               op = UnaryOp.INVALID;
            }

            return op;
         } else {
            throw new InvalidSyntaxException("Missing unary operator at " + this.getCurrentSourceInfo(tokens).toString());
         }
      }
   }
}
