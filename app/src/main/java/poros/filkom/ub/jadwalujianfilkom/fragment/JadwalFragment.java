package poros.filkom.ub.jadwalujianfilkom.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import poros.filkom.ub.jadwalujianfilkom.R;
import poros.filkom.ub.jadwalujianfilkom.adapter.JadwalAdapter;
import poros.filkom.ub.jadwalujianfilkom.model.Cell;
import poros.filkom.ub.jadwalujianfilkom.model.DetailJadwal;
import poros.filkom.ub.jadwalujianfilkom.model.JadwalResponse;
import poros.filkom.ub.jadwalujianfilkom.network.ApiGenerator;
import poros.filkom.ub.jadwalujianfilkom.network.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class JadwalFragment extends Fragment {

    public static String TAG= JadwalFragment.class.getSimpleName();

    private String TAGS = "Hmm";

    private RecyclerView recyclerView;
    private JadwalAdapter jadwalAdapter;
    private static ArrayList<DetailJadwal> jadwalKu =  new ArrayList<>();
    private static ArrayList<String> jadwalDouble = new ArrayList<>();

    static String ruangan = "";
    static String prodi = "";
    private String[] daftarHari = {"Senin, 21 Mei", "Selasa, 22 Mei", "Rabu, 23 Mei", "Kamis, 24 Mei", "Jumat, 25 Mei", "Sabtu, 26 Mei", "Senin, 28 Mei", "Rabu, 30 Mei", "Kamis, 31 Mei"};

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    private static ArrayList<DetailJadwal> jadwals;

    public JadwalFragment() {
    }

    public static JadwalFragment newInstance() {
        JadwalFragment fragment = new JadwalFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBundle();
        jadwalAdapter = new JadwalAdapter(getContext());

        sharedPreferences = getContext().getSharedPreferences("jadwal", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("offline", false)) {
            Toast.makeText(getContext(), "off", Toast.LENGTH_SHORT).show();
            getJadwalOff();
            //getJadwal();
        } else {
            Toast.makeText(getContext(), "on", Toast.LENGTH_SHORT).show();
            getJadwal();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jadwal, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initSharedPref();
        initRecyclerView();

        getJadwalKu();
    }

    private void initBundle() {
        Intent intent = getActivity().getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        jadwals = (ArrayList<DetailJadwal>) args.getSerializable("ARRAYLIST");
    }

    private void initSharedPref() {
        recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        jadwalAdapter = new JadwalAdapter(getContext());
    }

    private void initRecyclerView() {
        sharedPreferences = getContext().getSharedPreferences("jadwal", MODE_PRIVATE);
        prodi = sharedPreferences.getString("prodi", "");
    }

    private void loadJadwal(JadwalResponse jadwalResponse) {
        //JadwalResponse jadwalResponse = response.body();
                int ke = 0;
                for (int h = 0; h < jadwalResponse.getPages().size(); h++) {
                    for (int i = 1; i < jadwalResponse.getPages().get(h).getTables().get(0).getCells().size(); i++) {
                        ArrayList<Cell> cell = (ArrayList<Cell>) jadwalResponse.getPages().get(h).getTables().get(0).getCells();
                        if (cell.get(i).getI() <= 2) {

                        } else {
                            if (cell.get(i).getJ().toString().equals("1")) {
                                ruangan = cell.get(i).getContent();
                            }
                            switch (prodi) {
                                case "Magister Ilmu Komputer":
                                    prodi = "MIK";
                                    break;
                                case "Teknik Informatika":
                                    prodi = "TIF";
                                    break;
                                case "Teknik Komputer":
                                    prodi = "TEKOM";
                                    break;
                                case "Pendidikan Teknologi Informasi":
                                    prodi = "PTI";
                                    break;
                                case "Sistem Informasi":
                                    prodi = "SI";
                                    break;
                                case "Teknologi Informasi":
                                    prodi = "TI";
                                    break;
                            }

                            switch (cell.get(i).getJ().toString()) {
                                case "3":
                                    jadwalDouble.clear();
                                    for (int j = 0; j < jadwals.size(); j++) {
                                        jadwalDouble.add(jadwals.get(j).getMatkul().trim());
                                        int count = 0;
                                        for (int z = 0; z < jadwalDouble.size(); z++) {
                                            if (jadwalDouble.get(z).equals(cell.get(i).getContent().trim())) {
                                                count++;
                                            }
                                        }
                                        if ( count <= 1 &&
                                                prodi.equals(cell.get(i-1).getContent().trim()) &&
                                                        jadwals.get(j).getMatkul().trim().equals(cell.get(i).getContent().trim()) &&
                                                        jadwals.get(j).getKelas().trim().equals(cell.get(i+1).getContent().trim())) {
                                            Log.d(TAG, "JA Ruangan: "+ruangan);
                                            Log.d(TAG, "JA Matkul: "+cell.get(i).getContent());
                                            Log.d(TAG, "JA kelas: "+cell.get(i+1).getContent());
                                            DetailJadwal detailJadwal = new DetailJadwal();
                                            detailJadwal.setHari(daftarHari[h]);
                                            detailJadwal.setJam("08.00 - 09.30");
                                            detailJadwal.setKelas(cell.get(i+1).getContent());
                                            detailJadwal.setMatkul(cell.get(i).getContent());
                                            detailJadwal.setRuang(ruangan);
                                            jadwalKu.add(ke, detailJadwal);
                                            ke++;
                                        }
                                    }
                                    break;
                                case "6":
                                    jadwalDouble.clear();
                                    for (int j = 0; j < jadwals.size(); j++) {
                                        jadwalDouble.add(jadwals.get(j).getMatkul().trim());
                                        int count = 0;
                                        for (int z = 0; z < jadwalDouble.size(); z++) {
                                            if (jadwalDouble.get(z).equals(cell.get(i).getContent().trim())) {
                                                count++;
                                            }
                                        }
                                        if ( count <= 1 &&
                                                prodi.equals(cell.get(i-1).getContent().trim()) &&
                                                        jadwals.get(j).getMatkul().trim().equals(cell.get(i).getContent().trim()) &&
                                                        jadwals.get(j).getKelas().trim().equals(cell.get(i+1).getContent().trim())) {
                                            Log.d(TAG, "JA Ruangan: "+ruangan);
                                            Log.d(TAG, "JA Matkul: "+cell.get(i).getContent());
                                            Log.d(TAG, "JA kelas: "+cell.get(i+1).getContent());
                                            DetailJadwal detailJadwal = new DetailJadwal();
                                            detailJadwal.setHari(daftarHari[h]);
                                            if (daftarHari[h].equals("Jumat, 25 Mei")) {
                                                detailJadwal.setJam("09.30 - 11.00");
                                            } else {
                                                detailJadwal.setJam("10.00 - 11.30");
                                            }
                                            detailJadwal.setKelas(cell.get(i+1).getContent());
                                            detailJadwal.setMatkul(cell.get(i).getContent());
                                            detailJadwal.setRuang(ruangan);
                                            jadwalKu.add(ke, detailJadwal);
                                            ke++;
                                        }
                                    }
                                    break;
                                case "9":
                                    jadwalDouble.clear();
                                    for (int j = 0; j < jadwals.size(); j++) {
                                        jadwalDouble.add(jadwals.get(j).getMatkul().trim());
                                        int count = 0;
                                        for (int z = 0; z < jadwalDouble.size(); z++) {
                                            if (jadwalDouble.get(z).equals(cell.get(i).getContent().trim())) {
                                                count++;
                                            }
                                        }
                                        if ( count <= 1 &&
                                                prodi.equals(cell.get(i-1).getContent().trim()) &&
                                                        jadwals.get(j).getMatkul().trim().equals(cell.get(i).getContent().trim()) &&
                                                        jadwals.get(j).getKelas().trim().equals(cell.get(i+1).getContent().trim())) {
                                            Log.d(TAG, "JA Ruangan: "+ruangan);
                                            Log.d(TAG, "JA Matkul: "+cell.get(i).getContent());
                                            Log.d(TAG, "JA kelas: "+cell.get(i+1).getContent());
                                            DetailJadwal detailJadwal = new DetailJadwal();
                                            detailJadwal.setHari(daftarHari[h]);
                                            detailJadwal.setJam("13.00 - 14.30");
                                            detailJadwal.setKelas(cell.get(i+1).getContent());
                                            detailJadwal.setMatkul(cell.get(i).getContent());
                                            detailJadwal.setRuang(ruangan);
                                            jadwalKu.add(ke, detailJadwal);
                                            ke++;
                                        }
                                    }
                                    break;
                            }
                        }
                    }

                }
                jadwalAdapter.notifyDataSetChanged();
    }

    private void getJadwalOff() {
        jadwalKu.clear();
        sharedPreferences = getContext().getSharedPreferences("jadwal", MODE_PRIVATE);
        String jadwalUas = sharedPreferences.getString("jadwalUas", "");

        Gson gson = new Gson();
        //jadwalUas = gson.toJson(jadwalUas);
        //jadwalUas = jadwalUas.substring(1, jadwalUas.length()-1);
        Log.d(TAG, "getJadwalOff: "+jadwalUas);
        Log.d(TAG, "getJadwalOff: "+jadwalUas);
        JadwalResponse jadwalResponse = gson.fromJson(jadwalUas, JadwalResponse.class);
        Log.d(TAG, "gaaetJadwalOff: aa"+jadwalResponse.getPages().get(0).getTables().get(0).getCells().get(0).getI());
        loadJadwal(jadwalResponse);

    }

    private void getJadwal() {
        Service service = ApiGenerator.createService(Service.class);

        Call<JadwalResponse> call = service.getJadwalUAS();
        jadwalKu.clear();

        call.enqueue(new Callback<JadwalResponse>() {
            @Override
            public void onResponse(Call<JadwalResponse> call, Response<JadwalResponse> response) {

                Gson gson = new Gson();
                String jsonString = gson.toJson(response.body());

                sharedPreferences = getContext().getSharedPreferences("jadwal", MODE_PRIVATE);
                editor = sharedPreferences.edit();
                editor.putString("jadwalUas", jsonString);
                editor.apply();

                JadwalResponse jadwalResponse = gson.fromJson(jsonString , JadwalResponse.class);
                Log.d(TAG, "onResponse: jadwal2"+jadwalResponse.getPages().get(0).getNumber().toString());

                //JadwalResponse jadwalResponse = response.body();
                loadJadwal(jadwalResponse);
            }

            @Override
            public void onFailure(Call<JadwalResponse> call, Throwable t) {
                Toast.makeText(getContext(), "fail", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getJadwalKu() {
        for (int i = 0; i < jadwalKu.size(); i++) {
            Log.d(TAGS, "getJadwalKu: ==========================");
            Log.d(TAGS, "getJadwalKu: "+jadwalKu.get(i).getHari());
            Log.d(TAGS, "getJadwalKu: "+jadwalKu.get(i).getJam());
            Log.d(TAGS, "getJadwalKu: "+jadwalKu.get(i).getKelas());
            Log.d(TAGS, "getJadwalKu: "+jadwalKu.get(i).getMatkul());
            Log.d(TAGS, "getJadwalKu: "+jadwalKu.get(i).getRuang());
        }
        jadwalAdapter = new JadwalAdapter(getContext());
        jadwalAdapter.addItem(jadwalKu);
        recyclerView.setAdapter(jadwalAdapter);
    }
}
