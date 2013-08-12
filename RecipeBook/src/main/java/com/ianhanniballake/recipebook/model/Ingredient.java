package com.ianhanniballake.recipebook.model;

import java.util.Arrays;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;

import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.provider.RecipeContract;

/**
 * Class that manages the conversion to and from the string representation of ingredients to individual components
 */
public class Ingredient
{
	/**
	 * Actual item that makes up this ingredient
	 */
	private String item;
	/**
	 * Optional ingredient preparation
	 */
	private String preparation;
	/**
	 * Whole number quantity
	 */
	private int quantity;
	/**
	 * Denominator of a fractional quantity
	 */
	private int quantityDenominator;
	/**
	 * Numerator of a fractional quantity
	 */
	private int quantityNumerator;
	/**
	 * Unit of quantity
	 */
	private String unit;

	/**
	 * Creates a new, empty Ingredient
	 */
	public Ingredient()
	{
	}

	/**
	 * Creates a new Ingredient from a cursor representation
	 * 
	 * @param cursor
	 *            Cursor representation to load from
	 */
	public Ingredient(final Cursor cursor)
	{
		quantity = cursor.getInt(cursor.getColumnIndex(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY));
		quantityNumerator = cursor.getInt(cursor
				.getColumnIndex(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR));
		quantityDenominator = cursor.getInt(cursor
				.getColumnIndex(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR));
		unit = cursor.getString(cursor.getColumnIndex(RecipeContract.Ingredients.COLUMN_NAME_UNIT));
		item = cursor.getString(cursor.getColumnIndex(RecipeContract.Ingredients.COLUMN_NAME_ITEM));
		preparation = cursor.getString(cursor.getColumnIndex(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION));
	}

	/**
	 * Parses an ingredient from the given rawText, using the default values loaded from the Resources if the string
	 * does not contain all components
	 * 
	 * @param resources
	 *            Resources for loading default values
	 * @param rawText
	 *            Raw text to parse
	 */
	public Ingredient(final Resources resources, final String rawText)
	{
		setFromRaw(resources, rawText);
	}

	/**
	 * Parses an ingredient from the given rawText, using the default values loaded from the Resources if the string
	 * does not contain all components
	 * 
	 * @param resources
	 *            Resources for loading default values
	 * @param rawText
	 *            Raw text to parse
	 */
	public void setFromRaw(final Resources resources, final String rawText)
	{
		int startIndex = 0;
		int endIndex = rawText.indexOf(' ', startIndex);
		if (rawText.isEmpty() || endIndex == -1)
		{
			quantity = resources.getInteger(R.integer.default_ingredient_quantity);
			quantityNumerator = resources.getInteger(R.integer.default_ingredient_quantity_numerator);
			quantityDenominator = resources.getInteger(R.integer.default_ingredient_quantity_denominator);
			unit = resources.getString(R.string.default_ingredient_unit);
			item = rawText;
			preparation = resources.getString(R.string.default_ingredient_preparation);
			return;
		}
		try
		{
			quantity = Integer.parseInt(rawText.substring(startIndex, endIndex));
			startIndex = endIndex + 1;
		} catch (final NumberFormatException e)
		{
			// Don't change startIndex to retry that token
			quantity = resources.getInteger(R.integer.default_ingredient_quantity);
		}
		endIndex = rawText.indexOf('/', startIndex);
		if (endIndex != -1)
			try
			{
				// Ensure both parse correctly before assigning them to our
				// class variables
				final int numerator = Integer.parseInt(rawText.substring(startIndex, endIndex));
				final int tempStartIndex = endIndex + 1;
				endIndex = rawText.indexOf(' ', tempStartIndex);
				final int denominator = Integer.parseInt(rawText.substring(tempStartIndex, endIndex));
				startIndex = endIndex + 1;
				// Assign to our class variables
				quantityNumerator = numerator;
				quantityDenominator = denominator;
			} catch (final NumberFormatException e)
			{
				// Don't change startIndex to retry that token
				quantityNumerator = resources.getInteger(R.integer.default_ingredient_quantity_numerator);
				quantityDenominator = resources.getInteger(R.integer.default_ingredient_quantity_denominator);
			}
		endIndex = rawText.indexOf(' ', startIndex);
		if (endIndex == -1)
			unit = resources.getString(R.string.default_ingredient_unit);
		else
		{
			String possibleUnit = rawText.substring(startIndex, endIndex);
			// Strip off periods or final S's (lbs. -> lb, etc)
			if (possibleUnit.endsWith("s."))
				possibleUnit = possibleUnit.substring(0, possibleUnit.length() - 2);
			else if (possibleUnit.endsWith("."))
				possibleUnit = possibleUnit.substring(0, possibleUnit.length() - 1);
			if (Arrays.asList(resources.getStringArray(R.array.units)).contains(possibleUnit))
			{
				unit = possibleUnit;
				startIndex = endIndex + 1;
			}
			else
				unit = resources.getString(R.string.default_ingredient_unit);
		}
		endIndex = Math.max(rawText.indexOf(';', startIndex), rawText.indexOf(',', startIndex));
		// Take everything until the end of the string as the item if there
		// is no preparation
		if (endIndex == -1)
			endIndex = rawText.length();
		item = rawText.substring(startIndex, endIndex).trim();
		startIndex = endIndex + 1;
		endIndex = rawText.length();
		if (endIndex > startIndex)
			preparation = rawText.substring(startIndex, endIndex).trim();
		else
			preparation = resources.getString(R.string.default_ingredient_preparation);
	}

	/**
	 * Converts this ingredient into appropriate ContentValues for insertion into the RecipeProvider
	 * 
	 * @param recipeId
	 *            Mandatory recipeId to be associated with this ingredient
	 * @return ContentValues usable by the RecipeProvider
	 */
	public ContentValues toContentValues(final long recipeId)
	{
		final ContentValues contentValues = new ContentValues();
		contentValues.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId);
		contentValues.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, quantity);
		contentValues.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, quantityNumerator);
		contentValues.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, quantityDenominator);
		contentValues.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, unit);
		contentValues.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, item);
		contentValues.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, preparation);
		return contentValues;
	}

	@Override
	public String toString()
	{
		if (item.isEmpty())
			return "";
		final StringBuilder sb = new StringBuilder();
		if (quantity > 0)
			sb.append(quantity);
		if (quantityNumerator > 0)
		{
			if (sb.length() > 0)
				sb.append(' ');
			sb.append(quantityNumerator);
			sb.append('/');
			sb.append(quantityDenominator);
		}
		if (unit != null && !unit.equals(""))
		{
			if (sb.length() > 0)
				sb.append(' ');
			sb.append(unit);
		}
		if (sb.length() > 0)
			sb.append(' ');
		sb.append(item);
		if (preparation != null && !preparation.equals(""))
		{
			if (sb.length() > 0)
				sb.append("; ");
			sb.append(preparation);
		}
		return sb.toString();
	}
}
