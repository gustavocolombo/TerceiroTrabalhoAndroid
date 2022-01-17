package dadm.com.example.trabalhonavegacaoactivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static int REQUEST_ADD = 1;
    public static int REQUEST_EDIT = 2;
    int selected;
    ListView listViewConteudo;
    ArrayList<Time> listaTimes;
    ArrayAdapter adapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Time timeselecionado;
    Time t = new Time();

    Button bt_ir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_ir = findViewById(R.id.bt_ir);
        bt_ir.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent i = new Intent(MainActivity.this, MainActivity2.class);
                //passando valores pra outra activity
                i.putExtra("id", "gustavo");

                //iniciando a activity com valores passados de outra tela
                startActivityForResult(i, REQUEST_ADD);
            }
        });

        //representa o conteúdo, o adapter mostra o conteúdo
        listaTimes = new ArrayList<Time>();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listaTimes);
        ListView listViewConteudo = (ListView) findViewById(R.id.listViewConteudo);
        listViewConteudo.setAdapter(adapter);
        listViewConteudo.setSelector(android.R.color.holo_blue_light);

        //adicionando ação ao clique no elemento do list
        listViewConteudo.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3){
                Toast.makeText(MainActivity.this, "" + listaTimes.get(position).toString(), Toast.LENGTH_SHORT);
                selected = position;
             }
        });

        listViewConteudo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                timeselecionado = (Time)parent.getItemAtPosition(position);
                timeselecionado.setNome(timeselecionado.getNome());
                timeselecionado.setCores(timeselecionado.getCores());
                timeselecionado.setRegiao(timeselecionado.getRegiao());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        inicializarFirebase();
        eventoDatabase();
    }

    private void eventoDatabase() {
        databaseReference.child("Time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaTimes.clear();
                for(DataSnapshot objetoSnapshot: snapshot.getChildren()){
                    Time t = objetoSnapshot.getValue(Time.class);
                    listaTimes.add(t);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializarFirebase() {
        FirebaseApp.initializeApp(MainActivity.this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        databaseReference = firebaseDatabase.getReference();
    }


    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Toast.makeText(MainActivity.this, "" + item.getItemId(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()){
            case R.id.add:
                clicarAdicionar();
                break;
            case R.id.edit:
                clicarEditar();
                break;
            case R.id.delete:
                apagarItem();
                break;
        }
        return true;
    }

    public void clicarEditar(){
        Intent intent = new Intent(this, MainActivity2.class);
        startActivityForResult(intent, REQUEST_EDIT);
    }

    public void clicarAdicionar(){
        Intent intent = new Intent(this, MainActivity2.class);
        startActivityForResult(intent, REQUEST_ADD);
    }

    public void apagarItem(){
        if(selected >= 0){
            listaTimes.remove(selected);
            Time time = new Time();
            databaseReference.child("Time").child(String.valueOf(time.getId())).removeValue();
            adapter.notifyDataSetChanged();
        }else{
            Toast.makeText(this, "Selecione um item para deletar", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //fui e voltei da minha main com o botão adicionar
        if(requestCode == REQUEST_ADD && resultCode == REQUEST_ADD){
            String nome = (String)data.getExtras().get("nome");
            String cores = (String)data.getExtras().get("cores");
            String regiao = (String)data.getExtras().get("regiao");
            Time time = new Time(nome, cores, regiao);

            listaTimes.add(time);
            adapter.notifyDataSetChanged();
        }else if(requestCode == REQUEST_EDIT && resultCode == REQUEST_ADD){
            String nome = data.getExtras().get("nome").toString();
            String cores = data.getExtras().get("cores").toString();
            String regiao = data.getExtras().get("regiao").toString();
            listaTimes.get(selected).setNome(nome);
            listaTimes.get(selected).setCores(cores);
            listaTimes.get(selected).setRegiao(regiao);
            adapter.notifyDataSetChanged();
        }

        else if (resultCode == MainActivity2.RESULT_CANCEL){
            Toast.makeText(this, "Cancelado", Toast.LENGTH_LONG).show();
        }
    }
}