package edu.jmu.decaf;

import edu.jmu.decaf.ASTFunction.Parameter;
import edu.jmu.decaf.ASTNode.DataType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class BuildSymbolTables extends StaticAnalysis {
   private Deque<SymbolTable> tableStack = new ArrayDeque();

   public SymbolTable getCurrentTable() {
      assert this.tableStack.size() > 0;

      return (SymbolTable)this.tableStack.peek();
   }

   public SymbolTable initializeScope() {
      SymbolTable table = null;
      if (this.tableStack.size() > 0) {
         table = new SymbolTable(this.getCurrentTable());
      } else {
         table = new SymbolTable();
      }

      this.tableStack.push(table);
      return table;
   }

   public void finalizeScope() {
      assert this.tableStack.size() > 0;

      this.tableStack.pop();
   }

   public void insertFunctionSymbol(ASTFunction node) {
      try {
         List<DataType> ptypes = new ArrayList();
         Iterator var4 = node.parameters.iterator();

         while(var4.hasNext()) {
            Parameter p = (Parameter)var4.next();
            ptypes.add(p.type);
         }

         Symbol symbol = new Symbol(node.name, node.returnType, ptypes);
         this.getCurrentTable().insert(node.name, symbol);
      } catch (InvalidProgramException var5) {
         addError(var5);
      }

   }

   public void insertVariableSymbol(ASTVariable node) {
      try {
         Symbol symbol = new Symbol(node.name, node.type, node.isArray, node.arrayLength);
         this.getCurrentTable().insert(node.name, symbol);
      } catch (InvalidProgramException var3) {
         addError(var3);
      }

   }

   public void insertParamSymbol(Parameter p) {
      try {
         Symbol symbol = new Symbol(p.name, p.type);
         this.getCurrentTable().insert(p.name, symbol);
      } catch (InvalidProgramException var3) {
         addError(var3);
      }

   }

   public void insertPrintFunctionSymbol(String name, DataType type) {
      List<DataType> ptypes = new ArrayList();
      ptypes.add(type);

      try {
         this.getCurrentTable().insert(name, new Symbol(name, DataType.VOID, ptypes));
      } catch (InvalidProgramException var5) {
         addError(var5);
      }

   }

   public void preVisit(ASTProgram program) {
      program.attributes.put("symbolTable", this.initializeScope());
      this.insertPrintFunctionSymbol("print_str", DataType.STR);
      this.insertPrintFunctionSymbol("print_int", DataType.INT);
      this.insertPrintFunctionSymbol("print_bool", DataType.BOOL);
   }

   public void postVisit(ASTProgram program) {
      this.finalizeScope();
   }

   public void preVisit(ASTFunction function) {
      this.insertFunctionSymbol(function);
      function.attributes.put("symbolTable", this.initializeScope());

      for(int i = 0; i < function.parameters.size(); ++i) {
         this.insertParamSymbol((Parameter)function.parameters.get(i));
      }

   }

   public void postVisit(ASTFunction function) {
      this.finalizeScope();
   }

   public void preVisit(ASTBlock block) {
      block.attributes.put("symbolTable", this.initializeScope());
   }

   public void postVisit(ASTBlock block) {
      this.finalizeScope();
   }

   public void preVisit(ASTVariable var) {
      this.insertVariableSymbol(var);
   }

   public void postVisit(ASTVariable var) {
   }
}
