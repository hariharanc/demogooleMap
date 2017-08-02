package demo.free.com.demomap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        TextView tv2 = new TextView(this);
        tv2.setText("Hai i am second text");
        setContentView(tv2);
        /*TextView tv = new TextView(this);
        tv.setText("hai i am without XML file");
        setContentView(tv);*/

    }
}
