package com.ianhanniballake.recipebook.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ianhanniballake.recipebook.BuildConfig;
import com.ianhanniballake.recipebook.R;
import com.ianhanniballake.recipebook.auth.Auth;

/**
 * Provides access to a database of recipes. Each recipe has a title and a description.
 */
public class RecipeProvider extends ContentProvider
{
	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	static class DatabaseHelper extends SQLiteOpenHelper
	{
		/**
		 * Creates a new DatabaseHelper
		 * 
		 * @param context
		 *            context of this database
		 */
		DatabaseHelper(final Context context)
		{
			super(context, RecipeProvider.DATABASE_NAME, null, RecipeProvider.DATABASE_VERSION);
		}

		/**
		 * Creates the underlying database with table name and column names taken from the RecipeContract class.
		 */
		@Override
		public void onCreate(final SQLiteDatabase db)
		{
			if (BuildConfig.DEBUG)
				Log.d(RecipeProvider.TAG, "Creating the " + RecipeContract.Recipes.TABLE_NAME + " table");
			db.execSQL("CREATE TABLE " + RecipeContract.Recipes.TABLE_NAME + " (" + BaseColumns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + RecipeContract.Recipes.COLUMN_NAME_TITLE + " TEXT,"
					+ RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION + " TEXT"
					+ RecipeContract.Recipes.COLUMN_NAME_DRIVE_ID + " TEXT" + ");");
			if (BuildConfig.DEBUG)
				Log.d(RecipeProvider.TAG, "Creating the " + RecipeContract.Ingredients.TABLE_NAME + " table");
			db.execSQL("CREATE TABLE " + RecipeContract.Ingredients.TABLE_NAME + " (" + BaseColumns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID
					+ " INTEGER," + RecipeContract.Ingredients.COLUMN_NAME_QUANTITY + " INTEGER,"
					+ RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR + " INTEGER,"
					+ RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR + " INTEGER,"
					+ RecipeContract.Ingredients.COLUMN_NAME_UNIT + " TEXT,"
					+ RecipeContract.Ingredients.COLUMN_NAME_ITEM + " TEXT,"
					+ RecipeContract.Ingredients.COLUMN_NAME_PREPARATION + " TEXT,"
					+ " CONSTRAINT fk_recipe_ingredient FOREIGN KEY ("
					+ RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID + ") REFERENCES "
					+ RecipeContract.Recipes.TABLE_NAME + " (" + BaseColumns._ID + ") ON DELETE CASCADE" + ");");
			if (BuildConfig.DEBUG)
				Log.d(RecipeProvider.TAG, "Creating the " + RecipeContract.Instructions.TABLE_NAME + " table");
			db.execSQL("CREATE TABLE " + RecipeContract.Instructions.TABLE_NAME + " (" + BaseColumns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID
					+ " INTEGER," + RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION + " TEXT,"
					+ " CONSTRAINT fk_recipe_instruction FOREIGN KEY ("
					+ RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID + ") REFERENCES "
					+ RecipeContract.Recipes.TABLE_NAME + " (" + BaseColumns._ID + ") ON DELETE CASCADE" + ");");
			// Insert sample data
			final ContentValues values = new ContentValues();
			values.put(RecipeContract.Recipes.COLUMN_NAME_TITLE, "Chicken Marsala");
			values.put(
					RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION,
					"While this French inspired dish is usually prepared with Marsala wine, you can substitute for any red wine you might have or even simply chicken stock.");
			final long recipeId1 = db.insert(RecipeContract.Recipes.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Recipes.COLUMN_NAME_TITLE, "Cheese and Garlic Biscuits");
			values.put(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION, "As served at Red Lobster");
			final long recipeId2 = db.insert(RecipeContract.Recipes.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Recipes.COLUMN_NAME_TITLE, "Paleo Chocolate Chip Cookies");
			values.put(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION, "Paleo friendly treat");
			final long recipeId3 = db.insert(RecipeContract.Recipes.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Recipes.COLUMN_NAME_TITLE, "Black Pepper and Parmesan Ricotta Gnocchi");
			values.put(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION, "Dinner in 15");
			final long recipeId4 = db.insert(RecipeContract.Recipes.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Recipes.COLUMN_NAME_TITLE, "Salmon Baked with Herbed Butter");
			values.put(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION, "");
			final long recipeId5 = db.insert(RecipeContract.Recipes.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 5);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Boneless Chicken Breast");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "with or without skin");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Bacon");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "cut into 1 inch pieces");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "lb");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Button Mushroom");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Garlic Clove");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "minced");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Tomato Paste");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "cup");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Marsala");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "sub other red wine");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tbsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Lemon Juice");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tbsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Butter");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "divided into 4 pieces, sub ghee");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tbsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Parsley");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "minced");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tbsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Cooking Fat");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "cup");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Bisquick Baking Mix");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 3);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "cup");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Milk");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "cup");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Chedder Cheese");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "shredded");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "cup");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Butter");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "sub margarine");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Garlic Powder");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "cup");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Coconut Flour");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "cup");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Coconut Oil");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 3);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tbsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Honey");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Eggs");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Vanilla Extract");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 8);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Sea Salt");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "cup");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Unsweetened Coconut");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "shredded");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 3);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "cup");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Gluten Free Chocolate Chips");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "sub regular chocolate chips");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "cup");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Ricotta");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "cup");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Parmesan Cheese");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "finely grated");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Black Pepper");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "freshly grated");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Eggs");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Flour");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tbsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Salt");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tbsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Butter");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 4);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tbsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Butter");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "softened");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "cup");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Parsley");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "chopped");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 2);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Garlic Cloves");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "minced");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "tbsp");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Capers");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "chopped");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT, "lb");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Salmon");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "in 4 fillets");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY, 1);
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM, "Lemon");
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION, "thinly sliced");
			db.insert(RecipeContract.Ingredients.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Preheat your oven to 200F and place a large plate inside to keep the chicken warm later on");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(
					RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Heat some cooking fat in a large skillet over a medium-high heat, season the chicken breasts with salt and pepper and cook for about 3 minutes on each side, until golden brown and cooked through.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Reserve the cooked chicken to the plate in the oven.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Over a medium-low heat, cook the bacon for about 4 minutes, until crisp. Set the bacon aside and reserve.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(
					RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Add the mushrooms to the skillet and cook for about 8 minute on a medium-high heat, until the liquid extracted from the mushrooms has evaporated entirely and the mushrooms are slightly browned.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Add back the cooked bacon, garlic and tomato paste and cook for about a minute.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Add the Marsala or red wine, bring to a boil and cook until the liquid is reduced to about 1 ¼ cups, about 5 minutes.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Place the skillet off the heat, add in the lemon juice and whisk in the butter or ghee, one piece at a time.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Add the parsley and season to taste with salt and pepper.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId1);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Pour the Marsala sauce over the cooked chicken and serve.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId2);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION, "Heat oven to 450");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId2);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Mix baking mix, milk and cheese until soft dough forms");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId2);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION, "Beat vigorously for 30 seconds");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId2);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Drop dough by spoonfuls onto ungreased cookie sheet.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId2);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Bake 8 to 10 minutes or until golden brown.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId2);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION, "Mix margarine and garlic powder");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId2);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Brush over warm biscuits before removing them from cookie sheet and serve warm");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION, "Preheat the oven to 375 degrees");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Melt the honey and coconut oil together in the microwave for about 15 seconds");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"In a large bowl, mix together the coconut oil, honey, eggs, vanilla extract and sea salt");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Stir in the coconut flour, shredded coconut and chocolate chips");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION, "Line a baking sheet with parchment paper");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Roll out little tbsp sized balls of cookie dough and place on baking sheet");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId3);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Bake for 12 to 15 minutes or until golden brown");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId4);
			values.put(
					RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Combine all of the ingredients, except the butter, in a medium bowl and mix well with a wooden spoon. You should have a very think, soft and somewhat sticky dough.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId4);
			values.put(
					RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Generously flour a large cutting board. Dust your dough with a bit of flour and break off about a third of it with your hands. Roll the dough into a 1 inch thick log, adding more flour, as needed, to keep it from sticking.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId4);
			values.put(
					RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Use a bench scraper or a sharp knife to cut the log into 1 inch thick pieces and place them on a lined baking sheet. Repeat until you have used up the rest of the dough.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId4);
			values.put(
					RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Bring a large pot of salted water to a boil and cook the gnocchi: drop the gnocchi into the water in batches, cooking until they float to the top (about 3 minutes). Gently stir the water with a wooden spoon to nudge any gnocchi that stick to the bottom to float to the top. Meanwhile, melt the butter over medium-high heat in a large cast iron skillet.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId4);
			values.put(
					RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"As the gnocchi finish cooking, remove them from the water with a slotted spoon and add them to the skillet to brown them, tossing occassionally and adjusting the heat to ensure that you don’t burn them. Ideally, you’ll want to let the pieces crisp up on one side before turning them over.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId4);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Serve the gnocchi with additional Parmesan cheese and freshly grated black pepper.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION, "Preheat oven to 375 F");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Mix the butter, parsley, garlic and capers.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Place each piece of salmon in the center of a square of foil and season with salt and pepper.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Cut deep slashes into each piece of fish and spread the herbed butter into the cuts.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Cut the lemon slices in half and push halves into each slash in the fish.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Seal the foil around the salmon and place them on a baking sheet.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					"Bake for 10 to 15 minutes or until the salmon is cooked through.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
			values.clear();
			values.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID, recipeId5);
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION, "Remove lemon slices before serving.");
			db.insert(RecipeContract.Instructions.TABLE_NAME, null, values);
		}

		/**
		 * 
		 * Demonstrates that the provider must consider what happens when the underlying database is changed. Note that
		 * this currently just destroys and recreates the database - should upgrade in place
		 */
		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion)
		{
			Log.w(RecipeProvider.TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + RecipeContract.Recipes.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + RecipeContract.Ingredients.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + RecipeContract.Instructions.TABLE_NAME);
			onCreate(db);
		}
	}

	/**
	 * The database that the provider uses as its underlying data store
	 */
	private static final String DATABASE_NAME = "recipes.db";
	/**
	 * The database version
	 */
	private static final int DATABASE_VERSION = 3;
	/**
	 * The incoming URI matches the Ingredient ID URI pattern
	 */
	private static final int INGREDIENT_ID = 4;
	/**
	 * The incoming URI matches the Ingredients URI pattern
	 */
	private static final int INGREDIENTS = 3;
	/**
	 * The incoming URI matches the Instruction ID URI pattern
	 */
	private static final int INSTRUCTION_ID = 6;
	/**
	 * The incoming URI matches the Instructions URI pattern
	 */
	private static final int INSTRUCTIONS = 5;
	/**
	 * The incoming URI matches the Recipe ID URI pattern
	 */
	private static final int RECIPE_ID = 2;
	/**
	 * The incoming URI matches the Recipes URI pattern
	 */
	private static final int RECIPES = 1;
	/**
	 * Used for debugging and logging
	 */
	private static final String TAG = "RecipeProvider";
	/**
	 * A UriMatcher instance
	 */
	private static final UriMatcher uriMatcher = RecipeProvider.buildUriMatcher();

	/**
	 * Creates and initializes the URI matcher
	 * 
	 * @return the URI Matcher
	 */
	private static UriMatcher buildUriMatcher()
	{
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		// Add a pattern that routes URIs terminated with "recipes" to a RECIPES operation
		matcher.addURI(RecipeContract.AUTHORITY, "recipes", RecipeProvider.RECIPES);
		// Add a pattern that routes URIs terminated with "recipes" plus an integer to a recipe ID operation
		matcher.addURI(RecipeContract.AUTHORITY, "recipes/#", RecipeProvider.RECIPE_ID);
		// Add a pattern that routes URIs terminated with "ingredients" to a INGREDIENTS operation
		matcher.addURI(RecipeContract.AUTHORITY, "ingredients", RecipeProvider.INGREDIENTS);
		// Add a pattern that routes URIs terminated with "ingredients" plus an integer to a Ingredient ID operation
		matcher.addURI(RecipeContract.AUTHORITY, "ingredients/#", RecipeProvider.INGREDIENT_ID);
		// Add a pattern that routes URIs terminated with "instructions" to a INSTRUCTIONS operation
		matcher.addURI(RecipeContract.AUTHORITY, "instructions", RecipeProvider.INSTRUCTIONS);
		// Add a pattern that routes URIs terminated with "instructions" plus an integer to a Instruction ID operation
		matcher.addURI(RecipeContract.AUTHORITY, "instructions/#", RecipeProvider.INSTRUCTION_ID);
		return matcher;
	}

	/**
	 * Handle to a new DatabaseHelper.
	 */
	private DatabaseHelper databaseHelper;

	@Override
	public int delete(final Uri uri, final String where, final String[] whereArgs)
	{
		// Opens the database object in "write" mode.
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		String finalWhere;
		int count;
		// Does the delete based on the incoming URI pattern.
		switch (RecipeProvider.uriMatcher.match(uri))
		{
			case RECIPES:
				// If the incoming pattern matches the general pattern for recipes, does a delete based on the incoming
				// "where" columns and arguments.
				count = db.delete(RecipeContract.Recipes.TABLE_NAME, where, whereArgs);
				break;
			case RECIPE_ID:
				// If the incoming URI matches a single recipe ID, does the delete based on the incoming data, but
				// modifies the where clause to restrict it to the particular recipe ID.
				finalWhere = BaseColumns._ID + " = "
						+ uri.getPathSegments().get(RecipeContract.Recipes.RECIPE_ID_PATH_POSITION);
				// If there were additional selection criteria, append them to the final WHERE clause
				if (where != null)
					finalWhere = finalWhere + " AND " + where;
				count = db.delete(RecipeContract.Recipes.TABLE_NAME, finalWhere, whereArgs);
				break;
			case INGREDIENTS:
				// If the incoming pattern matches the general pattern for ingredients, does a delete based on the
				// incoming "where" columns and arguments.
				count = db.delete(RecipeContract.Ingredients.TABLE_NAME, where, whereArgs);
				break;
			case INGREDIENT_ID:
				// If the incoming URI matches a single ingredient ID, does the delete based on the incoming data, but
				// modifies the where clause to restrict it to the particular ingredient ID.
				finalWhere = BaseColumns._ID + " = "
						+ uri.getPathSegments().get(RecipeContract.Ingredients.INGREDIENT_ID_PATH_POSITION);
				// If there were additional selection criteria, append them to the final WHERE clause
				if (where != null)
					finalWhere = finalWhere + " AND " + where;
				count = db.delete(RecipeContract.Ingredients.TABLE_NAME, finalWhere, whereArgs);
				break;
			case INSTRUCTIONS:
				// If the incoming pattern matches the general pattern for instructions, does a delete based on the
				// incoming "where" columns and arguments.
				count = db.delete(RecipeContract.Instructions.TABLE_NAME, where, whereArgs);
				break;
			case INSTRUCTION_ID:
				// If the incoming URI matches a single instruction ID, does the delete based on the incoming data, but
				// modifies the where clause to restrict it to the particular instruction ID.
				finalWhere = BaseColumns._ID + " = "
						+ uri.getPathSegments().get(RecipeContract.Instructions.INSTRUCTION_ID_PATH_POSITION);
				// If there were additional selection criteria, append them to the final WHERE clause
				if (where != null)
					finalWhere = finalWhere + " AND " + where;
				count = db.delete(RecipeContract.Instructions.TABLE_NAME, finalWhere, whereArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
		localBroadcastManager.sendBroadcast(new Intent(Auth.ACTION_SYNC, uri));
		return count;
	}

	@Override
	public String getType(final Uri uri)
	{
		/**
		 * Chooses the MIME type based on the incoming URI pattern
		 */
		switch (RecipeProvider.uriMatcher.match(uri))
		{
			case RECIPES:
				// If the pattern is for recipes, returns the general content type.
				return RecipeContract.Recipes.CONTENT_TYPE;
			case RECIPE_ID:
				// If the pattern is for recipe IDs, returns the recipe ID content type.
				return RecipeContract.Recipes.CONTENT_ITEM_TYPE;
			case INGREDIENTS:
				// If the pattern is for ingredients, returns the general content type.
				return RecipeContract.Ingredients.CONTENT_TYPE;
			case INGREDIENT_ID:
				// If the pattern is for ingredient IDs, returns the ingredient ID content type.
				return RecipeContract.Ingredients.CONTENT_ITEM_TYPE;
			case INSTRUCTIONS:
				// If the pattern is for instructions, returns the general content type.
				return RecipeContract.Instructions.CONTENT_TYPE;
			case INSTRUCTION_ID:
				// If the pattern is for instruction IDs, returns the instruction ID content type.
				return RecipeContract.Instructions.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues initialValues)
	{
		// Validates the incoming URI. Only the full provider URI is allowed for inserts.
		if (RecipeProvider.uriMatcher.match(uri) == RecipeProvider.RECIPES)
			return insertRecipe(uri, initialValues);
		else if (RecipeProvider.uriMatcher.match(uri) == RecipeProvider.INGREDIENTS)
			return insertIngredient(uri, initialValues);
		else if (RecipeProvider.uriMatcher.match(uri) == RecipeProvider.INSTRUCTIONS)
			return insertInstruction(uri, initialValues);
		else
			throw new IllegalArgumentException("Unknown URI " + uri);
	}

	/**
	 * Creates a new Ingredient row.
	 * 
	 * @param uri
	 *            The content:// URI of the insertion request.
	 * @param initialValues
	 *            A set of column_name/value pairs to add to the database. Must contain the Recipe ID associated with
	 *            this ingredient
	 * @return The URI for the newly inserted item.
	 */
	private Uri insertIngredient(final Uri uri, final ContentValues initialValues)
	{
		ContentValues values;
		if (initialValues != null)
			values = new ContentValues(initialValues);
		else
			values = new ContentValues();
		if (!values.containsKey(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID))
			throw new IllegalArgumentException("Initial values must contain Recipe ID " + initialValues);
		if (!values.containsKey(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY))
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY,
					getContext().getResources().getInteger(R.integer.default_ingredient_quantity));
		if (!values.containsKey(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR))
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR, getContext().getResources()
					.getInteger(R.integer.default_ingredient_quantity_numerator));
		if (!values.containsKey(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR))
			values.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR, getContext().getResources()
					.getInteger(R.integer.default_ingredient_quantity_denominator));
		if (!values.containsKey(RecipeContract.Ingredients.COLUMN_NAME_UNIT))
			values.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT,
					getContext().getResources().getString(R.string.default_ingredient_unit));
		if (!values.containsKey(RecipeContract.Ingredients.COLUMN_NAME_ITEM))
			values.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM,
					getContext().getResources().getString(R.string.default_ingredient_item));
		if (!values.containsKey(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION))
			values.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION,
					getContext().getResources().getString(R.string.default_ingredient_preparation));
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		final long rowId = db.insert(RecipeContract.Ingredients.TABLE_NAME,
				RecipeContract.Ingredients.COLUMN_NAME_ITEM, values);
		// If the insert succeeded, the row ID exists.
		if (rowId > 0)
		{
			// Creates a URI with the ingredient ID pattern and the new row ID appended to it.
			final Uri ingredientUri = ContentUris.withAppendedId(RecipeContract.Ingredients.CONTENT_ID_URI_BASE, rowId);
			getContext().getContentResolver().notifyChange(ingredientUri, null);
			final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
			localBroadcastManager.sendBroadcast(new Intent(Auth.ACTION_SYNC, ingredientUri));
			return ingredientUri;
		}
		// If the insert didn't succeed, then the rowID is <= 0
		throw new SQLException("Failed to insert row into " + uri);
	}

	/**
	 * Creates a new Instruction row.
	 * 
	 * @param uri
	 *            The content:// URI of the insertion request.
	 * @param initialValues
	 *            A set of column_name/value pairs to add to the database. Must contain the Recipe ID associated with
	 *            this instruction
	 * @return The URI for the newly inserted item.
	 */
	private Uri insertInstruction(final Uri uri, final ContentValues initialValues)
	{
		ContentValues values;
		if (initialValues != null)
			values = new ContentValues(initialValues);
		else
			values = new ContentValues();
		if (!values.containsKey(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID))
			throw new IllegalArgumentException("Initial values must contain Recipe ID " + initialValues);
		if (!values.containsKey(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION))
			values.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
					getContext().getResources().getString(R.string.default_instruction_instruction));
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		final long rowId = db.insert(RecipeContract.Instructions.TABLE_NAME,
				RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION, values);
		// If the insert succeeded, the row ID exists.
		if (rowId > 0)
		{
			// Creates a URI with the instruction ID pattern and the new row ID appended to it.
			final Uri instructionUri = ContentUris.withAppendedId(RecipeContract.Instructions.CONTENT_ID_URI_BASE,
					rowId);
			getContext().getContentResolver().notifyChange(instructionUri, null);
			final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
			localBroadcastManager.sendBroadcast(new Intent(Auth.ACTION_SYNC, instructionUri));
			return instructionUri;
		}
		// If the insert didn't succeed, then the rowID is <= 0
		throw new SQLException("Failed to insert row into " + uri);
	}

	/**
	 * Creates a new Recipe row.
	 * 
	 * @param uri
	 *            The content:// URI of the insertion request.
	 * @param initialValues
	 *            A set of column_name/value pairs to add to the database.
	 * @return The URI for the newly inserted item.
	 */
	private Uri insertRecipe(final Uri uri, final ContentValues initialValues)
	{
		ContentValues values;
		if (initialValues != null)
			values = new ContentValues(initialValues);
		else
			values = new ContentValues();
		if (!values.containsKey(RecipeContract.Recipes.COLUMN_NAME_TITLE))
			values.put(RecipeContract.Recipes.COLUMN_NAME_TITLE,
					getContext().getResources().getString(R.string.default_recipe_title));
		if (!values.containsKey(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION))
			values.put(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION,
					getContext().getResources().getString(R.string.default_recipe_description));
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		final long rowId = db.insert(RecipeContract.Recipes.TABLE_NAME, RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION,
				values);
		// If the insert succeeded, the row ID exists.
		if (rowId > 0)
		{
			// Creates a URI with the recipe ID pattern and the new row ID appended to it.
			final Uri recipeUri = ContentUris.withAppendedId(RecipeContract.Recipes.CONTENT_ID_URI_BASE, rowId);
			getContext().getContentResolver().notifyChange(recipeUri, null);
			final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
			localBroadcastManager.sendBroadcast(new Intent(Auth.ACTION_SYNC, recipeUri));
			return recipeUri;
		}
		// If the insert didn't succeed, then the rowID is <= 0
		throw new SQLException("Failed to insert row into " + uri);
	}

	/**
	 * Creates the underlying DatabaseHelper
	 * 
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate()
	{
		databaseHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
			final String sortOrder)
	{
		if (RecipeProvider.uriMatcher.match(uri) == RecipeProvider.RECIPES
				|| RecipeProvider.uriMatcher.match(uri) == RecipeProvider.RECIPE_ID)
			return queryRecipe(uri, projection, selection, selectionArgs, sortOrder);
		else if (RecipeProvider.uriMatcher.match(uri) == RecipeProvider.INGREDIENTS
				|| RecipeProvider.uriMatcher.match(uri) == RecipeProvider.INGREDIENT_ID)
			return queryIngredient(uri, projection, selection, selectionArgs, sortOrder);
		else if (RecipeProvider.uriMatcher.match(uri) == RecipeProvider.INSTRUCTIONS
				|| RecipeProvider.uriMatcher.match(uri) == RecipeProvider.INSTRUCTION_ID)
			return queryInstruction(uri, projection, selection, selectionArgs, sortOrder);
		else
			throw new IllegalArgumentException("Unknown URI " + uri);
	}

	/**
	 * Queries for ingredient(s).
	 * 
	 * @param uri
	 *            The URI to query. This will be the full URI sent by the client; if the client is requesting a specific
	 *            record, the URI will end in a record number that the implementation should parse and add to a WHERE or
	 *            HAVING clause, specifying that _id value.
	 * @param projection
	 *            The list of columns to put into the cursor. If null all columns are included.
	 * @param selection
	 *            A selection criteria to apply when filtering rows. If null then all rows are included.
	 * @param selectionArgs
	 *            You may include ?s in selection, which will be replaced by the values from selectionArgs, in order
	 *            that they appear in the selection. The values will be bound as Strings.
	 * @param sortOrder
	 *            How the rows in the cursor should be sorted. If null then the provider is free to define the sort
	 *            order.
	 * @return A Cursor or null.
	 */
	private Cursor queryIngredient(final Uri uri, final String[] projection, final String selection,
			final String[] selectionArgs, final String sortOrder)
	{
		// Constructs a new query builder and sets its table name
		final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(RecipeContract.Ingredients.TABLE_NAME);
		final HashMap<String, String> allColumnProjectionMap = new HashMap<String, String>();
		allColumnProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
		allColumnProjectionMap.put(RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID,
				RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID);
		allColumnProjectionMap.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY,
				RecipeContract.Ingredients.COLUMN_NAME_QUANTITY);
		allColumnProjectionMap.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR,
				RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_NUMERATOR);
		allColumnProjectionMap.put(RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR,
				RecipeContract.Ingredients.COLUMN_NAME_QUANTITY_DENOMINATOR);
		allColumnProjectionMap.put(RecipeContract.Ingredients.COLUMN_NAME_UNIT,
				RecipeContract.Ingredients.COLUMN_NAME_UNIT);
		allColumnProjectionMap.put(RecipeContract.Ingredients.COLUMN_NAME_ITEM,
				RecipeContract.Ingredients.COLUMN_NAME_ITEM);
		allColumnProjectionMap.put(RecipeContract.Ingredients.COLUMN_NAME_PREPARATION,
				RecipeContract.Ingredients.COLUMN_NAME_PREPARATION);
		qb.setProjectionMap(allColumnProjectionMap);
		switch (RecipeProvider.uriMatcher.match(uri))
		{
			case INGREDIENTS:
				break;
			case INGREDIENT_ID:
				// If the incoming URI is for a single ingredient identified by its ID, appends "_ID = <ingredientId>"
				// to the where clause, so that it selects that single ingredient
				qb.appendWhere(BaseColumns._ID + "="
						+ uri.getPathSegments().get(RecipeContract.Ingredients.INGREDIENT_ID_PATH_POSITION));
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		String orderBy;
		if (TextUtils.isEmpty(sortOrder))
			orderBy = RecipeContract.Ingredients.DEFAULT_SORT_ORDER;
		else
			orderBy = sortOrder;
		final SQLiteDatabase db = databaseHelper.getReadableDatabase();
		final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	/**
	 * Queries for ingredient(s).
	 * 
	 * @param uri
	 *            The URI to query. This will be the full URI sent by the client; if the client is requesting a specific
	 *            record, the URI will end in a record number that the implementation should parse and add to a WHERE or
	 *            HAVING clause, specifying that _id value.
	 * @param projection
	 *            The list of columns to put into the cursor. If null all columns are included.
	 * @param selection
	 *            A selection criteria to apply when filtering rows. If null then all rows are included.
	 * @param selectionArgs
	 *            You may include ?s in selection, which will be replaced by the values from selectionArgs, in order
	 *            that they appear in the selection. The values will be bound as Strings.
	 * @param sortOrder
	 *            How the rows in the cursor should be sorted. If null then the provider is free to define the sort
	 *            order.
	 * @return A Cursor or null.
	 */
	private Cursor queryInstruction(final Uri uri, final String[] projection, final String selection,
			final String[] selectionArgs, final String sortOrder)
	{
		// Constructs a new query builder and sets its table name
		final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(RecipeContract.Instructions.TABLE_NAME);
		final HashMap<String, String> allColumnProjectionMap = new HashMap<String, String>();
		allColumnProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
		allColumnProjectionMap.put(RecipeContract.Instructions.COLUMN_NAME_RECIPE_ID,
				RecipeContract.Ingredients.COLUMN_NAME_RECIPE_ID);
		allColumnProjectionMap.put(RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION,
				RecipeContract.Instructions.COLUMN_NAME_INSTRUCTION);
		qb.setProjectionMap(allColumnProjectionMap);
		switch (RecipeProvider.uriMatcher.match(uri))
		{
			case INSTRUCTIONS:
				break;
			case INSTRUCTION_ID:
				// If the incoming URI is for a single instruction identified by its ID, appends "_ID = <instructionId>"
				// to the where clause, so that it selects that single instruction
				qb.appendWhere(BaseColumns._ID + "="
						+ uri.getPathSegments().get(RecipeContract.Instructions.INSTRUCTION_ID_PATH_POSITION));
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		String orderBy;
		if (TextUtils.isEmpty(sortOrder))
			orderBy = RecipeContract.Instructions.DEFAULT_SORT_ORDER;
		else
			orderBy = sortOrder;
		final SQLiteDatabase db = databaseHelper.getReadableDatabase();
		final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	/**
	 * Queries for recipe(s).
	 * 
	 * @param uri
	 *            The URI to query. This will be the full URI sent by the client; if the client is requesting a specific
	 *            record, the URI will end in a record number that the implementation should parse and add to a WHERE or
	 *            HAVING clause, specifying that _id value.
	 * @param projection
	 *            The list of columns to put into the cursor. If null all columns are included.
	 * @param selection
	 *            A selection criteria to apply when filtering rows. If null then all rows are included.
	 * @param selectionArgs
	 *            You may include ?s in selection, which will be replaced by the values from selectionArgs, in order
	 *            that they appear in the selection. The values will be bound as Strings.
	 * @param sortOrder
	 *            How the rows in the cursor should be sorted. If null then the provider is free to define the sort
	 *            order.
	 * @return A Cursor or null.
	 */
	private Cursor queryRecipe(final Uri uri, final String[] projection, final String selection,
			final String[] selectionArgs, final String sortOrder)
	{
		// Constructs a new query builder and sets its table name
		final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(RecipeContract.Recipes.TABLE_NAME);
		final HashMap<String, String> allColumnProjectionMap = new HashMap<String, String>();
		allColumnProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
		allColumnProjectionMap.put(RecipeContract.Recipes.COLUMN_NAME_TITLE, RecipeContract.Recipes.COLUMN_NAME_TITLE);
		allColumnProjectionMap.put(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION,
				RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION);
		allColumnProjectionMap.put(RecipeContract.Recipes.COLUMN_NAME_DRIVE_ID,
				RecipeContract.Recipes.COLUMN_NAME_DRIVE_ID);
		qb.setProjectionMap(allColumnProjectionMap);
		switch (RecipeProvider.uriMatcher.match(uri))
		{
			case RECIPES:
				break;
			case RECIPE_ID:
				// If the incoming URI is for a single recipe identified by its ID, appends "_ID = <recipeID>" to the
				// where clause, so that it selects that single recipe
				qb.appendWhere(BaseColumns._ID + "="
						+ uri.getPathSegments().get(RecipeContract.Recipes.RECIPE_ID_PATH_POSITION));
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		String orderBy;
		if (TextUtils.isEmpty(sortOrder))
			orderBy = RecipeContract.Recipes.DEFAULT_SORT_ORDER;
		else
			orderBy = sortOrder;
		final SQLiteDatabase db = databaseHelper.getReadableDatabase();
		final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs)
	{
		if (RecipeProvider.uriMatcher.match(uri) == RecipeProvider.RECIPES
				|| RecipeProvider.uriMatcher.match(uri) == RecipeProvider.RECIPE_ID)
			return updateRecipe(uri, values, selection, selectionArgs);
		else if (RecipeProvider.uriMatcher.match(uri) == RecipeProvider.INGREDIENTS
				|| RecipeProvider.uriMatcher.match(uri) == RecipeProvider.INGREDIENT_ID)
			return updateIngredient(uri, values, selection, selectionArgs);
		else if (RecipeProvider.uriMatcher.match(uri) == RecipeProvider.INSTRUCTIONS
				|| RecipeProvider.uriMatcher.match(uri) == RecipeProvider.INSTRUCTION_ID)
			return updateInstruction(uri, values, selection, selectionArgs);
		else
			throw new IllegalArgumentException("Unknown URI " + uri);
	}

	/**
	 * Update the matching ingredient(s)
	 * 
	 * @param uri
	 *            The URI to query. This can potentially have a record ID if this is an update request for a specific
	 *            record.
	 * @param values
	 *            A Bundle mapping from column names to new column values (NULL is a valid value).
	 * @param selection
	 *            An optional filter to match rows to update.
	 * @param selectionArgs
	 *            Arguments to the optional filter to match rows to update
	 * @return The number of rows affected.
	 */
	private int updateIngredient(final Uri uri, final ContentValues values, final String selection,
			final String[] selectionArgs)
	{
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		int count = 0;
		switch (RecipeProvider.uriMatcher.match(uri))
		{
			case INGREDIENTS:
				// If the incoming URI matches the general ingredients pattern, does the update based on the incoming
				// data.
				count = db.update(RecipeContract.Ingredients.TABLE_NAME, values, selection, selectionArgs);
				break;
			case INGREDIENT_ID:
				// If the incoming URI matches a single ingredients ID, does the update based on the incoming data, but
				// modifies the where clause to restrict it to the particular recipe ID.
				uri.getPathSegments().get(RecipeContract.Ingredients.INGREDIENT_ID_PATH_POSITION);
				String finalWhere = BaseColumns._ID + " = "
						+ uri.getPathSegments().get(RecipeContract.Ingredients.INGREDIENT_ID_PATH_POSITION);
				// If there were additional selection criteria, append them to the final WHERE clause
				if (selection != null)
					finalWhere = finalWhere + " AND " + selection;
				count = db.update(RecipeContract.Ingredients.TABLE_NAME, values, finalWhere, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
		localBroadcastManager.sendBroadcast(new Intent(Auth.ACTION_SYNC, uri));
		return count;
	}

	/**
	 * Update the matching instruction(s)
	 * 
	 * @param uri
	 *            The URI to query. This can potentially have a record ID if this is an update request for a specific
	 *            record.
	 * @param values
	 *            A Bundle mapping from column names to new column values (NULL is a valid value).
	 * @param selection
	 *            An optional filter to match rows to update.
	 * @param selectionArgs
	 *            Arguments to the optional filter to match rows to update
	 * @return The number of rows affected.
	 */
	private int updateInstruction(final Uri uri, final ContentValues values, final String selection,
			final String[] selectionArgs)
	{
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		int count = 0;
		switch (RecipeProvider.uriMatcher.match(uri))
		{
			case INSTRUCTIONS:
				// If the incoming URI matches the general instructions pattern, does the update based on the incoming
				// data.
				count = db.update(RecipeContract.Instructions.TABLE_NAME, values, selection, selectionArgs);
				break;
			case INSTRUCTION_ID:
				// If the incoming URI matches a single ingredients ID, does the
				// update based on the incoming data, but modifies the where
				// clause to restrict it to the particular recipe ID.
				uri.getPathSegments().get(RecipeContract.Instructions.INSTRUCTION_ID_PATH_POSITION);
				String finalWhere = BaseColumns._ID + " = "
						+ uri.getPathSegments().get(RecipeContract.Instructions.INSTRUCTION_ID_PATH_POSITION);
				// If there were additional selection criteria, append them to
				// the final WHERE clause
				if (selection != null)
					finalWhere = finalWhere + " AND " + selection;
				count = db.update(RecipeContract.Instructions.TABLE_NAME, values, finalWhere, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
		localBroadcastManager.sendBroadcast(new Intent(Auth.ACTION_SYNC, uri));
		return count;
	}

	/**
	 * Update the matching recipe(s)
	 * 
	 * @param uri
	 *            The URI to query. This can potentially have a record ID if this is an update request for a specific
	 *            record.
	 * @param values
	 *            A Bundle mapping from column names to new column values (NULL is a valid value).
	 * @param selection
	 *            An optional filter to match rows to update.
	 * @param selectionArgs
	 *            Arguments to the optional filter to match rows to update
	 * @return The number of rows affected.
	 */
	private int updateRecipe(final Uri uri, final ContentValues values, final String selection,
			final String[] selectionArgs)
	{
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		int count = 0;
		switch (RecipeProvider.uriMatcher.match(uri))
		{
			case RECIPES:
				// If the incoming URI matches the general recipes pattern, does the update based on the incoming data.
				count = db.update(RecipeContract.Recipes.TABLE_NAME, values, selection, selectionArgs);
				break;
			case RECIPE_ID:
				// If the incoming URI matches a single recipe ID, does the update based on the incoming data, but
				// modifies the where clause to restrict it to the particular recipe ID.
				uri.getPathSegments().get(RecipeContract.Recipes.RECIPE_ID_PATH_POSITION);
				String finalWhere = BaseColumns._ID + " = "
						+ uri.getPathSegments().get(RecipeContract.Recipes.RECIPE_ID_PATH_POSITION);
				// If there were additional selection criteria, append them to the final WHERE clause
				if (selection != null)
					finalWhere = finalWhere + " AND " + selection;
				count = db.update(RecipeContract.Recipes.TABLE_NAME, values, finalWhere, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
		localBroadcastManager.sendBroadcast(new Intent(Auth.ACTION_SYNC, uri));
		return count;
	}
}
