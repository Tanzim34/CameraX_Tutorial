package com.example.cameraxtutorial;

import androidx.camera.core.ImageCapture;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;

public class Camera extends AppCompatActivity {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private Button switchCameraButton;
    private Button captureButton;
    private ToggleButton recordButton;
    private CameraSelector cameraSelector;
    private ImageCapture imageCapture;
    private boolean isRecording = false;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        switchCameraButton = findViewById(R.id.switchCameraButton);
        captureButton = findViewById(R.id.captureButton);
        recordButton = findViewById(R.id.recordButton);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        switchCameraButton.setOnClickListener(view -> {
            switchCamera();
        });

        captureButton.setOnClickListener(view -> {
            captureImage();
        });

        recordButton.setOnClickListener(view -> {
            toggleRecording();
        });
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                PreviewView previewView = findViewById(R.id.cameraPreview);
                if(cameraSelector==null) cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void switchCamera() {
        if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        } else {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        }
        startCamera();
    }


    private void captureImage() {
        File photoFile = new File(getExternalFilesDir(null), "image.jpg");

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(photoFile)
                        .build(), ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        runOnUiThread(() ->
                                Toast.makeText(Camera.this, "Image saved", Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onError(ImageCaptureException exception) {
                        exception.printStackTrace();
                    }
                });
    }
    private void toggleRecording() {
        if (!isRecording) {
            // Start recording
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
            // Add video recording logic here
            isRecording = true;
        } else {
            // Stop recording
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
            // Add logic to stop recording here
            isRecording = false;
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, // For video recording
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int REQUEST_CODE_PERMISSIONS = 10;
}
