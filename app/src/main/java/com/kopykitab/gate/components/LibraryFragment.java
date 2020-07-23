package com.kopykitab.gate.components;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kopykitab.gate.LibraryActivity;
import com.kopykitab.gate.R;
import com.kopykitab.gate.components.adapters.BooksAdapter;
import com.kopykitab.gate.components.adapters.BooksAdapter.BooksAdapterListener;
import com.kopykitab.gate.models.BookItem;
import com.kopykitab.gate.settings.AppSettings;
import com.kopykitab.gate.settings.Constants;
import com.kopykitab.gate.settings.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@SuppressLint("ValidFragment")
public class LibraryFragment extends Fragment {

    private View rootView;

    private Context mContext;
    private String productTypes, customerId;
    private int totalCategories, categoryPosition;
    private LibraryAutofitRecyclerView bookListView;
    private BooksAdapter booksAdapter;
    private List<BookItem> bookItemList = new ArrayList<BookItem>();
    private BooksAdapterListener bookItemListener;
    private TextView storeOffer;
    private LinearLayout bottomNavigationWidget;
    private Button ebookButton, testPreparationButton, videoButton, storesButton, recommendationButton;

    public LibraryFragment() {
        super();
    }

    public LibraryFragment(Context mContext, String productTypes, int totalCategories, int categoryPosition, BooksAdapterListener bookItemListener) {
        this.mContext = mContext;
        this.productTypes = productTypes;
        this.totalCategories = totalCategories;
        this.categoryPosition = categoryPosition;
        this.bookItemListener = bookItemListener;
        customerId = AppSettings.getInstance(mContext).get("CUSTOMER_ID");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((totalCategories - 1) == categoryPosition) {
            rootView = inflater.inflate(R.layout.library_store, container, false);

            storeOffer = (TextView) rootView.findViewById(R.id.library_store_offer);
            try {
                String offers = AppSettings.getInstance(getContext()).get("offers");
                if (offers != null && !offers.isEmpty()) {
                    JSONArray offersArray = new JSONArray(offers);
                    if (offersArray != null && offersArray.length() > 0) {
                        storeOffer.setVisibility(View.VISIBLE);
                        storeOffer.setText(Html.fromHtml(offersArray.getString(0)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            rootView = inflater.inflate(R.layout.library_main, container, false);
            bookListView = (LibraryAutofitRecyclerView) rootView.findViewById(R.id.bookListView);
            bookListView.setHasFixedSize(true);

            new PrepareBookLibrary().execute();

            bookListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0 && bottomNavigationWidget.isShown()) {
                        hideBottomNavigationWidget();
                    } else if (dy < 0) {
                        showBottomNavigationWidget();

                    }
                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                    super.onScrollStateChanged(recyclerView, newState);
                }
            });
        }

        bottomNavigationWidget = (LinearLayout) getActivity().findViewById(R.id.bottom_navigation_widget);
        ebookButton = (Button) getActivity().findViewById(R.id.ebook_bottom_navigation_button);
        testPreparationButton = (Button) getActivity().findViewById(R.id.test_preparation_bottom_navigation_button);
        videoButton = (Button) getActivity().findViewById(R.id.video_bottom_navigation_button);
        storesButton = (Button) getActivity().findViewById(R.id.store_bottom_navigation_button);
        recommendationButton = (Button) getActivity().findViewById(R.id.recommendation_bottom_navigation_button);

        ebookButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                if (!Constants.getActivateBottomNavigationWidgetButton().equals("E-books")) {
                    disabledActivateBottomNavigationWidgetButton();

                    ebookButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_ebook, 0, 0);
                    ebookButton.setTypeface(null, Typeface.BOLD);
                    ebookButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_enable_color));

                    Utils.showLibrary(mContext, "ebook");
                    Utils.triggerGAEvent(mContext, "BottomNavigation", "E-books", customerId);
                }
            }
        });

        testPreparationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                if (!Constants.getActivateBottomNavigationWidgetButton().equals("Test Preparation")) {
                    disabledActivateBottomNavigationWidgetButton();

                    testPreparationButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_test_preparation, 0, 0);
                    testPreparationButton.setTypeface(null, Typeface.BOLD);
                    testPreparationButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_enable_color));

                    Utils.showLibrary(mContext, "test_preparation, mock_test");
                    Utils.triggerGAEvent(mContext, "BottomNavigation", "Test Preparation", customerId);
                }
            }
        });

        videoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                if (!Constants.getActivateBottomNavigationWidgetButton().equals("Video")) {
                    disabledActivateBottomNavigationWidgetButton();

                    videoButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_video, 0, 0);
                    videoButton.setTypeface(null, Typeface.BOLD);
                    videoButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_enable_color));

                    Utils.showLibrary(mContext, "video");
                    Utils.triggerGAEvent(mContext, "BottomNavigation", "Video", customerId);
                }
            }
        });

        storesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                if (!Constants.getActivateBottomNavigationWidgetButton().equals("Store")) {
                    disabledActivateBottomNavigationWidgetButton();

                    storesButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_store, 0, 0);
                    storesButton.setTypeface(null, Typeface.BOLD);
                    storesButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_enable_color));

                    Utils.showStoreActivity(mContext);
                    Utils.triggerGAEvent(mContext, "BottomNavigation", "Store", customerId);
                }
            }
        });

        recommendationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                if (!Constants.getActivateBottomNavigationWidgetButton().equals("Recommendation")) {
                    if (Utils.isNetworkConnected(mContext)) {
                        disabledActivateBottomNavigationWidgetButton();

                        recommendationButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_recommendation, 0, 0);
                        recommendationButton.setTypeface(null, Typeface.BOLD);
                        recommendationButton.setTextColor(getResources().getColor(R.color.bottom_navigation_widget_button_enable_color));

                        Utils.showRecommendationsActivity(mContext);
                    } else {
                        Utils.networkNotAvailableAlertBox(mContext);
                    }
                    Utils.triggerGAEvent(mContext, "BottomNavigation", "Recommendation", customerId);
                }
            }
        });

        return rootView;
    }

    public void setViewToListView() {
        if (booksAdapter != null) {
            bookListView.switchToListView();
            booksAdapter = new BooksAdapter(mContext, bookItemList, R.layout.library_list, "ListView", bookItemListener);
            bookListView.setAdapter(booksAdapter);
        }
    }

    public void setViewToGridView() {
        if (booksAdapter != null) {
            bookListView.switchToGridView();
            booksAdapter = new BooksAdapter(mContext, bookItemList, R.layout.library_grid, "GridView", bookItemListener);
            bookListView.setAdapter(booksAdapter);
        }
    }

    public BooksAdapter getAdapter() {
        return booksAdapter;
    }

    private class PrepareBookLibrary extends AsyncTask<String, BookItem, List<BookItem>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (LibraryActivity.isListView()) {
                booksAdapter = new BooksAdapter(mContext, R.layout.library_list, "ListView", bookItemListener);
            } else {
                booksAdapter = new BooksAdapter(mContext, R.layout.library_grid, "GridView", bookItemListener);
            }
            bookListView.setAdapter(booksAdapter);
        }

        @Override
        protected List<BookItem> doInBackground(String... strings) {
            List<HashMap<String, String>> booksList = new ArrayList<HashMap<String, String>>();
            try {
                String jsonFileName = productTypes.replaceAll("(\\s|,)+", "_") + "_by_categories.json";
                File jsonFile = new File(Utils.getDirectory(mContext) + jsonFileName);
                long serverTime = jsonFile.lastModified(), systemTime = new Date().getTime();

                FileInputStream jsonFileInputStream = new FileInputStream(jsonFile);
                byte[] fileRead = new byte[jsonFileInputStream.available()];
                jsonFileInputStream.read(fileRead);
                jsonFileInputStream.close();
                JSONObject productTypeJsonObject = new JSONObject(new String(Base64.decode(fileRead, Base64.DEFAULT)));
                booksList = new Gson().fromJson(productTypeJsonObject.getJSONArray("products").getJSONObject(categoryPosition).getJSONArray("products").toString(), new TypeToken<ArrayList<HashMap<String, String>>>() {
                }.getType());

                for (HashMap<String, String> book : booksList) {
                    book.put("image_url", Constants.IMAGE_URL + book.get("image_url"));
                    long diffDays = Math.abs(systemTime - serverTime) / 86400000; // Days calculation by 1000*60*60*24
                    if (book.get("left_days") == null || book.get("left_days").isEmpty() || book.get("left_days").equals("")) {
                        book.put("left_days", "999999");
                    }
                    int leftDays = Integer.valueOf(book.get("left_days")).intValue();
                    if (leftDays == 999999) {
                        BookItem bookItem = new BookItem(book.get("name"), book.get("order_product_id"), book.get("cidd"), book.get("image_url"), book.get("description"), book.get("price"), book.get("left_days"), book.get("product_id"), book.get("product_type"), book.get("option_name"), book.get("option_value"), book.get("pdf_downloaded_date"), book.get("license_period"), book.get("product_link"), book.get("date_added"));
                        bookItemList.add(bookItem);

                        publishProgress(bookItem);
                    } else {
                        if ((leftDays - diffDays) >= 0) {
                            BookItem bookItem = new BookItem(book.get("name"), book.get("order_product_id"), book.get("cidd"), book.get("image_url"), book.get("description"), book.get("price"), book.get("left_days"), book.get("product_id"), book.get("product_type"), book.get("option_name"), book.get("option_value"), book.get("pdf_downloaded_date"), book.get("license_period"), book.get("product_link"), book.get("date_added"));
                            bookItemList.add(bookItem);

                            publishProgress(bookItem);
                        } else {
                            Utils.removeExpiredFiles(mContext, book.get("product_link"), book.get("image_url"), book.get("product_id"));
                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return bookItemList;
        }

        @Override
        protected void onProgressUpdate(BookItem... values) {
            super.onProgressUpdate(values);
            booksAdapter.setItem(values[0]);
        }

        @Override
        protected void onPostExecute(List<BookItem> bookItemList) {
            super.onPostExecute(bookItemList);
        }
    }

    private void disabledActivateBottomNavigationWidgetButton() {
        try {
            Activity activity = getActivity();
            if (activity != null && isAdded()) {
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
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideBottomNavigationWidget() {
        bottomNavigationWidget.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_down));

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
        bottomNavigationWidget.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_up));
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
}