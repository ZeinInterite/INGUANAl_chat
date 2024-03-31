package com.example.inguanalchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_ADDRESS = "192.168.0.5";
    private static final int SERVER_PORT = 12345;
    private RecyclerView messageListView;
    private MessageAdapter adapter;
    private String name;
    private MessageViewModel messageViewModel;
    private EditText editTextMessage;
    private Button buttonSend;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Message message;
    private List<Message> messageList = new ArrayList<>();
    private ConnectivityManager connectivityManager;
    private boolean isConnectedToInternet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageListView = findViewById(R.id.messageRecyclerView);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        adapter = new MessageAdapter(messageList);

        messageListView.setLayoutManager(new LinearLayoutManager(this));
        messageListView.setAdapter(adapter);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        checkInternetConnection();

        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        List<Message> savedMessages = messageViewModel.getMessageList();
        if (savedMessages != null) {
            messageList.addAll(savedMessages);
            adapter.notifyDataSetChanged();
        }

        new Thread(() -> {
            try {
                clientSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String serverResponse;
                while ((in != null) && (serverResponse = in.readLine()) != null) {
                    final String response = serverResponse;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Server Response", response);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isConnectedToInternet && isServerReachable()) {
                            new SendMessageTask().execute(editTextMessage.getText().toString());
                        } else {
                            if (!isConnectedToInternet) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Отсутствует подключение к интернету", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Не удалось подключиться к серверу", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                }).start();
            }
        });
    }

    public String getCurrentDateTimeFormatted() {
        Date currentDate = new Date();
        @SuppressLint("SimpleDateFormat")    SimpleDateFormat dateFormat = new SimpleDateFormat("dd HH:mm");
        return dateFormat.format(currentDate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messageViewModel.setMessageList(messageList);
    }
    private void checkInternetConnection() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        isConnectedToInternet = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean isServerReachable() {
        try {
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT), 2000);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class SendMessageTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {

            String messageContent = params[0];
            String currentDateTime = getCurrentDateTimeFormatted();

            try {
                if (clientSocket == null || clientSocket.isClosed()) {
                    clientSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                }

                out.println(name + ": " + messageContent + " - " + currentDateTime);
                out.flush();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isConnected) {
            if (isConnected) {
                String messageContent = editTextMessage.getText().toString();
                String currentDateTime = getCurrentDateTimeFormatted();
                message = new Message();
                message.setName(name);
                message.setMessage(messageContent);
                message.setDate(currentDateTime);
                messageList.add(message);
                adapter.notifyDataSetChanged();
            } else {
            }

        }
    }
}
