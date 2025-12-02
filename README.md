	        implementation 'com.github.alphatech-apps:FastScroll:Tag'

[![](https://jitpack.io/v/alphatech-apps/FastScroll.svg)](https://jitpack.io/#alphatech-apps/FastScroll)



/**
 * FastScroller
 * <p>
 * - Attach to any RecyclerView (must use LinearLayoutManager)
 * - Works with common parent layouts (FrameLayout, LinearLayout, RelativeLayout, ConstraintLayout).
 * - Optional parameters (pass null to use defaults / theme values):
 * allWidthDp, marginFromEndDp, normalColor, activeColor, trackColor, swipeRefreshLayout
 * <p>
 * Usage:
 * FastScroller.attach(recyclerView); // defaults if layout has not swipeRefreshLayout
 * FastScroller.attach(recyclerView, swipeRefreshLayout);  // defaults if layout has swipeRefreshLayout
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
 * <p>
 * FastScroller.attach(recyclerView);
 * FastScroller.attach(recyclerView, null, null, null, null, null);
 * FastScroller.attach(recyclerView, swipeRefreshLayout);
 * FastScroller.attach(recyclerView, null, null, null, null, null, swipeRefreshLayout);
 * FastScroller.attach(recyclerView, null, null, null, null, Color.TRANSPARENT, swipeRefreshLayout);
 * <p>
 * FastScroller.attach(recyclerView, null, null, null, null, 0x00000000);
 * FastScroller.attach(recyclerView, null, null, null, null, Color.TRANSPARENT);
 * FastScroller.attach(recyclerView, null, null, Color.rgb(0,0,250), null, Color.parseColor("#00000000"));
 * FastScroller.attach(recyclerView, null, null, Color.rgb(255,0,0), null, null);
 * FastScroller.attach(recyclerView, 10, null, null, null, 0x00000000);
 * FastScroller.attach(recyclerView, null, null, null, null, null);
 */
