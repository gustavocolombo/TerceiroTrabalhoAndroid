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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
    private DAOTime timedao;

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

        DAOTime timeDao = new DAOTime();

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

    public void clicarAdicionar(){
        Intent intent = new Intent(this, MainActivity2.class);
        startActivityForResult(intent, REQUEST_ADD);
    }

    public void apagarItem(){
        if(selected >= 0){
            this.timedao.remove("-MtKdkd3lpFovt7S5_e_").addOnSuccessListener(suc ->{
                updateItemsList(listaTimes);
                Toast.makeText(this, "Time is removed", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(er ->{
                Toast.makeText(this, ""+er.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }else{
            Toast.makeText(this, "Selecione um item para deletar", Toast.LENGTH_SHORT).show();
        }
    }

    public void clicarEditar(){
        Intent intent = new Intent(this, MainActivity2.class);
        Time time = listaTimes.get(selected);
//        intent.putExtra("nome", time.getNome());
//        intent.putExtra("cores", time.getCores());
//        intent.putExtra("regiao", time.getRegiao());

        DAOTime timeDao = new DAOTime();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("nome", time.getNome().toString());
        hashMap.put("cores", time.getCores().toString());
        hashMap.put("regiao", time.getRegiao().toString());

        timeDao.update("-MtKdkd3lpFovt7S5_e_", hashMap).addOnSuccessListener(suc ->{
            Toast.makeText(this, "Time is inserted", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(er ->{
            Toast.makeText(this, ""+er.getMessage(), Toast.LENGTH_SHORT).show();
        });

        startActivityForResult(intent, REQUEST_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //fui e voltei da minha main com o botão adicionar
        Time time = null;
        if (requestCode == REQUEST_ADD && resultCode == REQUEST_ADD) {
            time = null;
            if (data.getExtras() != null) {
                time = (Time) data.getExtras().get("Adicione um time");
            }

            this.timedao.add(time).addOnSuccessListener(success -> {
                updateItemsList(listaTimes);
                Toast.makeText(this, "Time is inserted", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(er -> {
                Toast.makeText(this, "" + er.getMessage(), Toast.LENGTH_SHORT).show();
            });

            this.adapter.notifyDataSetChanged();
        } else if (requestCode == REQUEST_EDIT && resultCode == REQUEST_ADD) {
            Time book = null;

            if (data.getExtras() != null) {
                book = (Time) data.getExtras().get("element_add");
            }

            String keyUpdate = null;

            for (Time timeBusca : this.listaTimes) {
                if (timeBusca.getId() == book.getId()) {
                    keyUpdate = timeBusca.getKey();
                }
            }

            HashMap<String, Object> updateTime = new HashMap<>();
            updateTime.put("id", time.getId());
            updateTime.put("nome", time.getNome());
            updateTime.put("cores", time.getCores());
            updateTime.put("regiao", time.getRegiao());

            this.timedao.update(keyUpdate, updateTime).addOnSuccessListener(success -> {
                updateItemsList(listaTimes);
                Toast.makeText(this, "Time is updated", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(er -> {
                Toast.makeText(this, "" + er.getMessage(), Toast.LENGTH_SHORT).show();
            });
            adapter.notifyDataSetChanged();
        } else if (resultCode == MainActivity2.RESULT_CANCEL) {
            Toast.makeText(this, "Cancelado", Toast.LENGTH_LONG).show();
        }
    }

    private void updateItemsList(List<Time> list){
        list.clear();

        this.timedao.get().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data: snapshot.getChildren()){
                    Time timeData = data.getValue(Time.class);
                    timeData.setKey(data.getKey());
                    list.add(timeData);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "ERRO:" + error.getMessage() , Toast.LENGTH_SHORT).show();
            }
        });
    }
}