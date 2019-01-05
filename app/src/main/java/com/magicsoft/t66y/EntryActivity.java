package com.magicsoft.t66y;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class EntryActivity extends AppCompatActivity {

    public static String DomainName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        Button btnEntry = findViewById(R.id.btnEntry);
        final TextInputEditText txt = findViewById(R.id.txtDomain);

        final String domainName = loadDomain();
        txt.setText(domainName);

        btnEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String domainName = txt.getText().toString();
                saveDomain(domainName);

                String HOMEPAGE = "https://" + domainName + "/index.php";
                Log.e("entry", HOMEPAGE);
                Intent intent = new Intent(EntryActivity.this,
                        CLWebViewActivity.class);
                intent.putExtra("url", HOMEPAGE);
                startActivity(intent);
                finish();
            }
        });
    }

    public boolean saveDomain(String domainName) {
        SharedPreferences.Editor editor = getSharedPreferences("1024", MODE_PRIVATE).edit();
        editor.putString("domain", domainName);
        return editor.commit();
    }

    public String loadDomain() {
        SharedPreferences editor = getSharedPreferences("1024", MODE_PRIVATE);
        return editor.getString("domain", "www.t66y.com");

    }
}
