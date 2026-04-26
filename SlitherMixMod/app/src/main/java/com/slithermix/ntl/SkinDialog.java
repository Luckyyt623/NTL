package com.slithermix.ntl;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

/**
 * SkinDialog - Full skin customization UI
 * Lets players choose color hues, pattern, and accessories
 */
public class SkinDialog extends Dialog {

    public interface OnSkinAppliedListener {
        void onSkinApplied(int hue1, int hue2, int pattern, String accessory);
    }

    private OnSkinAppliedListener listener;
    private int hue1, hue2, pattern;
    private String accessory;
    private Context ctx;

    // UI refs
    private View color1Preview, color2Preview;
    private SeekBar hue1Slider, hue2Slider;
    private GridView skinGrid;
    private LinearLayout accessoryRow;

    // Available patterns (NTL mod has 60 patterns t_0 to t_59)
    private static final int PATTERN_COUNT = 60;

    // Available accessories from NTL mod
    private static final String[] ACCESSORIES = {
        "none", "a_none", "a_bat", "a_bear", "a_blonde", "a_blackhair",
        "a_angel", "a_antlers", "a_cap", "a_cape", "a_cat", "a_catglass",
        "a_crown", "a_deerstalker", "a_disguise", "a_dragon", "a_dreadlocks",
        "a_eyebrows", "a_funkystar", "a_ginger", "a_graduation", "a_hardhat",
        "a_headphones", "a_heartglass", "a_monocle", "a_mohawk", "a_nerdglass",
        "a_oakley", "a_rabbit", "a_spikecollar", "a_swirly", "a_3dglass",
        "a_unicorn", "a_visor"
    };

    private static final String[] ACCESSORY_EMOJIS = {
        "🚫","👻","🦇","🐻","👱","🖤","😇","🦌","🧢","🦸","🐱","😎",
        "👑","🕵","🥸","🐉","🪢","🤨","⭐","🧑‍🦰","🎓","👷","🎧",
        "❤️","🧐","💜","🤓","🕶️","🐰","💢","🌀","🥽","🦄","😎"
    };

    public SkinDialog(Context context, int hue1, int hue2, int pattern, String accessory) {
        super(context);
        this.ctx = context;
        this.hue1 = hue1;
        this.hue2 = hue2;
        this.pattern = pattern;
        this.accessory = accessory;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_skin);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setTitle("Choose Your Skin");

        color1Preview = findViewById(R.id.color1_preview);
        color2Preview = findViewById(R.id.color2_preview);
        hue1Slider = findViewById(R.id.hue1_slider);
        hue2Slider = findViewById(R.id.hue2_slider);
        skinGrid = findViewById(R.id.skin_grid);
        accessoryRow = findViewById(R.id.accessory_row);
        Button applyBtn = findViewById(R.id.btn_apply);
        Button randomBtn = findViewById(R.id.btn_random);

        // Init slider values
        hue1Slider.setProgress(hue1);
        hue2Slider.setProgress(hue2);
        updateColorPreviews();

        hue1Slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                hue1 = progress;
                updateColorPreviews();
            }
            public void onStartTrackingTouch(SeekBar sb) {}
            public void onStopTrackingTouch(SeekBar sb) {}
        });

        hue2Slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                hue2 = progress;
                updateColorPreviews();
            }
            public void onStartTrackingTouch(SeekBar sb) {}
            public void onStopTrackingTouch(SeekBar sb) {}
        });

        // Pattern grid
        skinGrid.setAdapter(new PatternAdapter());
        skinGrid.setOnItemClickListener((parent, view, position, id) -> {
            pattern = position;
            ((PatternAdapter) skinGrid.getAdapter()).notifyDataSetChanged();
            Toast.makeText(ctx, "Pattern " + position + " selected", Toast.LENGTH_SHORT).show();
        });

        // Accessory row
        buildAccessoryRow();

        // Buttons
        applyBtn.setOnClickListener(v -> {
            if (listener != null) listener.onSkinApplied(hue1, hue2, pattern, accessory);
            dismiss();
        });

        randomBtn.setOnClickListener(v -> {
            hue1 = (int)(Math.random() * 360);
            hue2 = (int)(Math.random() * 360);
            pattern = (int)(Math.random() * PATTERN_COUNT);
            accessory = ACCESSORIES[(int)(Math.random() * ACCESSORIES.length)];
            hue1Slider.setProgress(hue1);
            hue2Slider.setProgress(hue2);
            updateColorPreviews();
            ((BaseAdapter) skinGrid.getAdapter()).notifyDataSetChanged();
            buildAccessoryRow();
            Toast.makeText(ctx, "🎲 Random skin!", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateColorPreviews() {
        color1Preview.setBackgroundColor(hsvToRgb(hue1, 0.85f, 0.95f));
        color2Preview.setBackgroundColor(hsvToRgb(hue2, 0.85f, 0.95f));
    }

    private int hsvToRgb(float hue, float sat, float val) {
        float[] hsv = {hue, sat, val};
        return Color.HSVToColor(hsv);
    }

    private void buildAccessoryRow() {
        accessoryRow.removeAllViews();
        for (int i = 0; i < ACCESSORIES.length; i++) {
            final int idx = i;
            final String acc = ACCESSORIES[i];

            View item = new View(ctx);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(52, 52);
            lp.setMargins(4, 0, 4, 0);

            // Use emoji button since we don't have actual accessory images in dialog
            android.widget.TextView tv = new android.widget.TextView(ctx);
            tv.setLayoutParams(lp);
            tv.setText(idx < ACCESSORY_EMOJIS.length ? ACCESSORY_EMOJIS[idx] : "?");
            tv.setTextSize(20f);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setPadding(4, 4, 4, 4);

            boolean selected = acc.equals(accessory);
            tv.setBackgroundColor(selected ?
                Color.argb(180, 0, 170, 80) :
                Color.argb(80, 255, 255, 255));

            tv.setOnClickListener(v -> {
                accessory = acc;
                buildAccessoryRow(); // Refresh selection highlight
                Toast.makeText(ctx,
                    "Accessory: " + acc.replace("a_", ""),
                    Toast.LENGTH_SHORT).show();
            });

            accessoryRow.addView(tv);
        }
    }

    public void setOnSkinAppliedListener(OnSkinAppliedListener l) {
        this.listener = l;
    }

    // ---- Pattern Grid Adapter ----
    private class PatternAdapter extends BaseAdapter {
        @Override
        public int getCount() { return PATTERN_COUNT; }

        @Override
        public Object getItem(int position) { return position; }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            android.widget.TextView tv;
            if (convertView == null) {
                tv = new android.widget.TextView(ctx);
                tv.setLayoutParams(new GridView.LayoutParams(44, 44));
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setTextSize(10f);
                tv.setTextColor(Color.WHITE);
            } else {
                tv = (android.widget.TextView) convertView;
            }

            // Show pattern number and color-coded background
            tv.setText(String.valueOf(position));
            boolean selected = position == pattern;

            // Color based on pattern number (visual hint)
            float hue = (position * 360f / PATTERN_COUNT);
            int bg = Color.HSVToColor(selected ? 200 : 120,
                new float[]{hue, 0.7f, selected ? 0.9f : 0.5f});
            tv.setBackgroundColor(bg);

            if (selected) {
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tv.setTextColor(Color.argb(200, 255, 255, 255));
                tv.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            return tv;
        }
    }
}
