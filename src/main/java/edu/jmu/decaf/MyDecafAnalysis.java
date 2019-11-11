package edu.jmu.decaf;

import edu.jmu.decaf.ASTBinaryExpr.BinOp;
import edu.jmu.decaf.ASTNode.DataType;
import edu.jmu.decaf.ASTUnaryExpr.UnaryOp;
import java.util.ArrayList;
import java.util.Iterator;

public class MyDecafAnalysis extends DecafAnalysis {
   Symbol sym;
   Symbol arraysym;
   Symbol funcSym;
   Symbol assignSym;
   Symbol funcCallSym;
   Symbol voidCallSym;
   boolean loopBool = false;
   boolean assign = false;
   boolean funcCallBool = false;
   boolean ifLoopBool = false;
   boolean hasBoolOp = false;
   int returnCount = 0;
   int argumentSize = 0;
   int argumentCount = 0;
   int loopCount = 0;
   int exprCount = 0;

   public void defaultPreVisit(ASTNode node) {
   }

   public void defaultInVisit(ASTNode node) {
   }

   public void defaultPostVisit(ASTNode node) {
   }

   public void preVisit(ASTProgram node) {
      int mainCount = 0;
      ArrayList<ASTFunction> functions = (ArrayList)node.functions;
      Iterator var5 = functions.iterator();

      while(var5.hasNext()) {
         ASTFunction func = (ASTFunction)var5.next();
         if (func.name.equals("main")) {
            ++mainCount;
         }
      }

      if (mainCount != 1) {
         addError(new InvalidProgramException("There is more than one main in your program"));
      }

      this.sym = lookupSymbol(node, "main");
      if (this.sym == null || this.sym.type != DataType.INT || this.sym.paramTypes.size() > 0) {
         addError("The function main must have a return type of int and no parameters");
      }

      this.defaultPreVisit(node);
   }

   public void postVisit(ASTProgram node) {
   }

   public void preVisit(ASTFunction node) {
      this.funcSym = lookupSymbol(node, node.name);
      this.defaultPreVisit(node);
   }

   public void postVisit(ASTFunction node) {
      if (this.returnCount == 0) {
         if (this.funcSym.type != DataType.VOID) {
            addError("The following function needs a return type: " + node.name);
         }

         this.returnCount = 0;
      }

   }

   public void preVisit(ASTVariable node) {
      this.defaultPreVisit(node);
   }

   public void postVisit(ASTVariable node) {
      this.sym = lookupSymbol(node, node.name);
      if (this.sym.type == null || this.sym.type != DataType.INT && this.sym.type != DataType.BOOL) {
         addError("This aint gonna work sis");
      }

      if (this.sym.isArray && this.sym.length <= 0) {
         addError("Array length must be greater than zero");
      }

      this.defaultPostVisit(node);
   }

   public void preVisit(ASTBlock node) {
      this.defaultPreVisit(node);
   }

   public void postVisit(ASTBlock node) {
      this.defaultPostVisit(node);
   }

   public void preVisit(ASTAssignment node) {
      this.assign = true;
      this.assignSym = lookupSymbol(node.location, node.location.name);
      this.defaultPreVisit(node);
   }

   public void postVisit(ASTAssignment node) {
      this.defaultPostVisit(node);
      this.assign = false;
   }

   public void preVisit(ASTVoidFunctionCall node) {
      this.funcCallSym = lookupSymbol(node, node.name);
      this.argumentSize = this.funcCallSym.paramTypes.size();
      this.funcCallBool = true;
      ASTNode parent = node.getParent();
      if (parent instanceof ASTAssignment) {
         addError("Void function call cannot be assigned to variable");
      }

      this.argumentCount = 0;
      this.funcCallBool = false;
      this.defaultPostVisit(node);
      this.defaultPreVisit(node);
   }

   public void postVisit(ASTVoidFunctionCall node) {
      if (this.argumentCount > 0 && this.argumentCount != this.argumentSize) {
         addError("The listed arguments does not match the void function's");
      }

      ASTNode parent = node.getParent();
      if (parent instanceof ASTAssignment) {
         addError("Void function call cannot be assigned to variable");
      }

      this.argumentCount = 0;
      this.funcCallBool = false;
      this.defaultPostVisit(node);
   }

   public void preVisit(ASTConditional node) {
      this.ifLoopBool = true;
      this.defaultPreVisit(node);
   }

   public void postVisit(ASTConditional node) {
      this.ifLoopBool = false;
      this.defaultPostVisit(node);
   }

   public void preVisit(ASTWhileLoop node) {
      this.ifLoopBool = true;
      ++this.loopCount;
      this.defaultPreVisit(node);
   }

   public void postVisit(ASTWhileLoop node) {
      this.ifLoopBool = false;
      --this.loopCount;
      this.defaultPostVisit(node);
   }

   public void preVisit(ASTReturn node) {
      this.defaultPreVisit(node);
   }

   public void postVisit(ASTReturn node) {
      this.defaultPostVisit(node);
   }

   public void preVisit(ASTBreak node) {
      this.defaultPreVisit(node);
   }

   public void postVisit(ASTBreak node) {
      if (this.loopCount <= 0) {
         addError("Break was stated outside of a loop.");
      }

      this.defaultPostVisit(node);
   }

   public void preVisit(ASTContinue node) {
      this.defaultPreVisit(node);
   }

   public void postVisit(ASTContinue node) {
      if (this.loopCount <= 0) {
         addError("Continue was stated outside of a loop.");
      }

      this.defaultPostVisit(node);
   }

   public void preVisit(ASTBinaryExpr node) {
      ++this.exprCount;
      if (node.operator != BinOp.OR && node.operator != BinOp.AND) {
         if (node.operator == BinOp.EQ || node.operator == BinOp.NE || node.operator == BinOp.LT || node.operator == BinOp.GT || node.operator == BinOp.LE || node.operator == BinOp.GE || node.operator == BinOp.ADD || node.operator == BinOp.SUB || node.operator == BinOp.MUL || node.operator == BinOp.DIV || node.operator == BinOp.MOD) {
            node.setType(DataType.INT);
         }
      } else {
         node.setType(DataType.BOOL);
      }

      if (this.ifLoopBool) {
         ASTNode parent = node.getParent();
         if ((parent instanceof ASTConditional || parent instanceof ASTWhileLoop) && node.operator != BinOp.OR && node.operator != BinOp.AND && node.operator != BinOp.EQ && node.operator != BinOp.NE && node.operator != BinOp.LT && node.operator != BinOp.GT && node.operator != BinOp.LE && node.operator != BinOp.GE) {
            addError("Conditional or While loop does not have a boolean expression " + node.toString());
         }
      }

      this.defaultPreVisit(node);
   }

   public void inVisit(ASTBinaryExpr node) {
      this.defaultInVisit(node);
   }

   public void postVisit(ASTBinaryExpr node) {
      --this.exprCount;
      if (node.getParent() instanceof ASTVoidFunctionCall || node.getParent() instanceof ASTFunctionCall) {
         if (node.getType() != this.funcCallSym.paramTypes.get(this.argumentCount)) {
            addError(node.toString() + " The argument does not match the function argument type: " + node.getSourceInfo());
         }

         ++this.argumentCount;
      }

      this.defaultPostVisit(node);
   }

   public void preVisit(ASTUnaryExpr node) {
      ++this.exprCount;
      if (node.operator == UnaryOp.NEG) {
         node.setType(DataType.INT);
      } else if (node.operator == UnaryOp.NOT) {
         node.setType(DataType.BOOL);
      }

      this.defaultPreVisit(node);
   }

   public void postVisit(ASTUnaryExpr node) {
      --this.exprCount;
      if (node.getParent() instanceof ASTVoidFunctionCall || node.getParent() instanceof ASTFunctionCall) {
         if (node.getType() != this.funcCallSym.paramTypes.get(this.argumentCount)) {
            addError(node.toString() + " The argument does not match the function argument type: " + node.getSourceInfo());
         }

         ++this.argumentCount;
      }

      this.defaultPostVisit(node);
   }

   public void preVisit(ASTFunctionCall node) {
      this.funcCallSym = lookupSymbol(node, node.name);
      this.argumentSize = this.funcCallSym.paramTypes.size();
      this.funcCallBool = true;
      this.defaultPreVisit(node);
   }

   public void postVisit(ASTFunctionCall node) {
      if (this.argumentCount > 0 && this.argumentCount != this.argumentSize) {
         addError("The listed arguments does not match the function's");
      }

      this.argumentCount = 0;
      this.funcCallBool = false;
      this.defaultPostVisit(node);
   }

   public void preVisit(ASTLocation node) {
      this.sym = lookupSymbol(node, node.name);
      this.defaultPreVisit(node);
   }

   public void postVisit(ASTLocation node) {
      ASTNode tempN = node.getParent();
      if (tempN instanceof ASTReturn) {
         if (this.funcSym.type != this.sym.type) {
            addError(node.toString() + " is not the correct return type");
         }

         ++this.returnCount;
      }

      if (this.sym.isArray && !node.hasIndex()) {
         addError(node.toString() + " Array must be accessed by an index");
      }

      if (node.getParent() instanceof ASTVoidFunctionCall || node.getParent() instanceof ASTFunctionCall) {
         if (this.sym.type != this.funcCallSym.paramTypes.get(this.argumentCount)) {
            addError(node.toString() + " The argument does not match the function argument type");
         }

         ++this.argumentCount;
      }

      ASTNode temp4;
      if (this.exprCount > 0) {
         temp4 = node.getParent();
         if ((temp4 instanceof ASTBinaryExpr || temp4 instanceof ASTUnaryExpr) && this.sym.type != temp4.getType()) {
            addError(node.toString() + " The literal does not match the epxressions desired type");
         }
      }

      if (this.ifLoopBool) {
         temp4 = node.getParent();
         if (!(temp4 instanceof ASTBinaryExpr) && !(temp4 instanceof ASTUnaryExpr) && this.sym.type != DataType.BOOL) {
            addError(node.toString() + " is not appropriate for the conditon or loop ");
         }

         this.ifLoopBool = false;
      }

      temp4 = node.getParent();
      if (temp4 instanceof ASTLocation && this.sym.type != DataType.INT) {
         addError(" Arrays must be accessed by an integer: " + node.toString());
      }

      this.defaultPostVisit(node);
   }

   public void preVisit(ASTLiteral node) {
      this.defaultPreVisit(node);
   }

   public void postVisit(ASTLiteral node) {
      ASTNode tempN = node.getParent();
      if (tempN instanceof ASTReturn) {
         if (this.funcSym.type != node.type) {
            addError(node.toString() + " is not the correct return type ");
         }

         ++this.returnCount;
      }

      if (this.assign) {
         if (node.type != this.assignSym.type) {
            addError("The assignment does not match the variable type: " + node.toString() + " " + this.assignSym.name);
         }

         this.assign = false;
      }

      if (node.getParent() instanceof ASTVoidFunctionCall || node.getParent() instanceof ASTFunctionCall) {
         if (node.type != this.funcCallSym.paramTypes.get(this.argumentCount)) {
            addError(node.toString() + " The argument does not match the function argument type: " + node.getSourceInfo());
         }

         ++this.argumentCount;
      }

      ASTNode temp;
      if (this.ifLoopBool) {
         temp = node.getParent();
         if (!(temp instanceof ASTBinaryExpr) && !(temp instanceof ASTUnaryExpr) && node.type != DataType.BOOL) {
            addError(node.toString() + " is not appropriate for the conditon or loop " + node.getSourceInfo());
         }

         this.ifLoopBool = false;
      }

      if (this.exprCount > 0) {
         temp = node.getParent();
         if ((temp instanceof ASTBinaryExpr || temp instanceof ASTUnaryExpr) && node.type != temp.getType()) {
            addError("The literal does not match the epxressions desired type: " + node.toString() + temp.toString());
         }
      }

      temp = node.getParent();
      if (temp instanceof ASTLocation && node.type != DataType.INT && this.sym.isArray) {
         addError(" Arrays must be accessed by an integer: " + node.toString());
      }

      this.defaultPostVisit(node);
   }
}
