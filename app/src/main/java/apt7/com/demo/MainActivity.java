package apt7.com.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
//    static EditText editText1;
//    static EditText editText2;
//    Button button;
//    static ServerPref serverPref;
//    static Utils utils;
//    TelnetServer telnet;
//    public Handler mHandler;

    EditText editText1;
    EditText editText2;
    Button button;
    ServerPref serverPref;
    Utils utils;
    TelnetServer telnet;
    public Handler mHandler;
    private boolean isOpenPermission = false;
    private RequestPermission _requestPermission;

    class wifi_data {
        public String ssid;
        public int rssi;

        wifi_data(String ssid, int rssi) {
            this.ssid = ssid;
            this.rssi = rssi;
        }
    }

    class MyAdapter extends BaseAdapter {

        Context mContext = null;
        LayoutInflater mLayoutInflater = null;
        ArrayList<wifi_data> sample;

        public MyAdapter(Context context, ArrayList<wifi_data> data) {
            mContext = context;
            sample = data;
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return sample.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public wifi_data getItem(int position) {
            return sample.get(position);
        }

        @Override
        public View getView(int position, View converView, ViewGroup parent) {
            View view = mLayoutInflater.inflate(R.layout.listview_item, null);

            ImageView imageView = (ImageView) view.findViewById(R.id.poster);
            TextView movieName = (TextView) view.findViewById(R.id.movieName);
            TextView grade = (TextView) view.findViewById(R.id.grade);
            movieName.setText(getItem(position).ssid);

//            imageView.setImageResource(sample.get(position).getPoster());
//            movieName.setText(sample.get(position).getMovieName());
//            grade.setText(sample.get(position).getGrade());

            return view;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String test_string = "[{'ssid':'homewifi','rssi':-67},{'ssid':'homewifi1','rssi':-100},{'ssid':'homewifi2','rssi':-150},{'ssid':'homewifi3','rssi':-200}]";

        ArrayList<wifi_data> wifi = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(test_string);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject user = arr.getJSONObject(i);

                wifi.add(new wifi_data(user.get("ssid").toString(), Integer.parseInt(user.get("rssi").toString())));
                Log.i("parsing : ", wifi.get(i).ssid);
            }
        } catch (Exception e) {


        }


        ListView listView = (ListView) findViewById(R.id.listview1);
        final MyAdapter myAdapter = new MyAdapter(this, wifi);

        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Toast.makeText(getApplicationContext(), myAdapter.getItem(position).ssid, Toast.LENGTH_LONG).show();
                password_request_dialog(myAdapter.getItem(position).ssid);
            }
        });


        findViewById(R.id.id5).setVisibility(View.GONE);
        utils = new Utils(this);
        editText1 = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText1);

        serverPref = new ServerPref(this);
        mHandler = new Handler();

        button = (Button) findViewById(R.id.button);

        if (utils.isConnectedMobile() || utils.isConnectedWifi()) {
            runApp();
        } else {
            showAlertToUser();
        }

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editText1.setText("");
                connect();
                Toast.makeText(getApplicationContext(), telnet + "", Toast.LENGTH_LONG).show();
                findViewById(R.id.button2).setEnabled(true);
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showExitAlert();
            }
        });


    }


    private void connect() {
        telnet = new TelnetServer(this, this);
        telnet.start();
        findViewById(R.id.button1).setEnabled(false);
    }

    private void showExitAlert() {
        telnet.disconnect();
        AlertDialog.Builder alertDialogBuilder;
        final AlertDialog alert;
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Attention!\n\nDisconnected from Port closing app.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                    }
                });
        alert = alertDialogBuilder.create();
        alert.show();
    }

    private void runApp() {
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editText2.getText().toString().replaceAll("\\s", "").length() == 0) {
                    showAlertEmpty();
                } else {
                    send();
                }
            }
        });

        editText2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND && editText2.getText().toString().replaceAll("\\s", "").length() > 0) {
                    send();
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void showAlertEmpty() {
        AlertDialog.Builder alertDialogBuilder;
        final AlertDialog alert;
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Attention!\n\nInformation is missing.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        alert = alertDialogBuilder.create();
        alert.show();
    }

    private void showAlertToUser() {
        AlertDialog.Builder alertDialogBuilder;
        AlertDialog alert;
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Attention!\n\nPlease enable internet to shop.\nWould you like to enable them now?")
                .setCancelable(false)
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    }
                }).setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            if (!telnet.isAlive())
            startActivity(new Intent(this, SettingsActivity.class));
//            else
//                Toast.makeText(this, "Please disconnect connection to go to setting.", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void send() {


        NetworkThread thread = new NetworkThread();
        thread.start();
        thread.interrupt();

        mHandler.post(new Runnable() {
            public void run() {

                String lastLine = editText1.getText().toString();
                editText1.setText("");
                editText1.setText(lastLine + "\n>" + editText2.getText().toString());

                editText2.setText("");
                if (editText2.getText().toString().toLowerCase().equals("quit")) {
                    lastLine = editText1.getText().toString();
                    editText1.setText(lastLine + "\n" + "Disconnection from Server");
                    showExitAlert();
                }
            }
        });
    }

//    Thread temporary_thread = new Thread() {
//        public void run() {
//            telnet.out.println(editText2.getText().toString());
//            telnet.out.flush();
//        }
//    }.start();


    private class NetworkThread extends Thread {

        public void run() {
            telnet.out.println(editText2.getText().toString());
            telnet.out.flush();
        }
    }


    public void hideOrShow(String msg) {
        if (msg == null) {
            findViewById(R.id.id5).setVisibility(View.GONE);
        } else {
            findViewById(R.id.id5).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.textView)).setText(msg);
            ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);
            Ion.with(imageButton).load(msg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (telnet != null)
            telnet.disconnect();
    }


    String password;
    void password_request_dialog(String ssid) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle(ssid + "wifi의 비밀번호를 넣어주세요.");
        alertDialog.setMessage("Enter Password");

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.key);

        alertDialog.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        password = input.getText().toString();
                        Toast.makeText(getApplicationContext(), password,Toast.LENGTH_SHORT).show();

                    }
                });

        alertDialog.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }


}


