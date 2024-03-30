package com.example.inguanalchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_ADDRESS = "10.0.2.2"; // Хз че сюда писать
    private static final int SERVER_PORT = 12345;
    private RecyclerView messageListView;
    private EditText editTextSenderName;
    private Adapter adapter;
    private String name;
    private EditText editTextMessage;
    private Button buttonSend;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageListView = findViewById(R.id.messageRecyclerView);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        List<String> messages = new ArrayList<>();

        new Thread(() -> {
            try {
                clientSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = name + ": " + editTextMessage.getText().toString();
                out.println(message);
            }
        });

        new Thread(() -> {
            try {
                String serverResponse;
                while ((serverResponse = in.readLine()) != null) {
                    Log.d("Server Response", serverResponse);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}