package com.ianhanniballake.recipebook.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines a contract between the Recipe content provider and its clients. A contract defines the information that a
 * client needs to access the provider as one or more data tables. A contract is a public, non-extendable (final) class
 * that contains constants defining column names and URIs. A well-written client depends only on the constants in the
 * contract.
 */
public final class RecipeContract
{
	/**
	 * Ingredients table contract
	 */
	public static final class Ingredients implements BaseColumns
	{
		/**
		 * Column name of the ingredient item
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String COLUMN_NAME_ITEM = "item";
		/**
		 * Column name of the ingredient item's preparation
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String COLUMN_NAME_PREPARATION = "preparation";
		/**
		 * Column name of the ingredient quantity
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String COLUMN_NAME_QUANTITY = "quantity";
		/**
		 * Column name of the ingredient quantity's fractional denominator
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String COLUMN_NAME_QUANTITY_DENOMINATOR = "quantity_denominator";
		/**
		 * Column name of the ingredient quantity's fractional numerator
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String COLUMN_NAME_QUANTITY_NUMERATOR = "quantity_numerator";
		/**
		 * Column name of the recipe of this ingredient
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String COLUMN_NAME_RECIPE_ID = "recipe_id";
		/**
		 * Column name of the ingredient unit
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String COLUMN_NAME_UNIT = "unit";
		/**
		 * Path part for the Recipe ID URI
		 */
		private static final String PATH_INGREDIENT_ID = "/ingredients/";
		/**
		 * Path part for the Recipes URI
		 */
		private static final String PATH_INGREDIENTS = "/ingredients";
		/**
		 * The content URI base for a single ingredient. Callers must append a numeric ingredient id to this Uri to
		 * retrieve a ingredient
		 */
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_INGREDIENT_ID);
		/**
		 * The content URI match pattern for a single recipe, specified by its ID. Use this to match incoming URIs or to
		 * construct an Intent.
		 */
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_INGREDIENT_ID + "/#");
		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single ingredient.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ianhanniballake.ingredient";
		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of ingredients.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ianhanniballake.ingredients";
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_INGREDIENTS);
		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "";
		/**
		 * 0-relative position of a recipe ID segment in the path part of a recipe ID URI
		 */
		public static final int INGREDIENT_ID_PATH_POSITION = 1;
		/**
		 * The table name offered by this provider
		 */
		public static final String TABLE_NAME = "ingredients";

		/**
		 * This class cannot be instantiated
		 */
		private Ingredients()
		{
		}
	}
	/**
	 * Instructions table contract
	 */
	public static final class Instructions implements BaseColumns
	{
		/**
		 * Column name of the instruction
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String COLUMN_NAME_INSTRUCTION = "instruction";
		/**
		 * Column name of the recipe of this instruction
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String COLUMN_NAME_RECIPE_ID = "recipe_id";
		/**
		 * Path part for the Recipe ID URI
		 */
		private static final String PATH_INSTRUCTION_ID = "/instructions/";
		/**
		 * Path part for the Recipes URI
		 */
		private static final String PATH_INSTRUCTIONS = "/instructions";
		/**
		 * The content URI base for a single instruction. Callers must append a numeric instruction id to this Uri to
		 * retrieve a instruction
		 */
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_INSTRUCTION_ID);
		/**
		 * The content URI match pattern for a single instruction, specified by its ID. Use this to match incoming URIs or to
		 * construct an Intent.
		 */
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_INSTRUCTION_ID + "/#");
		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single instruction.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ianhanniballake.instruction";
		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of instructions.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ianhanniballake.instructions";
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_INSTRUCTIONS);
		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "";
		/**
		 * 0-relative position of a recipe ID segment in the path part of a recipe ID URI
		 */
		public static final int INSTRUCTION_ID_PATH_POSITION = 1;
		/**
		 * The table name offered by this provider
		 */
		public static final String TABLE_NAME = "instructions";

		/**
		 * This class cannot be instantiated
		 */
		private Instructions()
		{
		}
	}

	/**
	 * Recipes table contract
	 */
	public static final class Recipes implements BaseColumns
	{
		/**
		 * Column name of the recipe description
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String COLUMN_NAME_DESCRIPTION = "description";
		/**
		 * Column name for the title of the recipe
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String COLUMN_NAME_TITLE = "title";
		/**
		 * Path part for the Recipe ID URI
		 */
		private static final String PATH_RECIPE_ID = "/recipes/";
		/**
		 * Path part for the Recipes URI
		 */
		private static final String PATH_RECIPES = "/recipes";
		/**
		 * The content URI base for a single recipe. Callers must append a numeric recipe id to this Uri to retrieve a
		 * recipe
		 */
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_RECIPE_ID);
		/**
		 * The content URI match pattern for a single recipe, specified by its ID. Use this to match incoming URIs or to
		 * construct an Intent.
		 */
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_RECIPE_ID + "/#");
		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single recipe.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ianhanniballake.recipe";
		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of recipes.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ianhanniballake.recipes";
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_RECIPES);
		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "title DESC";
		/**
		 * 0-relative position of a recipe ID segment in the path part of a recipe ID URI
		 */
		public static final int RECIPE_ID_PATH_POSITION = 1;
		/**
		 * The table name offered by this provider
		 */
		public static final String TABLE_NAME = "recipes";

		/**
		 * This class cannot be instantiated
		 */
		private Recipes()
		{
		}
	}

	/**
	 * Base authority for this content provider
	 */
	public static final String AUTHORITY = "com.ianhanniballake.recipebook";
	/**
	 * The scheme part for this provider's URI
	 */
	private static final String SCHEME = "content://";

	/**
	 * This class cannot be instantiated
	 */
	private RecipeContract()
	{
	}
}
