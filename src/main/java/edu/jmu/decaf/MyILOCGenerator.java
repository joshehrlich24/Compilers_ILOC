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
    	ILOCOperand reg = ILOCOperand.newVirtualReg();
    	
    	emit(node, ILOCInstruction.Form.LOAD_I, ILOCOperand.newIntConstant(((Integer) node.value).intValue()),
    			ILOCOperand.newVirtualReg());
    	
    	setTempReg(node, reg);
    }
    
    @Override
    public void postVisit(ASTBinaryExpr node)
    {
    	ILOCOperand leftReg = getTempReg(node.leftChild);
    	ILOCOperand rightReg = getTempReg(node.rightChild);
    	ILOCOperand destReg = ILOCOperand.newVirtualReg();
    	copyCode(node, node.leftChild);
    	copyCode(node, node.rightChild);
    	emit(node, ILOCInstruction.Form.ADD, leftReg, rightReg, destReg);
    	setTempReg(node, destReg);
    	
    }

}
