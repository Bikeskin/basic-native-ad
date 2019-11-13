package com.example.nativeadsampleproject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ArtsAdapter extends RecyclerView.Adapter {

    private static final int TYPE_AD = 0;
    private static final int TYPE_ITEM = 1;
    private static final String TAG = "Advertisement";

    private Context context;
    private List<Object> mFeedList;
    private static HashMap<Integer, UnifiedNativeAdView> nativeMapViewsByPosition;

    public ArtsAdapter(List<Object> feedList) {
        this.mFeedList = feedList;
        nativeMapViewsByPosition = new HashMap<>();
    }

    @Override
    public int getItemViewType(int position) {
        return (position % MainActivity.ITEMS_PER_AD == 0) ? TYPE_AD : TYPE_ITEM;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        switch (viewType) {
            case TYPE_ITEM:
                View view = View.inflate(parent.getContext(), R.layout.item_art, null);
                return new ArtViewHolder(view);
            case TYPE_AD:

            default:
                View adView = View.inflate(parent.getContext(), R.layout.item_native_ad_container, null);
                return new AdViewHolder(adView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);

        Log.e("position", "->" + position);

        switch (viewType) {
            case TYPE_ITEM:

                ArtViewHolder artViewHolder = (ArtViewHolder) holder;

                Artwork artwork = (Artwork) mFeedList.get(position);

                artViewHolder.dynamicHeightImageView.setHeightRatio(artwork.getAspectRatio());
                Glide.with(context)
                        .load(artwork.getWebImageUrl())
                        .apply(new RequestOptions().placeholder(R.color.dark_purple).error(R.color.dark_purple))
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(artViewHolder.dynamicHeightImageView);

                artViewHolder.textView.setText(artwork.getTitle());

                break;

            case TYPE_AD:

                AdViewHolder adViewHolder = (AdViewHolder) holder;

                showArticleNativeAds("/6499/example/native",context,adViewHolder.frameLayout,holder.getAdapterPosition());

            default:

                break;

        }

    }

    @Override
    public int getItemCount() {
        return (mFeedList != null ? mFeedList.size() : 0);
    }

    public void showArticleNativeAds(String id, final Context context, final FrameLayout mAdContainer, final int position) {


        if (id == null) {
            return;
        }

        if (mAdContainer.getChildCount() > 0) {
            mAdContainer.removeAllViews();
        }

        if (nativeMapViewsByPosition.containsKey(position)) {
            UnifiedNativeAdView unifiedNativeAdView = nativeMapViewsByPosition.get(position);
            if (unifiedNativeAdView != null && unifiedNativeAdView.getParent() != null) {
                ((ViewGroup) unifiedNativeAdView.getParent()).removeView(unifiedNativeAdView);
                Log.d(TAG, "showArticleNativeAds: unifiedNativeAdView.getParent().removeView(unifiedNativeAdView)");
            }
            mAdContainer.addView(unifiedNativeAdView);
            Log.d(TAG, "showArticleNativeAds: sepetten alındı " + position);
            return;
        }

        AdLoader.Builder adLoader = new AdLoader.Builder(context, id)
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {

                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        if (inflater != null) {

                            UnifiedNativeAdView adView;
                            if (unifiedNativeAd.getStore() != null) {
                                adView = (UnifiedNativeAdView) inflater.inflate(R.layout.item_native_ad_app, null);
                            } else {
                                adView = (UnifiedNativeAdView) inflater.inflate(R.layout.item_native_ad_standart, null);
                            }
                            populateAppNativeAdView(unifiedNativeAd, adView);

                            if (mAdContainer.getChildCount() > 0) {
                                mAdContainer.removeAllViews();
                            }

                            nativeMapViewsByPosition.put(position, adView);
                            mAdContainer.addView(adView);
                        }
                    }
                });

        adLoader.withNativeAdOptions(new NativeAdOptions.Builder()
                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_BOTTOM_LEFT)
                .build()
        );

        adLoader.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.d(TAG, "onAdFailedToLoad: " + position + " " + i);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "onAdLoaded: native başarılı " + position);
            }
        });

        adLoader.build().loadAd(new AdRequest.Builder()
                .addTestDevice("C9B7528A8B643EC50E959FEFCEA84AE4")
                .addTestDevice("B3EEABB8EE11C2BE770B684D95219ECB")
                .build());
    }

    private void populateAppNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        // Set the media view. Media content will be automatically populated in the media view once
        // adView.setNativeAd() is called.
        final MediaView mediaView = adView.findViewById(R.id.ad_media_view);
        adView.setMediaView(mediaView);

        if (nativeAd != null && nativeAd.getImages() != null && nativeAd.getImages().size() > 0) {
            if (nativeAd.getImages().get(0).getDrawable() != null) {
                float width = nativeAd.getImages().get(0).getDrawable().getIntrinsicWidth();
                float height = nativeAd.getImages().get(0).getDrawable().getIntrinsicHeight();

                if (width != 0 && height != 0) {
                    final float aspectRatio = width / height;

                    mediaView.post(new Runnable() {
                        @Override
                        public void run() {
                            ViewGroup.LayoutParams layoutParams = mediaView.getLayoutParams();
                            layoutParams.height = (int) (mediaView.getWidth() / aspectRatio);
                            mediaView.setLayoutParams(layoutParams);

                        }
                    });
                }
            }
        }

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (adView.getBodyView() != null) {
            if (nativeAd.getBody() != null) {
                ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
                adView.getBodyView().setVisibility(View.VISIBLE);
            } else {
                adView.getBodyView().setVisibility(View.INVISIBLE);
            }
        }

        if (adView.getIconView() != null) {
            if (nativeAd.getIcon() != null) {
                ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
                adView.getIconView().setVisibility(View.VISIBLE);
            } else {
                adView.getIconView().setVisibility(View.GONE);
            }
        }

        if (adView.getStarRatingView() != null) {
            if (nativeAd.getStarRating() != null) {
                ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
                adView.getStarRatingView().setVisibility(View.VISIBLE);
            } else {
                adView.getStarRatingView().setVisibility(View.INVISIBLE);
            }
        }

        if (adView.getAdvertiserView() != null) {
            if (nativeAd.getAdvertiser() != null) {
                ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
                adView.getAdvertiserView().setVisibility(View.VISIBLE);
            } else {
                adView.getAdvertiserView().setVisibility(View.GONE);
            }
        }

        adView.setNativeAd(nativeAd);
    }

    public class ArtViewHolder extends RecyclerView.ViewHolder {

        DynamicHeightImageView dynamicHeightImageView;
        TextView textView;

        public ArtViewHolder(View itemView) {
            super(itemView);
            dynamicHeightImageView = itemView.findViewById(R.id.artImage);
            textView = itemView.findViewById(R.id.artTextView);
        }
    }

    public class AdViewHolder extends RecyclerView.ViewHolder {

        FrameLayout frameLayout;

        public AdViewHolder(View itemView) {
            super(itemView);
            frameLayout = itemView.findViewById(R.id.fl_native_ad);
        }

    }

}
