package com.ianhanniballake.recipebook.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines a contract between the Recipe content provider and its clients. A
 * contract defines the information that a client needs to access the provider
 * as one or more data tables. A contract is a public, non-extendable (final)
 * class that contains constants defining column names and URIs. A well-written
 * client depends only on the constants in the contract.
 */
public final class RecipeContract
{
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
		 * The scheme part for this provider's URI
		 */
		private static final String SCHEME = "content://";
		/**
		 * Path part for the Recipe ID URI
		 */
		private static final String PATH_RECIPE_ID = "/recipes/";
		/**
		 * Path part for the Recipes URI
		 */
		private static final String PATH_RECIPES = "/recipes";
		/**
		 * The content URI base for a single recipe. Callers must append a
		 * numeric note id to this Uri to retrieve a note
		 */
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME
				+ AUTHORITY + PATH_RECIPE_ID);
		/**
		 * The content URI match pattern for a single note, specified by its ID.
		 * Use this to match incoming URIs or to construct an Intent.
		 */
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME
				+ AUTHORITY + PATH_RECIPE_ID + "/#");
		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * recipe.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ianhanniballake.recipe";
		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * recipes.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ianhanniballake.recipes";
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY
				+ PATH_RECIPES);
		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "title DESC";
		/**
		 * 0-relative position of a recipe ID segment in the path part of a
		 * recipe ID URI
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
	 * This class cannot be instantiated
	 */
	private RecipeContract()
	{
	}
}
