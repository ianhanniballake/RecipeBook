package com.ianhanniballake.recipebook.model;

import android.content.ContentValues;

import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Class that manages the information associated with an instruction
 */
public class Instruction
{
	private String instruction;

	/**
	 * Creates a new, empty instruction
	 */
	public Instruction()
	{
	}

	/**
	 * Creates a new instruction with the given text
	 * 
	 * @param instruction
	 *            Instruction text
	 */
	public Instruction(final String instruction)
	{
		this.instruction = instruction;
	}

	/**
	 * Setter for the instruction text
	 * 
	 * @param instruction
	 *            Instruction text
	 */
	public void setInstruction(final String instruction)
	{
		this.instruction = instruction;
	}

	/**
	 * Converts this instruction into appropriate ContentValues for insertion into the RecipeProvider
	 * 
	 * @param recipeId
	 *            Mandatory recipeId to be associated with this instruction
	 * @return ContentValues usable by the RecipeProvider
	 */
	public ContentValues toContentValues(final long recipeId)
	{
		final ContentValues contentValues = new ContentValues();
		contentValues.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId);
		contentValues.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION, instruction);
		return contentValues;
	}

	@Override
	public String toString()
	{
		return instruction;
	}
}
