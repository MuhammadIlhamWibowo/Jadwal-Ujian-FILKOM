package poros.filkom.ub.jadwalujianfilkom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import poros.filkom.ub.jadwalujianfilkom.model.DetailJadwal;
import poros.filkom.ub.jadwalujianfilkom.network.Service;
import poros.filkom.ub.jadwalujianfilkom.network.SiamGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.et_username)
    EditText etUsername;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private String html;
    static private String cookie;

    SharedPreferences sharedPreferencesJadwal;
    SharedPreferences.Editor editorJadwal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        sharedPreferencesJadwal = getSharedPreferences("jadwal", MODE_PRIVATE);
        editorJadwal = sharedPreferencesJadwal.edit();

        if (!sharedPreferencesJadwal.getString("username", "").equals("")) {
            etUsername.setText(sharedPreferencesJadwal.getString("username", ""));
        }

        if (!sharedPreferencesJadwal.getString("password", "").equals("")) {
            etPassword.setText(sharedPreferencesJadwal.getString("password", ""));
        }

        //get unAuthenticated token
        getCookie();
    }

    @OnClick(R.id.btn_login)
    public void submit() {
        //authenticated token
        editorJadwal.putBoolean("offline", false);
        editorJadwal.commit();
        login();
    }

    @OnClick(R.id.btn_offline)
    public void offline() {
        String jadwalSiam = sharedPreferencesJadwal.getString("jadwalSiam", "");
        editorJadwal.putBoolean("offline", true);
        editorJadwal.commit();
        getJadwal(jadwalSiam);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    }

    private void disableProgressBar() {
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void getCookie() {
        Service service = SiamGenerator.createService(Service.class);

        Call<String> call = service.getCookie();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                html = response.body().toString();
                cookie = response.headers().get("Set-Cookie");
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }


    private void login() {
        showProgressBar();

        String username = etUsername.getText().toString();
        editorJadwal.putString("username", username);
        String password = etPassword.getText().toString();
        editorJadwal.putString("password", password);
        editorJadwal.apply();

        Service service = SiamGenerator.createService(Service.class);

        Call<String> call = service.login(
                cookie,
                RequestBody.create(MediaType.parse("text/plain"), etUsername.getText().toString()),
                RequestBody.create(MediaType.parse("text/plain"), etPassword.getText().toString()),
                RequestBody.create(MediaType.parse("text/plain"), "Masuk"));

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                // after token authenticated,
                // get jadwal with authenticated token
                String resp = response.body();
                List<String> items = Arrays.asList(resp.split("<div><span class=\"label\">Program Studi<i class=\"fa fa-angle-right\"></i></span>"));
                try {
                    List<String> prodi = Arrays.asList(items.get(1).split("</div>"));
                    Log.d("zxcresp", "onResponse: "+prodi.get(0));
                    editorJadwal.putString("prodi", prodi.get(0));
                    editorJadwal.apply();
                    jadwal();
                } catch (Exception e) {
                    disableProgressBar();
                    Toast.makeText(LoginActivity.this, "Password salah", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                disableProgressBar();
            }
        });
    }

    private void getJadwal(String param) {
        html = param;
//                webView.loadDataWithBaseURL("", html, mimeType, encoding, "");
                List<String> items = Arrays.asList(html.split("<tr class=\"text\" bgcolor=\"#ffffff\" align=\"center\">"));
//                for (int i = 0; i < items.size(); i++) {
//                    System.out.println("=========================================================");
//                    System.out.println(items.get(i));
//                }

                /**
                 * length = 10
                 itemdatasize
                 itemdatasize>Selasa</td>
                 itemdatasize>09:30 - 11:59</td>
                 itemdatasize>H</td>
                 itemdatasize>CIF61252</td>
                 itemdatasize align="left">&nbsp; Pengenalan Pola</td>
                 itemdatasize align="center">&nbsp; 2016</td>
                 itemdatasize align="left">&nbsp; </td>
                 itemdatasize>Gedung E PTIIK - E2.1</td>
                 itemdatasize>
                 */

                String[][] jadwal = new String[items.size() - 1][8];
                for (int i = 1; i < items.size(); i++) {
//                    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"+i);
                    List<String> itemsData = Arrays.asList(items.get(i).split("<td"));

                    for (int j = 1; j < 9; j++) {
                        jadwal[i - 1][j - 1] = itemsData.get(j);
                    }
                }

                ArrayList<DetailJadwal> jadwals = new ArrayList<>();

                for (int i = 0; i < jadwal.length; i++) {
//                    System.out.println("##############################################################");
//                    for (int j = 0; j < jadwal[0].length; j++) {
//                        System.out.println(jadwal[i][j]);
//                    }
                    DetailJadwal detailJadwal = new DetailJadwal();
                    String hari = jadwal[i][0];
                    hari = hari.replace(">", "");
                    hari = hari.split("</td", 2)[0];

                    String jam = jadwal[i][1];
                    jam = jam.replace(">", "");
                    jam = jam.split("</td", 2)[0];

                    String kelas = jadwal[i][2];
                    kelas = kelas.replace(">", "");
                    kelas = kelas.split("</td", 2)[0];

                    String kode = jadwal[i][3];
                    kode = kode.replace(">", "");
                    kode = kode.split("</td", 2)[0];

                    String matkul = jadwal[i][4];
                    matkul = matkul.replace("align=\"left\">&nbsp; ", "");
                    matkul = matkul.split("</td", 2)[0];

                    String tahun = jadwal[i][5];
                    tahun = tahun.replace("align=\"center\">&nbsp; ", "");
                    tahun = tahun.split("</td", 2)[0];

                    String dosen = jadwal[i][6];
                    dosen = dosen.replace("align=\"left\">&nbsp; ", "");
                    dosen = dosen.split("</td", 2)[0];

                    String ruang = jadwal[i][7];
                    ruang = ruang.replace(">", "");
                    ruang = ruang.split("</td", 2)[0];

                    detailJadwal.setHari(hari);
                    detailJadwal.setJam(jam);
                    detailJadwal.setKelas(kelas);
                    detailJadwal.setKode(kode);
                    detailJadwal.setMatkul(matkul);
                    detailJadwal.setTahun(tahun);
                    detailJadwal.setDosen(dosen);
                    detailJadwal.setRuang(ruang);

                    jadwals.add(detailJadwal);

                }

                Intent intent = new Intent(new Intent(getApplicationContext(), HomeActivity.class));

                //Toast.makeText(LoginActivity.this, String.valueOf(jadwals.size()), Toast.LENGTH_SHORT).show();
                for (int k = 0; k < jadwals.size(); k++) {
                   // Toast.makeText(LoginActivity.this, jadwals.get(k).getMatkul(), Toast.LENGTH_SHORT).show();
                    //Log.d("hmm jadwal", "onResponse: "+jadwals.get(k).getMatkul());
                }

                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST", (Serializable) jadwals);
                intent.putExtra("BUNDLE", args);

                startActivity(intent);

                /** * For the brave souls who get this far: You are the chosen ones,
                 * the valiant knights of programming who toil away, without rest,
                 * fixing our most awful code. To you, true saviors, kings of men,
                 * I say this: never gonna give you up, never gonna let you down.
                 */
                disableProgressBar();
    }

    private void jadwal() {

        Service service = SiamGenerator.createService(Service.class);

        Call<String> call = service.jadwal(cookie);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                sharedPreferencesJadwal = getSharedPreferences("jadwal", MODE_PRIVATE);
                editorJadwal = sharedPreferencesJadwal.edit();
                editorJadwal.putString("jadwalSiam", response.body());

                html = response.body().toString();
                getJadwal(html);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "siam fail", Toast.LENGTH_SHORT).show();
                disableProgressBar();
            }
            
        });
    }
}
