package com.example.healthcalc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.healthcalc.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.w3c.dom.Text;

public class MapActivity extends AppCompatActivity {

    TextView mainTextView;
    Button btnUserInfo;
    Button btnUserCcal;
    Button btnCalculateCcal;
    Button btnWiki;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    FirebaseUser currentUser;

    RelativeLayout root;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        currentUser = auth.getCurrentUser();

        mainTextView = findViewById(R.id.mainTextView);
        root = findViewById(R.id.root_element_map);

        btnUserInfo = findViewById(R.id.btnUserInfo);
        btnUserCcal = findViewById(R.id.btnUserCcal);
        btnCalculateCcal = findViewById(R.id.btnCalculateCcal);

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                if (user.getIndexWeightBody() == null) {
                    confirmInformation();
                }
                mainTextView.setText("Привет, " + user.getName() + "!");
                btnUserInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showUserInformation(user);
                    }
                });
                btnUserCcal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showUserCalories(user);
                    }
                });
                btnCalculateCcal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        calculateUserCcal(user);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(root, "Ошибка загрузки пользователя", Snackbar.LENGTH_LONG).show();
            }
        };
        users.child(currentUser.getUid()).addListenerForSingleValueEvent(userListener);
    }

    private void showUserInformation(User user) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Информация о пользователе");

        LayoutInflater inflater = LayoutInflater.from(this);
        final View addUserInfoWindow = inflater.inflate(R.layout.user_info_window, null);
        dialog.setView(addUserInfoWindow);

        dialog.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        final MaterialEditText textViewName = addUserInfoWindow.findViewById(R.id.textViewName);
        final MaterialEditText textViewEmail = addUserInfoWindow.findViewById(R.id.textViewEmail);
        final MaterialEditText textViewPhone = addUserInfoWindow.findViewById(R.id.textViewPhone);
        final MaterialEditText textViewSex = addUserInfoWindow.findViewById(R.id.textViewSex);
        final MaterialEditText textViewAge = addUserInfoWindow.findViewById(R.id.textViewAge);
        final MaterialEditText textViewHeight = addUserInfoWindow.findViewById(R.id.textViewHeight);
        final MaterialEditText textViewWeight = addUserInfoWindow.findViewById(R.id.textViewWeight);
        final MaterialEditText textViewIbw = addUserInfoWindow.findViewById(R.id.textViewIbw);

        textViewName.setText(user.getName());
        textViewEmail.setText(user.getEmail());
        textViewPhone.setText(user.getPhone());
        textViewSex.setText(user.getSex());
        textViewAge.setText(user.getAge());
        textViewHeight.setText(user.getHeight());
        textViewWeight.setText(user.getWeight());
        textViewIbw.setText(user.getIndexWeightBody());

        dialog.show();
    }

    private void showUserCalories(final User user) {
        if (user.getDci().equals("Не рассчитан.")) {
            Snackbar.make(root, "DCI не рассчитан. Перейдите в \"Рассчитать дневную норму\"", Snackbar.LENGTH_LONG).show();
        }
        else {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Ваша дневная норма килокалорий");

            LayoutInflater inflater = LayoutInflater.from(this);
            final View userCcalWindow = inflater.inflate(R.layout.user_ccal_window, null);
            dialog.setView(userCcalWindow);

            MaterialEditText textViewIbw = userCcalWindow.findViewById(R.id.textViewIbw);
            textViewIbw.setText(user.getIndexWeightBody());

            TextView textViewIbwStatus = userCcalWindow.findViewById(R.id.ibwStatus);
            final MaterialEditText textViewDci = userCcalWindow.findViewById(R.id.textViewDci);
            final Spinner spinner = userCcalWindow.findViewById(R.id.spinnerChooseWant);

            double ibwDouble = Double.parseDouble(user.getIndexWeightBody());
            if (ibwDouble <= 16) {
                textViewIbwStatus.setBackgroundColor(getResources().getColor(R.color.less16));
                textViewIbwStatus.setText("Выраженный дифицит массы тела");
            } else if (ibwDouble <= 18.5) {
                textViewIbwStatus.setBackgroundColor(getResources().getColor(R.color.less18));
                textViewIbwStatus.setText("Недостаточная масса тела");
            } else if (ibwDouble <= 25) {
                textViewIbwStatus.setBackgroundColor(getResources().getColor(R.color.less25));
                textViewIbwStatus.setText("Нормальная масса тела");
            } else if (ibwDouble <= 30) {
                textViewIbwStatus.setBackgroundColor(getResources().getColor(R.color.less30));
                textViewIbwStatus.setText("Избыточная масса тела");
            } else if (ibwDouble <= 35) {
                textViewIbwStatus.setBackgroundColor(getResources().getColor(R.color.less35));
                textViewIbwStatus.setText("Ожирение первой степени");
            } else if (ibwDouble <= 40) {
                textViewIbwStatus.setBackgroundColor(getResources().getColor(R.color.less40));
                textViewIbwStatus.setText("Ожирение второй степени");
            } else {
                textViewIbwStatus.setBackgroundColor(getResources().getColor(R.color.more40));
                textViewIbwStatus.setText("Ожирение третей степени");
            }
            dialog.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });


            textViewDci.setText(user.getDci());

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    double dciInt = Double.parseDouble(user.getDci());
                    if (spinner.getSelectedItemPosition() == 0) {
                        textViewDci.setText("> " + user.getDci());
                    }
                    if (spinner.getSelectedItemPosition() == 1) {
                        textViewDci.setText(user.getDci());
                    }
                    if (spinner.getSelectedItemPosition() == 2) {
                        dciInt -= dciInt * 15/100;
                        textViewDci.setText(String.valueOf(dciInt));
                    }
                    if (spinner.getSelectedItemPosition() == 3) {
                        dciInt -= dciInt * 20/100;
                        textViewDci.setText(String.valueOf(dciInt));
                    }
                    if (spinner.getSelectedItemPosition() == 4) {
                        dciInt -= dciInt * 30/100;
                        textViewDci.setText((String.valueOf(dciInt)));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    textViewDci.setText(user.getDci());
                }
            });
            dialog.show();
        }
    }

    private void calculateUserCcal(final User user) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Расчитать дневную норму килокалорий (DCI)");

        LayoutInflater inflater = LayoutInflater.from(this);
        final View calculateCcal = inflater.inflate(R.layout.calculate_ccal_window, null);
        dialog.setView(calculateCcal);

        final Spinner spinner = calculateCcal.findViewById(R.id.spinner);

        final MaterialEditText textViewSex = calculateCcal.findViewById(R.id.textViewSex);
        final MaterialEditText textViewAge = calculateCcal.findViewById(R.id.textViewAge);
        final MaterialEditText textViewHeight = calculateCcal.findViewById(R.id.textViewHeight);
        final MaterialEditText textViewWeight = calculateCcal.findViewById(R.id.textViewWeight);

        textViewSex.setText(user.getSex());
        textViewAge.setText(user.getAge());
        textViewHeight.setText(user.getHeight());
        textViewWeight.setText(user.getWeight());

        dialog.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Рассчитать", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                double coefPhysAct;
                switch (spinner.getSelectedItemPosition()) {
                    case 0:
                        coefPhysAct = 1.2;
                        break;
                    case 1:
                        coefPhysAct = 1.38;
                        break;
                    case 2:
                        coefPhysAct = 1.46;
                        break;
                    case 3:
                        coefPhysAct = 1.55;
                        break;
                    case 4:
                        coefPhysAct = 1.64;
                        break;
                    case 5:
                        coefPhysAct = 1.73;
                        break;
                    case 6:
                        coefPhysAct = 1.9;
                        break;
                    default:
                        coefPhysAct = 1.2;
                        break;
                }
                int lastSexCoef;
                if (textViewSex.getText().toString() == "Мужчина") {
                    lastSexCoef = 5;
                } else {
                    lastSexCoef = -161;
                }

                double weightInt = Double.parseDouble(user.getWeight());
                double heightInt = Double.parseDouble(user.getHeight()) * 100;
                double ageInt = Double.parseDouble(user.getAge());

                double dci = (weightInt*10 + heightInt*6.25 - ageInt*5 + lastSexCoef) * coefPhysAct;

                user.setDci(String.valueOf(dci));

                users.child(currentUser.getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(root, "DCI рассчитан.", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }

    private void showWiki() {
        Snackbar.make(root, "НЕ ЛІЗЬ СЮДА БЛЯТЬ", Snackbar.LENGTH_SHORT).show();
    }

    private void confirmInformation() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Вход выполнен первый раз");
        dialog.setMessage("Заполните информацию о себе");

        LayoutInflater inflater = LayoutInflater.from(this);
        final View addInfoWindow = inflater.inflate(R.layout.add_info_window, null);
        dialog.setView(addInfoWindow);

        final MaterialEditText weight = addInfoWindow.findViewById(R.id.weightField);
        final MaterialEditText height = addInfoWindow.findViewById(R.id.heightField);
        final MaterialEditText age = addInfoWindow.findViewById(R.id.ageField);

        dialog.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if ((weight.getText().toString() == "") || (height.getText().toString() == "") || (age.getText().toString() == "")) {
                    Snackbar.make(root, "Заполните все поля", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                user.setWeight(weight.getText().toString());
                user.setHeight(height.getText().toString());
                user.setAge(age.getText().toString());
                double weightInt = Double.parseDouble(user.getWeight());
                double heightInt = Double.parseDouble(user.getHeight());
                double indexWeightBody = weightInt/(heightInt*heightInt);
                user.setIndexWeightBody(String.valueOf(indexWeightBody));
                users.child(currentUser.getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(root, "Added.", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.show();
    }

    public void onClickChangeSex(View view){
        RadioButton button = (RadioButton) view;
        int id = button.getId();
        if (id == R.id.rbMale) {
            user.setSex("Мужчина");
        }
        else {
            user.setSex("Женщина");
        }
    }
}
