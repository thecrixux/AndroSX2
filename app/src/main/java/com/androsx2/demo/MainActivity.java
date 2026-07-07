package com.androsx2.demo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton buttonLaunch;
    private FloatingActionButton buttonROM;
    private com.google.android.material.switchmaterial.SwitchMaterial switchFastBoot;
    private com.google.android.material.switchmaterial.SwitchMaterial switchVSync;
    private com.google.android.material.switchmaterial.SwitchMaterial switchFPS;
    private com.google.android.material.switchmaterial.SwitchMaterial switchStats;
    private com.google.android.material.switchmaterial.SwitchMaterial switchResolution;
    private com.google.android.material.switchmaterial.SwitchMaterial switchVersion;
    private Spinner spinnerRenderer;
    private Spinner spinnerDisplay;
    private ArrayList<String> rendererList = new ArrayList<>();
    private ArrayList<String> displayList = new ArrayList<>();
    private Intent intentLaunch = new Intent();
    private Intent FilePicker = new Intent(Intent.ACTION_GET_CONTENT);
    private SharedPreferences SharedPreference1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Vues
        buttonLaunch = findViewById(R.id.buttonLaunch);
        buttonROM = findViewById(R.id.buttonROM);

        switchFastBoot = findViewById(R.id.switchFastBoot);
        switchVSync = findViewById(R.id.switchVSync);
        switchFPS = findViewById(R.id.switchFPS);
        switchStats = findViewById(R.id.switchStats);
        switchResolution = findViewById(R.id.switchResolution);
        switchVersion = findViewById(R.id.switchVersion);

        spinnerRenderer = findViewById(R.id.spinnerRenderer);
        spinnerDisplay = findViewById(R.id.spinnerDisplay);

        // SP
        SharedPreference1 = getSharedPreferences("Data", Context.MODE_PRIVATE);

        // Settings switch
        switchFastBoot.setChecked(SharedPreference1.getBoolean("FastBoot", true));
        switchVSync.setChecked(SharedPreference1.getBoolean("VSync", false));
        switchFPS.setChecked(SharedPreference1.getBoolean("OSD_FPS", false));
        switchStats.setChecked(SharedPreference1.getBoolean("OSD_Stats", false));
        switchResolution.setChecked(SharedPreference1.getBoolean("OSD_Resolution", false));
        switchVersion.setChecked(SharedPreference1.getBoolean("OSD_Version", false));

        buttonLaunch.setOnClickListener(v -> {
            intentLaunch.setClass(getApplicationContext(), EmulatorActivity.class);
            startActivity(intentLaunch);
        });

        buttonROM.setOnClickListener(v -> {
            FilePicker.setType("*/*");
            startActivityForResult(FilePicker, 1);
        });

        buttonROM.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(), "ROM Path Reset", Toast.LENGTH_SHORT).show();
                SharedPreference1.edit().putString("gamePath", "").apply();
                return true;
            }
        });

        displayList.add("Stretch");
        displayList.add("Auto 4:3/3:2");
        displayList.add("4:3");
        displayList.add("16:9");
        ArrayAdapter<String> displayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, displayList);
        displayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDisplay.setAdapter(displayAdapter);

        //Spinner selection
        int SPDisplay = SharedPreference1.getInt("Display", 2);
        spinnerDisplay.setSelection(SPDisplay);

        spinnerDisplay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreference1.edit().putInt("Display", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        rendererList.add("OpenGL ES Hardware");
        rendererList.add("OpenGL ES Software");
        rendererList.add("Vulkan Hardware");
        ArrayAdapter<String> rendererAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rendererList);
        rendererAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRenderer.setAdapter(rendererAdapter);

        //Spinner selection
        int SPRenderer = SharedPreference1.getInt("Renderer", 14);
        spinnerRenderer.setSelection(SPRenderer-12);

        spinnerRenderer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreference1.edit().putInt("Renderer", 12+position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        configureOnCheckedChangeListener(switchFastBoot, "FastBoot");
        configureOnCheckedChangeListener(switchVSync, "VSync");
        configureOnCheckedChangeListener(switchFPS, "OSD_FPS");
        configureOnCheckedChangeListener(switchStats, "OSD_Stats");
        configureOnCheckedChangeListener(switchResolution, "OSD_Resolution");
        configureOnCheckedChangeListener(switchVersion, "OSD_Version");

    }

    private void configureOnCheckedChangeListener(CompoundButton button, String key) {
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPreference1.edit().putBoolean(key, true).apply();
                } else {
                    SharedPreference1.edit().putBoolean(key, false).apply();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK && data != null) {
                    String uriString = data.getData().toString();
                    String fileName = uriString.substring(uriString.lastIndexOf('/')+1).toLowerCase();
                    if (fileName.endsWith(".iso") || fileName.endsWith(".chd") || fileName.endsWith(".bin") || fileName.endsWith(".img") || fileName.endsWith(".elf")) {
                        // vide = bios
                        SharedPreference1.edit().putString("gamePath", uriString).apply();
                        Toast.makeText(this, "ROM selected", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Invalid ROM File", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // vide = bios
                    SharedPreference1.edit().putString("gamePath", "").apply();
                }
        }
    }
}