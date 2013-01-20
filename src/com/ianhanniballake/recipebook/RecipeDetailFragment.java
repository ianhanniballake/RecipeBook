package com.ianhanniballake.recipebook;

import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ianhanniballake.recipebook.dummy.DummyContent;

/**
 * A fragment representing a single Recipe detail screen. This fragment is either contained in a
 * {@link RecipeListActivity} in two-pane mode (on tablets) or a {@link RecipeDetailActivity} on handsets.
 */
public class RecipeDetailFragment extends Fragment
{
	/**
	 * The dummy content this fragment is presenting.
	 */
	private DummyContent.DummyItem mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation
	 * changes).
	 */
	public RecipeDetailFragment()
	{
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (getArguments().containsKey(BaseColumns._ID))
			// Load the dummy content specified by the fragment arguments. In a real-world scenario, use a Loader to
			// load content from a content provider.
			mItem = DummyContent.ITEM_MAP.get(getArguments().getLong(BaseColumns._ID));
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		final View rootView = inflater.inflate(R.layout.fragment_recipe_detail, container, false);
		// Show the dummy content as text in a TextView.
		if (mItem != null)
			((TextView) rootView.findViewById(R.id.recipe_detail)).setText(mItem.content);
		return rootView;
	}
}
