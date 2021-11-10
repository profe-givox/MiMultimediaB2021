package net.ivanvega.mimultimediab2021.ui.notifications;

import android.Manifest;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import net.ivanvega.mimultimediab2021.R;
import net.ivanvega.mimultimediab2021.databinding.FragmentNotificationsBinding;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private ActivityResultLauncher<String[]> laucherPermmison;
    private MediaPlayer mediaPlayer;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        validarAudioWriteExternal();

    }

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    Button btnGr , btmRe;
    MediaRecorder recorder;

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;


    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean permissionToWriteAccepted = false;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnGr = binding.btnRecord;
        btmRe = binding.btnRepro;

        btnGr.setOnClickListener(view -> {

            if(btnGr.getText().toString().equals("Detener")){
                recorder.stop();
                recorder.release();
                btnGr.setText("Grabar");
                return ;
            }

            if (permissionToRecordAccepted && permissionToWriteAccepted)
                grabarAudio();

            /*
            recorder.stop();
            recorder.reset();   // You can reuse the object by going back to setAudioSource() step
            recorder.release(); // Now the object cannot be reused
             */
        });

        btmRe.setOnClickListener(view -> {
            if(btnGr.getText().toString().equals("Detener")){
                mediaPlayer.stop();
                mediaPlayer.release();
                btmRe.setText("Reproducir");
                return ;
            }

            if (permissionToRecordAccepted && permissionToWriteAccepted)
                reproducirAudio();


        });

        final TextView textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        laucherPermmison.launch(permissions);

        return root;
    }

    private void reproducirAudio() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath() +
                            "/" + "miaudio.mp3"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(mediaPlayer1 -> {
            mediaPlayer1.start();
            btmRe.setText("Detener");
        });
        mediaPlayer.prepareAsync();

    }

    private void validarAudioWriteExternal() {

           laucherPermmison = registerForActivityResult(
                  new ActivityResultContracts.RequestMultiplePermissions(),
                  new ActivityResultCallback<Map<String, Boolean>>() {
                      @Override
                      public void onActivityResult(Map<String, Boolean> result) {

                          if(result.get(permissions[0])){
                              permissionToRecordAccepted = true;
                          }

                          if(result.get(permissions[1])){
                              permissionToWriteAccepted = true;
                          }

                          btnGr.setEnabled(permissionToRecordAccepted);
                          btmRe.setEnabled(permissionToWriteAccepted);


                      }
                  }
          );
           
    }

    private void grabarAudio() {
        //validar permiso para acceder a estos directorios de la memoria externa
        File dirMusicExternalPublic =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        File audio = new File(dirMusicExternalPublic, "miaudio.mp3");

        Log.i("PATHFILEAUDIO", audio.getPath());
        Log.i("PATHFILEAUDIO", audio.getAbsolutePath());

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            recorder.setOutputFile(audio);
        }
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();   // Recording is now started
        btnGr.setText("Detener");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}