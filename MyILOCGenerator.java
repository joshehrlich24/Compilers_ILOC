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
    	emit(node, ILOCInstruction.Form.PUSH, ILOCOperand.REG_BP);
    	emit(node, ILOCInstruction.Form.I2I, ILOCOperand.REG_SP, ILOCOperand.REG_BP);
    	emitLocalVarStackAdjustment(node); // allocate space for local variables (might be in the wrong spot)
        // propagate code from body block to the function level
        copyCode(node, node.body);
        System.out.println(getCode(node));

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
       //System.out.println(getCode(node));
    }
    
    @Override
    public void postVisit(ASTLiteral node)
    {
    	ILOCOperand reg = ILOCOperand.newVirtualReg();
    	emit(node, ILOCInstruction.Form.LOAD_I, ILOCOperand.newIntConstant(((Integer) node.value).intValue()),
    			reg);
   
    	
    	setTempReg(node, reg);
    	
    	copyCode(node, node.getParent());
    	
    	//System.out.println(getCode(node));
    }
    
    @Override
    public void postVisit(ASTBinaryExpr node)
    {

    	ILOCOperand leftReg;
    	ILOCOperand rightReg; 
    	ILOCOperand destReg;
    	
    	leftReg = getTempReg(node.leftChild); 
   	 	rightReg = getTempReg(node.rightChild);
   	 	destReg = ILOCOperand.newVirtualReg();
   	
   	 	copyCode(node, node.leftChild);
   	 	copyCode(node, node.rightChild);
    	
   	 	//System.out.println("BinaryExpr op --> " + node.operator);
   	 	
    	switch(node.operator)
    	{
    		case ADD:	 
    	    	emit(node, ILOCInstruction.Form.ADD, leftReg, rightReg, destReg);
    	    	
    			break;
    		
    		case SUB:
    			
    	    	emit(node, ILOCInstruction.Form.SUB, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
    	    	
        		break;
    		
    		case MUL:
    			
    			
   	    	 
    			emit(node, ILOCInstruction.Form.MULT, leftReg, rightReg, destReg);
    			
    	    	setTempReg(node, destReg);
    			
        		break;
        		
    		case DIV:
    			
    			
    			
    			emit(node, ILOCInstruction.Form.DIV, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
    			
        		break;
        		
    		case AND:
    			
    			
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
			
    			break;
    		
    		case EQ:
    			
    			
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
			
    			break;
		
    		case GE:
    			
    			
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
			
    			break;
		
    		case GT:
    			
    			
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
			
    			break;
		
    		case LE:
    			
    			
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
			
    			break;
		
    		case LT:
    			
    			
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
			
    			break;
		
//    		case MOD: No mod in ILOC reference
//    			
//    			
//			
//    			break;
		
    		case NE:
			
    			
    			
    			emit(node, ILOCInstruction.Form.AND, leftReg, rightReg, destReg);
    	    	setTempReg(node, destReg);
    	    	
    			break;
		
    		case OR:
    			
    			
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
    	// 1)load the arguments into registers 
    			//loadAI [bp-4] => r5 {a} 	 --> if its a variable
    			//loadI 2 => r6				 --> if its a literal
    	// 2)Push the registers that contain the args onto the stack
    	// push r6
    	// push r5
    	//Call the function --> funcCall(arg1, arg2);
    	
    	//When the function returns --> Deallocate space for parameters?
    	
    }
    
    @Override 
    public void postVisit(ASTLocation loc)
    {	
    	//loadAI
    	emitLoad(loc);
    }
    
    @Override 
    public void postVisit(ASTAssignment assign)
    {	
    	ILOCOperand reg = ILOCOperand.newVirtualReg();
  	
    	emitStore(assign, reg); // Allocate space on the stack [bp - 4]
    }
    
    @Override 
    public void postVisit(ASTVoidFunctionCall call)
    {
    	
    }
    
    @Override 
    public void postVisit(ASTVariable var)
    {
    	if(var.isArray)
    	{
    		//DoSomething()
    	}
    	
    	else
    	{
    		//Local Variable 
    		//Allocate -4 On the BP stack?
    		
    		
    	}
    	
    	
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
