package com.app.sf.myjiugonggephotoupload;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class SearchView extends FrameLayout implements View.OnClickListener {

    public static final String TAG = "SearchView";
    public static final int SPEECH_REQUEST_CODE = 100;

    private static int mIconColor = Color.BLACK;
    private static int mTextColor = Color.BLACK;
    private static int mTextHighlightColor = Color.BLACK;
    private static int mTextStyle = Typeface.NORMAL;
    private static Typeface mTextFont = Typeface.DEFAULT;
    private final Context mContext;
    private SearchArrowDrawable mSearchArrowDrawable;
    private View mViewShadow;
    private View mViewDivider;
    private View mMenuItemView;
    private CardView mCardView;
    private LinearLayout mLinearLayout;
    private ImageView mImageViewArrow;
    private ImageView mImageViewMic;
    private ImageView mImageViewClear;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerViewAdapter;
    private FlexboxLayout mFlexboxLayout;
    private SearchEditText mSearchEditText;
    private OnQueryTextListener mOnQueryTextListener;
    private OnOpenCloseListener mOnOpenCloseListener;
    private OnNavigationIconClickListener mOnNavigationIconClickListener;
    private OnVoiceIconClickListener mOnVoiceIconClickListener;
    private List<Boolean> mSearchFiltersStates;
    private List<SearchFilter> mSearchFilters;
    private int mVersion;
    private int mVersionMargins;
    private int mTheme;
    private int mMenuItemCx = -1;
    private int mAnimationDuration;
    private float mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_HAMBURGER;
    private boolean mArrow = false;
    private boolean mShadow = true;
    private boolean mVoice = true;
    private boolean mShouldClearOnOpen = false;
    private boolean mShouldClearOnClose = false;
    private boolean mShouldHideOnKeyboardClose = true;
    private CharSequence mQuery = "";
    private String mVoiceText = "";

    // ---------------------------------------------------------------------------------------------
    public SearchView(@NonNull Context context) {
        super(context);
        mContext = context;
        initView();
        initStyle(null, 0, 0);
    }

    public SearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
        initStyle(attrs, 0, 0);
    }

    public SearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        initStyle(attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        initView();
        initStyle(attrs, defStyleAttr, defStyleRes);
    }

    // ---------------------------------------------------------------------------------------------
    @ColorInt
    public static int getIconColor() {
        return mIconColor;
    }

    public void setIconColor(@ColorInt int color) {
        mIconColor = color;
        ColorFilter colorFilter = new PorterDuffColorFilter(mIconColor, PorterDuff.Mode.SRC_IN);

        mImageViewArrow.setColorFilter(colorFilter);
        mImageViewMic.setColorFilter(colorFilter);
        mImageViewClear.setColorFilter(colorFilter);
    }

    @ColorInt
    public static int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(@ColorInt int color) {
        mTextColor = color;
        mSearchEditText.setTextColor(mTextColor);

        for (int i = 0, n = mFlexboxLayout.getChildCount(); i < n; i++) {
            View child = mFlexboxLayout.getChildAt(i);
            if (child instanceof CheckBox)
                ((CheckBox) child).setTextColor(mTextColor);
        }
    }

    public static Typeface getTextFont() {
        return mTextFont;
    }

    public void setTextFont(Typeface font) {
        mTextFont = font;
        mSearchEditText.setTypeface((Typeface.create(mTextFont, mTextStyle)));
    }

    @ColorInt
    public static int getTextHighlightColor() {
        return mTextHighlightColor;
    }

    public void setTextHighlightColor(@ColorInt int color) {
        mTextHighlightColor = color;
    }

    public static int getTextStyle() {
        return mTextStyle;
    }

    public void setTextStyle(int style) {
        mTextStyle = style;
        mSearchEditText.setTypeface((Typeface.create(mTextFont, mTextStyle)));
    }

    // ---------------------------------------------------------------------------------------------
    private void initView() {
        LayoutInflater.from(mContext).inflate((R.layout.search_view), this, true);

        mAnimationDuration = mContext.getResources().getInteger(R.integer.search_animation_duration);

        mViewShadow = findViewById(R.id.view_shadow);
        mViewShadow.setBackgroundColor(ContextCompat.getColor(mContext, R.color.search_shadow_layout));
        mViewShadow.setOnClickListener(this);
        mViewShadow.setVisibility(View.GONE);

        mViewDivider = findViewById(R.id.view_divider);
        mViewDivider.setVisibility(View.GONE);

        mCardView = (CardView) findViewById(R.id.cardView);
        mLinearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        mSearchArrowDrawable = new SearchArrowDrawable(mContext);

        mImageViewArrow = (ImageView) findViewById(R.id.imageView_arrow);
        mImageViewArrow.setImageDrawable(mSearchArrowDrawable);
        mImageViewArrow.setOnClickListener(this);

        mImageViewMic = (ImageView) findViewById(R.id.imageView_mic);
        mImageViewMic.setImageResource(R.drawable.ic_mic_black_24dp);
        mImageViewMic.setOnClickListener(this);
        mImageViewMic.setVisibility(View.GONE);

        mImageViewClear = (ImageView) findViewById(R.id.imageView_clear);
        mImageViewClear.setImageResource(R.drawable.ic_clear_black_24dp);
        mImageViewClear.setOnClickListener(this);
        mImageViewClear.setVisibility(View.GONE);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setVisibility(View.GONE);
        // mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard();
                }
            }
        });

        mFlexboxLayout = (FlexboxLayout) findViewById(R.id.flexboxLayout);
        mFlexboxLayout.setVisibility(View.GONE);

        mSearchEditText = (SearchEditText) findViewById(R.id.searchEditText);
        mSearchEditText.setSearchView(this);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SearchView.this.onTextChanged(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                onSubmitQuery();
                return true;
            }
        });
        mSearchEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    addFocus();
            /*
        if (mAdapter != null && mAdapter instanceof Filterable) {
        ((Filterable) mAdapter).getFilter().filter(getQuery());
        }
            * */
                } else {
                    removeFocus();
                }
            }
        });

        setVersion(Version.TOOLBAR);
        setVersionMargins(VersionMargins.TOOLBAR_SMALL);
        setTheme(Theme.LIGHT);
        setVoice(true);
    }

    // todo
    private void initStyle(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray attr = mContext.obtainStyledAttributes(attrs, R.styleable.SearchView, defStyleAttr, defStyleRes);
        if (attr != null) {
            if (attr.hasValue(R.styleable.SearchView_search_height)) {
                setCustomHeight(attr.getDimensionPixelSize(R.styleable.SearchView_search_height, mContext.getResources().getDimensionPixelSize(R.dimen.search_height)));
            }
            if (attr.hasValue(R.styleable.SearchView_search_version)) {
                setVersion(attr.getInt(R.styleable.SearchView_search_version, Version.TOOLBAR));
            }
            if (attr.hasValue(R.styleable.SearchView_search_version_margins)) {
                setVersionMargins(attr.getInt(R.styleable.SearchView_search_version_margins, VersionMargins.TOOLBAR_SMALL));
            }
            if (attr.hasValue(R.styleable.SearchView_search_theme)) {
                setTheme(attr.getInt(R.styleable.SearchView_search_theme, Theme.LIGHT));
            }
            if (attr.hasValue(R.styleable.SearchView_search_navigation_icon)) {
                setNavigationIcon(attr.getResourceId(R.styleable.SearchView_search_navigation_icon, 0));
            }
            if (attr.hasValue(R.styleable.SearchView_search_icon_color)) {
                setIconColor(attr.getColor(R.styleable.SearchView_search_icon_color, Color.BLACK));
            }
            if (attr.hasValue(R.styleable.SearchView_search_background_color)) {
                setBackgroundColor(attr.getColor(R.styleable.SearchView_search_background_color, Color.WHITE));
            }
            if (attr.hasValue(R.styleable.SearchView_search_text_color)) {
                setTextColor(attr.getColor(R.styleable.SearchView_search_text_color, Color.BLACK));
            }
            if (attr.hasValue(R.styleable.SearchView_search_text_highlight_color)) {
                setTextHighlightColor(attr.getColor(R.styleable.SearchView_search_text_highlight_color, Color.GRAY));
            }
            if (attr.hasValue(R.styleable.SearchView_search_text_size)) {
                setTextSize(attr.getDimension(R.styleable.SearchView_search_text_size, mContext.getResources().getDimension(R.dimen.search_text_medium)));
            }
            if (attr.hasValue(R.styleable.SearchView_search_text_style)) {
                setTextStyle(attr.getInt(R.styleable.SearchView_search_text_style, TextStyle.NORMAL));
            }
            if (attr.hasValue(R.styleable.SearchView_search_hint)) {
                setHint(attr.getString(R.styleable.SearchView_search_hint));
            }
            if (attr.hasValue(R.styleable.SearchView_search_hint_color)) {
                setHintColor(attr.getColor(R.styleable.SearchView_search_hint_color, Color.BLACK));
            }
            if (attr.hasValue(R.styleable.SearchView_search_voice)) {
                setVoice(attr.getBoolean(R.styleable.SearchView_search_voice, true));
            }
            if (attr.hasValue(R.styleable.SearchView_search_voice_text)) {
                setVoiceText(attr.getString(R.styleable.SearchView_search_voice_text));
            }
            if (attr.hasValue(R.styleable.SearchView_search_animation_duration)) {
                setAnimationDuration(attr.getInteger(R.styleable.SearchView_search_animation_duration, mAnimationDuration));
            }
            if (attr.hasValue(R.styleable.SearchView_search_shadow)) {
                setShadow(attr.getBoolean(R.styleable.SearchView_search_shadow, true));
            }
            if (attr.hasValue(R.styleable.SearchView_search_shadow_color)) {
                setShadowColor(attr.getColor(R.styleable.SearchView_search_shadow_color, Color.TRANSPARENT));
            }
            if (attr.hasValue(R.styleable.SearchView_search_elevation)) {
                setElevation(attr.getDimensionPixelSize(R.styleable.SearchView_search_elevation, 0));
            }
            if (attr.hasValue(R.styleable.SearchView_search_clear_on_open)) {
                setShouldClearOnOpen(attr.getBoolean(R.styleable.SearchView_search_clear_on_open, false));
            }
            if (attr.hasValue(R.styleable.SearchView_search_clear_on_close)) {
                setShouldClearOnClose(attr.getBoolean(R.styleable.SearchView_search_clear_on_close, false));
            }
            if (attr.hasValue(R.styleable.SearchView_search_hide_on_keyboard_close)) {
                setShouldHideOnKeyboardClose(attr.getBoolean(R.styleable.SearchView_search_hide_on_keyboard_close, true));
            }
            if (attr.hasValue(R.styleable.SearchView_search_cursor_drawable)) {
                setCursorDrawable(attr.getResourceId(R.styleable.SearchView_search_cursor_drawable, R.drawable.custom_cursor));
            }
            attr.recycle();
        }
    }

    // ---------------------------------------------------------------------------------------------
    @Version
    public int getVersion() {
        return mVersion;
    }

    public void setVersion(@Version int version) {
        mVersion = version;

        if (mVersion == Version.MENU_ITEM) {
            setVisibility(View.GONE);
        }
    }

    public void setTheme(@Theme int theme, boolean tint) {
        mTheme = theme;

        if (theme == Theme.LIGHT) {
            setBackgroundColor(ContextCompat.getColor(mContext, R.color.search_light_background));
            if (tint) {
                setIconColor(ContextCompat.getColor(mContext, R.color.search_light_icon));
                setHintColor(ContextCompat.getColor(mContext, R.color.search_light_hint));
                setTextColor(ContextCompat.getColor(mContext, R.color.search_light_text));
                setTextHighlightColor(ContextCompat.getColor(mContext, R.color.search_light_text_highlight));
            }
        }

        if (theme == Theme.DARK) {
            setBackgroundColor(ContextCompat.getColor(mContext, R.color.search_dark_background));
            if (tint) {
                setIconColor(ContextCompat.getColor(mContext, R.color.search_dark_icon));
                setHintColor(ContextCompat.getColor(mContext, R.color.search_dark_hint));
                setTextColor(ContextCompat.getColor(mContext, R.color.search_dark_text));
                setTextHighlightColor(ContextCompat.getColor(mContext, R.color.search_dark_text_highlight));
            }
        }

        if (theme == Theme.PLAY_STORE) {
            setBackgroundColor(ContextCompat.getColor(mContext, R.color.search_play_store_background));
            if (tint) {
                setIconColor(ContextCompat.getColor(mContext, R.color.search_play_store_icon));
                setHintColor(ContextCompat.getColor(mContext, R.color.search_play_store_hint));
                setTextColor(ContextCompat.getColor(mContext, R.color.search_play_store_text));
                setTextHighlightColor(ContextCompat.getColor(mContext, R.color.search_play_store_text_highlight));
            }
        }
    }

    @Theme
    public int getTheme() {
        return mTheme;
    }

    public void setTheme(@Theme int theme) {
        setTheme(theme, true);
    }

    @VersionMargins
    public int getVersionMargins() {
        return mVersionMargins;
    }

    public void setVersionMargins(@VersionMargins int versionMargins) {
        mVersionMargins = versionMargins;

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );

        if (versionMargins == VersionMargins.TOOLBAR_SMALL) {
            int top = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_top);
            int leftRight = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_small_left_right);
            int bottom = 0;

            params.setMargins(leftRight, top, leftRight, bottom);

        } else if (versionMargins == VersionMargins.TOOLBAR_BIG) {
            int top = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_top);
            int leftRight = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_big_left_right);
            int bottom = 0;

            params.setMargins(leftRight, top, leftRight, bottom);

        } else if (versionMargins == VersionMargins.MENU_ITEM) {
            int top = mContext.getResources().getDimensionPixelSize(R.dimen.search_menu_item_margin);
            int leftRight = mContext.getResources().getDimensionPixelSize(R.dimen.search_menu_item_margin_left_right);
            int bottom = mContext.getResources().getDimensionPixelSize(R.dimen.search_menu_item_margin);

            params.setMargins(leftRight, top, leftRight, bottom);

        } else {
            params.setMargins(0, 0, 0, 0);
        }

        mCardView.setLayoutParams(params);
    }

    public int getCustomHeight() {
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();
        return params.height;
    }

    public void setCustomHeight(int height) {
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();
        params.height = height;
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        mLinearLayout.setLayoutParams(params);
    }

    public void setQuery(CharSequence query, boolean submit) {
        setQueryWithoutSubmitting(query);

        if (!TextUtils.isEmpty(mQuery)) {
            mImageViewClear.setVisibility(View.GONE);
            if (mVoice) {
                mImageViewMic.setVisibility(View.VISIBLE);
            }
        }

        if (submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery();
        }
    }

    public CharSequence getQuery() {
        return mSearchEditText.getText();
    }

    public void setTextOnly(CharSequence text) {
        mSearchEditText.setText(text);
    }

    public CharSequence getTextOnly() {
        return mSearchEditText.getText();
    }

    public void setTextOnly(@StringRes int text) {
        mSearchEditText.setText(text);
    }

    public CharSequence getHint() {
        return mSearchEditText.getHint();
    }

    public void setHint(@StringRes int hint) {
        mSearchEditText.setHint(hint);
    }

    public void setHint(CharSequence hint) {
        mSearchEditText.setHint(hint);
    }

    public int getImeOptions() {
        return mSearchEditText.getImeOptions();
    }

    public void setImeOptions(int imeOptions) {
        mSearchEditText.setImeOptions(imeOptions);
    }

    public int getInputType() {
        return mSearchEditText.getInputType();
    }

    public void setInputType(int inputType) {
        mSearchEditText.setInputType(inputType);
    }

    public boolean getShouldClearOnClose() {
        return mShouldClearOnClose;
    }

    public void setShouldClearOnClose(boolean shouldClearOnClose) {
        mShouldClearOnClose = shouldClearOnClose;
    }

    public boolean getShouldClearOnOpen() {
        return mShouldClearOnOpen;
    }

    public void setShouldClearOnOpen(boolean shouldClearOnOpen) {
        mShouldClearOnOpen = shouldClearOnOpen;
    }

    public boolean getShouldHideOnKeyboardClose() {
        return mShouldHideOnKeyboardClose;
    }

    public void setShouldHideOnKeyboardClose(boolean shouldHideOnKeyboardClose) {
        mShouldHideOnKeyboardClose = shouldHideOnKeyboardClose;
    }

    public RecyclerView.Adapter getAdapter() {
        return mRecyclerView.getAdapter();
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecyclerViewAdapter = adapter;
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    /**
     * new SearchDivider(Context)
     * new DividerItemDecoration(Context, DividerItemDecoration.VERTICAL)
     */
    public void addDivider(RecyclerView.ItemDecoration itemDecoration) {
        mRecyclerView.addItemDecoration(itemDecoration);
    }

    /**
     * new SearchDivider(Context)
     * new DividerItemDecoration(Context, DividerItemDecoration.VERTICAL)
     */
    public void removeDivider(RecyclerView.ItemDecoration itemDecoration) {
        mRecyclerView.removeItemDecoration(itemDecoration);
    }

    public void showKeyboard() {
        if (!isInEditMode()) {
            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(mSearchEditText, 0);
                inputMethodManager.showSoftInput(this, 0);
            }
        }
    }

    public void hideKeyboard() {
        if (!isInEditMode()) {
            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    public void open(boolean animate) {
        open(animate, null);
    }

    public void open(boolean animate, MenuItem menuItem) {
        if (mVersion == Version.MENU_ITEM) {
            setVisibility(View.VISIBLE);

            if (animate) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (menuItem != null) {
                        getMenuItemPosition(menuItem.getItemId());
                    }
                    mCardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                mCardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                            SearchAnimator.revealOpen(
                                    mCardView,
                                    mMenuItemCx,
                                    mAnimationDuration,
                                    mContext,
                                    mSearchEditText,
                                    mShouldClearOnOpen,
                                    mOnOpenCloseListener);
                        }
                    });
                } else {
                    SearchAnimator.fadeOpen(
                            mCardView,
                            mAnimationDuration,
                            mSearchEditText,
                            mShouldClearOnOpen,
                            mOnOpenCloseListener);
                }
            } else {
                mCardView.setVisibility(View.VISIBLE);
                if (mOnOpenCloseListener != null) {
                    mOnOpenCloseListener.onOpen();
                }
                if (mShouldClearOnOpen && mSearchEditText.length() > 0) {
                    mSearchEditText.getText().clear();
                }
                mSearchEditText.requestFocus();
            }
        }

        if (mVersion == Version.TOOLBAR) {
            if (mShouldClearOnOpen && mSearchEditText.length() > 0) {
                mSearchEditText.getText().clear();
            }
            mSearchEditText.requestFocus();
        }
    }

    public void close(boolean animate) {
        close(animate, null);
    }

    public void close(boolean animate, MenuItem menuItem) {
        if (mVersion == Version.MENU_ITEM) {

            if (animate) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (menuItem != null) {
                        getMenuItemPosition(menuItem.getItemId());
                    }
                    SearchAnimator.revealClose(
                            mCardView,
                            mMenuItemCx,
                            mAnimationDuration,
                            mContext,
                            mSearchEditText,
                            mShouldClearOnClose,
                            this,
                            mOnOpenCloseListener);
                } else {
                    SearchAnimator.fadeClose(
                            mCardView,
                            mAnimationDuration,
                            mSearchEditText,
                            mShouldClearOnClose,
                            this,
                            mOnOpenCloseListener);
                }
            } else {
                if (mShouldClearOnClose && mSearchEditText.length() > 0) {
                    mSearchEditText.getText().clear();
                }
                mSearchEditText.clearFocus();
                mCardView.setVisibility(View.GONE);
                setVisibility(View.GONE);
                if (mOnOpenCloseListener != null) {
                    mOnOpenCloseListener.onClose();
                }
            }
        }

        if (mVersion == Version.TOOLBAR) {
            if (mShouldClearOnClose && mSearchEditText.length() > 0) {
                mSearchEditText.getText().clear();
            }
            mSearchEditText.clearFocus();
        }
    }

    public void setFilters(@Nullable List<SearchFilter> filters) {
        mSearchFilters = filters;
        mFlexboxLayout.removeAllViews();
        if (filters == null) {
            mSearchFiltersStates = null;
            mFlexboxLayout.setVisibility(View.GONE);
        } else {
            mSearchFiltersStates = new ArrayList<>();
            for (SearchFilter filter : filters) {
                CheckBox checkBox = new CheckBox(mContext);
                checkBox.setText(filter.getTitle());
                checkBox.setTextSize(12);
                checkBox.setTextColor(mTextColor);
                checkBox.setChecked(filter.isChecked());

                FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(getResources().getDimensionPixelSize(R.dimen.search_filter_margin_start), getResources().getDimensionPixelSize(R.dimen.search_filter_margin_top), getResources().getDimensionPixelSize(R.dimen.search_filter_margin_top), getResources().getDimensionPixelSize(R.dimen.search_filter_margin_top));

                checkBox.setLayoutParams(lp);
                checkBox.setTag(filter.getTagId());
                mFlexboxLayout.addView(checkBox);
                mSearchFiltersStates.add(filter.isChecked());
            }
        }
    }

    public List<SearchFilter> getSearchFilters() {
        if (mSearchFilters == null) {
            return new ArrayList<>();
        }

        dispatchFilters();

        List<SearchFilter> searchFilters = new ArrayList<>();
        for (SearchFilter filter : mSearchFilters) {
            searchFilters.add(new SearchFilter(filter.getTitle(), filter.isChecked(), filter.getTagId()));
        }

        return searchFilters;
    }

    public List<Boolean> getFiltersStates() {
        return mSearchFiltersStates;
    }

    public void addFocus() {
        if (mArrow) {
            mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_ARROW;
        } else {
            if (mSearchArrowDrawable != null) {
                mSearchArrowDrawable.setVerticalMirror(false);
                mSearchArrowDrawable.animate(SearchArrowDrawable.STATE_ARROW, mAnimationDuration);
                mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_ARROW;
            }
        }

        if (mShadow) {
            SearchAnimator.fadeIn(mViewShadow, mAnimationDuration);
        }

        if (!TextUtils.isEmpty(mQuery)) {
            mImageViewClear.setVisibility(View.VISIBLE);
            if (mVoice) {
                mImageViewMic.setVisibility(View.GONE);
            }
        }

        showKeyboard();
        showSuggestions();

        if (mVersion == Version.TOOLBAR) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mOnOpenCloseListener != null) {
                        mOnOpenCloseListener.onOpen();
                    }
                }
            }, mAnimationDuration);
        }
    }

    public void removeFocus() {
        if (mArrow) {
            mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_HAMBURGER;
        } else {
            if (mSearchArrowDrawable != null) {
                mSearchArrowDrawable.setVerticalMirror(true);
                mSearchArrowDrawable.animate(SearchArrowDrawable.STATE_HAMBURGER, mAnimationDuration);
                mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_HAMBURGER;
            }
        }

        if (mShadow) {
            SearchAnimator.fadeOut(mViewShadow, mAnimationDuration);
        }

        if (!TextUtils.isEmpty(mQuery)) {
            mImageViewClear.setVisibility(View.GONE);
            if (mVoice) {
                mImageViewMic.setVisibility(View.VISIBLE);
            }
        }

        hideKeyboard();
        hideSuggestions();

        if (mVersion == Version.TOOLBAR) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mOnOpenCloseListener != null) {
                        mOnOpenCloseListener.onClose();
                    }
                }
            }, mAnimationDuration);
        }
    }

    public void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    public void showSuggestions() {
        if (mFlexboxLayout.getChildCount() > 0 && mFlexboxLayout.getVisibility() == View.GONE) {
            mViewDivider.setVisibility(View.VISIBLE);
            mFlexboxLayout.setVisibility(View.VISIBLE);
        }

        if (mRecyclerViewAdapter != null && mRecyclerViewAdapter.getItemCount() > 0) {
            mViewDivider.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            SearchAnimator.fadeIn(mRecyclerView, mAnimationDuration);
        }
    }

    public void hideSuggestions() {
        if (mFlexboxLayout.getVisibility() == View.VISIBLE) {
            mViewDivider.setVisibility(View.GONE);
            mFlexboxLayout.setVisibility(View.GONE);
        }

        if (mRecyclerViewAdapter != null) {
            mViewDivider.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            SearchAnimator.fadeOut(mRecyclerView, mAnimationDuration);
        }
    }

    public void setShadowColor(@ColorInt int color) {
        mViewShadow.setBackgroundColor(color);
    }

    public void setGoogleIcons() {
        mImageViewArrow.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_google_color_24dp));
        mImageViewMic.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_mic_color_24dp));
    }

    public void setVoiceText(String text) {
        mVoiceText = text;
    }

    public void setTextSize(float size) {
        mSearchEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setHintColor(@ColorInt int color) {
        mSearchEditText.setHintTextColor(color);
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        mOnQueryTextListener = listener;
    }

    public void setOnOpenCloseListener(OnOpenCloseListener listener) {
        mOnOpenCloseListener = listener;
    }

    public void setNavigationIcon(@DrawableRes int resource) {
        mImageViewArrow.setImageResource(resource);
    }

    public void setNavigationIcon(@Nullable Drawable drawable) {
        if (drawable == null) {
            mImageViewArrow.setVisibility(View.GONE);
        } else {
            mImageViewArrow.setImageDrawable(drawable);
        }
    }

    public void setNavigationIconClickListener(OnClickListener listener) {
        mImageViewArrow.setOnClickListener(listener);
    }

    public void setOnNavigationIconClickListener(OnNavigationIconClickListener listener) {
        mOnNavigationIconClickListener = listener;
    }

    public void setNavigationIconAnimation(boolean animate) {
        if (animate) {
            if (mSearchArrowDrawable != null) {
                mSearchArrowDrawable.setVerticalMirror(false);
                mSearchArrowDrawable.animate(SearchArrowDrawable.STATE_ARROW, mAnimationDuration);
            }
        } else {
            mSearchArrowDrawable.setProgress(SearchArrowDrawable.STATE_ARROW);
        }

        mArrow = true; // todo init plus !animate
    }

    public void setVoiceIcon(@DrawableRes int resource) {
        mImageViewMic.setImageResource(resource);
    }

    public void setVoiceIcon(@Nullable Drawable drawable) {
        if (drawable == null) {
            mImageViewMic.setVisibility(View.GONE);
        } else {
            mImageViewMic.setImageDrawable(drawable);
        }
    }

    public void setVoiceIconClickListener(OnClickListener listener) {
        mImageViewMic.setOnClickListener(listener);
    }

    public void setOnVoiceIconClickListener(OnVoiceIconClickListener listener) {
        mOnVoiceIconClickListener = listener;
    }

    public void setVoice(boolean voice) {
        if (voice && isVoiceAvailable()) {
            mImageViewMic.setVisibility(View.VISIBLE);
        } else {
            mImageViewMic.setVisibility(View.GONE);
        }
        mVoice = voice;
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        mCardView.setCardBackgroundColor(color);
    }

    @Override
    public void setElevation(float elevation) {
        mCardView.setMaxCardElevation(elevation);
        mCardView.setCardElevation(elevation);
    }

    public void setSuggestionsList(List<SearchItem> suggestionsList) {
        if (mRecyclerViewAdapter instanceof SearchAdapter) {
            ((SearchAdapter) mRecyclerViewAdapter).setSuggestionsList(suggestionsList);
        }
    }

    /**
     * This method might not work.
     * http://stackoverflow.com/questions/11554078/set-textcursordrawable-programatically
     */
    public void setCursorDrawable(@DrawableRes int drawable) {
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            try {
                f.set(mSearchEditText, drawable);
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public boolean isOpen() {
        return getVisibility() == View.VISIBLE;
    }

    public boolean isShowingProgress() {
        return mProgressBar.getVisibility() == View.VISIBLE;
    }

    public void setShadow(boolean shadow) {
        if (shadow) {
            mViewShadow.setVisibility(View.VISIBLE);
        } else {
            mViewShadow.setVisibility(View.GONE);
        }
        mShadow = shadow;
    }

    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    // ---------------------------------------------------------------------------------------------
    private void restoreFiltersState(List<Boolean> states) {
        mSearchFiltersStates = states;
        for (int i = 0, j = 0, n = mFlexboxLayout.getChildCount(); i < n; i++) {
            View view = mFlexboxLayout.getChildAt(i);
            if (view instanceof CheckBox) {
                ((CheckBox) view).setChecked(mSearchFiltersStates.get(j++));
            }
        }
    }

    private void getMenuItemPosition(int menuItemId) {
        if (mMenuItemView != null) {
            mMenuItemCx = getCenterX(mMenuItemView);
        }
        ViewParent viewParent = getParent();
        while (viewParent != null && viewParent instanceof View) {
            View parent = (View) viewParent;
            View view = parent.findViewById(menuItemId);
            if (view != null) {
                mMenuItemView = view;
                mMenuItemCx = getCenterX(mMenuItemView);
                break;
            }
            viewParent = viewParent.getParent();
        }
    }

    private int getCenterX(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[0] + view.getWidth() / 2;
    }

    private void onVoiceClicked() {
        if (mOnVoiceIconClickListener != null) {
            mOnVoiceIconClickListener.onVoiceIconClick();
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, mVoiceText);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        if (mContext instanceof FragmentActivity) {
            ((FragmentActivity) mContext).startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } else if (mContext instanceof Activity) {
            ((Activity) mContext).startActivityForResult(intent, SPEECH_REQUEST_CODE);
        }
    }

    private boolean isVoiceAvailable() {
        if (isInEditMode()) {
            return true;
        }
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return activities.size() != 0;
    }

    private void onTextChanged(CharSequence newText) {
        mQuery = newText;

        if (mRecyclerViewAdapter != null && mRecyclerViewAdapter instanceof Filterable) {
            ((SearchAdapter) mRecyclerViewAdapter).getFilter().filter(newText);
        }

        if (!TextUtils.isEmpty(newText)) {
            mImageViewClear.setVisibility(View.VISIBLE);
            if (mVoice) {
                mImageViewMic.setVisibility(View.GONE);
            }
        } else {
            mImageViewClear.setVisibility(View.GONE);
            if (mVoice) {
                mImageViewMic.setVisibility(View.VISIBLE);
            }
        }

        if (mOnQueryTextListener != null) {
            dispatchFilters();
            mOnQueryTextListener.onQueryTextChange(newText.toString());
        }
    }

    private void setQueryWithoutSubmitting(CharSequence query) {
        mSearchEditText.setText(query);
        if (query != null) {
            mSearchEditText.setSelection(mSearchEditText.length());
            mQuery = query;
        } else {
            mSearchEditText.getText().clear();
        }
    }

    private void onSubmitQuery() {
        CharSequence query = mSearchEditText.getText();
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            dispatchFilters();
            if (mOnQueryTextListener == null || !mOnQueryTextListener.onQueryTextSubmit(query.toString())) {
                mSearchEditText.setText(query);
            }
        }
    }

    private void dispatchFilters() {
        if (mSearchFiltersStates != null) {
            for (int i = 0, j = 0, n = mFlexboxLayout.getChildCount(); i < n; i++) {
                View view = mFlexboxLayout.getChildAt(i);
                if (view instanceof CheckBox) {
                    boolean isChecked = ((CheckBox) view).isChecked();
                    mSearchFiltersStates.set(j, isChecked);
                    mSearchFilters.get(j).setChecked(isChecked);
                    j++;
                }
            }
        }
    }

    // todo?    // TODO Annotate
    private void setInfo() {
        mVoice = isVoiceAvailable();
        if (mVoice) {
            mSearchEditText.setPrivateImeOptions("nm");
        }
    }

    // ---------------------------------------------------------------------------------------------
    @Override
    public void onClick(View view) {
        if (view == mImageViewArrow) {
            if (mSearchArrowDrawable != null && mIsSearchArrowHamburgerState == SearchArrowDrawable.STATE_ARROW) {
                close(true);
            } else {
                if (mOnNavigationIconClickListener != null) {
                    mOnNavigationIconClickListener.onNavigationIconClick(mIsSearchArrowHamburgerState);
                }
            }
        } else if (view == mImageViewMic) {
            onVoiceClicked();
        } else if (view == mImageViewClear) {
            if (mSearchEditText.length() > 0) {
                mSearchEditText.getText().clear();
            }
        } else if (view == mViewShadow) {
            close(true);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.query = mQuery != null ? mQuery.toString() : null;
        ss.isSearchOpen = getVisibility() == View.VISIBLE;
        dispatchFilters();
        ss.searchFiltersStates = mSearchFiltersStates;
        ss.searchFilters = mSearchFilters;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        if (ss.isSearchOpen) {
            open(true);
            setQueryWithoutSubmitting(ss.query);
            mSearchEditText.requestFocus();
        }

        restoreFiltersState(ss.searchFiltersStates);
        mSearchFilters = ss.searchFilters;
        super.onRestoreInstanceState(ss.getSuperState());
        requestLayout();// todo
    }

    // ---------------------------------------------------------------------------------------------
    @IntDef({Version.TOOLBAR, Version.MENU_ITEM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Version {
        int TOOLBAR = 1000;
        int MENU_ITEM = 1001;
    }

    @IntDef({VersionMargins.TOOLBAR_SMALL, VersionMargins.TOOLBAR_BIG, VersionMargins.MENU_ITEM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface VersionMargins {
        int TOOLBAR_SMALL = 2000;
        int TOOLBAR_BIG = 2001;
        int MENU_ITEM = 2002;
    }

    @IntDef({Theme.LIGHT, Theme.DARK, Theme.PLAY_STORE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Theme {
        int LIGHT = 3000;
        int DARK = 3001;
        int PLAY_STORE = 3002;
    }

    @IntDef({TextStyle.NORMAL, TextStyle.BOLD, TextStyle.ITALIC, TextStyle.BOLD_ITALIC})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TextStyle {
        int NORMAL = 0;
        int BOLD = 1;
        int ITALIC = 2;
        int BOLD_ITALIC = 3;
    }

    // ---------------------------------------------------------------------------------------------
    public interface OnQueryTextListener {
        boolean onQueryTextChange(String newText);

        boolean onQueryTextSubmit(String query);
    }

    public interface OnOpenCloseListener {
        boolean onClose();

        boolean onOpen();
    }

    public interface OnNavigationIconClickListener {
        void onNavigationIconClick(float state);
    }

    public interface OnVoiceIconClickListener {
        void onVoiceIconClick();
    }

    // ---------------------------------------------------------------------------------------------
    private static class SavedState extends BaseSavedState {

        public String query;
        public boolean isSearchOpen;
        public List<Boolean> searchFiltersStates;
        public List<SearchFilter> searchFilters;

        public SavedState(Parcel source) {
            super(source);
            this.query = source.readString();
            this.isSearchOpen = source.readInt() == 1;
            searchFiltersStates = new ArrayList<>();
            searchFilters = new ArrayList<>();
            source.readList(searchFiltersStates, List.class.getClassLoader());
            source.readTypedList(searchFilters, SearchFilter.CREATOR);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @RequiresApi(api = Build.VERSION_CODES.N)
        public SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            this.query = source.readString();
            this.isSearchOpen = source.readInt() == 1;
            searchFiltersStates = new ArrayList<>();
            searchFilters = new ArrayList<>();
            source.readList(searchFiltersStates, List.class.getClassLoader());
            source.readTypedList(searchFilters, SearchFilter.CREATOR);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

    }

}
