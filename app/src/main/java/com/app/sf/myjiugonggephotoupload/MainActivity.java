package com.app.sf.myjiugonggephotoupload;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

//https://github.com/lovetuzitong/MultiImageSelector   图片选择器好东西
public class MainActivity extends Activity {

    private SearchHistoryTable mHistoryDatabase;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        findViewById(R.id.bt_shanc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHistoryDatabase.clearDatabase();
            }
        });
    }

    private void init() {
        mHistoryDatabase =  SearchHistoryTable.getSearchHistoryTable(this);

        // to API 25
        mSearchView = (SearchView) findViewById(R.id.searchView);
        if (mSearchView != null) {
            mSearchView.setVersionMargins(SearchView.VersionMargins.TOOLBAR_SMALL);
            mSearchView.setHint("搜索");
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    mHistoryDatabase.addItem(new SearchItem(query));
                    mSearchView.close(false);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
            mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
                @Override
                public boolean onOpen() {
//                    if (mFab != null) {
//                        mFab.hide();
//                    }
                    return true;
                }

                @Override
                public boolean onClose() {
//                    if (mFab != null && !mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
//                        mFab.show();
//                    }
                    return true;
                }
            });
//            mSearchView.setVoiceText("Set permission on Android 6.0+ !");
            mSearchView.setVoice(false);
//            mSearchView.setOnVoiceIconClickListener(new SearchView.OnVoiceIconClickListener() {
//                @Override
//                public void onVoiceIconClick() {
//
//                }
//            });

            SearchAdapter searchAdapter = new SearchAdapter(this);
            searchAdapter.setOnSearchItemClickListener(new SearchAdapter.OnSearchItemClickListener() {
                @Override
                public void onSearchItemClick(View view, int position) {
                    TextView textView = (TextView) view.findViewById(R.id.textView);
                    String query = textView.getText().toString();
                    Log.i("字符串",query);
                    //mHistoryDatabase.addItem(new SearchItem(""));
                    mSearchView.setTextOnly(query);
                    mSearchView.close(false);
//                    mHistoryDatabase.clearDatabase();
                }
            });

            mSearchView.setAdapter(searchAdapter);
            searchAdapter.notifyDataSetChanged();

//            List<SearchFilter> filter = new ArrayList<>();
//            filter.add(new SearchFilter("Filter1", true));
//            filter.add(new SearchFilter("Filter2", true));
//            mSearchView.setFilters(filter);
            //use mSearchView.getFiltersStates() to consider filter when performing search
        }
    }
}
