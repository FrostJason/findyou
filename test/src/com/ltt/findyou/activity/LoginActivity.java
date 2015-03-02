package com.ltt.findyou.activity;

import com.ltt.findyou.MainActivity;
import com.ltt.findyou.R;
import com.ltt.findyou.app.MyApplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

@SuppressLint("NewApi")
public class LoginActivity extends Activity {
	private EditText name;
	private Button login;
	private MyApplication myApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.login);
		myApplication=(MyApplication)getApplication();
		
		name = (EditText)findViewById(R.id.name);
		login=(Button)findViewById(R.id.login);
		login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myApplication.userName=name.getText().toString();
				Intent i=new Intent(LoginActivity.this, MainActivity.class);
				startActivity(i);
				finish();
			}
		});
	}
}
