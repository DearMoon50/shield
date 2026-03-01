package com.dearmoon.shield.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.dearmoon.shield.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        
        viewPager.setAdapter(new DashboardPagerAdapter(this));
        
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Overview"); break;
                case 1: tab.setText("Events"); break;
                case 2: tab.setText("Pipeline"); break;
            }
        }).attach();
    }
    
    private static class DashboardPagerAdapter extends FragmentStateAdapter {
        public DashboardPagerAdapter(FragmentActivity fa) {
            super(fa);
        }
        
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new OverviewFragment();
                case 1: return new EventStreamFragment();
                case 2: return new DetectionPipelineFragment();
                default: return new OverviewFragment();
            }
        }
        
        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
