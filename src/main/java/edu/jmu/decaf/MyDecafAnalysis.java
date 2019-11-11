package edu.jmu.decaf;

import java.util.List;

import edu.jmu.decaf.ASTBinaryExpr.BinOp;
import edu.jmu.decaf.ASTNode.DataType;
import edu.jmu.decaf.ASTUnaryExpr.UnaryOp;

/**
 * Perform type checking using the Decaf type rules.
 * Author - Josh Ehrlich
 *  Honor Code: This file abides by the JMU Honor code and is soley my work
 */
public class MyDecafAnalysis extends DecafAnalysis
{
   	/**
	 * Previsit for an ASTFunction --> infer types using the symbolTable
	 */
    public void preVisit (ASTFunction function)         
    { 
    	boolean hasReturn = false;
    	
    	Symbol sym = lookupSymbol(function, function.name);
    	
    	if(sym != null)
    	{
    		function.setType(sym.type);
    		
    		 if(function.returnType != DataType.VOID) //Checks for return statement in all functions not declared as void
        	 {
        		 for(int i = 0; i < function.body.statements.size(); i ++)
        		 { 
        			 if(function.body.statements.get(i).getASTTypeStr().equals("Return"))
        			 {
        				hasReturn = true;
        			 }
        		 }
        		 
        		 if(!hasReturn)
            	 {
            		 addError(new InvalidProgramException("Function " + function.name + " is missing a return statement at " + function.getSourceInfo()));
            	 } 	 
        	 }
    	}
    }
    
    /**
	 * Previsit for an ASTVariable --> infer types using the symbolTable
	 */
    public void preVisit (ASTVariable var)         
    { 
    	Symbol sym = lookupSymbol(var, var.name);
    	
    	if(sym.type == DataType.VOID)
    	{
    		addError(new InvalidProgramException(var.name + " is of type void " + var.getSourceInfo()));
    	}
    }
    
    /**
     * Previsit method for an ASTBinaryExpression --> infer types using the type rules
     */
	@Override
	public void preVisit(ASTBinaryExpr expr)
	{   
	   // '+', '-', '*', '/', '%'
	   if(expr.operator.equals(BinOp.ADD) || expr.operator.equals(BinOp.SUB) || expr.operator.equals(BinOp.MUL) 
	    || expr.operator.equals(BinOp.DIV) || expr.operator.equals(BinOp.MOD))
	    {
		   expr.setType(DataType.INT);
	    }
	   	// '<', '<=', '>', '>='
	    else if(expr.operator.equals(BinOp.LT) || expr.operator.equals(BinOp.GT) || expr.operator.equals(BinOp.LE)
	    	|| expr.operator.equals(BinOp.GE))
	    {
	    	expr.setType(DataType.BOOL);
	    }
	    else if(expr.operator.equals(BinOp.EQ) || expr.operator.equals(BinOp.NE))
	    {
	    	expr.setType(DataType.BOOL);
	    }
	    else if(expr.operator.equals(BinOp.AND) || expr.operator.equals(BinOp.OR))
	    {
	    	expr.setType(DataType.BOOL);
	    }
	    	
	}
	    /**
	     * Previsit for an ASTUnaryExpression --> infer types using the type rules
	     */
	    @Override
	    public void preVisit(ASTUnaryExpr expr)
	    {
	    	if(expr.operator.equals(UnaryOp.NOT))
	    	{
	    		expr.setType(DataType.BOOL);
	    	}
	    	else if(expr.operator.equals(UnaryOp.NEG))
	    	{
	    		expr.setType(DataType.INT);
	    	}
	    }
	    
	    /**
	     * PreVisit method for an ASTFunctionCall --> infer types using the symbolTable
	     */
	    @Override
	    public void preVisit(ASTFunctionCall call) 
	    {
	    	Symbol sym = lookupSymbol(call, call.name);
	    	
	    	if(sym != null)
	    	{
	    		call.setType(sym.type);
	    	}
	    	
	    }
	    
	    /**
	     * PreVisit method for an ASTLocation --> infer types using the symbolTable
	     */
	    @Override
	    public void preVisit(ASTLocation loc) // How to I handle ArrayLocations?
	    {
	    	Symbol sym = lookupSymbol(loc, loc.name);
	    	
	    	if(sym != null)
	    	{
				loc.setType(sym.type);	
	    	}	
	    }
	    
	    /**
	     * PreVisit method for an ASTLiteral --> infer types using the type rules
	     */
	    @Override
	    public void preVisit(ASTLiteral lit)
	    {
	    	if(lit.type == DataType.INT)
	    	{  		
	    		lit.setType(DataType.INT);
	    	}
	    	else if(lit.type == DataType.STR)
	    	{
	    		lit.setType(DataType.STR);
	    	}
	    	else if(lit.type == DataType.BOOL)
	    	{
	    		lit.setType(DataType.BOOL);
	    	}
	    	else if(lit.type == DataType.VOID)
	    	{
	    		lit.setType(DataType.VOID);
	    	}
	    	else
	    	{
	    		addError(new InvalidProgramException("Invalid literal " + lit.toString() + " at " + lit.getSourceInfo()));
	    	}
	    } 
	    
	    /**
	     * PreVisit method for an ASTReturn --> --> infer types by going to the function definition and getting it's type
	     */
	    public void preVisit (ASTReturn ret) 
	    {
	    	ASTNode node = ret.getParent();
	    	
	    	while(!node.getASTTypeStr().equals("Function")) // Go up the tree and find the return type of the function
	    	{
	    		node = node.getParent();
	    		
	    		if(node.getASTTypeStr().equals("Function"))
	    		{
	    			ret.setType(node.getType()); //set the type to the expected value
	    		}
	    	}

	    }
	    
	    /**
	     * PreVisit method for an ASTConditional --> infer types using the type rules 
	     */
	    public void preVisit(ASTConditional cond)
	    {
	    	cond.setType(DataType.BOOL);
	    }
	    
	    //Post Visits
	      
	    /**
	     * PostVisit method for an ASTBinaryExpression --> Check actual types of children and compare to inferred type
	     */
	    @Override
	    public void postVisit(ASTBinaryExpr expr)
	    {
	    	switch(expr.operator)
	    	{
	    	case ADD:
	    	case SUB:
	    	case MUL:
	    	case DIV:
	    	case MOD:
	    		
	    		if(expr.leftChild.getType() != DataType.INT || expr.rightChild.getType() != DataType.INT)
	    		{
	    			addError(new InvalidProgramException("Type mismatch: Invalid operand types for operation at " + expr.getSourceInfo()));
	    		}
	    	break;
	    	
	    	case LE:
	    	case GE:
	    	case LT:
	    	case GT:
	    		
	    		if(expr.leftChild.getType() != DataType.INT || expr.rightChild.getType() != DataType.INT)
	    		{
	    			addError(new InvalidProgramException("Invalid comparison: The values compared were not of type INT " + expr.getSourceInfo() ));
	    		}
	    		
	    		if(expr.getType() != DataType.BOOL)
	    		{
	    			addError(new InvalidProgramException("Invalid comparison: The resulting comparison must result in a boolean value " + expr.getSourceInfo()));
	    		}
	    	break;
	    	
	    	case EQ:
	    	case NE:
	    		
	    		if(expr.leftChild.getType() != expr.rightChild.getType())
	    		{
	    			addError("Invalid comparison: The operands being compared are not of the same type " + expr.getSourceInfo());
	    		}
	    		if(expr.getType() != DataType.BOOL)
	    		{
	    			addError(new InvalidProgramException("Invalid comparison: The resulting comparison must result in a boolean value " + expr.getSourceInfo()));
	    		}
	    		break;
	    		
	    	default: 
	    		addError(new InvalidProgramException("No operation presented"));
	    	
	    	
	    	}
	    }
	    
	    /**
	     * PostVisit method for an ASTUnaryExpression --> Check actual types of children and compare to inferred type
	     */
	    @Override
	    public void postVisit(ASTUnaryExpr expr)
	    {
	    	if(expr.getType() != DataType.BOOL)
	    	{
	    		addError(new InvalidProgramException("Cannot perform operation: Expression is not of type boolean " + expr.getSourceInfo()));
	    	}
	    	
	    	if(expr.child.getType() != DataType.BOOL)
	    	{
	    		addError(new InvalidProgramException("The operand you are trying to perform this operation on is not of type boolean " + expr.getSourceInfo()));
	    	}
	    }

	    /**
	     * PostVisit method for an ASTFunctionCall --> Checks for matching number of params/args and their types
	     */
	    @Override
	    public void postVisit(ASTFunctionCall call)
	    {
	    	Symbol sym = lookupSymbol(call, call.name);

	    	if(sym != null)
	    	{
	    		if(sym.paramTypes.size() == call.arguments.size())
	    		{
	    			for(int i = 0; i < sym.paramTypes.size(); i++)
			    	{
			    		if(call.arguments.get(i).getType() != sym.paramTypes.get(i))
			    		{
			    			addError(new InvalidProgramException("Types of arguments passed do not match the type of parameters required " + call.getSourceInfo()));
			    		}
			    	}    
	    		}
	    		else
	    		{
	    			addError(new InvalidProgramException("Number of arguments passed and number of "
	    					+ "parameters required do not match " + call.getSourceInfo()));
	    		}				
	    	}
	    }
	    
	    /**
	     * PostVisit method for an ASTLocation --> checks for valid array accesses 
	     */
	    @Override
	    public void postVisit(ASTLocation loc)
	    {
	    	Symbol sym = lookupSymbol(loc, loc.name);
	    	
	    	if(sym.isArray)
	    	{
	    		if(!loc.hasIndex())
	    		{
	    			addError(new InvalidProgramException("Must provide an index to use/access " + loc.name + " at " + loc.getSourceInfo()));
	    		}
	    		else
	    		{
	    			if(loc.index != null)
	    			{
	    				int index = Integer.parseInt(loc.index.toString());
	    				if(index > sym.length)
	    				{
	    					addError(new InvalidProgramException("Index out of bounds: "
	    							+ "the index passed is larger than the array provided at " + loc.getSourceInfo()));
	    				}
	    			}
	    			
	    		}
	    	}
	    }
	    
	    /**
	     * PostVisit method for ASTVariables --> Checks for valid variable declarations such as arrays
	     */
	    public void postVisit(ASTVariable var)         
	    {
	    	Symbol sym = lookupSymbol(var, var.name);
	    	 	
	    	if(var.isArray)
	    	{
	    		if(var.arrayLength <= 0)
	    		{
	    			addError(new InvalidProgramException("Invalid array declaration: " + var.name + " must be given a size greater than 0 " + var.getSourceInfo() ));
	    		}
	    	}
	    }

	    
	    
	    /**
	     * PostVisit method for ASTAssignment --> Checks if the item being assigned is given the expected type
	     */
	 	@Override
	    public void postVisit(ASTAssignment assign)
	    {
	    	if(assign.location.getType() != assign.value.getType())
	    	{
	    		addError(new InvalidProgramException("Cannot assign type " + assign.value.getType() 
	    		+ " to " + assign.location.getType() + " at " + assign.getSourceInfo()));
	    	}
	    }
	 
	 	/**
	 	 * PostVisit method for an ASTVoidFunctionCall --> Checks to see if the void function being called exists
	 	 */
	    @Override
	    public void postVisit(ASTVoidFunctionCall funcCall)
	    {
	    	Symbol sym = lookupSymbol(funcCall, funcCall.name);
	    	 
	    	if(sym != null)
	    	{
	    		funcCall.setType(sym.type);
	    		
	    		if (sym.paramTypes.size() == funcCall.arguments.size()) 
	    		{
					for (int i = 0; i < sym.paramTypes.size(); i++) 
					{
						if (funcCall.arguments.get(i).getType() != sym.paramTypes.get(i)) 
						{
							addError(new InvalidProgramException("Types of arguments passed do not match the type of parameters required " + funcCall.getSourceInfo()));
						}
					}
				} 
	    		else 
	    		{
	    			addError(new InvalidProgramException("Number of arguments passed and number of "
	    					+ "parameters required do not match " + funcCall.getSourceInfo()));
				}
	    	}
	    	
	    	

	    }
	    
	    
	    /**
	     * PostVisit for ASTConditional --> Checks that the condition being evaluated is in fact a boolean.
	     *  Also checks that the block is not empty
	     */
	    @Override
	    public void postVisit(ASTConditional cond)
	    {
	    	if(cond.condition.getType() != DataType.BOOL)
	    	{
	    		addError(new InvalidProgramException("Conditional must evaluate to a boolean type " + cond.getSourceInfo()));
	    	}
	    	
	    	if(cond.ifBlock.statements.size() == 0)
	    	{
	    		addError(new InvalidProgramException("Must have atleast one statement in the block " + cond.ifBlock.getSourceInfo()));
	    	}
	    }
	    
	    /**
	     * PostVisit method for an ASTReturn --> Makes sure that the return type matches the expected one defined in the function 
	     * definition for all functions that are not void
	     */
	    @Override
	    public void postVisit(ASTReturn ret)
	    {
	    	if(ret.getType() != DataType.VOID)
			{
	    		if(ret.value != null)
	    		{
	    			if(ret.getType() != ret.value.getType())
	    			{
	    				addError(new InvalidProgramException("The return type in the function declaration and "
	    						+ "the value being returned are not of the same type " + ret.getSourceInfo()));
	    			}
	    		}
			}
	    }	   
	    
	    /**
	     * PostVisit method for an ASTBreak --> Checks that the break statement is contained within a loop
	     */
	    @Override
	    public void postVisit(ASTBreak b)
	    {
	    	ASTNode node = b.getParent();
	    	
	    	while(!node.getASTTypeStr().equals("WhileLoop"))
	    	{
	    		if(node.getASTTypeStr().equals("Function"))
	    		{
	    			addError(new InvalidProgramException("Break statement not contained within a loop " + b.getSourceInfo()));
	    			break;
	    		}
	    		node = node.getParent();
	    	}
	    	
	    }
	    
	    /**
	     * PostVisit for an ASTContinue --> Checks that the continue statement is contained within a loop
	     */
	    @Override
	    public void postVisit(ASTContinue c)
	    {
	    	ASTNode node = c.getParent();
	    	
	    	while(!node.getASTTypeStr().equals("WhileLoop"))
	    	{
	    		if(node.getASTTypeStr().equals("Function"))
	    		{
	    			addError(new InvalidProgramException("Continue statement not contained within a loop " + c.getSourceInfo()));
	    			break;
	    		}
	    		node = node.getParent();
	    	}
	    }

	    /**
	     * PostVisit method for an ASTProgram --> Checks that the program contains a proper main function that returns an 
	     * int and has no paramaters 
	     */
	    public void postVisit(ASTProgram program)          
	    { 
	    	boolean hasMain = false;
	  
	    	for(int i = 0; i < program.functions.size(); i ++)
	    	{
	    		
	    		if(program.functions.get(i).name.trim().equals("main"))
	    		{
	    			ASTFunction mainFunc = program.functions.get(i);
	    			hasMain = true;
	    			
	    			if(mainFunc.parameters.size() > 0)
	    			{
	    				addError("The function Main should contain no parameters " + program.getSourceInfo());
	    			}
	    			if(mainFunc.returnType != DataType.INT)
	    			{
	    				addError("The function main should have a return type of int " + program.getSourceInfo());
	    			}
	    		}
	    	}
	    	
	    	if(hasMain == false)
	    	{
	    		addError(new InvalidProgramException("Program does not contain a main function"));
	    	}
	    }	    
}
