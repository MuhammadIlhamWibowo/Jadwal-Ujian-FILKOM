package poros.filkom.ub.jadwalujianfilkom

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import poros.filkom.ub.jadwalujianfilkom.model.DetailJadwalKotlin
import poros.filkom.ub.jadwalujianfilkom.model.JadwalResponse
import poros.filkom.ub.jadwalujianfilkom.network.ApiClient

class MainActivity : AppCompatActivity() {

    var mCompositeDisposable = CompositeDisposable()
    var jadwals: ArrayList<DetailJadwalKotlin>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val intent: Intent = getIntent();
        val args: Bundle  = intent.getBundleExtra("BUNDLE");
        jadwals = args.getSerializable("ARRAYLIST") as ArrayList<DetailJadwalKotlin>?;

       // Log.d("hmm", jadwals!![0].matkul!![1].toString())
       // Log.d("hmm", jadwals!![0].matkul!![1].toString())

       var i: Int = 0
        for (jadwal: DetailJadwalKotlin in this.jadwals!!) {
            //Toast.makeText(this@MainActivity, jadwal.matkul[i].toString(), Toast.LENGTH_SHORT).show();
            i++;
            Log.d("hmm", jadwal.matkul!![i].toString())
            Log.d("hmm2", jadwal.kelas!![i].toString())
            Log.d("hmm3", jadwal.kode!![i].toString())
        }

        fab.setOnClickListener { view -> getJadwalDosen() }
    }

    private fun getJadwalDosen() {
        mCompositeDisposable.add(ApiClient.create("jadwal").getJadwalDosen()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<JadwalResponse>() {
                    override fun onNext(t: JadwalResponse) {
                        val x = t.pages.size.toString()
                        Toast.makeText(this@MainActivity, x, Toast.LENGTH_SHORT).show()
                        tv_berita_judul.text = t.pages.size.toString()
                        tv_berita_konten.text = t.pages.get(0).tables[0].cells[0].content
                    }

                    override fun onError(e: Throwable) {
                        Log.d("MainActivity", e.message)
                    }

                    override fun onComplete() {
                        Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT).show()
                    }
                }))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
