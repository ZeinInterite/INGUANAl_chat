package com.example.inguanalchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
                    Log.d("Server Response", serverResponse);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageContent = editTextMessage.getText().toString();
                String currentDateTime = getCurrentDateTimeFormatted(); // Получаем текущую дату и время
                message = new Message();
                message.setName(name);
                message.setMessage(messageContent);
                message.setDate(currentDateTime);
                messageList.add(message);
                adapter.notifyDataSetChanged();

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if (out != null) {
                            out.println(name + ": " + messageContent + " - " + currentDateTime); // Отправляем дату на сервер
                            out.flush(); // Принудительно отправить данные
                        } else {
                            Log.e("PrintWriter", "PrintWriter 'out' is null. Message not sent.");
                        }
                        return null;
                    }
                }.execute();
            }
        });
    }

    public String getCurrentDateTimeFormatted() {
        Date currentDate = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd HH:mm");
        return dateFormat.format(currentDate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messageViewModel.setMessageList(messageList);
    }
}
