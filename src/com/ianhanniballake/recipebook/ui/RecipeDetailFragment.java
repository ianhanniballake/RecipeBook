package com.ianhanniballake.recipebook.ui;

import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ianhanniballake.recipebook.R;

/**
 * Activity which displays only the Recipe details
 */
public abstract class RecipeDetailFragment extends Fragment
{
	/**
	 * Id of the current recipe to show
	 */
	protected long recipeId = 0;

	/**
	 * Gets the pager adapter associated with this fragment
	 * 
	 * @param fm
	 *            FragmentManager used to create fragments
	 * @return A valid pager adapter
	 */
	protected abstract PagerAdapter getPagerAdapter(FragmentManager fm);

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		if (getArguments() != null)
			recipeId = getArguments().getLong(BaseColumns._ID, 0);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState)
	{
		final View v = inflater.inflate(R.layout.fragment_recipe_detail,
				container, false);
		// As we cannot create a fragment pager while initiating a fragment, we
		// create the PagerAdapter after the fact
		v.post(new Runnable()
		{
			@Override
			public void run()
			{
				final ViewPager pager = (ViewPager) v.findViewById(R.id.pager);
				pager.setAdapter(getPagerAdapter(getActivity()
						.getSupportFragmentManager()));
			}
		});
		return v;
	}
}
