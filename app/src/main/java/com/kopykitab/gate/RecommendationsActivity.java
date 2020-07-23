package com.kopykitab.gate;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.kopykitab.gate.components.Button;
import com.kopykitab.gate.components.LibrarySwipeLayout;
import com.kopykitab.gate.settings.AppSettings;
import com.kopykitab.gate.settings.Constants;
import com.kopykitab.gate.settings.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable divider;

    public DividerItemDecoration(Drawable divider) {
        this.divider = divider;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent) {
        // TODO Auto-generated method stub
        super.onDraw(c, parent);
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }
}

class RecommendationsAdapter extends RecyclerView.Adapter<RecommendationsAdapter.ViewHolder> {
    private Context mContext;
    private List<HashMap<String, String>> booksList;
    private String customerId;

    public RecommendationsAdapter(Context c, List<HashMap<String, String>> books) {
        mContext = c;
        booksList = books;
        customerId = AppSettings.getInstance(mContext).get("CUSTOMER_ID");
    }

    public void setItem(HashMap<String, String> book) {
        booksList.add(book);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        return booksList.size();
    }

    @Override
    public RecommendationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // TODO Auto-generated method stub
        View v = LayoutInflater.from(mContext).inflate(R.layout.books_list, parent, false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // TODO Auto-generated method stub
        final HashMap<String, String> bookListItem = booksList.get(position);

        View bookView = holder.v;
        ImageView book_image = (ImageView) bookView.findViewById(R.id.book_list_image);
        book_image.setImageDrawable(null);
        Utils.getImageLoader(mContext).displayImage(bookListItem.get("book_image_url").replaceAll(" ", "%20"), book_image);
        ImageView book_depiction = (ImageView) bookView.findViewById(R.id.book_list_depiction);
        TextView book_name = (TextView) bookView.findViewById(R.id.book_list_name);
        TextView book_author = (TextView) bookView.findViewById(R.id.book_list_author);
        RelativeLayout book_price_details = (RelativeLayout) bookView.findViewById(R.id.book_price_details);
        TextView book_rental_period = (TextView) bookView.findViewById(R.id.book_rental_period);
        TextView book_price_1 = (TextView) bookView.findViewById(R.id.book_price_1);
        ((com.kopykitab.gate.components.TextView) book_price_1).setAddStrike(true);
        TextView book_price_2 = (TextView) bookView.findViewById(R.id.book_price_2);
        book_depiction.setVisibility(View.INVISIBLE);
        book_price_details.setVisibility(View.VISIBLE);
        book_name.setText(Html.fromHtml(bookListItem.get("book_name")));
        book_author.setText(Html.fromHtml(bookListItem.get("book_author")));

        String bookRentalPeriod = bookListItem.get("rental_period");
        String bookPrice1 = bookListItem.get("price_1");
        String bookPrice2 = bookListItem.get("price_2");
        if (bookRentalPeriod != null && !bookRentalPeriod.isEmpty() && !bookRentalPeriod.equals("")) {
            book_rental_period.setText("(" + bookRentalPeriod + ")");
            book_rental_period.setVisibility(View.VISIBLE);
        } else {
            book_rental_period.setVisibility(View.GONE);
        }

        if (bookPrice2 != null && !bookPrice2.equals("") && !bookPrice2.isEmpty()) {
            book_price_1.setText(bookPrice1);
            book_price_2.setText(bookPrice2);
        } else {
            book_price_1.setVisibility(View.GONE);
            book_price_2.setText(bookPrice1);
        }

        bookView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent webViewIntent = new Intent(mContext, WebViewActivity.class);
                webViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                webViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                webViewIntent.putExtra("web_url", bookListItem.get("index_page_url"));

                Activity activity = (Activity) mContext;
                activity.startActivity(webViewIntent);
                activity.getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                Log.i("Recommended Product View", "Name: " + bookListItem.get("book_name") + "\nUrl: " + bookListItem.get("index_page_url"));
                Utils.triggerGAEvent(mContext, "Recommended_Product", bookListItem.get("book_product_id"), customerId);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RecommendationsActivity extends AppCompatActivity {

    private LibrarySwipeLayout recommendationSwipeView;
    private ViewFlipper recommendationFlipper;
    private RecyclerView recommendationListView;
    private RecommendationsAdapter recommendationAdapter;
    private String customerId;
    private LinearLayout bottomNavigationWidget;
    private Button ebookButton, testPreparationButton, videoButton, storesButton, recommendationButton;
    private int firstVisibleInRecyclerView;

    private String screenName = "Recommendations";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommendations);

        customerId = AppSettings.getInstance(RecommendationsActivity.this).get("CUSTOMER_ID");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.back_button);
        actionBar.setDisplayHomeAsUpEnabled(true);
        SpannableString actionBarLabel = new SpannableString(getResources().getString(R.string.recommendation_label));
        actionBarLabel.setSpan(new TypefaceSpan("" + Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf")), 0, actionBarLabel.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(actionBarLabel);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.action_bar_background_dark));
        }

        recommendationFlipper = (ViewFlipper) findViewById(R.id.recommendation_flipper);
        recommendationListView = (RecyclerView) findViewById(R.id.books_list);
        recommendationSwipeView = (LibrarySwipeLayout) findViewById(R.id.recommendation_swipeview);
        recommendationSwipeView.setTargetView(recommendationListView);
        recommendationSwipeView.setColorSchemeColors(getResources().getColor(R.color.action_bar_background), getResources().getColor(R.color.action_bar_background_dark));
        recommendationSwipeView.setDistanceToTriggerSync(20);
        recommendationSwipeView.setSize(SwipeRefreshLayout.DEFAULT);
        recommendationSwipeView.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub
                new GetRecommendations().execute();
            }
        });

        recommendationListView.setLayoutManager(new LinearLayoutManager(RecommendationsActivity.this));
        recommendationListView.addItemDecoration(new DividerItemDecoration(ContextCompat.getDrawable(RecommendationsActivity.this, R.drawable.item_divider)));

        bottomNavigationWidget = (LinearLayout) findViewById(R.id.bottom_navigation_widget);
        ebookButton = (Button) bottomNavigationWidget.findViewById(R.id.ebook_bottom_navigation_button);
        testPreparationButton = (Button) bottomNavigationWidget.findViewById(R.id.test_preparation_bottom_navigation_button);
        videoButton = (Button) bottomNavigationWidget.findViewById(R.id.video_bottom_navigation_button);
        storesButton = (Button) bottomNavigationWidget.findViewById(R.id.store_bottom_navigation_button);
        recommendationButton = (Button) bottomNavigationWidget.findViewById(R.id.recommendation_bottom_navigation_button);

        new GetRecommendations().execute();

        ebookButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                disabledActivateBottomNavigationWidgetButton();

                Drawable drawableTop = getResources().getDrawable(R.drawable.ic_ebook);
                ebookButton.setCompoundDrawables(null, drawableTop, null, null);
                ebookButton.setTypeface(null, Typeface.BOLD);
                ebookButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_enable_color));

                Utils.showLibrary(RecommendationsActivity.this, "ebook");
                Utils.triggerGAEvent(RecommendationsActivity.this, "BottomNavigation", "E-books", customerId);
            }
        });

        testPreparationButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                disabledActivateBottomNavigationWidgetButton();

                Drawable drawableTop = getResources().getDrawable(R.drawable.ic_test_preparation);
                testPreparationButton.setCompoundDrawables(null, drawableTop, null, null);
                testPreparationButton.setTypeface(null, Typeface.BOLD);
                testPreparationButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_enable_color));

                Utils.showLibrary(RecommendationsActivity.this, "test_preparation, mock_test");
                Utils.triggerGAEvent(RecommendationsActivity.this, "BottomNavigation", "Test Preparation", customerId);
            }
        });

        videoButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                if (!Constants.getActivateBottomNavigationWidgetButton().equals("Video")) {
                    disabledActivateBottomNavigationWidgetButton();

                    videoButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_video, 0, 0);
                    videoButton.setTypeface(null, Typeface.BOLD);
                    videoButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_enable_color));

                    Utils.showLibrary(RecommendationsActivity.this, "video");
                    Utils.triggerGAEvent(RecommendationsActivity.this, "BottomNavigation", "Video", customerId);
                }
            }
        });

        storesButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                disabledActivateBottomNavigationWidgetButton();

                Drawable drawableTop = getResources().getDrawable(R.drawable.ic_store);
                storesButton.setCompoundDrawables(null, drawableTop, null, null);
                storesButton.setTypeface(null, Typeface.BOLD);
                storesButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_enable_color));

                Utils.showStoreActivity(RecommendationsActivity.this);

                Utils.triggerGAEvent(RecommendationsActivity.this, "BottomNavigation", "Store", customerId);
            }
        });

        recommendationButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                if (!Constants.getActivateBottomNavigationWidgetButton().equals("Recommendation")) {
                    if (Utils.isNetworkConnected(RecommendationsActivity.this)) {
                        Utils.showRecommendationsActivity(RecommendationsActivity.this);
                    }
                    Utils.triggerGAEvent(RecommendationsActivity.this, "BottomNavigation", "Recommendation", customerId);
                }
            }
        });

        recommendationListView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int currentFirstVisible = ((LinearLayoutManager) recommendationListView.getLayoutManager()).findFirstVisibleItemPosition();
                if (currentFirstVisible > firstVisibleInRecyclerView) {
                    //hide Store Bottom Navigation View
                    if (bottomNavigationWidget.getVisibility() == View.VISIBLE) {
                        hideBottomNavigationWidget();
                    }
                } else if (currentFirstVisible < firstVisibleInRecyclerView) {
                    //show Store Bottom Navigation View
                    if (bottomNavigationWidget.getVisibility() == View.GONE) {
                        showBottomNavigationWidget();
                    }
                }
                firstVisibleInRecyclerView = currentFirstVisible;
            }
        });
    }

    private void disabledActivateBottomNavigationWidgetButton() {
        String lastActivatedButton = Constants.getActivateBottomNavigationWidgetButton();
        if (lastActivatedButton.equals("E-books")) {
            ebookButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_ebook_disabled, 0, 0);
            ebookButton.setTypeface(null, Typeface.NORMAL);
            ebookButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_disable_color));
        } else if (lastActivatedButton.equals("Test Preparation")) {
            testPreparationButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_test_preparation_disabled, 0, 0);
            testPreparationButton.setTypeface(null, Typeface.NORMAL);
            testPreparationButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_disable_color));
        } else if (lastActivatedButton.equals("Video")) {
            videoButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_video_disabled, 0, 0);
            videoButton.setTypeface(null, Typeface.NORMAL);
            videoButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_disable_color));
        } else if (lastActivatedButton.equals("Store")) {
            storesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_store_disabled, 0, 0);
            storesButton.setTypeface(null, Typeface.NORMAL);
            storesButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_disable_color));
        } else if (lastActivatedButton.equals("Recommendation")) {
            recommendationButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_recommendation_disabled, 0, 0);
            recommendationButton.setTypeface(null, Typeface.NORMAL);
            recommendationButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_disable_color));
        }
    }

    private void disabledAllActivatedBottomNavigationWidgetButton() {
        //for E-books
        ebookButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_ebook_disabled, 0, 0);
        ebookButton.setTypeface(null, Typeface.NORMAL);
        ebookButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_disable_color));

        //for Test Preparation
        testPreparationButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_test_preparation_disabled, 0, 0);
        testPreparationButton.setTypeface(null, Typeface.NORMAL);
        testPreparationButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_disable_color));

        //for Video
        videoButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_video_disabled, 0, 0);
        videoButton.setTypeface(null, Typeface.NORMAL);
        videoButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_disable_color));

        //for Store
        storesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_store_disabled, 0, 0);
        storesButton.setTypeface(null, Typeface.NORMAL);
        storesButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_disable_color));

        //for Recommendation
        recommendationButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_recommendation_disabled, 0, 0);
        recommendationButton.setTypeface(null, Typeface.NORMAL);
        recommendationButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_disable_color));
    }

    private void hideBottomNavigationWidget() {
        bottomNavigationWidget.setAnimation(AnimationUtils.loadAnimation(RecommendationsActivity.this, R.anim.slide_down));

        Animation anim = new TranslateAnimation(0, 0, 0, bottomNavigationWidget.getHeight());
        anim.setDuration(500);
        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                bottomNavigationWidget.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
            }
        });
        bottomNavigationWidget.startAnimation(anim);
    }

    private void showBottomNavigationWidget() {
        bottomNavigationWidget.setAnimation(AnimationUtils.loadAnimation(RecommendationsActivity.this, R.anim.slide_up));
        Animation anim = new TranslateAnimation(0, 0, bottomNavigationWidget.getHeight(), 0);
        anim.setDuration(500);
        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                bottomNavigationWidget.setVisibility(View.VISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
            }
        });
        bottomNavigationWidget.startAnimation(anim);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        //enable recommendation bottom button
        disabledAllActivatedBottomNavigationWidgetButton();
        Drawable drawableTop = getResources().getDrawable(R.drawable.ic_recommendation);
        recommendationButton.setCompoundDrawables(null, drawableTop, null, null);
        recommendationButton.setTypeface(null, Typeface.BOLD);
        recommendationButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_enable_color));
        Constants.setActivateBottomNavigationWidgetButton("Recommendation");

        Utils.triggerScreen(RecommendationsActivity.this, screenName);
    }

    private class GetRecommendations extends AsyncTask<Void, HashMap<String, String>, String> {

        private ProgressDialog pDialog;
        private PowerManager.WakeLock wakeLock;
        private List<HashMap<String, String>> featuredProduct = new ArrayList<HashMap<String, String>>();

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            recommendationSwipeView.setRefreshing(false);
            pDialog = new ProgressDialog(RecommendationsActivity.this);
            pDialog.setMessage("Loading Recommendations... Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            wakeLock.acquire();
            pDialog.show();

            recommendationAdapter = new RecommendationsAdapter(RecommendationsActivity.this, new ArrayList<HashMap<String, String>>());
            recommendationListView.setAdapter(recommendationAdapter);
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO Auto-generated method stub
            String response = null;
            try {
                response = Utils.sendPost(RecommendationsActivity.this, Constants.RECOMMENDATIONS_URL, "customer_id=" + URLEncoder.encode(customerId, "UTF-8") + "&source=" + URLEncoder.encode(Constants.LOGIN_SOURCE, "UTF-8"));
                Log.i("Recommended List", response);
                JSONObject recommendationJsonObject = new JSONObject(response);
                JSONArray recommendationBooksJsonArray = recommendationJsonObject.getJSONArray("products");
                if (recommendationBooksJsonArray.length() > 0) {
                    for (int i = 0; i < recommendationBooksJsonArray.length(); i++) {
                        HashMap<String, String> tempBook = new HashMap<String, String>();
                        JSONObject book = recommendationBooksJsonArray.getJSONObject(i);

                        String productId = book.getString("product_id").trim();
                        String name = book.getString("name").trim();
                        String author = book.getString("description");
                        String imageURL = book.getString("image");
                        String href = book.getString("href");
                        String productType = book.getString("product_type");
                        String rentalPeroid = book.getString("rental_period");
                        String price_1 = book.getString("price_1");
                        String price_2 = book.getString("price_2");

                        if (book.has("featured") && book.getBoolean("featured")) {
                            tempBook.put("book_product_id", productId);
                            tempBook.put("book_name", name);
                            tempBook.put("book_author", author);
                            tempBook.put("book_image_url", imageURL);
                            tempBook.put("index_page_url", href);
                            tempBook.put("rental_period", rentalPeroid);
                            tempBook.put("price_1", price_1);
                            tempBook.put("price_2", price_2);

                            String buttonText = "";
                            if (book.has("button_text")) {
                                buttonText = book.getString("button_text").trim();
                            }
                            tempBook.put("button_text", buttonText);

                            featuredProduct.add(tempBook);
                        } else {
                            tempBook.put("book_product_id", productId);
                            tempBook.put("book_name", name);
                            tempBook.put("book_author", author);
                            tempBook.put("book_image_url", imageURL);
                            tempBook.put("pdf_url", "");
                            tempBook.put("index_page_url", href);
                            tempBook.put("product_type", productType);
                            tempBook.put("rental_period", rentalPeroid);
                            tempBook.put("price_1", price_1);
                            tempBook.put("price_2", price_2);

                            publishProgress(tempBook);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onProgressUpdate(HashMap<String, String>... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            recommendationAdapter.setItem(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject recommendationJsonObject = new JSONObject(result);
                if (recommendationJsonObject.getInt("total_products") <= 0) {
                    recommendationFlipper.setDisplayedChild(1);
                    TextView emptyTitle = (TextView) findViewById(R.id.empty_title);
                    emptyTitle.setText(recommendationJsonObject.getJSONObject("empty_details").getString("title"));
                    TextView emptyDescription = (TextView) findViewById(R.id.empty_description);
                    emptyDescription.setText(Html.fromHtml(recommendationJsonObject.getJSONObject("empty_details").getString("description")));
                    emptyDescription.setMovementMethod(LinkMovementMethod.getInstance());
                    recommendationSwipeView.setEnabled(false);
                } else {
                    if (featuredProduct != null && featuredProduct.size() > 0) {
                        Collections.shuffle(featuredProduct);
                        final HashMap<String, String> product = featuredProduct.get(0);
                        if (product != null) {
                            final BottomSheetDialog mBottomSheetDialog = Utils.createBottomSheetDialog(RecommendationsActivity.this, R.layout.book_featured);
                            mBottomSheetDialog.show();

                            LinearLayout bookLayout = (LinearLayout) mBottomSheetDialog.findViewById(R.id.book_layout);
                            ImageView bookImage = ((ImageView) mBottomSheetDialog.findViewById(R.id.book_image));
                            ImageView closeButton = (ImageView) mBottomSheetDialog.findViewById(R.id.close_button);
                            ((TextView) mBottomSheetDialog.findViewById(R.id.book_name)).setText(Html.fromHtml(product.get("book_name")));
                            ((TextView) mBottomSheetDialog.findViewById(R.id.book_author)).setText(Html.fromHtml(product.get("book_author")));
                            TextView price_1 = ((TextView) mBottomSheetDialog.findViewById(R.id.book_price_1));
                            TextView price_2 = ((TextView) mBottomSheetDialog.findViewById(R.id.book_price_2));
                            ((com.kopykitab.gate.components.TextView) price_1).setAddStrike(true);

                            String price1 = product.get("price_1");
                            String price2 = product.get("price_2");
                            if (price1 != null && !price1.equals("") && !price1.isEmpty()) {
                                price_1.setText(price1);
                                price_2.setText(price2);
                            } else {
                                price_1.setVisibility(View.GONE);
                                price_2.setText(price2);
                            }

                            TextView subscription = ((TextView) mBottomSheetDialog.findViewById(R.id.book_subscription_text));
                            String buttonText = product.get("button_text");
                            if (buttonText != null && !buttonText.isEmpty() && buttonText.length() > 0) {
                                subscription.setText(buttonText);
                            } else {
                                subscription.setVisibility(View.INVISIBLE);
                            }

                            bookImage.setImageDrawable(null);
                            Utils.getImageLoader(RecommendationsActivity.this).displayImage(product.get("book_image_url").replaceAll(" ", "%20"), bookImage);

                            bookLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String url = product.get("index_page_url");
                                    if (url != null && !url.isEmpty()) {
                                        if (Utils.isNetworkConnected(RecommendationsActivity.this)) {
                                            Utils.showWebViewActivity(RecommendationsActivity.this, url);
                                            Utils.triggerGAEventOnline(RecommendationsActivity.this, "Featured_Recommended_Product", product.get("book_product_id"), customerId);
                                        } else {
                                            Utils.networkNotAvailableAlertBox(RecommendationsActivity.this);
                                        }
                                    }
                                }
                            });

                            closeButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mBottomSheetDialog.dismiss();
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }
}