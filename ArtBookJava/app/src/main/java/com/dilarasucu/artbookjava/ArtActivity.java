package com.dilarasucu.artbookjava;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.dilarasucu.artbookjava.databinding.ActivityArtBinding;
import com.dilarasucu.artbookjava.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ArtActivity extends AppCompatActivity {


    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher; //galeriye gitmek için kullanacaz
    ActivityResultLauncher<String> permissionLauncher; //izini istemek için kullanacaz

    Bitmap selectedImage;
    private Uri imageData;

    View view;

    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        database=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null); //Bu kod, Android uygulamanızda bir yerel veritabanını açmak veya oluşturmak için kullanılır

        Intent intent=getIntent();
        String info=intent.getStringExtra("info");

        if (info.matches("new")){ //equals matches yerine bu da yazılabilir
            //new art
            binding.nameText.setText("");
            binding.artistText.setText("");
            binding.yearText.setText("");
            binding.button.setVisibility(View.VISIBLE);
           // binding.imageView2.setImageResource(R.drawable.image);
            Bitmap image = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.image);
            binding.imageView2.setImageBitmap(image);
        }
        else {
            int artId=intent.getIntExtra("artId",1);
            binding.button.setVisibility(View.INVISIBLE);

            try {
                Cursor cursor=database.rawQuery("SELECT * FROM arts WHERE id= ? ",new String[] {String.valueOf(artId)});
                int artNameIx=cursor.getColumnIndex("artname");
                int painterNameIx=cursor.getColumnIndex("paintername");
                int yearIx=cursor.getColumnIndex("year");
                int imageIx=cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.nameText.setText(cursor.getString(artNameIx));
                    binding.artistText.setText(cursor.getString(painterNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    byte[] bytes=cursor.getBlob(imageIx);
                    Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                    binding.imageView2.setImageBitmap(bitmap);
                }
                cursor.close();

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void save(View view) {

        String name=binding.nameText.getText().toString(); //string turunde bir degisken olusturup nametext i stringe cevirerek degiskene atadık
        String artistName=binding.artistText.getText().toString();
        String year=binding.yearText.getText().toString();

        Bitmap smallImage=makeSmallerImage(selectedImage,300); //makeSmallerImage metodu büyük boyutlu resmi alarak, belirtilen boyuta (300 piksel genişliğinde)
                                                                          // küçültülmüş bir smallImage isimli yeni Bitmap nesnesi oluşturur.

        ByteArrayOutputStream outputStream=new ByteArrayOutputStream(); //verileri bir ByteArrayOutputStream nesnesine yazdığınızda, bu veriler bir byte dizisine
                                                                       // dönüştürülür ve hafızada tutulur.
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);//Bu kod, bir Bitmap nesnesini sıkıştırmak için kullanılır ve sıkıştırılmış verileri bir
                                                                             // OutputStream nesnesine yazarak hafızada tutar. Özellikle resimleri bellekte daha az yer kaplaması ve veri aktarımlarında daha hızlı olması için sıkıştırmak istediğinizde kullanılır.
        byte[] byteArray=outputStream.toByteArray();//Bu kod satırı, önceki adımlarda ByteArrayOutputStream nesnesine yazılan verileri, bir byte dizisine dönüştürmek için kullanılır

        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY ,artname VARCHAR,paintername VARCHAR,year VARCHAR,image BLOB)"); //Bu sorgu, "arts" adında bir tablo oluşturur veya varsa bu isimde bir tablo yoksa tabloyu oluşturur.ve tablonunn alanlarını belirttik

            String sqlString="INSERT INTO arts(artname,paintername,year,image) VALUES(?,?,?,?)"; // Bu kod, SQLite veritabanında veri eklemek için kullanılır.
                                                                                                 // INSERT INTO sorgusu, SQLiteStatement kullanılarak derlenir ve bağlanan değişkenlerle çalıştırılır.
                                                                                                 // ? işaretleri, veri bağlama yapacağımız yerleri belirtir.


            SQLiteStatement sqLiteStatement= database.compileStatement(sqlString); //Veritabanından gelen SQL sorgusu sqlString kullanılarak SQLiteStatement nesnesi oluşturulur.
            sqLiteStatement.bindString(1,name); //bindString yöntemi, ? işaretlerine değer bağlamak için kullanılır.
                                                       // name değişkeni, sqlString içindeki ilk ? işaretine bağlanır.
            sqLiteStatement.bindString(2,artistName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute(); //SQLiteStatement nesnesi ile bağlantı yapılan verilerin tabloya eklenmesi için execute yöntemi çağrılır ve sorgu çalıştırılır.
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Intent intent=new Intent(ArtActivity.this,MainActivity.class);//Bu kod, ArtActivity sınıfından MainActivity sınıfına bir geçiş yapmak için bir Intent oluşturur.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// Bu kod parçası, Intent nesnesine bir bayrak ekleyerek hedef aktiviteye geçiş yapılırken mevcut aktivite yığınının temizlenmesini sağlar.
                                                        // Yani,hedef aktiviteye geçiş yapıldığında, geri tuşu ile önceki aktivitelere dönüşte, geçiş yaptığımız aktivite dışındaki tüm aktiviteler yığından kaldırılır.
        startActivity(intent);//intenti baslatır

    }

    public Bitmap makeSmallerImage(Bitmap image,int maximumSize){
        int width=image.getWidth();//bu guncel gorselimizin genisliginin almak için kullanılır
        int height=image.getHeight();//bu guncel gorselimizin yuksekligini almak için kullanılır

        float bitmapRatio=(float) width / (float) height;

        if (bitmapRatio > 1 ){ //burada yaptıgımız olay yatay ve dikey resimlerede orantılı olrak resimlerin buyuyup kuculmesi
            //landscape image (yatay gorsel)
            width=maximumSize;
            height=(int) (width / bitmapRatio);

        }
        else {
            //portrait image (dikey gorsel)
            height=maximumSize;
            width=(int) (height * bitmapRatio);
        }
        return image.createScaledBitmap(image,width,height,true);
    }


    public void selectImage(View view) {
        //burada eger build i gormesse android.os.Build yazabiliriz
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //burası eger apı 33 ise izin için bunu kullanmamız lazım
            //android 33 ve uzeri için
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) { //burada manifest android olan secilmeli yoksa hata verir  //burada iznin verilip verilmedigini kontrol ederiz
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("give permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //request permission(izin iste)
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);//kullanıcının uygulamanızın harici depolama alanına (örneğin, SD kart) erişim iznini istemek için kullanılır.

                        }
                    }).show();
                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }


            } else {
                //galery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //burada galeriden bir tane foto alıp gelecez
                activityResultLauncher.launch(intentToGallery);
            }
        }
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                }
                else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
            else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }
    }


    private void registerLauncher(){ //izin istemek için kullanılacak metot
        //galeriye gitmek için
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode()==RESULT_OK){ //kullanıcı bir sey secmişse buaraya giderecek
                    Intent intentFromResult=result.getData(); //ActivityResultCallback içinde bir sonuç alındığında, bu sonuçtan dönen verileri elde etmek için kullanılır.
                                                             //Android'de Intent intentFromResult = result.getData(); ifadesi, ActivityResultCallback içinde bir sonuç alındığında, bu sonuçtan dönen verileri elde etmek için kullanılır.
                                                            //result.getData() ifadesi, ActivityResult nesnesinden dönen sonuç verilerine erişim sağlar. ActivityResult nesnesi, ActivityResultCallback içindeki onActivityResult veya onActivityResult(int requestCode, int resultCode, @Nullable Intent data) yöntemi ile alınır. Bu yöntemde, başlatılan aktivite veya içerik sağlayıcının sonucu bulunur.
                                                             // Bu sonuç verileri bir Intent nesnesi şeklinde döner. Başlatılan aktivite veya içerik sağlayıcısı, sonucu ek bilgiler veya dönüş değerleri içeren bu Intent nesnesi ile geri dönebilir. Bu veriler, başlatılan aktiviteye geçirilen verilere veya yapılan işlemlere bağlı olarak değişebilir.
                    if (intentFromResult != null){
                         imageData = intentFromResult.getData(); //burada ki getdata bize uri yi veriyor yani kullanıcının sectıgı gorselin nerede kayıtlı oldugunu veriyor
                        //binding.imageView2.setImageURI(imageData); //kullanıcının sectıgı gorseli bunun içinde gosterebilliriz fakat bu her zaman işimizi gormeyebilir cunku bize bu resmin nerede kayıtlı oldugu degil(uri) o goresellin verisi lazım cunku ben bu veritabanına kaydedecem. gorseli sadece imageview de gostermek ısteseydik işimize yarardı

                    }

                    try {
                        if (Build.VERSION.SDK_INT >=28){ //sdk sı 28in ustunde olan telefonlar ıcın
                            ImageDecoder.Source source=ImageDecoder.createSource(ArtActivity.this.getContentResolver(),imageData);
                            selectedImage = ImageDecoder.decodeBitmap(source);
                            binding.imageView2.setImageBitmap(selectedImage);
                        }
                        else {
                            selectedImage=MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(),imageData);
                            binding.imageView2.setImageBitmap(selectedImage);
                        }




                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }

            }
        });


        //izin lamak için
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                    //permission granted(izin verildi)
                    Intent intentToGallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //burada galeriden bir tane foto alıp gelecez
                    activityResultLauncher.launch(intentToGallery); //intent i baslatmak için kullanılır
                }
                else {
                    //permission denied(izin verilmedi)
                    Toast.makeText(ArtActivity.this,"permission needed!",Toast.LENGTH_LONG).show(); //izin verilmediginde bir kısa mesaj gosterecez

                }

            }
        });
    }
}