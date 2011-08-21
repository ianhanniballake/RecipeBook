package com.ianhanniballake.recipebook.provider;

import java.util.HashMap;

import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.ianhanniballake.recipebook.R;

/**
 * Provides access to a database of recipes. Each recipe has a title and a
 * description.
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
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		 * Creates the underlying database with table name and column names
		 * taken from the RecipeContract class.
		 */
		@Override
		public void onCreate(final SQLiteDatabase db)
		{
			Log.d(TAG, "Creating the " + RecipeContract.Recipes.TABLE_NAME
					+ " table");
			db.execSQL("CREATE TABLE " + RecipeContract.Recipes.TABLE_NAME
					+ " (" + BaseColumns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ RecipeContract.Recipes.COLUMN_NAME_TITLE + " TEXT,"
					+ RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION + " TEXT"
					+ ");");
		}

		/**
		 * 
		 * Demonstrates that the provider must consider what happens when the
		 * underlying database is changed. Note that this currently just
		 * destroys and recreates the database - should upgrade in place
		 */
		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int newVersion)
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS "
					+ RecipeContract.Recipes.TABLE_NAME);
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
	private static final int DATABASE_VERSION = 1;
	/**
	 * The incoming URI matches the Recipe ID URI pattern
	 */
	private static final int RECIPE_ID = 2;
	/**
	 * This describes the MIME types that are supported for opening a note URI
	 * as a stream.
	 */
	private static ClipDescription RECIPE_STREAM_TYPES = new ClipDescription(
			null, new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN });
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
	private static final UriMatcher uriMatcher = buildUriMatcher();

	/**
	 * Creates and initializes the URI matcher
	 * 
	 * @return the URI Matcher
	 */
	private static UriMatcher buildUriMatcher()
	{
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		// Add a pattern that routes URIs terminated with "recipes" to a RECIPES
		// operation
		matcher.addURI(RecipeContract.AUTHORITY, "recipes", RECIPES);
		// Add a pattern that routes URIs terminated with "recipes" plus an
		// integer to a recipe ID operation
		matcher.addURI(RecipeContract.AUTHORITY, "recipes/#", RECIPE_ID);
		return matcher;
	}

	/**
	 * Handle to a new DatabaseHelper.
	 */
	private DatabaseHelper databaseHelper;

	/**
	 * Deletes record(s) from the database
	 * 
	 * @see android.content.ContentProvider#delete(android.net.Uri,
	 *      java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(final Uri uri, final String where,
			final String[] whereArgs)
	{
		// Opens the database object in "write" mode.
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		String finalWhere;
		int count;
		// Does the delete based on the incoming URI pattern.
		switch (uriMatcher.match(uri))
		{
			case RECIPES:
				// If the incoming pattern matches the general pattern for
				// recipes, does a delete based on the incoming "where" columns
				// and arguments.
				count = db.delete(RecipeContract.Recipes.TABLE_NAME, where,
						whereArgs);
				break;
			case RECIPE_ID:
				// If the incoming URI matches a single recipe ID, does the
				// delete based on the incoming data, but modifies the where
				// clause to restrict it to the particular recipe ID.
				finalWhere = BaseColumns._ID
						+ " = "
						+ uri.getPathSegments().get(
								RecipeContract.Recipes.RECIPE_ID_PATH_POSITION);
				// If there were additional selection criteria, append them to
				// the final WHERE clause
				if (where != null)
					finalWhere = finalWhere + " AND " + where;
				count = db.delete(RecipeContract.Recipes.TABLE_NAME,
						finalWhere, whereArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	/**
	 * Returns the types of available data streams. URIs to specific recipes are
	 * supported. The application can convert such a recipe to a plain text
	 * stream.
	 * 
	 * @see android.content.ContentProvider#getStreamTypes(android.net.Uri,
	 *      java.lang.String)
	 */
	@Override
	public String[] getStreamTypes(final Uri uri, final String mimeTypeFilter)
	{
		/**
		 * Chooses the data stream type based on the incoming URI pattern.
		 */
		switch (uriMatcher.match(uri))
		{
			case RECIPES:
				// If the pattern is for recipes, return null. Data streams are
				// not supported for this type of URI.
				return null;
			case RECIPE_ID:
				// If the pattern is for recipe IDs and the MIME filter is
				// text/plain, then return text/plain
				return RECIPE_STREAM_TYPES.filterMimeTypes(mimeTypeFilter);
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	/**
	 * Returns the MIME data type of the URI given as a parameter.
	 * 
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(final Uri uri)
	{
		/**
		 * Chooses the MIME type based on the incoming URI pattern
		 */
		switch (uriMatcher.match(uri))
		{
			case RECIPES:
				// If the pattern is for recipes, returns the general content
				// type.
				return RecipeContract.Recipes.CONTENT_TYPE;
			case RECIPE_ID:
				// If the pattern is for note IDs, returns the recipe ID content
				// type.
				return RecipeContract.Recipes.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	/**
	 * Inserts a new row into the database.
	 * 
	 * @see android.content.ContentProvider#insert(android.net.Uri,
	 *      android.content.ContentValues)
	 */
	@Override
	public Uri insert(final Uri uri, final ContentValues initialValues)
	{
		// Validates the incoming URI. Only the full provider URI is allowed for
		// inserts.
		if (uriMatcher.match(uri) != RECIPES)
			throw new IllegalArgumentException("Unknown URI " + uri);
		ContentValues values;
		if (initialValues != null)
			values = new ContentValues(initialValues);
		else
			values = new ContentValues();
		if (!values.containsKey(RecipeContract.Recipes.COLUMN_NAME_TITLE))
			values.put(RecipeContract.Recipes.COLUMN_NAME_TITLE, getContext()
					.getResources().getString(R.string.default_recipe_title));
		if (!values.containsKey(RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION))
			values.put(
					RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION,
					getContext().getResources().getString(
							R.string.default_recipe_description));
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		final long rowId = db.insert(RecipeContract.Recipes.TABLE_NAME,
				RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION, values);
		// If the insert succeeded, the row ID exists.
		if (rowId > 0)
		{
			// Creates a URI with the recipe ID pattern and the new row ID
			// appended to it.
			final Uri noteUri = ContentUris.withAppendedId(
					RecipeContract.Recipes.CONTENT_ID_URI_BASE, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
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

	/**
	 * Queries the database and returns a cursor containing the results.
	 * 
	 * @see android.content.ContentProvider#query(android.net.Uri,
	 *      java.lang.String[], java.lang.String, java.lang.String[],
	 *      java.lang.String)
	 */
	@Override
	public Cursor query(final Uri uri, final String[] projection,
			final String selection, final String[] selectionArgs,
			final String sortOrder)
	{
		// Constructs a new query builder and sets its table name
		final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(RecipeContract.Recipes.TABLE_NAME);
		final HashMap<String, String> allColumnProjectionMap = new HashMap<String, String>();
		allColumnProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
		allColumnProjectionMap.put(RecipeContract.Recipes.COLUMN_NAME_TITLE,
				RecipeContract.Recipes.COLUMN_NAME_TITLE);
		allColumnProjectionMap.put(
				RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION,
				RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION);
		qb.setProjectionMap(allColumnProjectionMap);
		switch (uriMatcher.match(uri))
		{
			case RECIPE_ID:
				// If the incoming URI is for a single recipe identified by its
				// ID, appends "_ID = <recipeID>" to the where clause, so that
				// it selects that single recipe
				qb.appendWhere(BaseColumns._ID
						+ "="
						+ uri.getPathSegments().get(
								RecipeContract.Recipes.RECIPE_ID_PATH_POSITION));
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
		final Cursor c = qb.query(db, projection, selection, selectionArgs,
				null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	/**
	 * Updates recipes, notifying listeners as appropriate
	 * 
	 * @see android.content.ContentProvider#update(android.net.Uri,
	 *      android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(final Uri uri, final ContentValues values,
			final String where, final String[] whereArgs)
	{
		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri))
		{
			case RECIPES:
				// If the incoming URI matches the general notes pattern, does
				// the update based on the incoming data.
				count = db.update(RecipeContract.Recipes.TABLE_NAME, values,
						where, whereArgs);
				break;
			case RECIPE_ID:
				// If the incoming URI matches a single recipe ID, does the
				// update based on the incoming data, but modifies the where
				// clause to restrict it to the particular recipe ID.
				uri.getPathSegments().get(
						RecipeContract.Recipes.RECIPE_ID_PATH_POSITION);
				String finalWhere = BaseColumns._ID
						+ " = "
						+ uri.getPathSegments().get(
								RecipeContract.Recipes.RECIPE_ID_PATH_POSITION);
				// If there were additional selection criteria, append them to
				// the final WHERE clause
				if (where != null)
					finalWhere = finalWhere + " AND " + where;
				count = db.update(RecipeContract.Recipes.TABLE_NAME, values,
						finalWhere, whereArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
