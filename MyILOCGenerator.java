package edu.jmu.decaf;

import java.util.*;

/**
 * Concrete ILOC generator class.
 */
public class MyILOCGenerator extends ILOCGenerator
{

    public MyILOCGenerator()
    {
    	
    }
    
    @Override 
    public void postVisit(ASTProgram program)
    {
    	
    }

    @Override
    public void postVisit(ASTFunction node)
    {
        // TODO: emit prologue
    	
    	
        // propagate code from body block to the function level
        copyCode(node, node.body);

        // TODO: emit epilogue
    }

    @Override
    public void postVisit(ASTBlock node)
    {
        // concatenate the generated code for all child statements
        for (ASTStatement s : node.statements) {
            copyCode(node, s);
        }
    }

    @Override
    public void postVisit(ASTReturn node)
    {
    	if(node.hasValue()) //Handles the return value
    	{
    		copyCode(node, node.value);
    		ILOCOperand reg = getTempReg(node.value);
    		emit(node, ILOCInstruction.Form.I2I, reg, ILOCOperand.REG_RET);
    	}
    	
        // TODO: handle return value and emit epilogue
        emit(node, ILOCInstruction.Form.RETURN);
        
    }
    
    @Override
    public void postVisit(ASTLiteral node)
    {
//    	ILOCOperand reg = ILOCOperand.newVirtualReg();
    	
    	emit(node, ILOCInstruction.Form.LOAD_I, ILOCOperand.newIntConstant(((Integer) node.value).intValue()),
    			ILOCOperand.newVirtualReg());
    	copyCode(node, node.getParent());
//    	setTempReg(node, reg);
    	
    	//System.out.println("Node code: " + getCode(node));
    }
    
    @Override
    public void postVisit(ASTBinaryExpr node)
    {

    	ILOCOperand leftReg;
    	ILOCOperand rightReg; 
    	ILOCOperand destReg; 

    	System.out.println(node.operator);
    	
    	switch(node.operator)
    	{
    		case ADD:
    			 leftReg = getTempReg(node.leftChild); 
    	    	 rightReg = getTempReg(node.rightChild);
    	    	 destReg = ILOCOperand.newVirtualReg();
    	    	
    	    	copyCode(node, node.leftChild);
    	    	copyCode(node, node.rightChild);
    			
    	    	emit(node, ILOCInstruction.Form.ADD, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);	
    	    	
    			System.out.println(getCode(node));
    			break;
    		
    		case SUB:
    			
    			 leftReg = getTempReg(node.leftChild); //Does all this code need to be in each case stmt
    	    	 rightReg = getTempReg(node.rightChild);
    	    	 destReg = ILOCOperand.newVirtualReg();
    	    	
    	    	copyCode(node, node.leftChild);
    	    	copyCode(node, node.rightChild);
    			
    	    	emit(node, ILOCInstruction.Form.SUB, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
    	    	
        		break;
    		
    		case MUL:
    			
    			leftReg = getTempReg(node.leftChild); //Does all this code need to be in each case stmt
    			rightReg = getTempReg(node.rightChild);
    			destReg = ILOCOperand.newVirtualReg();
   	    	 
    			emit(node, ILOCInstruction.Form.MULT, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
    			
        		break;
        		
    		case DIV:
    			
    			leftReg = getTempReg(node.leftChild);
    			rightReg = getTempReg(node.rightChild);
    			destReg = ILOCOperand.newVirtualReg();
    			
    			emit(node, ILOCInstruction.Form.DIV, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
    			
        		break;
        		
    		case AND:
    			
    			leftReg = getTempReg(node.leftChild); 
    			rightReg = getTempReg(node.rightChild);
    			destReg = ILOCOperand.newVirtualReg();
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
			
    			break;
    		
    		case EQ:
    			
    			leftReg = getTempReg(node.leftChild); 
    			rightReg = getTempReg(node.rightChild);
    			destReg = ILOCOperand.newVirtualReg();
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
			
    			break;
		
    		case GE:
    			
    			leftReg = getTempReg(node.leftChild); 
    			rightReg = getTempReg(node.rightChild);
    			destReg = ILOCOperand.newVirtualReg();
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
			
    			break;
		
    		case GT:
    			
    			leftReg = getTempReg(node.leftChild); 
    			rightReg = getTempReg(node.rightChild);
    			destReg = ILOCOperand.newVirtualReg();
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
			
    			break;
		
    		case LE:
    			
    			leftReg = getTempReg(node.leftChild); 
    			rightReg = getTempReg(node.rightChild);
    			destReg = ILOCOperand.newVirtualReg();
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
			
    			break;
		
    		case LT:
    			
    			leftReg = getTempReg(node.leftChild); 
    			rightReg = getTempReg(node.rightChild);
    			destReg = ILOCOperand.newVirtualReg();
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
			
    			break;
		
//    		case MOD: No mod in ILOC reference
//    			
//    			
//			
//    			break;
		
    		case NE:
			
    			leftReg = getTempReg(node.leftChild); 
    			rightReg = getTempReg(node.rightChild);
    			destReg = ILOCOperand.newVirtualReg();
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
    	    	
    			break;
		
    		case OR:
    			
    			leftReg = getTempReg(node.leftChild); 
    			rightReg = getTempReg(node.rightChild);
    			destReg = ILOCOperand.newVirtualReg();
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
			
    			break;
		
    		default:
    			
    			System.out.println("Default in case stmt");
			
    			break;
        		
    	}
    }
    
    @Override 
    public void postVisit(ASTUnaryExpr expr)
    {
    	
    }
    
    @Override 
    public void postVisit(ASTFunctionCall call)
    {
    	
    }
    
    @Override 
    public void postVisit(ASTLocation loc)
    {
//    	System.out.println(loc);
    	
    	ILOCOperand destReg = ILOCOperand.newVirtualReg();
    	setTempReg(loc, destReg);
    }
    
    @Override 
    public void postVisit(ASTAssignment assign)
    {
    	System.out.println("Assign");
    	
    }
    
    @Override 
    public void postVisit(ASTVoidFunctionCall call)
    {
    	
    }
    
    @Override 
    public void postVisit(ASTVariable var)
    {
    	
    }
    
    @Override 
    public void postVisit(ASTConditional cond)
    {
    	
    }
    
    @Override 
    public void postVisit(ASTWhileLoop loop)
    {
    	
    }
    
    @Override 
    public void postVisit(ASTBreak myBreak)
    {
    	
    }
}
