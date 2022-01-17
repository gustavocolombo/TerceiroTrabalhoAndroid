package dadm.com.example.trabalhonavegacaoactivities;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;

public class DAOTime {
    private DatabaseReference databaseReference;
    public DAOTime(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(Time.class.getSimpleName());
    }

    public Task<Void> add(Time time){
        return databaseReference.push().setValue(time);
    }
}
