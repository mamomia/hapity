package com.app.hopity.appIntro;

/**
 * Created by Mushi on 2/23/2016.
 */

import android.content.Intent;
import android.os.Bundle;

import com.app.hopity.LoginActivity;
import com.app.hopity.R;
import com.github.paolorotolo.appintro.AppIntro2;

public class HapityIntro extends AppIntro2 {
    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(ModelSlide.newInstance(R.layout.intro));
        addSlide(ModelSlide.newInstance(R.layout.intro2));
    }

    private void loadMainActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDonePressed() {
        loadMainActivity();
    }
}
