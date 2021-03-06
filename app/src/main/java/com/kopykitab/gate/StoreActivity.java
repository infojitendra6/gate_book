package com.kopykitab.gate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kopykitab.gate.components.AutoScrollViewPager;
import com.kopykitab.gate.components.MaterialSearchView;
import com.kopykitab.gate.components.adapters.BannerPagerAdapter;
import com.kopykitab.gate.components.adapters.MenuListAdapter;
import com.kopykitab.gate.components.adapters.SearchAutoCompleteAdapter;
import com.kopykitab.gate.components.adapters.StoreCategoryHelper;
import com.kopykitab.gate.components.adapters.StoreItemClickListener;
import com.kopykitab.gate.components.adapters.StoreRecommendationsAdapter;
import com.kopykitab.gate.models.DatabaseHelper;
import com.kopykitab.gate.models.StoreBannerItem;
import com.kopykitab.gate.models.StoreCategoryItem;
import com.kopykitab.gate.models.StoreCategorySection;
import com.kopykitab.gate.models.StoreRecommendationsItem;
import com.kopykitab.gate.models.StoreSearchItem;
import com.kopykitab.gate.settings.Constants;
import com.kopykitab.gate.settings.DatabaseConstants.RecommendationsEntry;
import com.kopykitab.gate.settings.DatabaseConstants.SearchSuggestionsEntry;
import com.kopykitab.gate.settings.DatabaseConstants.StoreBannersEntry;
import com.kopykitab.gate.settings.DatabaseConstants.StoreCategoryEntry;
import com.kopykitab.gate.settings.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class StoreActivity extends MainFoundationActivity implements StoreItemClickListener {

    private Toolbar toolbar;
    private android.support.v4.widget.DrawerLayout drawerLayout;
    private ImageView drawerNavigation;
    private LinearLayout dashboardMenuContainer;
    private ExpandableListView leftDrawerList;
    private MenuListAdapter navigationDrawerAdapter;

    //search
    private TextView searchTextView;
    private MaterialSearchView searchView;
    private SearchAutoCompleteAdapter autoSearchAdapter;

    //category
    private StoreCategoryHelper mCategoryHelper;
    private RecyclerView mCategoryView;
    private StoreCategoryHelper mCategoryHelperTop;
    private RecyclerView mCategoryViewTop;

    //recommendation
    private LinearLayout recommendationLayout;
    private Button recommendationButton;
    private List<StoreRecommendationsItem> recommendationsItemList = new ArrayList<>();
    private RecyclerView mRecommendationsView;
    private StoreRecommendationsAdapter mRecommendationsAdapter;

    //banner
    private AutoScrollViewPager mBannerScrollViewPager;
    private ViewPager mBannerViewPager;
    private PagerAdapter mBannerPagerAdapter;
    private List<StoreBannerItem> bannerItemList = new ArrayList<>();

    private ViewFlipper storeFlip;
    private ProgressBar mProgressBar;
    private LinearLayout storeMainLayout;

    private String screenName = "Store";
    private String searchScreenName = "Store_Search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.action_bar_background_dark));
        }

        drawerLayout = (android.support.v4.widget.DrawerLayout) findViewById(R.id.store_drawer_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerNavigation = (ImageView) findViewById(R.id.drawer_navigation_button);
        dashboardMenuContainer = (LinearLayout) findViewById(R.id.dashboard_menu_container);
        dashboardMenuContainer.setOnClickListener(null);
        leftDrawerList = (ExpandableListView) findViewById(R.id.menu_list_view);

        searchTextView = (TextView) findViewById(R.id.search_widget_view);
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        storeMainLayout = (LinearLayout) findViewById(R.id.store_main_layout);
        storeFlip = (ViewFlipper) findViewById(R.id.store_flip);
        storeFlip.setDisplayedChild(0);

        mProgressBar = (ProgressBar) findViewById(R.id.store_progressBar);
        mProgressBar.incrementProgressBy(1);

        // If using native accentColor (SDK < 21)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int color = Color.parseColor("#383838");
            mProgressBar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            mProgressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        mCategoryViewTop = (RecyclerView) findViewById(R.id.category_view_top);
        mCategoryHelperTop = new StoreCategoryHelper(this, mCategoryViewTop, this, 2);
        mCategoryView = (RecyclerView) findViewById(R.id.category_view);
        mCategoryHelper = new StoreCategoryHelper(this, mCategoryView, this, 2);

        recommendationLayout = (LinearLayout) findViewById(R.id.recommendation_layout);
        recommendationButton = (Button) findViewById(R.id.recommendation_button);
        mRecommendationsView = (RecyclerView) findViewById(R.id.recommendation_view);
        mRecommendationsAdapter = new StoreRecommendationsAdapter(this, recommendationsItemList, this);

        // horizontal RecyclerView
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecommendationsView.setLayoutManager(mLayoutManager);
        mRecommendationsView.setItemAnimator(new DefaultItemAnimator());
        mRecommendationsView.setAdapter(mRecommendationsAdapter);

        mBannerScrollViewPager = (AutoScrollViewPager) findViewById(R.id.banner_scroll_viewpager);
        mBannerScrollViewPager.setCycle(false);

        mBannerViewPager = mBannerScrollViewPager.getViewPager();
        mBannerPagerAdapter = new BannerPagerAdapter(StoreActivity.this, bannerItemList, this);

        //A little space between pages
        mBannerViewPager.setPageMargin(16);
        //If hardware acceleration is enabled, you should also remove clipping on the pager for its children.
        mBannerViewPager.setClipChildren(true);

        if (Utils.isNetworkConnected(this)) {
            new ShowPromotionalBanner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            if (DatabaseHelper.validateDatabaseAndTableHasData(this, StoreCategoryEntry.TABLE_NAME)) {
                if (DatabaseHelper.isDatabaseVersionConflict(this)) {
                    DatabaseHelper.handleDatabaseVersionConflict(this);
                }
                storeFlip.setDisplayedChild(0);
                new PrepareBannersData().execute();
                new PrepareRecommendationsData().execute();
                new PrepareCategoryData().execute();
            } else {
                new PrepareBannersDataFromAPI().execute();
                new PrepareRecommendationsDataFromAPI().execute();
                new PrepareCategoryDataFromAPI().execute();
                storeFlip.setDisplayedChild(0);
            }
        } else {
            storeFlip.setDisplayedChild(1);
        }

        new PrepareStoreComponents().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        drawerNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        ((LinearLayout) dashboardMenuContainer.findViewById(R.id.menu_list_header_back_button)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });

        searchView.isShowSearchWidget(searchTextView);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String url = StoreSearchItem.getSearchUrl();
                if (!url.equals("")) {
                    Utils.showWebViewActivity(StoreActivity.this, url);
                    saveSuggestionSearch();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                toolbar.setVisibility(View.GONE);
                storeMainLayout.setVisibility(View.GONE);
                Utils.triggerScreen(StoreActivity.this, searchScreenName);
            }

            @Override
            public void onSearchViewClosed() {
                toolbar.setVisibility(View.VISIBLE);
                storeMainLayout.setVisibility(View.VISIBLE);
            }
        });

        autoSearchAdapter = new SearchAutoCompleteAdapter(this, this);
        searchView.setLiveSearchAdapter(autoSearchAdapter, this);

        recommendationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.showRecommendationsActivity(StoreActivity.this);
            }
        });

    }

    public void OnRefreshButtonClick(View v) {
        if (Utils.isNetworkConnected(this)) {
            new PrepareBannersDataFromAPI().execute();
            new PrepareRecommendationsDataFromAPI().execute();
            new PrepareCategoryDataFromAPI().execute();
            storeFlip.setDisplayedChild(0);
        } else {
            storeFlip.setDisplayedChild(1);
        }
    }

    public void OnSettingsButtonClick(View v) {
        Activity activity = (Activity) this;
        activity.startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        Utils.triggerScreen(StoreActivity.this, screenName);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void itemClicked(StoreSearchItem search) {
        String url = search.getOverviewURL();
        if (!url.equals("")) {
            Utils.showWebViewActivity(StoreActivity.this, url);
            Utils.triggerGAEventOnline(StoreActivity.this, "Store_Search_Result", customerId, url);
        }

        String resultCount = search.getObjectCount();
        if (resultCount != null && !resultCount.equals("0")) {
            saveSuggestionSearch();
        }
    }

    @Override
    public void itemClicked(StoreBannerItem banner) {
        String url = banner.getHref();
        if (!url.equals("")) {
            Utils.showWebViewActivity(StoreActivity.this, url);
            Utils.triggerGAEventOnline(StoreActivity.this, "Store_Banner", customerId, "");
        }
    }

    @Override
    public void itemClicked(StoreRecommendationsItem recommendation) {
        String url = recommendation.getHref();
        if (!url.equals("")) {
            Utils.showWebViewActivity(StoreActivity.this, url);
            Utils.triggerGAEventOnline(StoreActivity.this, "Store_Recommended_Product", recommendation.getProductId(), customerId);
        }
    }

    @Override
    public void itemClicked(StoreCategorySection section) {
        String url = section.getUrl();
        if (!url.equals("")) {
            Utils.showWebViewActivity(StoreActivity.this, url);
            Utils.triggerGAEventOnline(StoreActivity.this, "Store_Menu", customerId, section.getName());
        }
    }

    @Override
    public void itemClicked(StoreCategoryItem item) {
        String url = item.getUrl();
        if (!url.equals("")) {
            Utils.showWebViewActivity(StoreActivity.this, url);
            Utils.triggerGAEventOnline(StoreActivity.this, "Store_Menu", customerId, item.getName());
        }
    }

    private void saveSuggestionSearch() {
        final String typedKeyword = searchView.getTypedKeyword();
        if (typedKeyword != null && typedKeyword.length() >= 3) {
            final String results = StoreSearchItem.getSearchResults();
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    long newRowId = -1;
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());

                    // Create a new column
                    ContentValues insertValues = new ContentValues();
                    /*insertValues.put(SearchSuggestionsEntry.COLUMN_ID, "");*/
                    insertValues.put(SearchSuggestionsEntry.COLUMN_KEYWORD, typedKeyword);
                    insertValues.put(SearchSuggestionsEntry.COLUMN_RESULTS, results);
                    insertValues.put(SearchSuggestionsEntry.COLUMN_DATE, date);

                    // update the column
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(SearchSuggestionsEntry.COLUMN_RESULTS, results);
                    updateValues.put(SearchSuggestionsEntry.COLUMN_DATE, date);

                    // Which row to update, based on the keyword
                    String selection = SearchSuggestionsEntry.COLUMN_KEYWORD + " = ?";
                    String[] selectionArgs = {typedKeyword};

                    newRowId = DatabaseHelper.getInstance(StoreActivity.this).insertOrUpdateData(SearchSuggestionsEntry.TABLE_NAME, insertValues, updateValues, selection, selectionArgs);

                    return null;
                }
            }.execute();
        }
    }

    private class PrepareStoreComponents extends AsyncTask<Void, Void, Void> {
        private LinkedList<HashMap<String, String>> menuItemList = null;

        @Override
        protected Void doInBackground(Void... voids) {
            menuItemList = Utils.getMenuItems();
            if (menuItemList == null) {
                if (sharedPrefs == null) {
                    sharedPrefs = getSharedPreferences("kk_shared_prefs", Context.MODE_PRIVATE);
                }
                String custId = sharedPrefs.getString("customerId", null);
                if (custId != null && custId.equals(customerId)) {
                    String imageBaseURL = sharedPrefs.getString("image_base_url", null);
                    menuItemList = new Gson().fromJson(new String(sharedPrefs.getString("menu_items", null)), new TypeToken<LinkedList<HashMap<String, String>>>() {
                    }.getType());

                    // Set base image URL & menu items
                    Constants.setImageBaseUrl(imageBaseURL);
                    Utils.setMenuItems(menuItemList);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                navigationDrawerAdapter = new MenuListAdapter(StoreActivity.this, "Preparing Store, Please wait...", drawerLayout);
                List<HashMap<String, String>> menuItems = new ArrayList<HashMap<String, String>>();
                for (HashMap<String, String> item : menuItemList) {
                    menuItems.add(navigationDrawerAdapter.createMenuItem(item.get("text"), item.get("left_drawer_icon"), item.get("product_type")));
                }
                navigationDrawerAdapter.setMenuGroupsItems("My Library", menuItems);
                leftDrawerList.setAdapter(navigationDrawerAdapter);
                leftDrawerList.setOnChildClickListener(navigationDrawerAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class PrepareBannersData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                List<HashMap<String, String>> bannerList = DatabaseHelper.getInstance(StoreActivity.this).getDetails(StoreBannersEntry.TABLE_NAME, null, null, -1);
                StoreBannerItem storeBannerItem;
                if (bannerList.size() >= 2) {
                    //add last banner as first banner for dummy banner
                    HashMap<String, String> lastBanner = bannerList.get(bannerList.size() - 1);
                    storeBannerItem = new StoreBannerItem(lastBanner.get("image_url"), lastBanner.get("description"), lastBanner.get("href"));
                    bannerItemList.add(storeBannerItem);
                    for (HashMap<String, String> banner : bannerList) {
                        storeBannerItem = new StoreBannerItem(banner.get("image_url"), banner.get("description"), banner.get("href"));
                        bannerItemList.add(storeBannerItem);
                    }
                    //add first banner as last banner for dummy banner
                    HashMap<String, String> firstBanner = bannerList.get(0);
                    storeBannerItem = new StoreBannerItem(firstBanner.get("image_url"), firstBanner.get("description"), firstBanner.get("href"));
                    bannerItemList.add(storeBannerItem);
                } else if (bannerList.size() == 1) {
                    HashMap<String, String> firstBanner = bannerList.get(0);
                    storeBannerItem = new StoreBannerItem(firstBanner.get("image_url"), firstBanner.get("description"), firstBanner.get("href"));
                    bannerItemList.add(storeBannerItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            mBannerViewPager.setAdapter(mBannerPagerAdapter);
            mBannerViewPager.setOffscreenPageLimit(mBannerPagerAdapter.getCount());

            if (bannerItemList.size() <= 0) {
                mBannerScrollViewPager.setVisibility(View.GONE);
            } else if (bannerItemList.size() >= 2) {
                mBannerScrollViewPager.startAutoScroll();
                mBannerScrollViewPager.setInterval(3000);
                mBannerScrollViewPager.setAutoScrollDurationFactor(3.0);
                mBannerScrollViewPager.setSwipeScrollDurationFactor(3.0);
                mBannerScrollViewPager.setCycle(true);
                mBannerScrollViewPager.setStopScrollWhenTouch(true);
                mBannerViewPager.setCurrentItem(1);
            }
            mBannerPagerAdapter.notifyDataSetChanged();
        }
    }

    private class PrepareRecommendationsData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                List<HashMap<String, String>> recommendationList = DatabaseHelper.getInstance(StoreActivity.this).getDetails(RecommendationsEntry.TABLE_NAME, null, null, 20);
                StoreRecommendationsItem storeRecommendationsItem;
                for (HashMap<String, String> recommendation : recommendationList) {
                    storeRecommendationsItem = new StoreRecommendationsItem(recommendation.get("product_id"), recommendation.get("image"), recommendation.get("name"), recommendation.get("price_1"), recommendation.get("price_2"), recommendation.get("href"));
                    recommendationsItemList.add(storeRecommendationsItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (recommendationsItemList.size() <= 0) {
                recommendationLayout.setVisibility(View.GONE);
            }

            mProgressBar.setVisibility(View.GONE);
            mRecommendationsAdapter.notifyDataSetChanged();
        }
    }

    private class PrepareCategoryData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String whereClause = " WHERE " + StoreCategoryEntry.COLUMN_LEVEL + " = 0";
                List<HashMap<String, String>> categories = DatabaseHelper.getInstance(StoreActivity.this).getDetails(StoreCategoryEntry.TABLE_NAME, whereClause, null, -1);
                ArrayList<StoreCategoryItem> arrayList;
                LinkedList<String> subCategories;
                for (int i = 0; i < categories.size(); i++) {
                    HashMap<String, String> category = categories.get(i);
                    String categoryName = category.get("name");
                    whereClause = " WHERE " + StoreCategoryEntry.COLUMN_PARENT_NAME + " = '" + categoryName + "'";
                    List<HashMap<String, String>> categoryDetails = DatabaseHelper.getInstance(StoreActivity.this).getDetails(StoreCategoryEntry.TABLE_NAME, whereClause, null, -1);

                    arrayList = new ArrayList<>();
                    subCategories = new LinkedList<String>();
                    int k = 0;
                    for (HashMap<String, String> details : categoryDetails) {
                        String subCategoryName = details.get("name");
                        arrayList.add(new StoreCategoryItem(subCategoryName, k, details.get("url")));
                        subCategories.add(subCategoryName);
                        k++;
                    }

                    //for adding empty grid
                    if (k % 2 != 0) {
                        arrayList.add(new StoreCategoryItem("", k, ""));
                    }

                    if (i == 0) {
                        String subCategoryText = getResources().getString(R.string.store_subcategory_top_text);
                        mCategoryHelperTop.addSection(categoryName, category.get("url"), arrayList, subCategories, true, subCategoryText);
                    } else {
                        mCategoryHelper.addSection(categoryName, category.get("url"), arrayList, subCategories, false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            mCategoryHelperTop.notifyDataSetChanged();
            mCategoryHelper.notifyDataSetChanged();

            if (Utils.isNetworkConnected(StoreActivity.this)) {
                KopykitabApplication application = (KopykitabApplication) getApplication();
                application.prepareCachedAPI(StoreActivity.this);
            }
        }
    }

    private class PrepareBannersDataFromAPI extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String response = Utils.sendPost(Constants.BANNERS_URL, "customer_id=" + URLEncoder.encode(customerId, "UTF-8") + "&source=" + URLEncoder.encode(Constants.LOGIN_SOURCE, "UTF-8"));
                if (response != null) {
                    JSONArray bannerArray = new JSONArray(response);
                    StoreBannerItem storeBannerItem;
                    if (bannerArray.length() >= 2) {
                        //add last banner as first banner for dummy banner
                        JSONObject lastBanner = bannerArray.getJSONObject(bannerArray.length() - 1);
                        storeBannerItem = new StoreBannerItem(lastBanner.getString("image_url"), lastBanner.getString("description"), lastBanner.getString("href"));
                        bannerItemList.add(storeBannerItem);
                        for (int i = 0; i < bannerArray.length(); i++) {
                            JSONObject banner = bannerArray.getJSONObject(i);
                            storeBannerItem = new StoreBannerItem(banner.getString("image_url"), banner.getString("description"), banner.getString("href"));
                            bannerItemList.add(storeBannerItem);
                        }
                        //add first banner as last banner for dummy banner
                        JSONObject firstBanner = bannerArray.getJSONObject(0);
                        storeBannerItem = new StoreBannerItem(firstBanner.getString("image_url"), firstBanner.getString("description"), firstBanner.getString("href"));
                        bannerItemList.add(storeBannerItem);
                    } else if (bannerArray.length() == 1) {
                        JSONObject firstBanner = bannerArray.getJSONObject(0);
                        storeBannerItem = new StoreBannerItem(firstBanner.getString("image_url"), firstBanner.getString("description"), firstBanner.getString("href"));
                        bannerItemList.add(storeBannerItem);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            mBannerViewPager.setAdapter(mBannerPagerAdapter);
            mBannerViewPager.setOffscreenPageLimit(mBannerPagerAdapter.getCount());

            if (bannerItemList.size() <= 0) {
                mBannerScrollViewPager.setVisibility(View.GONE);
            } else if (bannerItemList.size() >= 2) {
                mBannerScrollViewPager.startAutoScroll();
                mBannerScrollViewPager.setInterval(3000);
                mBannerScrollViewPager.setAutoScrollDurationFactor(3.0);
                mBannerScrollViewPager.setSwipeScrollDurationFactor(3.0);
                mBannerScrollViewPager.setCycle(true);
                mBannerScrollViewPager.setStopScrollWhenTouch(true);
                mBannerViewPager.setCurrentItem(1);
            }
            mBannerPagerAdapter.notifyDataSetChanged();

            if (Utils.isNetworkConnected(StoreActivity.this)) {
                KopykitabApplication application = (KopykitabApplication) getApplication();
                application.prepareCachedAPI(StoreActivity.this);
            }
        }
    }

    private class PrepareRecommendationsDataFromAPI extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String response = Utils.sendPost(Constants.RECOMMENDATIONS_URL, "customer_id=" + URLEncoder.encode(customerId, "UTF-8") + "&source=" + URLEncoder.encode(Constants.LOGIN_SOURCE, "UTF-8"));
                if (response != null) {
                    JSONObject recommendationObject = new JSONObject(response);
                    JSONArray recommendationBooksArray = recommendationObject.getJSONArray("products");
                    if (recommendationBooksArray.length() > 0) {
                        StoreRecommendationsItem storeRecommendationsItem;
                        for (int i = 0; i < 20 && i < recommendationBooksArray.length(); i++) {
                            JSONObject book = recommendationBooksArray.getJSONObject(i);
                            storeRecommendationsItem = new StoreRecommendationsItem(book.getString("product_id").trim(), book.getString("image"), book.getString("name").trim(), book.getString("price_1"), book.getString("price_2"), book.getString("href"));
                            recommendationsItemList.add(storeRecommendationsItem);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (recommendationsItemList.size() <= 0) {
                recommendationLayout.setVisibility(View.GONE);
            }

            mProgressBar.setVisibility(View.GONE);
            mRecommendationsAdapter.notifyDataSetChanged();
        }
    }

    private class PrepareCategoryDataFromAPI extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String response = Utils.sendGet(Constants.CATEGORIES_URL, "customer_id=" + URLEncoder.encode(customerId, "UTF-8") + "&login_source=" + URLEncoder.encode(Constants.LOGIN_SOURCE, "UTF-8"));
                if (response != null) {
                    LinkedList<ContentValues> valuesList = new LinkedList<ContentValues>();
                    JSONArray topCategoriesJsonArray = new JSONArray(response);
                    ArrayList<StoreCategoryItem> arrayList;
                    LinkedList<String> subCategories;
                    for (int i = 0; i < topCategoriesJsonArray.length(); i++) {
                        JSONObject parentObject = topCategoriesJsonArray.getJSONObject(i);
                        String parentName = parentObject.getString("name"), parentUrl = "";

                        if (parentObject.has("url")) {
                            parentUrl = parentObject.getString("url");
                        }

                        arrayList = new ArrayList<>();
                        subCategories = new LinkedList<String>();
                        int k = 0;

                        JSONArray subCategoriesJsonArray = topCategoriesJsonArray.getJSONObject(i).getJSONArray("children");
                        for (int j = 0; j < subCategoriesJsonArray.length(); j++) {
                            JSONObject childObject = subCategoriesJsonArray.getJSONObject(j);
                            String childName = childObject.getString("name"), childUrl = childObject.getString("url");

                            String subCategoryName = childName;
                            arrayList.add(new StoreCategoryItem(subCategoryName, k, childUrl));
                            subCategories.add(subCategoryName);
                            k++;
                        }

                        //for adding empty grid
                        if (k % 2 != 0) {
                            arrayList.add(new StoreCategoryItem("", k, ""));
                        }

                        if (i == 0) {
                            String subCategoryText = getResources().getString(R.string.store_subcategory_top_text);
                            mCategoryHelperTop.addSection(parentName, parentUrl, arrayList, subCategories, true, subCategoryText);
                        } else {
                            mCategoryHelper.addSection(parentName, parentUrl, arrayList, subCategories, false);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            mCategoryHelperTop.notifyDataSetChanged();
            mCategoryHelper.notifyDataSetChanged();
        }
    }

    private class ShowPromotionalBanner extends AsyncTask<Void, Void, Void> {
        private StoreBannerItem promotionalBannerItem;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String response = Utils.sendPost(Constants.PROMOTIONAL_BANNERS_URL, "customer_id=" + URLEncoder.encode(customerId, "UTF-8") + "&login_source=" + URLEncoder.encode(Constants.LOGIN_SOURCE, "UTF-8"));
                if (response != null) {
                    JSONArray bannerArray = new JSONArray(response);
                    JSONObject banner = bannerArray.getJSONObject(0);
                    promotionalBannerItem = new StoreBannerItem(banner.getString("image_url"), banner.getString("description"), banner.getString("href"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                if (promotionalBannerItem != null) {
                    final AlertDialog alertDialog = Utils.createAlertBox(StoreActivity.this, R.layout.store_promotional_banner);
                    alertDialog.show();

                    ImageView promotionalImage = (ImageView) alertDialog.findViewById(R.id.promotional_banner_image);
                    promotionalImage.setImageDrawable(null);
                    Utils.getImageLoader(StoreActivity.this).displayImage(promotionalBannerItem.getImage().replaceAll(" ", "%20"), promotionalImage);
                    ImageButton closeButton = (ImageButton) alertDialog.findViewById(R.id.promotional_banner_close);
                    closeButton.bringToFront();

                    promotionalImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String url = promotionalBannerItem.getHref();
                            if (url != null && !url.isEmpty()) {
                                if (Utils.isNetworkConnected(StoreActivity.this)) {
                                    Utils.showWebViewActivity(StoreActivity.this, url);
                                    Utils.triggerGAEventOnline(StoreActivity.this, "Store_Promotional_Banner", customerId, "");
                                }
                            }
                        }
                    });
                    closeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class PrepareStore extends SyncData {

        public PrepareStore(Context context, boolean syncFromAPI) {
            super(context, syncFromAPI);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
        }
    }

    @Override
    protected void syncDataFromAPI() {
        if (Utils.isNetworkConnected(this)) {
            Utils.triggerGAEvent(this, "REFRESH", customerId, "");
            new PrepareStore(StoreActivity.this, true).execute();
        } else {
            Utils.networkNotAvailableAlertBox(this);
        }
    }
}
