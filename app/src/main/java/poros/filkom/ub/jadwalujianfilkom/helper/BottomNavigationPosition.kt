package poros.filkom.ub.jadwalujianfilkom.helper

import android.support.v4.app.Fragment
import poros.filkom.ub.jadwalujianfilkom.R
import poros.filkom.ub.jadwalujianfilkom.fragment.FeedbackFragment
import poros.filkom.ub.jadwalujianfilkom.fragment.JadwalFragment
import poros.filkom.ub.jadwalujianfilkom.fragment.InfoFragment

/**
 * Created by mat on 5/14/18.
 */

enum class BottomNavigationPosition(val position: Int, val id: Int) {
    HOME(0, R.id.home),
    DASHBOARD(1, R.id.dashboard),
    PROFILE(2, R.id.profile);
}

fun findNavigationPositionById(id: Int): BottomNavigationPosition = when (id) {
    BottomNavigationPosition.HOME.id -> BottomNavigationPosition.HOME
    BottomNavigationPosition.DASHBOARD.id -> BottomNavigationPosition.DASHBOARD
    BottomNavigationPosition.PROFILE.id -> BottomNavigationPosition.PROFILE
    else -> BottomNavigationPosition.HOME
}

fun BottomNavigationPosition.createFragment(): Fragment = when (this) {
    BottomNavigationPosition.HOME -> JadwalFragment.newInstance()
    BottomNavigationPosition.DASHBOARD -> FeedbackFragment.newInstance()
    BottomNavigationPosition.PROFILE -> InfoFragment.newInstance()
}

fun BottomNavigationPosition.getTag(): String = when (this) {
    BottomNavigationPosition.HOME -> JadwalFragment.TAG
    BottomNavigationPosition.DASHBOARD -> FeedbackFragment.TAG
    BottomNavigationPosition.PROFILE -> InfoFragment.TAG
}
