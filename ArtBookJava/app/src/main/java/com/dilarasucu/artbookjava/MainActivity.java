package com.dilarasucu.artbookjava;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dilarasucu.artbookjava.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Art> artArrayList;
    ArtAdapter artAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        artArrayList =new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        artAdapter=new ArtAdapter(artArrayList);
        binding.recyclerView.setAdapter(artAdapter);

        getData();



    }

    private void getData(){
        SQLiteDatabase sqLiteDatabase=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null); //burada arts tablosunu actık

        Cursor cursor=sqLiteDatabase.rawQuery("SELECT * FROM arts",null); //Bu kod, SQLiteDatabase nesnesi üzerinden rawQuery() yöntemini kullanarak
                                                                                         // "arts" tablosundaki tüm verileri seçmek için bir SQL sorgusu çalıştırır ve sonucunu bir Cursor nesnesi olarak döndürür.
        int nameIx=cursor.getColumnIndex("artname");//Bu kod, Cursor nesnesinin içindeki "artname" sütununun indeksini almak için kullanılır.
        int idIx=cursor.getColumnIndex("id");

        while (cursor.moveToNext()){ //Bu kod parçası, Cursor nesnesi üzerinde bir döngü kullanarak veritabanından alınan tüm verileri gezme amacıyla kullanılmıştır.
            String name=cursor.getString(nameIx);//Bu kod, Cursor nesnesi üzerinde bulunan nameIx indeksi ile "artname" sütunundaki değeri alarak bir String değişkenine atar
            int id=cursor.getInt(idIx);
            Art art=new Art(name,id);
            artArrayList.add(art);

        }
        artAdapter.notifyDataSetChanged();
        cursor.close();

    }
//bu asagıdaki ıkı metot menuyu baglamak için kullanılır
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // buu method menu olsuturuldgunda ne olacagını verir

        MenuInflater menuInflater=getMenuInflater(); //menu xml dosyasını koda baglıyoruz
        menuInflater.inflate(R.menu.art_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // bu method menudeki seceneklerden birine tıklandıgında ne olacagını verir
        if(item.getItemId()==R.id.add_art){ //tıklanılan itemin (ogenin) idesi add_art ise ne yapılacagını verir.buarada kac tane oge varsa o kadar if else if yapısı kullanılarak tıklanıla ogenin idesi buna esitse bunu yap deriz
            Intent intent=new Intent(this,ArtActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}