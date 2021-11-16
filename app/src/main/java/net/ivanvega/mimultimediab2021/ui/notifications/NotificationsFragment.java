package net.ivanvega.mimultimediab2021.ui.notifications;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NotificationsFragment extends Fragment {

    private ActivityResultLauncher<String[]> laucherPermmison;
    private ActivityResultLauncher<String[]> launcherSAF;
    private MediaPlayer mediaPlayer;
    private Button btnMul;

    List<Video> videoList = new ArrayList<Video>();


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        validarAudioWriteExternal();

        launcherSAF = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        Log.i("SAFUriResult", result.toString());
                        startActivity(
                                new Intent(
                                        Intent.ACTION_VIEW
                                ).setData(result)
                        );
                    }
                }

        );

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
    private String [] permissions =
            {Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
            };
    private boolean permissionToWriteAccepted = false;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnGr = binding.btnRecord;
        btmRe = binding.btnRepro;
        btnMul = binding.btnMult;

        binding.btnSAF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launcherSAF.launch(new String[]{"application/msword",
                "text/html", "application/pdf"});
            }
        });

        btnMul.setOnClickListener(view -> listarMultimedia()  );

        btnGr.setOnClickListener(view -> {

            if(btnGr.getText().toString().equals("Detener")){
                recorder.stop();
                recorder.release();
                btnGr.setText("Grabar");
                return ;
            }
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

    private void listarMultimedia() {
        String[] projection = new String[] {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
        };
        String selection = MediaStore.Video.Media.DURATION +
                " >= ?";
        String[] selectionArgs = new String[] {
                String.valueOf(TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES))
            };

        String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";

        try (Cursor cursor = getActivity().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);

            Log.i("MulVideo", String.valueOf( cursor.getCount()));

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);

                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                videoList.add(new Video(contentUri, name, duration, size));
            }

            for (Video video : videoList) {
                Log.i("MulVideo", video.toString());
            }

        }
    }

    private void reproducirAudio() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath() +
                            "/" + "miaudiob.mp3"
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

        File audio = new File(dirMusicExternalPublic, "miaudiob.mp3");
        /*

         */
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