package com.jakir.fastscroller;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * FastScroller
 * <p>
 * - Attach to any RecyclerView (must use LinearLayoutManager)
 * - Works with common parent layouts (FrameLayout, LinearLayout, RelativeLayout, ConstraintLayout).
 * - Optional parameters (pass null to use defaults / theme values):
 * allWidthDp, marginFromEndDp, normalColor, activeColor, trackColor
 * <p>
 * Usage:
 * FastScroller.attach(recyclerView); // defaults
 * <p>
 * Or customize:
 * ........................
 * FastScroller.attach(recyclerView,
 * 10,                  // allWidthDp
 * 8,                   // marginFromEndDp
 * 0xFF2196F3,          // normalColor
 * 0xFFFFFFFF,          // activeColor
 * null                 // trackColor (null -> derived from theme)
 * swipeRefreshLayout   // swipeRefreshLayout enable/disable
 * );
 * Or ....................

 * FastScroller.attach(recyclerView);
 * FastScroller.attach(recyclerView, null, null, null, null, null);
 * FastScroller.attach(recyclerView, swipeRefreshLayout);
 * FastScroller.attach(recyclerView, null, null, null, null, null, swipeRefreshLayout);
 * FastScroller.attach(recyclerView, null, null, null, null, Color.TRANSPARENT, swipeRefreshLayout);

 * FastScroller.attach(recyclerView, null, null, null, null, 0x00000000);
 * FastScroller.attach(recyclerView, null, null, null, null, Color.TRANSPARENT);
 * FastScroller.attach(recyclerView, null, null, Color.rgb(0,0,250), null, Color.parseColor("#00000000"));
 * FastScroller.attach(recyclerView, null, null, Color.rgb(255,0,0), null, null);
 * FastScroller.attach(recyclerView, 10, null, null, null, 0x00000000);
 * FastScroller.attach(recyclerView, null, null, null, null, null);
 */

/**
 * Created by JAKIR HOSSAIN on 11/22/2025.
 **************************************************************************************************************************************************************/
public class FastScroller {

    private static final String TAG_TRACK = "fs_track";
    private static final String TAG_THUMB = "fs_thumb";
    private static final String TAG_TOUCH = "fs_touch";
    // smoothing
    private final float smoothFactor = 0.25f; // position smoothing
    private final float heightSmoothFactor = 0.20f; // height smoothing
    // recycler + layout
    private final RecyclerView recyclerView;
    private final LinearLayoutManager layoutManager;
    // handler
    private final Handler hideHandler = new Handler();
    // animation defaults
    private final long animDuration = 200L;
    private final long hideDelay = 2000L;
    private final int minimumThumbHeightDp = 70;    // min thumb height (dp)
    private final int touchAreaWidthDp = 20;        // touch area width (dp)
    private final float extraTouchAreaHeight = 100; // touch area height (dp)
    private int allWidthDp = 7;                     // thumb & track visual width (dp)
    private int marginFromEndDp = 6;                // margin from end (dp)
    private int normalColor;
    private int activeColor;
    private int trackColor;
    // views
    private View track;
    private View thumb;
    private View touchArea;
    private GradientDrawable thumbDrawable;
    // state
    private float touchDownY;
    private float thumbDownY;
    private boolean isVisible = false;
    private Runnable hideRunnable;
    private float lastY = 0f;
    private float lastHeight = 0f;
    private boolean firstCall = true;


    // -------------------- Public attach helpers --------------------
    private FastScroller(RecyclerView rv, Integer pAllWidthDp, Integer pMarginFromEndDp, Integer pNormalColor, Integer pActiveColor, Integer pTrackColor, androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout) {
        this.recyclerView = rv;

        if (!(rv.getLayoutManager() instanceof LinearLayoutManager)) {
            throw new IllegalStateException("FastScroller: RecyclerView must use LinearLayoutManager");
        }
        layoutManager = (LinearLayoutManager) rv.getLayoutManager();

        // apply optional overrides
        if (pAllWidthDp != null) this.allWidthDp = pAllWidthDp;
        if (pMarginFromEndDp != null) this.marginFromEndDp = pMarginFromEndDp;

        // initialize colors (nullable user-provided allowed)
        initColors(rv.getContext(), pNormalColor, pActiveColor, pTrackColor);

        // create views

        createTrack(rv.getContext());
        createThumb(rv.getContext());
        createTouchArea(rv.getContext());

        track.setTag(TAG_TRACK);
        thumb.setTag(TAG_THUMB);
        touchArea.setTag(TAG_TOUCH);

        // listeners
        setupScrollListener(rv.getContext());

        setupTouchAreaDrag(swipeRefreshLayout);

        // start hidden
        hideThumbImmediately();
    }

    // -------------------- Constructor --------------------

    /**
     * Attach with optional parameters. Pass null to use default/theme.
     *
     * @param recyclerView       target RecyclerView (must have LinearLayoutManager)
     * @param allWidthDp         width (dp) for track & thumb visual (nullable)
     * @param marginFromEndDp    margin-end (dp) from parent edge (nullable)
     * @param normalColor        color int for thumb normal state (nullable -> theme)
     * @param activeColor        color int for thumb active/touch state (nullable -> theme)
     * @param trackColor         color int for track (nullable -> derived from theme)
     * @param swipeRefreshLayout enable/disable swipeRefreshLayout by dragging thumb
     */
    public static void attach(RecyclerView recyclerView, Integer allWidthDp, Integer marginFromEndDp, Integer normalColor, Integer activeColor, Integer trackColor, androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout) {
        removeExistingFastScroller(recyclerView);
        new FastScroller(recyclerView, allWidthDp, marginFromEndDp, normalColor, activeColor, trackColor, swipeRefreshLayout);
    }

    public static void attach(RecyclerView recyclerView, Integer allWidthDp, Integer marginFromEndDp, Integer normalColor, Integer activeColor, Integer trackColor) {
        removeExistingFastScroller(recyclerView);
        new FastScroller(recyclerView, allWidthDp, marginFromEndDp, normalColor, activeColor, trackColor, null);
    }

    public static void attach(RecyclerView recyclerView, androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout) {
        attach(recyclerView, null, null, null, null, null, swipeRefreshLayout);
    }

    public static void attach(RecyclerView recyclerView) {
        attach(recyclerView, null, null, null, null, null, null);
    }

    // -------------------- remove Existing Fast Scroller--------------------
    private static void removeExistingFastScroller(RecyclerView rv) {
        ViewGroup parent = (ViewGroup) rv.getParent();
        if (parent == null) return;

        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            View child = parent.getChildAt(i);
            Object tag = child.getTag();
            if (TAG_TRACK.equals(tag) || TAG_THUMB.equals(tag) || TAG_TOUCH.equals(tag)) {
                parent.removeView(child);
            }
        }
    }

    // -------------------- Setup color --------------------
    private void initColors(Context context, Integer userNormal, Integer userActive, Integer userTrack) {
        TypedValue tv = new TypedValue();

        // normalColor -> user or theme colorPrimaryVariant (Material)
        if (userNormal != null) {
            normalColor = userNormal;
        } else {
            if (context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryVariant, tv, true)) {
                normalColor = tv.data;
            } else if (context.getTheme().resolveAttribute(android.R.attr.colorPrimary, tv, true)) {
                normalColor = tv.data;
            } else {
                normalColor = 0xFF666666;
            }
        }

        // activeColor -> user or theme colorSecondary / colorOnPrimary fallback
        if (userActive != null) {
            activeColor = userActive;
        } else {
            if (context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, tv, true)) {
                activeColor = tv.data;
            } else if (context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, tv, true)) {
                activeColor = tv.data;
            } else {
                activeColor = 0xFFFF0000;
            }
        }

        // trackColor -> user or primary with alpha (80% visible -> 20% transparent? you earlier asked 80% alpha)
        if (userTrack != null) {
            trackColor = userTrack;
        } else {
            int base;
            if (userNormal != null) {
                base = userNormal;
            } else if (context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryVariant, tv, true)) {
                base = tv.data;
            } else if (context.getTheme().resolveAttribute(android.R.attr.colorPrimary, tv, true)) {
                base = tv.data;
            } else {
                base = normalColor;
            }
            // Set alpha to 20% (i.e. 20% opaque). If you want 80% opacity, change value.
            trackColor = ColorUtils.setAlphaComponent(base, (int) (255 * 0.20f));
        }
    }

    // -------------------- View creation --------------------
    private void createTrack(Context context) {
        ViewGroup container = (ViewGroup) recyclerView.getParent();
        track = new View(context);
        track.setBackgroundColor(trackColor);

        ViewGroup.LayoutParams lp = generateLayoutParams(container, allWidthDp, MATCH_PARENT, marginFromEndDp);
        track.setLayoutParams(lp);

        container.addView(track);
        fixLinearLayoutPosition(track);
    }

    private void createThumb(Context context) {
        ViewGroup container = (ViewGroup) recyclerView.getParent();

        thumb = new View(context);
        thumbDrawable = new GradientDrawable();
        thumbDrawable.setColor(normalColor);
        thumbDrawable.setCornerRadius(dpToPx(context, 50));
        thumb.setBackground(thumbDrawable);

        ViewGroup.LayoutParams lp = generateLayoutParams(container, allWidthDp, minimumThumbHeightDp, marginFromEndDp);
        thumb.setLayoutParams(lp);

        container.addView(thumb);
        fixLinearLayoutPosition(thumb);
    }

    private void createTouchArea(Context context) {
        ViewGroup container = (ViewGroup) recyclerView.getParent();

        touchArea = new View(context);
        touchArea.setBackgroundColor(0x00000000); // invisible by default

        // initial height same as minimum thumb height
        ViewGroup.LayoutParams lp = generateLayoutParams(container, touchAreaWidthDp, minimumThumbHeightDp, 0);
        touchArea.setLayoutParams(lp);

        container.addView(touchArea);
        fixLinearLayoutPosition(touchArea);
    }

    // -------------------- Layout params helper (multi-parent safe) --------------------

    private ViewGroup.LayoutParams generateLayoutParams(View parent, int widthDp, int heightDp, int marginFromEndDp) {
        int widthPx = (widthDp == MATCH_PARENT) ? MATCH_PARENT : dpToPx(parent.getContext(), widthDp);
        int heightPx = (heightDp == MATCH_PARENT) ? MATCH_PARENT : dpToPx(parent.getContext(), heightDp);
        int endMarginPx = dpToPx(parent.getContext(), marginFromEndDp);

        if (parent instanceof FrameLayout) {
            FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(widthPx, heightPx);
            fl.gravity = Gravity.END;
            fl.setMarginEnd(endMarginPx);
            return fl;
        } else if (parent instanceof RelativeLayout) {
            RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(widthPx, heightPx);
            rl.addRule(RelativeLayout.ALIGN_PARENT_END);
            rl.setMarginEnd(endMarginPx);
            return rl;
        } else if (parent instanceof ConstraintLayout) {
            ConstraintLayout.LayoutParams cl = new ConstraintLayout.LayoutParams(widthPx, heightPx);
            cl.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            cl.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            cl.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            cl.setMarginEnd(endMarginPx);
            return cl;
        } else if (parent instanceof LinearLayout) {
            LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(widthPx, heightPx);
            // gravity on child in LinearLayout is relative to parent's orientation.
            // We set margin end; final X-position will be corrected via translation for reliable right-alignment.
            ll.setMarginEnd(endMarginPx);
            return ll;
        } else {
            ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(widthPx, heightPx);
            mlp.setMarginEnd(endMarginPx);
            return mlp;
        }
    }

    /**
     * LinearLayout children cannot be simply right-aligned with gravity unless the LinearLayout is horizontal/uses gravity.
     * To guarantee right-edge placement in any LinearLayout parent, we translate the child to the parent's end.
     */
    private void fixLinearLayoutPosition(final View child) {
        ViewGroup parent = (ViewGroup) recyclerView.getParent();
        if (!(parent instanceof LinearLayout)) return;

        // Post to ensure parent width is available after layout
        parent.post(() -> {
            int parentWidth = parent.getWidth();
            if (parentWidth == 0) return;

            int endMarginPx = dpToPx(parent.getContext(), marginFromEndDp);
            int childWidth = child.getLayoutParams().width;
            if (childWidth == MATCH_PARENT) {
                // nothing to do
                return;
            }

            float targetX = parentWidth - endMarginPx - childWidth;
            if (targetX < 0) targetX = 0;
            child.setTranslationX(targetX);
        });
    }

    // -------------------- Helpers --------------------
    private int dpToPx(Context ctx, int dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density);
    }

    // -------------------- Scroll listener --------------------
    private void setupScrollListener(Context context) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);

                showThumb();

                int visible = layoutManager.getChildCount();
                int total = layoutManager.getItemCount();
                if (total == 0) return;

                // ---------------- WIDTH LOGIC ----------------
                if (visible < total) {

                    int baseWidth = dpToPx(context, allWidthDp);
                    int halfWidth = dpToPx(context, allWidthDp / 2);

                    int targetWidth;
                    if (visible * 4 > total) {
                        targetWidth = halfWidth;   // half width until visible x 4 is reached
                    } else {
                        targetWidth = baseWidth;   // full width
                    }

                    // thumb width
                    ViewGroup.LayoutParams tLp = thumb.getLayoutParams();
                    tLp.width = targetWidth;
                    thumb.setLayoutParams(tLp);

                    // track width
                    ViewGroup.LayoutParams trLp = track.getLayoutParams();
                    trLp.width = targetWidth;
                    track.setLayoutParams(trLp);
                } else {
                    // no scrolling possible → hide
                    track.setAlpha(0f);
                    thumb.setAlpha(0f);
                    touchArea.setAlpha(0f);
                    return;
                }

                // ---------------- HEIGHT + POSITION LOGIC (unchanged) ----------------
                int containerHeight = rv.getHeight();
                int targetHeightPx = Math.max(dpToPx(rv.getContext(), minimumThumbHeightDp), (int) ((float) visible / total * containerHeight));

                int first = layoutManager.findFirstVisibleItemPosition();
                float scrollRatio = (float) first / (total - visible);
                float targetY = scrollRatio * (containerHeight - targetHeightPx);

                lastY += (targetY - lastY) * smoothFactor;
                lastHeight += (targetHeightPx - lastHeight) * heightSmoothFactor;

                // apply
                ViewGroup.LayoutParams lpThumb = thumb.getLayoutParams();
                lpThumb.height = (int) lastHeight;
                thumb.setLayoutParams(lpThumb);
                thumb.setY(lastY);

                // touchArea
                ViewGroup.LayoutParams lpTouch = touchArea.getLayoutParams();
                lpTouch.height = (int) lastHeight + (int) extraTouchAreaHeight;
                touchArea.setLayoutParams(lpTouch);
                touchArea.setY(lastY - (extraTouchAreaHeight / 2));
            }

        });
    }

    // -------------------- Touch/drag on touchArea --------------------
    private void setupTouchAreaDrag(SwipeRefreshLayout swipeRefreshLayout) {
        touchArea.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    touchDownY = event.getRawY();
                    thumbDownY = thumb.getY();
                    showThumb();
                    if (thumbDrawable != null) thumbDrawable.setColor(activeColor);
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setEnabled(false);
                    return true;

                case MotionEvent.ACTION_MOVE: {
                    float dy = event.getRawY() - touchDownY;
                    float newY = thumbDownY + dy;

                    int containerHeight = recyclerView.getHeight();
                    int thumbHeight = thumb.getHeight();

                    if (newY < 0) newY = 0;
                    if (newY > containerHeight - thumbHeight) newY = containerHeight - thumbHeight;


                    // compute target position and scroll recycler
                    int total = layoutManager.getItemCount();
                    int visible = layoutManager.getChildCount();
                    float scrollRatio = newY / (containerHeight - thumbHeight);
                    int targetPos = (int) (scrollRatio * (total - visible));
                    if (targetPos < 0) targetPos = 0;

                    layoutManager.scrollToPositionWithOffset(targetPos, 0);

                    // update thumb & touchArea
                    thumb.setY(newY);
                    touchArea.setY(newY - (extraTouchAreaHeight / 2));
                    return true;
                }

                case MotionEvent.ACTION_UP:
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setEnabled(true);
                case MotionEvent.ACTION_CANCEL:
                    if (thumbDrawable != null) thumbDrawable.setColor(normalColor);
                    scheduleHide();
                    return true;
            }
            return false;
        });
    }

    // -------------------- Show / Hide --------------------
    private void hideThumbImmediately() {
        isVisible = false;

        // move out to end and hide alpha
        thumb.setTranslationX(thumb.getWidth());
        thumb.setAlpha(0f);

        track.setTranslationX(track.getWidth());
        track.setAlpha(0f);

        touchArea.setTranslationX(touchArea.getWidth());
        touchArea.setAlpha(0f);
    }

    private void showThumb() {
        // first-call behavior: keep hidden (avoid flicker on initial layout)
        if (firstCall) {
            hideThumbImmediately();
            firstCall = false;
            return;
        }

        if (!isVisible) {
            isVisible = true;

            AnimatorSet set = new AnimatorSet();

            ObjectAnimator animXTrack = ObjectAnimator.ofFloat(track, "translationX", track.getWidth(), 0f);
            ObjectAnimator animAlphaTrack = ObjectAnimator.ofFloat(track, "alpha", 0f, 1f);

            ObjectAnimator animXThumb = ObjectAnimator.ofFloat(thumb, "translationX", thumb.getWidth(), 0f);
            ObjectAnimator animAlphaThumb = ObjectAnimator.ofFloat(thumb, "alpha", 0f, 1f);

            ObjectAnimator animXTouch = ObjectAnimator.ofFloat(touchArea, "translationX", touchArea.getWidth(), 0f);
            ObjectAnimator animAlphaTouch = ObjectAnimator.ofFloat(touchArea, "alpha", 0f, 1f);

            set.playTogether(animXTrack, animAlphaTrack, animXThumb, animAlphaThumb, animXTouch, animAlphaTouch);
            set.setDuration(animDuration);
            set.start();
        }

        scheduleHide();
    }

    private void scheduleHide() {
        if (hideRunnable != null) hideHandler.removeCallbacks(hideRunnable);

        hideRunnable = () -> {
            AnimatorSet set = new AnimatorSet();

            ObjectAnimator animXTrack = ObjectAnimator.ofFloat(track, "translationX", 0f, track.getWidth());
            ObjectAnimator animAlphaTrack = ObjectAnimator.ofFloat(track, "alpha", 1f, 0f);

            ObjectAnimator animXThumb = ObjectAnimator.ofFloat(thumb, "translationX", 0f, thumb.getWidth());
            ObjectAnimator animAlphaThumb = ObjectAnimator.ofFloat(thumb, "alpha", 1f, 0f);

            ObjectAnimator animXTouch = ObjectAnimator.ofFloat(touchArea, "translationX", 0f, touchArea.getWidth());
            ObjectAnimator animAlphaTouch = ObjectAnimator.ofFloat(touchArea, "alpha", 1f, 0f);

            set.playTogether(animXTrack, animAlphaTrack, animXThumb, animAlphaThumb, animXTouch, animAlphaTouch);
            set.setDuration(animDuration);
            set.start();

            isVisible = false;
        };

        hideHandler.postDelayed(hideRunnable, hideDelay);
    }
}
