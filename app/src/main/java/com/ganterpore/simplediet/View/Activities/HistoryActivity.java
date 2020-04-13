package com.ganterpore.simplediet.View.Activities;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Controller.WeeklyIntake;
import com.ganterpore.simplediet.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class HistoryActivity extends AppCompatActivity {
    DietController dietController;
    WeeklyIntake week1;
    WeeklyIntake week2;
    WeeklyIntake week3;
    WeeklyIntake week4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dietController = BasicDietController.getInstance();
        week1 = dietController.getWeeksIntake(0);
        week2 = dietController.getWeeksIntake(1);
        week3 = dietController.getWeeksIntake(2);
        week4 = dietController.getWeeksIntake(3);
        setView();
    }

    private void setView() {
        final int SCALE_FACTOR = 100; //how much to scale the progress bars by (to allow more granularity)
        NumberFormat df = new DecimalFormat("##.##"); //format to show all decimal strings
        View monthlyContainer = findViewById(R.id.monthly_container);

        //get the text views from the main activity
        TextView monthlyVegTV = findViewById(R.id.monthly_veges_intake);
        TextView monthlyProteinTV = findViewById(R.id.monthly_protein_intake);
        TextView monthlyDairyTV = findViewById(R.id.monthly_dairy_intake);
        TextView monthlyGrainTV = findViewById(R.id.monthly_grain_intake);
        TextView monthlyFruitTV = findViewById(R.id.monthly_fruit_intake);
        TextView monthlyWaterTV = findViewById(R.id.monthly_water_intake);
        TextView monthlyCaffeineTV = findViewById(R.id.monthly_caffeine_count);
        TextView monthlyAlcoholTV = findViewById(R.id.monthly_alcohol_count);
        TextView monthlyCheatTV = findViewById(R.id.monthly_cheat_count);

        //get the progress bars from the main activity
        ProgressBar monthlyCheatsPB = monthlyContainer.findViewById(R.id.progress_cheats);

        //creating arrays of the text views to update
        TextView[] textViewsCount = {monthlyVegTV, monthlyProteinTV, monthlyDairyTV, monthlyGrainTV, monthlyFruitTV, monthlyWaterTV,
                monthlyCaffeineTV, monthlyAlcoholTV, monthlyCheatTV};
//       monthlyCheatsPB};
        double[] counts = {week1.getVegCount()+week2.getVegCount()+week3.getVegCount()+week4.getVegCount(),
                        week1.getProteinCount()+week2.getProteinCount()+week3.getProteinCount()+week4.getProteinCount(),
                        week1.getDairyCount()+week2.getDairyCount()+week3.getDairyCount()+week4.getDairyCount(),
                        week1.getGrainCount()+week2.getGrainCount()+week3.getGrainCount()+week4.getGrainCount(),
                        week1.getFruitCount()+week2.getFruitCount()+week3.getFruitCount()+week4.getFruitCount(),
                        week1.getWaterCount()+week2.getWaterCount()+week3.getWaterCount()+week4.getWaterCount(),
                        week1.getCaffieneCount()+week2.getCaffieneCount()+week3.getCaffieneCount()+week4.getCaffieneCount(),
                        week1.getAlcoholCount()+week2.getAlcoholCount()+week3.getAlcoholCount()+week4.getAlcoholCount(),
                        week1.getTotalCheats()+week2.getTotalCheats()+week3.getTotalCheats()+week4.getTotalCheats()};
        double[] plans = {week1.getWeeklyLimitVeg()+week2.getWeeklyLimitVeg()+week3.getWeeklyLimitVeg()+week4.getWeeklyLimitVeg(),
                        week1.getWeeklyLimitProtein()+week2.getWeeklyLimitProtein()+week3.getWeeklyLimitProtein()+week4.getWeeklyLimitProtein(),
                        week1.getWeeklyLimitDairy()+week2.getWeeklyLimitDairy()+week3.getWeeklyLimitDairy()+week4.getWeeklyLimitDairy(),
                        week1.getWeeklyLimitGrain()+week2.getWeeklyLimitGrain()+week3.getWeeklyLimitGrain()+week4.getWeeklyLimitGrain(),
                        week1.getWeeklyLimitFruit()+week2.getWeeklyLimitFruit()+week3.getWeeklyLimitFruit()+week4.getWeeklyLimitFruit(),
                        week1.getWeeklyLimitHydration()+week2.getWeeklyLimitHydration()+week3.getWeeklyLimitHydration()+week4.getWeeklyLimitHydration(),
                        week1.getWeeklyLimitCaffiene()+week2.getWeeklyLimitCaffiene()+week3.getWeeklyLimitCaffiene()+week4.getWeeklyLimitCaffiene(),
                        week1.getWeeklyLimitAlcohol()+week2.getWeeklyLimitAlcohol()+week3.getWeeklyLimitAlcohol()+week4.getWeeklyLimitAlcohol(),
                        week1.getWeeklyLimitCheats()+week2.getWeeklyLimitCheats()+week3.getWeeklyLimitCheats()+week4.getWeeklyLimitCheats()};

        //updating text for all the main food groups
        for(int i=0;i<textViewsCount.length;i++) {
            //getting values from the arrays for this index
            TextView countTV = textViewsCount[i];
            double count = counts[i];
            double plan = plans[i];

            countTV.setText(String.format("%s/%s", df.format(count), df.format(plan)));
        }

        monthlyCheatsPB.setMax((int) (week1.getWeeklyLimitCheats() * 4 * SCALE_FACTOR));
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(monthlyCheatsPB, "progress",
                (int) (week1.getTotalCheats()+week2.getTotalCheats()+week3.getTotalCheats()+week4.getTotalCheats())*SCALE_FACTOR);
        objectAnimator.setDuration(500);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();

        //refreshing the other views
//        mealView.refreshRecommendations();
    }
}
